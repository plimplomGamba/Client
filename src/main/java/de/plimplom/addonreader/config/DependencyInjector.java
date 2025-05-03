package de.plimplom.addonreader.config;

import de.plimplom.addonreader.event.EventBus;
import de.plimplom.addonreader.model.ApplicationProperties;
import de.plimplom.addonreader.repository.PropertiesRepository;
import de.plimplom.addonreader.service.*;
import de.plimplom.addonreader.viewmodel.MainContentViewModel;
import de.plimplom.addonreader.viewmodel.SettingsViewModel;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DependencyInjector {
    private static final Map<Class<?>, Object> instances = new HashMap<>();
    private static final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    static {
        // Register singletons
        register(EventBus.class, new EventBus());
        register(ExecutorService.class, executorService);
        register(WoWFolderService.class, new WoWFolderService());
        register(PropertiesRepository.class, new PropertiesRepository());
        register(ApplicationProperties.class, new ApplicationProperties(getService(PropertiesRepository.class),getService(WoWFolderService.class)));

        // Register services
        register(StatsDataService.class, new StatsDataService(
                getService(ApplicationProperties.class),
                getService(ExecutorService.class),
                getService(EventBus.class)
        ));

        register(StatsFileListenerService.class, new StatsFileListenerService(
                getService(ApplicationProperties.class),
                getService(StatsDataService.class),
                getService(ExecutorService.class),
                getService(EventBus.class)
        ));

        register(PlatformIntegrationService.class, new PlatformIntegrationService());

        // Register ViewModels
        register(MainContentViewModel.class, new MainContentViewModel(
                getService(ApplicationProperties.class),
                getService(WoWFolderService.class),
                getService(StatsDataService.class),
                getService(StatsFileListenerService.class),
                getService(EventBus.class)
        ));
        register(SettingsViewModel.class, new SettingsViewModel(
                getService(ApplicationProperties.class),
                getService(PlatformIntegrationService.class),
                getService(EventBus.class)
        ));

    }

    public static MinimizeService createMinimizeService(Stage stage, String iconPath, String tooltip) {
        return new MinimizeService(stage, iconPath, tooltip, getService(ApplicationProperties.class));
    }

    private static <T> void register(Class<T> type, T instance) {
        instances.put(type, instance);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getService(Class<T> type) {
        return (T) instances.get(type);
    }

    public static void shutdown() {
        executorService.shutdown();
    }
}
