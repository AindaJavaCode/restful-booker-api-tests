package com.restfulbooker.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * Reads settings from config.properties. Lookup order: system property, environment
 * variable (key upper-cased with dots replaced by underscores), then the properties file.
 */
public final class Config {

    private static final Properties PROPS = load();

    private Config() {
    }

    public static String baseUrl() {
        return get("base.url");
    }

    public static String username() {
        return get("auth.username");
    }

    public static String password() {
        return get("auth.password");
    }

    public static int timeoutMs() {
        return Integer.parseInt(get("http.timeout.ms"));
    }

    public static boolean logAll() {
        return Boolean.parseBoolean(get("log.all"));
    }

    public static String get(String key) {
        String value = System.getProperty(key);
        if (value == null) {
            value = System.getenv(key.toUpperCase().replace('.', '_'));
        }
        if (value == null) {
            value = PROPS.getProperty(key);
        }
        if (value == null) {
            throw new IllegalStateException("Missing config key: " + key);
        }
        return value;
    }

    private static Properties load() {
        Properties props = new Properties();
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new IllegalStateException("config.properties not found on classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return props;
    }
}
