package de.plimplom.addonreader.service;

import de.plimplom.addonreader.lua.TableParser;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import de.plimplom.addonreader.event.DataSyncEvent;
import de.plimplom.addonreader.event.EventBus;
import de.plimplom.addonreader.model.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Future;

@Slf4j
public class StatsFileListenerService {
    private final ApplicationProperties properties;
    private final StatsDataService dataService;
    private final ExecutorService executorService;
    private final EventBus eventBus;

    private WatchService watchService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Future<?> watchTask;

    public StatsFileListenerService(
            ApplicationProperties properties,
            StatsDataService dataService,
            ExecutorService executorService,
            EventBus eventBus) {
        this.properties = properties;
        this.dataService = dataService;
        this.executorService = executorService;
        this.eventBus = eventBus;
    }

    public synchronized void startListening() {
        if (running.get()) {
            log.info("File listener is already running");
            return;
        }

        String folderPath = properties.wowFolderProperty().get();
        String accountName = properties.wowAccountProperty().get();

        if (folderPath == null || accountName == null || folderPath.isEmpty() || accountName.isEmpty()) {
            log.warn("Cannot start file listener: WoW folder or account name not set");
            eventBus.publish(new DataSyncEvent(false, "WoW folder or account name not set"));
            return;
        }

        Path savedVariablesPath = Paths.get(folderPath, "WTF", "Account", accountName, "SavedVariables");
        if (!Files.exists(savedVariablesPath)) {
            log.warn("Cannot start file listener: SavedVariables directory does not exist: {}", savedVariablesPath);
            eventBus.publish(new DataSyncEvent(false, "SavedVariables directory does not exist"));
            return;
        }

        try {
            log.info("Starting file listener for directory: {}", savedVariablesPath);
            watchService = FileSystems.getDefault().newWatchService();
            savedVariablesPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);

            running.set(true);

            // Process the addon file if it already exists
            processAddonFile(savedVariablesPath);

            // Start the watch task
            watchTask = executorService.submit(() -> watchDirectory(savedVariablesPath));

            log.info("File listener started successfully");
        } catch (IOException e) {
            log.error("Failed to start file listener", e);
            eventBus.publish(new DataSyncEvent(false, "Failed to start file listener: " + e.getMessage()));
        }
    }

    private void watchDirectory(Path directoryPath) {
        log.info("Watch task started for directory: {}", directoryPath);

        try {
            WatchKey key;
            while (running.get() && (key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.context() instanceof Path contextPath) {
                        if (contextPath.toString().equals("plimplomGamba.lua")) {
                            log.debug("Detected change in plimplomGamba.lua");
                            processAddonFile(directoryPath);
                        }
                    }
                }

                // Reset the key - this step is critical for receiving further events
                boolean valid = key.reset();
                if (!valid) {
                    log.warn("Watch key is no longer valid. Directory may no longer be accessible");
                    break;
                }
            }
        } catch (InterruptedException e) {
            if (running.get()) {
                log.error("Watch task interrupted", e);
            } else {
                log.debug("Watch task stopped by request");
            }
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Error in watch task", e);
            eventBus.publish(new DataSyncEvent(false, "Error watching for file changes: " + e.getMessage()));
        }
    }

    public void processAddonFile(Path directoryPath) {
        try {
            Path addonFilePath = directoryPath.resolve("plimplomGamba.lua");
            if (Files.exists(addonFilePath)) {
                log.info("Processing addon file: {}", addonFilePath);

                // Parse the Lua file
                var jsonData = TableParser.parseLuaTableToJsonNative(Files.newInputStream(addonFilePath));

                // Extract the account name from the path
                String accountName = directoryPath.getName(directoryPath.getNameCount() - 2).toString();

                // Send the data to the server
                dataService.sendData(jsonData, accountName)
                        .exceptionally(ex -> {
                            log.error("Failed to send data", ex);
                            eventBus.publish(new DataSyncEvent(false, "Failed to send data: " + ex.getMessage()));
                            return null;
                        });
            } else {
                log.warn("Addon file does not exist: {}", addonFilePath);
            }
        } catch (Exception e) {
            log.error("Failed to process addon file", e);
            eventBus.publish(new DataSyncEvent(false, "Failed to process addon file: " + e.getMessage()));
        }
    }

    public synchronized void stopListening() {
        if (!running.get()) {
            return;
        }

        log.info("Stopping file listener");
        running.set(false);

        // Cancel the watch task
        if (watchTask != null && !watchTask.isDone()) {
            watchTask.cancel(true);
        }

        // Close the watch service
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                log.error("Error closing watch service", e);
            }
        }

        log.info("File listener stopped");
    }

    public boolean isRunning() {
        return running.get();
    }
//
//    private static StatsFileListenerService INSTANCE;
//    private final
//    WatchService watchService
//            = FileSystems.getDefault().newWatchService();
//    private final StatsDataSender dataSender;
//
//    private final AtomicBoolean running = new AtomicBoolean(false);
//    private final ExecutorService executor;
//    private static final Logger log = LoggerFactory.getLogger(StatsFileListenerService.class);
//
//    private StatsFileListenerService(StatsDataSender dataSender, ExecutorService executor) throws IOException {
//        this.executor = executor;
//        this.dataSender = dataSender;
//    }
//
////    public static synchronized StatsFileListener createInstance(StatsDataSender dataSender, ExecutorService executor) throws IOException {
////        if (INSTANCE == null) {
////            INSTANCE = new StatsFileListener(dataSender, executor);
////        }
////    }
//
//    public static synchronized StatsFileListenerService getInstance(ApplicationProperties properties) throws IOException {
//        if (INSTANCE == null) {
//            INSTANCE = new StatsFileListenerService(new StatsDataSender(properties), Executors.newSingleThreadExecutor());
//        }
//        return INSTANCE;
//    }
//
//    public void startListening(File directoryPath) throws IOException {
//        log.info("Starting to listen to folder {}", directoryPath);
//        if (running.get()) {
//            return;
//        }
//        running.set(true);
//
//        Path path = Paths.get(directoryPath.getAbsolutePath());
//
//        path.register(
//                watchService,
//                StandardWatchEventKinds.ENTRY_CREATE);
//
//        executor.submit(() -> {
//
//            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
//                var addonLuaPath = Paths.get(directoryPath.getAbsolutePath(), "plimplomGamba.lua");
//                var data = TableParser.parseLuaTableToJsonNative(Files.newInputStream(addonLuaPath));
//                dataSender.sendData(data, path.getName(path.getNameCount() - 2).toString());
//
//                path.register(
//                        watchService,
//                        StandardWatchEventKinds.ENTRY_CREATE
//                );
//
//                WatchKey key;
//                while (running.get() && (key = watchService.take()) != null) {
//                    for (WatchEvent<?> event : key.pollEvents()) {
//                        if (event.context() instanceof Path contextPath && contextPath.toString().equals("plimplomGamba.lua")) {
//                            data = TableParser.parseLuaTableToJsonNative(Files.newInputStream(addonLuaPath));
//
//                            dataSender.sendData(data, path.getName(path.getNameCount() - 2).toString());
//                        }
//                    }
//                    key.reset();
//                }
//            } catch (Exception e) {
//                log.error("Exception occurred while trying to listen to folder {}.", directoryPath, e);
//            }
//        });
//    }
//
//    public void stopListening() {
//        running.set(false);
//    }
}
