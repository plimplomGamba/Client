package de.plimplom.addonreader.view;

import de.plimplom.addonreader.event.OpenSyncSettingsEvent;
import de.plimplom.addonreader.event.SettingsChangedEvent;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class SettingsView extends BaseController {
    @FXML
    private MFXButton generalButton;
    @FXML
    private MFXButton syncButton;
    @FXML
    private MFXButton closeSettingsButton;
    @FXML
    private Pane settingsMainPane;

    private final Map<SettingsPaneType, Node> settingsPanes = new HashMap<>();
    private boolean initialized = false;
    private MFXButton selectedButton;

    @FXML
    private void initialize() throws IOException {
        loadSettingsPanes();
        setupEventHandlers();

        // Start with general settings
        settingsMainPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && !initialized) {
                // Start with general settings
                settingsMainPane.getChildren().add(settingsPanes.get(SettingsPaneType.GENERAL));
                updateButtonStyle(generalButton);
                initialized = true;
            }
        });
        eventBus.subscribe(SettingsChangedEvent.class, this::onSettingsClosedEvent);
        eventBus.subscribe(OpenSyncSettingsEvent.class, this::onOpenSyncSettingsEvent);
    }

    private void onOpenSyncSettingsEvent(OpenSyncSettingsEvent openSyncSettingsEvent) {
        switchToPane(SettingsPaneType.SYNC);
        updateButtonStyle(syncButton);
    }

    private void loadSettingsPanes() throws IOException {
        // Load general settings pane
        FXMLLoader generalLoader = new FXMLLoader(getClass().getResource("/fxml/settings-general-view.fxml"));
        settingsPanes.put(SettingsPaneType.GENERAL, generalLoader.load());

        // Load sync settings pane
        FXMLLoader syncLoader = new FXMLLoader(getClass().getResource("/fxml/settings-sync-view.fxml"));
        settingsPanes.put(SettingsPaneType.SYNC, syncLoader.load());
    }

    private void setupEventHandlers() {
        generalButton.setOnAction(event -> {
            switchToPane(SettingsPaneType.GENERAL);
            updateButtonStyle(generalButton);
            event.consume();
        });

        syncButton.setOnAction(event -> {
            switchToPane(SettingsPaneType.SYNC);
            updateButtonStyle(syncButton);
            event.consume();
        });
    }

    public void initController(Consumer<Void> onCloseButtonClickAction) {
        closeSettingsButton.setOnAction(event -> {
            switchToPane(SettingsPaneType.GENERAL);
            updateButtonStyle(generalButton);

            if (onCloseButtonClickAction != null) {
                onCloseButtonClickAction.accept(null);
            }

            eventBus.publish(new SettingsChangedEvent("settings_closed", null));
            event.consume();
        });
    }

    private void switchToPane(SettingsPaneType paneType) {
        settingsMainPane.getChildren().clear();
        settingsMainPane.getChildren().add(settingsPanes.get(paneType));

        Stage stage = getStageFromNode(settingsMainPane);
        if (stage != null) {
            stage.sizeToScene();
        }
    }

    private void onSettingsClosedEvent(SettingsChangedEvent settingsChangedEvent) {
        log.debug("Settings closed event received: {}", settingsChangedEvent);
        if (settingsChangedEvent.getSettingName().equals("settings_closed")) {
            setDefaultPane();
        }
    }

    private void setDefaultPane() {
        switchToPane(SettingsPaneType.GENERAL);
        updateButtonStyle(generalButton);
    }

    private void updateButtonStyle(MFXButton newSelected) {
        if (selectedButton == newSelected) {
            return;
        }

        newSelected.setStyle("-fx-background-color: #94a0b8;");
        if (selectedButton != null) {
            selectedButton.setStyle("-fx-background-color: #333C4DFF;");
        }
        selectedButton = newSelected;
    }

    private enum SettingsPaneType {
        GENERAL,
        SYNC
    }
}
