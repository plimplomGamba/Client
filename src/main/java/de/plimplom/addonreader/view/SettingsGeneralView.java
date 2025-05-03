package de.plimplom.addonreader.view;

import de.plimplom.addonreader.config.DependencyInjector;
import de.plimplom.addonreader.event.SettingsChangedEvent;
import de.plimplom.addonreader.viewmodel.SettingsViewModel;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import javafx.fxml.FXML;

public class SettingsGeneralView extends BaseController {
    @FXML
    private MFXCheckbox startWithWindows;
    @FXML
    private MFXCheckbox minimizeToTray;

    private final SettingsViewModel viewModel;

    public SettingsGeneralView() {
        this.viewModel = DependencyInjector.getService(SettingsViewModel.class);

        // Subscribe to settings changed events
        eventBus.subscribe(SettingsChangedEvent.class, this::onSettingsChanged);
    }

    @FXML
    private void initialize() {
        // Bind UI elements to ViewModel properties
        startWithWindows.selectedProperty().bindBidirectional(viewModel.startWithWindowsProperty());
        minimizeToTray.selectedProperty().bindBidirectional(viewModel.minimizeToTrayProperty());
    }

    private void onSettingsChanged(SettingsChangedEvent event) {
        if (event.getSettingName().equals("startWithWindowsError")) {

            // Reset the checkbox to match the actual state
            // This avoids the checkbox showing an invalid state
            startWithWindows.setSelected(!startWithWindows.isSelected());
        }
    }
}
