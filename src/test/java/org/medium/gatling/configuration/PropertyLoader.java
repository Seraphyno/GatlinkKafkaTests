package org.medium.gatling.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class to load properties from the application.properties file.
 * This class provides a method to retrieve property values with a default fallback.
 */
public class PropertyLoader {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = PropertyLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load application.properties", ex);
        }
    }

    public static String getOrDefault(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
