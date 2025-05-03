package de.plimplom.addonreader.view.custom;

import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.scene.control.TextField;

public class CustomTextField extends MFXTextField {

    public TextField getBoundTextField() {
        return boundField;
    }
}
