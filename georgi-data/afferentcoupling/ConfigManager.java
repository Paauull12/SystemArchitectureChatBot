package com.example.config;

import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class ConfigManager {
    private Properties properties = new Properties();

    public ConfigManager(String configFile) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFile)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
