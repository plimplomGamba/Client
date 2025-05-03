package de.plimplom.addonreader.view;

import de.plimplom.addonreader.config.DependencyInjector;
import de.plimplom.addonreader.event.DataSyncEvent;
import de.plimplom.addonreader.event.InfoMessageEvent;
import de.plimplom.addonreader.event.ResizeEvent;
import de.plimplom.addonreader.event.SettingsChangedEvent;
import de.plimplom.addonreader.model.ApplicationProperties;
import de.plimplom.addonreader.util.FileUtils;
import de.plimplom.addonreader.view.custom.CustomTextField;
import de.plimplom.addonreader.view.message.SyncFailedMessage;
import de.plimplom.addonreader.viewmodel.MainContentViewModel;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.mfxcore.controls.Label;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static de.plimplom.addonreader.event.InfoMessageEvent.EventName.SYNC_FAILED;

@Slf4j
public class MainContentView extends BaseController {
    @FXML
    public VBox accountSelectBox;
    @FXML
    public VBox syncBox;
    @FXML
    public VBox infoMessageBox;
    @FXML
    public MFXButton syncDataNowButton;
    @FXML
    public MFXCheckbox syncDataAutoCheckbox;
    @FXML
    private CustomTextField folderSelectTextField;
    @FXML
    private MFXComboBox<String> accountSelectComboBox;
    @FXML
    private AnchorPane mainAnchor;

    private final MainContentViewModel viewModel;
    private boolean isFolderSelectOpen = false;
    @Setter
    private Runnable onOpenSettings;
    private BooleanProperty requireSettingsChange = new SimpleBooleanProperty(false);
    private BooleanProperty showMessageBox = new SimpleBooleanProperty(false);
    private final Map<InfoMessageEvent.EventName, VBox> openMessages = new HashMap<>();

    public MainContentView() {
        this.viewModel = DependencyInjector.getService(MainContentViewModel.class);

        // Register for events
        eventBus.subscribe(DataSyncEvent.class, this::onDataSyncEvent);
    }

    @FXML
    private void initialize() {
        setupUI();
        setupEventHandlers();
        setupBindings();
    }

    private void setupUI() {
        accountSelectComboBox.rowsCountProperty().set(1);
        accountSelectComboBox.setContextMenuDisabled(true);

        Label trailingIcon = new Label("🗁");
        trailingIcon.setStyle("-fx-text-fill: white;-fx-font-size: 24px");
        folderSelectTextField.setTrailingIcon(trailingIcon);
        folderSelectTextField.setContextMenuDisabled(true);

        Tooltip folderTooltip = new Tooltip("Select your World of Warcraft installation folder");
        Tooltip.install(folderSelectTextField, folderTooltip);

        Tooltip accountTooltip = new Tooltip("Select your WoW account");
        Tooltip.install(accountSelectComboBox, accountTooltip);

        eventBus.publish(new ResizeEvent());
    }

    private void setupEventHandlers() {
        mainAnchor.setOnMouseClicked(event -> {
            mainAnchor.requestFocus();
            event.consume();
        });

        accountSelectComboBox.setOnAction(event -> {
            mainAnchor.requestFocus();
            event.consume();
        });

        folderSelectTextField.getBoundTextField().onMouseClickedProperty().set(event -> {
            event.consume();
            try {
                handleFolderSelect();
            } catch (IOException e) {
                log.error("Error occurred while trying to select WoW folder.", e);
                //TODO add error message
            }
        });

        folderSelectTextField.setOnMouseClicked(mouseEvent -> {
            mouseEvent.consume();
            try {
                handleFolderSelect();
            } catch (IOException e) {
                log.error("Error occurred while trying to select WoW folder.", e);
                //TODO add error message
            }
        });

        syncDataNowButton.setOnAction(event -> {
            viewModel.syncDataNow();
            event.consume();
        });
    }

    private void setupBindings() {
        folderSelectTextField.textProperty().bindBidirectional(viewModel.wowFolderProperty());
        accountSelectComboBox.valueProperty().bindBidirectional(viewModel.wowAccountProperty());
        accountSelectComboBox.textProperty().bindBidirectional(viewModel.wowAccountProperty());
        accountSelectComboBox.itemsProperty().bind(viewModel.accountsProperty());


        // Bind the account selection box visibility to folder validity and addon installation
        accountSelectBox.visibleProperty().bind(
                Bindings.and(
                        viewModel.folderValidProperty(),
                        viewModel.addonInstalledProperty()
                )
        );

        // Also bind managed property to avoid empty space
        accountSelectBox.managedProperty().bind(accountSelectBox.visibleProperty());

        syncBox.visibleProperty().bind(
                Bindings.and(
                        Bindings.and(
                                viewModel.folderValidProperty(),
                                viewModel.addonInstalledProperty()
                        ),
                        Bindings.not(viewModel.wowAccountProperty().isEmpty())
                )
        );

        syncBox.managedProperty().bind(syncBox.visibleProperty());

        syncBox.managedProperty().addListener((observable, oldValue, newValue) -> {
            eventBus.publish(new ResizeEvent());
        });
        accountSelectBox.managedProperty().addListener((observable, oldValue, newValue) -> {
            eventBus.publish(new ResizeEvent());
        });
        infoMessageBox.managedProperty().addListener((observable, oldValue, newValue) -> {
            eventBus.publish(new ResizeEvent());
        });

        infoMessageBox.visibleProperty().bind(showMessageBox);
        infoMessageBox.managedProperty().bind(infoMessageBox.visibleProperty());

        syncDataAutoCheckbox.selectedProperty().bindBidirectional(viewModel.autoSyncEnabledProperty());

        // Bind visibility and enabled state of sync controls
        BooleanBinding accountSelected = Bindings.createBooleanBinding(
                () -> accountSelectComboBox.getValue() != null && !accountSelectComboBox.getValue().isEmpty(),
                accountSelectComboBox.valueProperty()
        );

        // Sync controls should only be enabled when sync config is valid
        syncDataNowButton.disableProperty().bind(
                Bindings.or(
                        Bindings.or(
                                Bindings.not(viewModel.syncConfigValidProperty()),
                                Bindings.not(accountSelected)
                        ),
                        requireSettingsChange
                )
        );

        syncDataAutoCheckbox.disableProperty().bind(
                Bindings.or(
                        Bindings.or(
                                Bindings.not(viewModel.syncConfigValidProperty()),
                                Bindings.not(accountSelected)
                        ),
                        requireSettingsChange
                )
        );

        // Sync controls box should only be visible when folder is valid and addon is installed
        syncBox.visibleProperty().bind(
                Bindings.and(
                        viewModel.folderValidProperty(),
                        viewModel.addonInstalledProperty()
                )
        );
        syncBox.managedProperty().bind(syncBox.visibleProperty());

        var properties = DependencyInjector.getService(ApplicationProperties.class);

        properties.syncPassProperty().addListener((observable, oldValue, newValue) -> {
            requireSettingsChange.set(false);
        });
        properties.syncHostProperty().addListener((observable, oldValue, newValue) -> {
            requireSettingsChange.set(false);
        });

        eventBus.subscribe(InfoMessageEvent.class, this::onInfoMessageEvent);
        eventBus.subscribe(SettingsChangedEvent.class, this::onSettingsChangedEvent);
    }

    private void onSettingsChangedEvent(SettingsChangedEvent settingsChangedEvent) {
        if (settingsChangedEvent.getSettingName().equals("settings_error")) {
            requireSettingsChange.set(false);
        }
    }

    private synchronized void onInfoMessageEvent(InfoMessageEvent event) {
        Platform.runLater(() -> {
            if (event.getEventType() == InfoMessageEvent.EventType.CREATE) {
                log.debug("Checking if message is already present: {}", event.getMessage());
                openMessages.putIfAbsent(event.getEventName(), event.getMessage());
            } else if (event.getEventType() == InfoMessageEvent.EventType.DELETE) {
                log.debug("Removing info message: {}", event.getEventName());
                openMessages.remove(event.getEventName());
            }

            infoMessageBox.getChildren().clear();
            infoMessageBox.getChildren().addAll(openMessages.values());

            showMessageBox.set(!infoMessageBox.getChildren().isEmpty());
            eventBus.publish(new ResizeEvent());
        });
    }

    private void handleFolderSelect() throws IOException {
        if (isFolderSelectOpen) {
            return;
        }
        isFolderSelectOpen = true;

        Stage stage = getStageFromNode(mainAnchor);
        File selectedDirectory = FileUtils.chooseDirectory(stage, "Select WoW Folder",
                folderSelectTextField.getText());

        if (selectedDirectory != null) {
            folderSelectTextField.setText(selectedDirectory.getAbsolutePath());

            try {
                viewModel.updateAccountOptions(selectedDirectory);
            } catch (IOException e) {
                log.error("Failed to update account options", e);
                //TODO add error message
            }
            mainAnchor.requestFocus();
        }

        mainAnchor.requestFocus();
        isFolderSelectOpen = false;
    }

    private void onDataSyncEvent(DataSyncEvent event) {
        if (event.isSuccess()) {
            log.info("Successfully synchronized data");
            //TODO add error message
            eventBus.publish(new InfoMessageEvent(InfoMessageEvent.EventType.DELETE, SYNC_FAILED));
        } else {
            log.error("Failed to synchronize data: {}", event.getErrorMessage());
            DependencyInjector.getService(ApplicationProperties.class).autoSyncEnabledProperty().set(false);
            requireSettingsChange.set(true);
            eventBus.publish(new InfoMessageEvent(InfoMessageEvent.EventType.CREATE, SYNC_FAILED, new SyncFailedMessage()));
            //TODO add error message
        }
    }
}
