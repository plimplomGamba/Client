package de.plimplom.addonreader.view.message;

import de.plimplom.addonreader.config.DependencyInjector;
import de.plimplom.addonreader.event.EventBus;
import de.plimplom.addonreader.event.OpenSyncSettingsEvent;
import de.plimplom.addonreader.event.SettingsChangedEvent;
import io.github.palexdev.mfxcore.controls.Text;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

@Slf4j
public class SyncFailedMessage extends MessageBox {
    public SyncFailedMessage() {
        getStyleClass().add("error");

        title.setText("Data could not be synced");

        VBox contentPane = new VBox();
        contentPane.getStyleClass().add("content-pane");

        Text content = new Text("An error occurred while trying to sync addon data to the server. Make sure you are connected to the internet and have put in the correct setting.");
        content.getStyleClass().add("content");

        Hyperlink settingsLink = new Hyperlink("Open settings");
        settingsLink.getStyleClass().add("link");

        settingsLink.setOnAction(event -> {
            DependencyInjector.getService(EventBus.class).publish(new OpenSyncSettingsEvent());
            event.consume();
        });

        Text content2 = new Text("If the error persists, please open an issue on the addon repository:");
        content2.getStyleClass().add("content");
        Hyperlink issueLink = new Hyperlink("Github page");
        issueLink.getStyleClass().add("link");

        issueLink.setOnAction(event -> {
            try {
                Desktop.getDesktop().browse(java.net.URI.create("https://github.com/plimplomGamba/Addon/issues"));
            } catch (Exception e) {
                log.error("Failed to open addon download link", e);
            }
            event.consume();
        });

        contentPane.getChildren().addAll(content, settingsLink, content2, issueLink);
        contentPane.autosize();

        getChildren().addAll(contentPane);

        requestFocus();
        contentPane.requestFocus();

        contentPane.autosize();
        autosize();

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(10), _ -> {
                    DependencyInjector.getService(EventBus.class).publish(new SettingsChangedEvent("settings_error", null));
                })
        );
        timeline.play();
    }
}
