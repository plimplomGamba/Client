package de.plimplom.addonreader.viewmodel;

import de.plimplom.addonreader.event.EventBus;
import de.plimplom.addonreader.event.SettingsChangedEvent;
import de.plimplom.addonreader.model.ApplicationProperties;
import de.plimplom.addonreader.service.PlatformIntegrationService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SettingsViewModel {
    private final ApplicationProperties applicationProperties;
    private final PlatformIntegrationService platformService;
    private final EventBus eventBus;

    public SettingsViewModel(
            ApplicationProperties applicationProperties,
            PlatformIntegrationService platformService,
            EventBus eventBus) {
        this.applicationProperties = applicationProperties;
        this.platformService = platformService;
        this.eventBus = eventBus;

        // Add listener for startWithWindows property changes
        applicationProperties.startWithWindowsProperty().addListener((obs, oldVal, newVal) -> {
            applyStartWithWindowsSetting(newVal);
        });
    }

    private void applyStartWithWindowsSetting(boolean enabled) {
        log.info("Applying 'Start with Windows' setting: {}", enabled);

        boolean success = platformService.setStartWithWindows(enabled);

        if (!success) {
            log.warn("Failed to {} 'Start with Windows' setting",
                    enabled ? "enable" : "disable");

            // Notify the UI about the failure
            eventBus.publish(new SettingsChangedEvent(
                    "startWithWindowsError",
                    "Failed to " + (enabled ? "enable" : "disable") +
                            " automatic startup with Windows. Please check permissions."));
        } else {
            log.info("Application is now starting with Windows automatically.");
        }
    }

    public BooleanProperty minimizeToTrayProperty() {
        return applicationProperties.minimizeToTrayProperty();
    }

    public BooleanProperty startWithWindowsProperty() {
        return applicationProperties.startWithWindowsProperty();
    }

    public StringProperty syncHostProperty() {
        return applicationProperties.syncHostProperty();
    }

    public StringProperty syncPassProperty() {
        return applicationProperties.syncPassProperty();
    }
}
