package de.plimplom.addonreader.viewmodel;

import de.plimplom.addonreader.event.DataSyncEvent;
import de.plimplom.addonreader.event.EventBus;
import de.plimplom.addonreader.event.InfoMessageEvent;
import de.plimplom.addonreader.model.ApplicationProperties;
import de.plimplom.addonreader.service.StatsDataService;
import de.plimplom.addonreader.service.StatsFileListenerService;
import de.plimplom.addonreader.service.WoWFolderService;
import de.plimplom.addonreader.util.HttpClientFactory;
import de.plimplom.addonreader.view.message.InstallAddonMessage;
import de.plimplom.addonreader.view.message.InvalidFolderMessage;
import de.plimplom.addonreader.view.message.SyncFailedMessage;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import static de.plimplom.addonreader.event.InfoMessageEvent.EventName.*;

public class MainContentViewModel {
    private final ApplicationProperties applicationProperties;
    private final WoWFolderService wowFolderService;
    private final StatsDataService dataService;
    private final StatsFileListenerService fileListenerService;
    private final EventBus eventBus;

    private final BooleanProperty folderValid = new SimpleBooleanProperty(false);
    private final BooleanProperty addonInstalled = new SimpleBooleanProperty(false);
    private final BooleanProperty syncConfigValid = new SimpleBooleanProperty(false);
    private final ObservableList<String> accountOptions = FXCollections.observableArrayList();
    private final ListProperty<String> accountsProperty = new SimpleListProperty<>(accountOptions);

    public MainContentViewModel(
            ApplicationProperties applicationProperties,
            WoWFolderService wowFolderService,
            StatsDataService dataService,
            StatsFileListenerService fileListenerService,
            EventBus eventBus) {
        this.applicationProperties = applicationProperties;
        this.wowFolderService = wowFolderService;
        this.dataService = dataService;
        this.fileListenerService = fileListenerService;
        this.eventBus = eventBus;

        if (applicationProperties.wowAccountProperty().get() != null) {
            accountOptions.add(applicationProperties.wowAccountProperty().get());
        }else if(applicationProperties.wowFolderProperty().get() != null){
            try {
                accountOptions.addAll(wowFolderService.getAccountNames(new File(applicationProperties.wowFolderProperty().get())));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Initialize validity state if folder path is already set
        if (applicationProperties.wowFolderProperty().get() != null &&
                !applicationProperties.wowFolderProperty().get().isEmpty()) {
            validateFolder(new File(applicationProperties.wowFolderProperty().get()));
        }

        // Listen for folder path changes
        applicationProperties.wowFolderProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                validateFolder(new File(newVal));
            } else {
                folderValid.set(false);
                addonInstalled.set(false);
                accountOptions.clear();
            }
        });

        // Initial validation
        validateSyncConfig();

        // Listen for sync config changes
        applicationProperties.syncHostProperty().addListener((_, _, _) -> validateSyncConfig());
        applicationProperties.syncPassProperty().addListener((_, _, _) -> validateSyncConfig());

        // Listen for auto-sync changes
        applicationProperties.autoSyncEnabledProperty().addListener((_, _, newValue) -> {
            if (newValue && syncConfigValid.get() && folderValid.get() && addonInstalled.get()) {
                startListening();
            } else if (!newValue && fileListenerService.isRunning()) {
                fileListenerService.stopListening();
            }
        });

        // Start auto-sync if enabled
        if (applicationProperties.autoSyncEnabledProperty().get() &&
                syncConfigValid.get() && folderValid.get() && addonInstalled.get()) {
            startListening();
        }
    }

    public StringProperty wowFolderProperty() {
        return applicationProperties.wowFolderProperty();
    }

    public StringProperty wowAccountProperty() {
        return applicationProperties.wowAccountProperty();
    }

    public ListProperty<String> accountsProperty() {
        return accountsProperty;
    }

    public BooleanProperty folderValidProperty() {
        return folderValid;
    }

    public BooleanProperty addonInstalledProperty() {
        return addonInstalled;
    }

    public BooleanProperty autoSyncEnabledProperty() {
        return applicationProperties.autoSyncEnabledProperty();
    }

    public BooleanProperty syncConfigValidProperty() {
        return syncConfigValid;
    }

    public void updateAccountOptions(File selectedDirectory) throws IOException {
        validateFolder(selectedDirectory);

        if (folderValid.get()) {
            List<String> accounts = wowFolderService.getAccountNames(selectedDirectory);
            accountOptions.clear();
            accountOptions.addAll(accounts);
        }
    }

    private void validateFolder(File folder) {
        boolean isValid = wowFolderService.isValidWowFolder(folder);
        folderValid.set(isValid);

        boolean hasAddon = false;
        if (isValid) {
            hasAddon = wowFolderService.isAddonInstalled(folder);
            eventBus.publish(new InfoMessageEvent(InfoMessageEvent.EventType.DELETE, INVALID_FOLDER));
        } else {
            eventBus.publish(new InfoMessageEvent(InfoMessageEvent.EventType.CREATE, INVALID_FOLDER, new InvalidFolderMessage()));
            accountOptions.clear();
            addonInstalled.set(hasAddon);
            return;
        }
        addonInstalled.set(hasAddon);

        if (!hasAddon) {
            eventBus.publish(new InfoMessageEvent(InfoMessageEvent.EventType.CREATE, ADDON_NOT_INSTALLED, new InstallAddonMessage()));
        } else {
            eventBus.publish(new InfoMessageEvent(InfoMessageEvent.EventType.DELETE, ADDON_NOT_INSTALLED));
        }

        // Clear accounts if folder is invalid
        if (!isValid) {
            accountOptions.clear();
        }
    }

    private void validateSyncConfig() {
        String host = applicationProperties.syncHostProperty().get();
        String pass = applicationProperties.syncPassProperty().get();

        boolean isValid = host != null && !host.trim().isEmpty() &&
                pass != null && !pass.trim().isEmpty();

        syncConfigValid.set(isValid);

        if (!isValid) {
            eventBus.publish(new InfoMessageEvent(InfoMessageEvent.EventType.CREATE, SYNC_FAILED, new SyncFailedMessage()));
            applicationProperties.autoSyncEnabledProperty().set(false);
        } else {
            try (var client = HttpClientFactory.createDefaultClient()) {
                String authHeader = Base64.getEncoder().encodeToString(
                        String.format("%s:%s", "gamba", applicationProperties.syncPassProperty().get()).getBytes()
                );

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(applicationProperties.syncHostProperty().get()))
                        .header("Authorization", "Basic " + authHeader)
                        .build();

                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 401) {
                    eventBus.publish(new InfoMessageEvent(InfoMessageEvent.EventType.CREATE, SYNC_FAILED, new SyncFailedMessage()));
                    applicationProperties.autoSyncEnabledProperty().set(false);
                } else {
                    eventBus.publish(new InfoMessageEvent(InfoMessageEvent.EventType.DELETE, SYNC_FAILED));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void syncDataNow() {
        if (!syncConfigValid.get()) {
            return;
        }

        String folderPath = applicationProperties.wowFolderProperty().get();
        String account = applicationProperties.wowAccountProperty().get();

        if (folderPath == null || account == null || folderPath.isEmpty() || account.isEmpty()) {
            eventBus.publish(new DataSyncEvent(false, "WoW folder or account not selected"));
            return;
        }

        try {
            fileListenerService.processAddonFile(Paths.get(folderPath, "WTF", "Account", account, "SavedVariables"));
        } catch (Exception e) {
            eventBus.publish(new DataSyncEvent(false, "Failed to sync data: " + e.getMessage()));
        }
    }

    private void startListening() {
        if (fileListenerService.isRunning()) {
            return;
        }

        if (applicationProperties.wowFolderProperty().get() != null &&
                applicationProperties.wowAccountProperty().get() != null) {
            fileListenerService.startListening();
        }
    }
}
