package de.plimplom.addonreader.view.message;

import de.plimplom.addonreader.config.DependencyInjector;
import de.plimplom.addonreader.event.EventBus;
import de.plimplom.addonreader.event.RecheckAddonInstallEvent;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.mfxcore.controls.Text;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

@Slf4j
public class InstallAddonMessage extends MessageBox {

    public InstallAddonMessage() {
        getStyleClass().add("error");

        title.setText("Addon missing");

        VBox contentPane = new VBox();
        contentPane.getStyleClass().add("content-pane");

        Text content = new Text("It looks like you don't have plimplomGamba installed. Make sure you have selected the correct WoW installation folder. If you don't have the addon installed, download the addon here:");
        content.getStyleClass().add("content");

        Hyperlink addonLink = new Hyperlink("Download addon");
        addonLink.setOnAction(event -> {
            try {
                Desktop.getDesktop().browse(java.net.URI.create("https://github.com/plimplomGamba/Addon/releases"));
            } catch (Exception e) {
                log.error("Failed to open addon download link", e);
            }
            event.consume();
        });

        contentPane.getChildren().addAll(content, addonLink);

        Text content2 = new Text("After you installed the addon, please click the button below to refresh.");
        content2.getStyleClass().add("content");

        MFXButton button = new MFXButton("Refresh");

        button.setOnAction(event -> {
            DependencyInjector.getService(EventBus.class).publish(new RecheckAddonInstallEvent());
        });


        getChildren().addAll(contentPane, content2, button);

        contentPane.autosize();
        autosize();
    }
}
