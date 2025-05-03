package de.plimplom.addonreader.util;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class FileUtils {
    public static File chooseDirectory(Stage stage, String title, String initialDirectory) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(title);

        if (initialDirectory != null && !initialDirectory.isEmpty()) {
            File initialDir = new File(initialDirectory);
            if (initialDir.exists() && initialDir.isDirectory()) {
                chooser.setInitialDirectory(initialDir);
            } else {
                chooser.setInitialDirectory(new File(System.getProperty("user.home")));
            }
        } else {
            chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }

        return chooser.showDialog(stage);
    }

    public static boolean folderExists(String path) {
        if (path == null || path.isEmpty()) return false;

        File folder = new File(path);
        return folder.exists() && folder.isDirectory();
    }

    public static List<Path> listFiles(Path directory, int depth, Predicate<Path> filter) throws IOException {
        try (Stream<Path> pathStream = Files.walk(directory, depth)) {
            return pathStream.filter(filter).collect(Collectors.toList());
        }
    }

    public static String getAppDataPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String appName = "plimplomGamba";

        if (os.contains("win")) {
            return System.getProperty("user.home") + "\\AppData\\Roaming\\" + appName;
        } else if (os.contains("mac")) {
            return System.getProperty("user.home") + "/Library/Application Support/" + appName;
        } else {
            return System.getProperty("user.home") + "/." + appName;
        }
    }

    public static void createDirectoryIfNotExists(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
}
