package de.plimplom.addonreader.view;

import de.plimplom.addonreader.config.DependencyInjector;
import de.plimplom.addonreader.event.EventBus;
import de.plimplom.addonreader.event.OpenSyncSettingsEvent;
import de.plimplom.addonreader.event.SettingsChangedEvent;
import de.plimplom.addonreader.service.MinimizeService;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MainWindowView extends BaseController {
    @FXML
    private MFXButton settingsButton;
    @FXML
    private MFXButton exitButton;
    @FXML
    private MFXButton minimizeButton;
    @FXML
    private ImageView titleBarImage;
    @FXML
    private HBox titleBar;
    @FXML
    private Pane mainPane;

    private double xOffset;
    private double yOffset;

    private Stage stage;

    private OpenPaneEnum openPane = OpenPaneEnum.MAIN;
    private final Map<OpenPaneEnum, Node> openPanes = new HashMap<>();
    private MinimizeService minimizeService;

    @FXML
    private void initialize() {
        settingsButton.setText("⚙");

        titleBarImage.setImage(new Image(getClass().getResourceAsStream("/images/plimplomgamba_appbar_no space.png")));

        titleBar.setOnMousePressed(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            xOffset = stage.getX() - event.getScreenX();
            yOffset = stage.getY() - event.getScreenY();
            titleBar.requestFocus();
            event.consume();
        });

        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.setX(event.getScreenX() + xOffset);
            stage.setY(event.getScreenY() + yOffset);
            event.consume();
        });

        mainPane.setOnMouseClicked(_ -> mainPane.requestFocus());
    }

    public void setStage(Stage stage) throws IOException {
        this.stage = stage;
        settingsButton.setOnAction(_ -> onSettingsToggle());

        Platform.runLater(() -> minimizeService = DependencyInjector.createMinimizeService(
                stage, "/images/plimplomgamba32.png", "/images/plimplomGamba"));

        exitButton.setOnAction(_ -> {
            minimizeService.remove();
            stage.close();
            Platform.exit();
        });

        minimizeButton.setOnAction(_ -> minimizeService.minimize());

        var mainLoader = new FXMLLoader(MainContentView.class.getResource("/fxml/main-content-view.fxml"));
        var settingsLoader = new FXMLLoader(SettingsView.class.getResource("/fxml/settings-view.fxml"));

        openPanes.put(OpenPaneEnum.MAIN, mainLoader.load());
        openPanes.put(OpenPaneEnum.SETTINGS, settingsLoader.load());

        mainPane.getChildren().add(openPanes.get(OpenPaneEnum.MAIN));

        // Get and configure the main view controller
        MainContentView mainViewController = mainLoader.getController();
        mainViewController.setOnOpenSettings(this::onSettingsToggle);

        // Configure settings controller
        SettingsView settingsController = settingsLoader.getController();
        settingsController.initController(_ -> onSettingsToggle());

        DependencyInjector.getService(EventBus.class).subscribe(OpenSyncSettingsEvent.class, _ -> {
            openPane = OpenPaneEnum.SETTINGS;
            mainPane.getChildren().removeFirst();
            mainPane.getChildren().add(openPanes.get(OpenPaneEnum.SETTINGS));
            settingsButton.setStyle("-fx-background-color: #94a0b8;");
        });
    }

    private void onSettingsToggle() {
        if (openPane == OpenPaneEnum.MAIN) {
            openPane = OpenPaneEnum.SETTINGS;
            mainPane.getChildren().removeFirst();
            mainPane.getChildren().add(openPanes.get(OpenPaneEnum.SETTINGS));
            settingsButton.setStyle("-fx-background-color: #94a0b8;");
        } else {
            eventBus.publish(new SettingsChangedEvent("settings_closed", null));
            openPane = OpenPaneEnum.MAIN;
            mainPane.getChildren().removeFirst();
            mainPane.getChildren().add(openPanes.get(OpenPaneEnum.MAIN));
            settingsButton.setStyle("-fx-background-color: #0B0E14FF;");
        }
        stage.sizeToScene();
    }

    private enum OpenPaneEnum {
        MAIN,
        SETTINGS;
    }
}
