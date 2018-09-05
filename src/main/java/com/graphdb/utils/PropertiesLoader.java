package com.graphdb.utils;

import java.io.FileInputStream;
import java.util.Properties;

import static com.graphdb.utils.Constants.CONFIG_FILE;

public class PropertiesLoader {

    static {
        try {
            ourInstance = new PropertiesLoader();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static PropertiesLoader ourInstance;
    private final Properties properties;

    public static PropertiesLoader getInstance() {
        return ourInstance;
    }

    private PropertiesLoader() throws Exception {
        properties = new Properties();
        properties.load(new FileInputStream(CONFIG_FILE));
    }

    public Properties getProperties() {
        return properties;
    }
}
