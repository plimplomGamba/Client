package de.plimplom.addonreader.view.message;

import io.github.palexdev.mfxcore.controls.Label;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class MessageBox extends VBox {
    protected final Label title = new Label();

    protected MessageBox() {
        log.debug("Creating message box {}", getClass().getSimpleName());
        getStyleClass().add("message-box");

        title.getStyleClass().add("title");
        getChildren().add(title);
        setSpacing(10);
    }

}
