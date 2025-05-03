package de.plimplom.addonreader.app;

import de.plimplom.addonreader.config.DependencyInjector;
import de.plimplom.addonreader.config.LoggerConfiguration;
import de.plimplom.addonreader.event.EventBus;
import de.plimplom.addonreader.event.ResizeEvent;
import de.plimplom.addonreader.service.StatsFileListenerService;
import de.plimplom.addonreader.util.ThreadManager;
import de.plimplom.addonreader.view.MainWindowView;
import fr.brouillard.oss.cssfx.CSSFX;
import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class PlimplomGambaClient extends Application {
    @Override
    public void init() {
        // Initialize logger
        LoggerConfiguration.configureLogger();
        log.info("Application starting up");

        // Configure SSL to trust all certificates
        System.setProperty("javax.net.ssl.trustAll", "true");
    }

    @Override
    public void start(Stage stage) throws IOException {
        log.info("Loading application fonts and styles");
        loadFonts();
        setupStyles();

        // Set implicit exit to false to control application shutdown
        Platform.setImplicitExit(false);

        // Create main window
        log.info("Creating main application window");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/main-window-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        MainWindowView controller = fxmlLoader.getController();
        stage.setResizable(true);
        controller.setStage(stage);
        stage.setScene(scene);

        // Set window style and icon
        stage.initStyle(StageStyle.UNDECORATED);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/plimplomgamba512.png")));

        // Show the window
        stage.show();
        stage.sizeToScene();

        DependencyInjector.getService(EventBus.class).subscribe(ResizeEvent.class, _ -> Platform.runLater(stage::sizeToScene) );

        log.info("Application started successfully");
    }

    private void loadFonts() {
        Font jetbrainsFont = Font.loadFont(getClass().getResourceAsStream("/fonts/jetbrains-mono.regular.ttf"), 12);
        log.info("Loaded font {} - {}.", jetbrainsFont.getName(), jetbrainsFont.getFamily());
    }

    private void setupStyles() {
        CSSFX.start();

        UserAgentBuilder.builder()
                .themes(JavaFXThemes.MODENA)
                .themes(MaterialFXStylesheets.forAssemble(true))
                .setDeploy(true)
                .setResolveAssets(true)
                .build()
                .setGlobal();
    }

    @Override
    public void stop() throws Exception {
        log.info("Application shutting down");

        // Stop services
        DependencyInjector.getService(StatsFileListenerService.class).stopListening();
        DependencyInjector.getService(EventBus.class).shutdown();
        DependencyInjector.shutdown();

        // Shutdown thread pools
        ThreadManager.shutdown();

        super.stop();
        log.info("Application shutdown complete");
    }

    public static void main(String[] args) {
        launch();
    }
}