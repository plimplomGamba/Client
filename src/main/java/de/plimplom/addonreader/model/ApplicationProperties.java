package de.plimplom.addonreader.model;

import de.plimplom.addonreader.repository.PropertiesRepository;
import de.plimplom.addonreader.service.WoWFolderService;
import javafx.beans.property.*;
import lombok.Getter;

import java.io.File;
import java.util.Objects;
import java.util.Properties;

public class ApplicationProperties {
    @Getter
    private final BooleanProperty minimizeToTray = new SimpleBooleanProperty(false);
    private final BooleanProperty startWithWindows = new SimpleBooleanProperty(false);
    private final StringProperty wowFolderPath = new SimpleStringProperty();
    private final StringProperty wowAccount = new SimpleStringProperty();
    private final StringProperty syncHost = new SimpleStringProperty();
    private final StringProperty syncPass = new SimpleStringProperty();
    private final BooleanProperty autoSyncEnabled = new SimpleBooleanProperty(false);

    private final PropertiesRepository repository;
    private final WoWFolderService folderService;

    public ApplicationProperties(PropertiesRepository repository, WoWFolderService folderService) {
        this.repository = repository;
        this.folderService = folderService;
        loadFromRepository();
        setupListeners();

        checkState();
    }

    private void checkState() {
        if (wowFolderPath.get() == null || wowFolderPath.get().isEmpty() || !folderService.isValidWowFolder(new File(wowFolderPath.get())) || !folderService.isAddonInstalled(new File(wowFolderPath.get()))) {
            wowFolderPath.set(null);
            wowAccount.set(null);
            autoSyncEnabled.set(false);
        }
    }

    private void setupListeners() {
        minimizeToTray.addListener((_, _, newValue) ->
                repository.setProperty(ApplicationPropertyEnum.MINIMIZE_TO_TRAY, newValue ? "true" : "false"));

        startWithWindows.addListener((_, _, newValue) ->
                repository.setProperty(ApplicationPropertyEnum.START_WITH_WINDOWS, newValue ? "true" : "false"));

        wowFolderPath.addListener((_, _, newValue) ->
                repository.setProperty(ApplicationPropertyEnum.FOLDER_PATH, newValue));

        wowAccount.addListener((_, _, newValue) ->
                repository.setProperty(ApplicationPropertyEnum.ACCOUNT_ID, newValue));

        syncHost.addListener((_, _, newValue) ->
                repository.setProperty(ApplicationPropertyEnum.SYNC_HOST, newValue));

        syncPass.addListener((_, _, newValue) ->
                repository.setProperty(ApplicationPropertyEnum.SYNC_PASS, newValue));

        autoSyncEnabled.addListener((_, _, newValue) ->
                repository.setProperty(ApplicationPropertyEnum.AUTO_SYNC_ENABLED, newValue ? "true" : "false"));
    }

    private void loadFromRepository() {
        Properties props = repository.getAllProperties();

        loadProperty(minimizeToTray, props.get(ApplicationPropertyEnum.MINIMIZE_TO_TRAY.name().toLowerCase()));
        loadProperty(startWithWindows, props.get(ApplicationPropertyEnum.START_WITH_WINDOWS.name().toLowerCase()));
        loadProperty(wowFolderPath, props.get(ApplicationPropertyEnum.FOLDER_PATH.name().toLowerCase()));
        loadProperty(wowAccount, props.get(ApplicationPropertyEnum.ACCOUNT_ID.name().toLowerCase()));
        loadProperty(syncHost, props.get(ApplicationPropertyEnum.SYNC_HOST.name().toLowerCase()));
        loadProperty(syncPass, props.get(ApplicationPropertyEnum.SYNC_PASS.name().toLowerCase()));
        loadProperty(autoSyncEnabled, props.get(ApplicationPropertyEnum.AUTO_SYNC_ENABLED.name().toLowerCase()));
    }

    private void loadProperty(Property<?> property, Object value) {
        if (value == null) return;
        if (property instanceof BooleanProperty booleanProperty) {
            booleanProperty.setValue(Objects.equals(value.toString(), "true"));
        } else if (property instanceof StringProperty stringProperty) {
            stringProperty.setValue(value.toString());
        }
    }

    public BooleanProperty minimizeToTrayProperty() {
        return minimizeToTray;
    }

    public BooleanProperty startWithWindowsProperty() {
        return startWithWindows;
    }

    public StringProperty syncHostProperty() {
        return syncHost;
    }

    public StringProperty syncPassProperty() {
        return syncPass;
    }

    public StringProperty wowFolderProperty() {
        return wowFolderPath;
    }

    public StringProperty wowAccountProperty() {
        return wowAccount;
    }

    public BooleanProperty autoSyncEnabledProperty() {
        return autoSyncEnabled;
    }
}
