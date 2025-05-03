package de.plimplom.addonreader.repository;

import de.plimplom.addonreader.model.ApplicationPropertyEnum;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

@Slf4j
public class PropertiesRepository {
    private final Properties properties = new Properties();
    private final String propertiesFilePath;

    public PropertiesRepository() {
        // Get platform-specific app data directory
        String appDataPath;
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            appDataPath = System.getProperty("user.home") + "\\AppData\\Roaming\\plimplomGamba";
        } else if (os.contains("mac")) {
            appDataPath = System.getProperty("user.home") + "/Library/Application Support/plimplomGamba";
        } else {
            appDataPath = System.getProperty("user.home") + "/.plimplomGamba";
        }

        // Create directory if it doesn't exist
        File directory = new File(appDataPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        propertiesFilePath = appDataPath + File.separator + "application.properties";
        loadProperties();
    }

    private void loadProperties() {
        File propertiesFile = new File(propertiesFilePath);

        if (propertiesFile.exists()) {
            try (FileInputStream fis = new FileInputStream(propertiesFile)) {
                properties.load(fis);
            } catch (IOException e) {
                log.error("Error loading properties:", e);
            }
        }
    }

    public void saveProperties() {
        log.debug("Saving properties to file: {}", propertiesFilePath);
        try (FileOutputStream fos = new FileOutputStream(propertiesFilePath)) {
            properties.store(fos, "Client Properties");
        } catch (IOException e) {
            log.error("Error saving properties:", e);
        }
    }

    public String getProperty(ApplicationPropertyEnum property) {
        return properties.getProperty(property.name().toLowerCase());
    }

    public void setProperty(ApplicationPropertyEnum property, String value) {
        if (value == null) {
            properties.remove(property.name().toLowerCase());
        } else {
            log.info("Setting property {} to {}", property, value);
            properties.setProperty(property.name().toLowerCase(), value);
        }
        saveProperties();
    }

    public Properties getAllProperties() {
        return properties;
    }
}
