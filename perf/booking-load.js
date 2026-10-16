import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:3001';

// restful-booker returns 418 unless Accept is exactly application/json
const JSON_HEADERS = {
    'Content-Type': 'application/json',
    Accept: 'application/json',
};

export const options = {
    stages: [
        { duration: '15s', target: 10 }, // ramp up to 10 virtual users
        { duration: '30s', target: 10 }, // hold steady load
        { duration: '10s', target: 0 },  // ramp down
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'],                         // under 1% of requests fail
        http_req_duration: ['p(95)<500'],                       // 95% of requests under 500 ms
        'http_req_duration{name:POST /booking}': ['p(95)<500'],
        'http_req_duration{name:GET /booking/{id}}': ['p(95)<300'],
        checks: ['rate>0.99'],                                  // over 99% of checks pass
    },
};

// Runs once before the load starts: one token shared by all virtual users
export function setup() {
    const res = http.post(
        `${BASE_URL}/auth`,
        JSON.stringify({ username: __ENV.AUTH_USERNAME || 'admin', password: __ENV.AUTH_PASSWORD || 'password123' }),
        { headers: JSON_HEADERS },
    );
    const token = res.json('token');
    if (!token) {
        throw new Error(`Could not get a token: ${res.status} ${res.body}`);
    }
    return { token };
}

// Each virtual user repeats this journey: create, read, delete
export default function (data) {
    const booking = {
        firstname: `Perf${__VU}`,
        lastname: `Iter${__ITER}`,
        totalprice: 100 + __ITER,
        depositpaid: true,
        bookingdates: { checkin: '2026-12-01', checkout: '2026-12-05' },
        additionalneeds: 'Breakfast',
    };

    const created = http.post(`${BASE_URL}/booking`, JSON.stringify(booking), {
        headers: JSON_HEADERS,
        tags: { name: 'POST /booking' },
    });
    check(created, {
        'create: status is 200': (r) => r.status === 200,
        'create: has bookingid': (r) => r.json('bookingid') !== undefined,
    });
    const id = created.json('bookingid');
    if (!id) {
        return;
    }

    const fetched = http.get(`${BASE_URL}/booking/${id}`, {
        headers: JSON_HEADERS,
        tags: { name: 'GET /booking/{id}' },
    });
    check(fetched, {
        'get: status is 200': (r) => r.status === 200,
        'get: firstname matches': (r) => r.json('firstname') === booking.firstname,
    });

    const deleted = http.del(`${BASE_URL}/booking/${id}`, null, {
        headers: { ...JSON_HEADERS, Cookie: `token=${data.token}` },
        tags: { name: 'DELETE /booking/{id}' },
    });
    check(deleted, { 'delete: status is 201': (r) => r.status === 201 });

    sleep(1); // think time between journeys
}