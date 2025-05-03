package de.plimplom.addonreader.view;

import de.plimplom.addonreader.config.DependencyInjector;
import de.plimplom.addonreader.event.SettingsChangedEvent;
import de.plimplom.addonreader.viewmodel.SettingsViewModel;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;

public class SettingsSyncView extends BaseController {
    @FXML
    private MFXTextField syncHostField;
    @FXML
    private MFXPasswordField syncPassField;

    private final SettingsViewModel viewModel;

    public SettingsSyncView() {
        this.viewModel = DependencyInjector.getService(SettingsViewModel.class);
    }

    @FXML
    private void initialize() {
        // Bind UI elements to ViewModel properties
        syncHostField.textProperty().bindBidirectional(viewModel.syncHostProperty());
        syncPassField.textProperty().bindBidirectional(viewModel.syncPassProperty());

        // Set up change listeners to publish events
        syncHostField.textProperty().addListener((obs, oldVal, newVal) -> {
            eventBus.publish(new SettingsChangedEvent("syncHost", newVal));
        });

        syncPassField.textProperty().addListener((obs, oldVal, newVal) -> {
            eventBus.publish(new SettingsChangedEvent("syncPass", newVal));
        });

        setFloating(syncHostField);
        setFloating(syncPassField);
    }

    private void setFloating(MFXTextField textField) {
        if(!textField.getText().isEmpty()){
            textField.floatingProperty().set(true);
        }
    }
}
