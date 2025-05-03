package de.plimplom.addonreader.view.message;

import io.github.palexdev.mfxcore.controls.Text;
import javafx.scene.paint.Color;

public class InvalidFolderMessage extends MessageBox {
    public InvalidFolderMessage() {
        getStyleClass().add("error");

        title.setText("Invalid folder path");

        Text content = new Text("Please select a folder with a valid WoW installation. \nThe folder to select contains the WoW executable, i.e. \"Wow.exe\".");
        content.getStyleClass().add("content");

        getChildren().addAll(content);

        autosize();
    }
}
