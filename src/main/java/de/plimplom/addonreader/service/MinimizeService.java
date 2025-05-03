package de.plimplom.addonreader.service;

import de.plimplom.addonreader.model.ApplicationProperties;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;

@Slf4j
public class MinimizeService {

    private final Stage stage;
    private final ApplicationProperties properties;
    private SystemTray tray;
    private TrayIcon trayIcon;

    public MinimizeService(Stage stage, String iconPath, String tooltip, ApplicationProperties properties) {
        this.properties = properties;
        this.stage = stage;

        // Check if system tray is supported
        if (!SystemTray.isSupported()) {
            log.warn("System tray is not supported");
            return;
        }

        try {
            tray = SystemTray.getSystemTray();

            // Load the tray icon image
            Image image = ImageIO.read(getClass().getResourceAsStream(iconPath));

            // Create a popup menu
            PopupMenu popup = buildPopupMenu(stage);

            // Create tray icon
            trayIcon = new TrayIcon(image, tooltip, popup);
            trayIcon.setImageAutoSize(true);

            // Add tray icon to system tray
            tray.add(trayIcon);

            // Add double-click behavior
            trayIcon.addActionListener(e -> Platform.runLater(() -> {
                stage.show();
                stage.setIconified(false);
                stage.toFront();
            }));

        } catch (Exception e) {
            log.error("Error while trying to minimize to tray:", e);
        }
    }

    private PopupMenu buildPopupMenu(Stage stage) {
        PopupMenu popup = new PopupMenu();

        // Create menu items
        MenuItem openItem = new MenuItem("Open");
        openItem.addActionListener(e -> {
            Platform.runLater(() -> {
                if (stage != null) {
                    stage.show();
                    stage.setIconified(false);
                    stage.toFront();
                }
            });
        });

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> {
            tray.remove(trayIcon);
            Platform.exit();
        });

        // Add menu items to popup
        popup.add(openItem);
        popup.addSeparator();
        popup.add(exitItem);
        return popup;
    }

    // Method to minimize to system tray
    public void minimizeToTray() {
        Platform.runLater(stage::hide);
    }

    public void minimize() {
        if (properties.getMinimizeToTray().get()) {
            Platform.runLater(stage::hide);
        } else {
            stage.setIconified(true);
        }
    }

    public void remove() {
        tray = SystemTray.getSystemTray();
        tray.remove(trayIcon);
    }
}
