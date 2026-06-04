package com.cts.mfrp.oneappetite.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigReader {

    private static final Properties PROPS = new Properties();

    static {
        load("config.properties");
        String env = System.getProperty("env", "");
        if (!env.isBlank()) {
            load("config-" + env + ".properties");
        }
    }

    private ConfigReader() {}

    private static void load(String resource) {
        try (InputStream in = ConfigReader.class.getClassLoader().getResourceAsStream(resource)) {
            if (in != null) PROPS.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + resource, e);
        }
    }

    public static String get(String key) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return sys;
        String env = System.getenv(key.toUpperCase().replace('.', '_'));
        if (env != null && !env.isBlank()) return env;
        return PROPS.getProperty(key);
    }

    public static String get(String key, String fallback) {
        String v = get(key);
        return (v == null || v.isBlank()) ? fallback : v;
    }

    public static int getInt(String key, int fallback) {
        try {
            return Integer.parseInt(get(key));
        } catch (NumberFormatException | NullPointerException e) {
            return fallback;
        }
    }

    public static boolean getBoolean(String key, boolean fallback) {
        String v = get(key);
        return v == null ? fallback : Boolean.parseBoolean(v.trim());
    }
}
