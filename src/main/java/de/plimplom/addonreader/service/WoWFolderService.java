package de.plimplom.addonreader.service;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class WoWFolderService {
    public List<String> getAccountNames(File selectedDirectory) throws IOException {
        Path accountFolder = Paths.get(selectedDirectory.getAbsolutePath(), "WTF", "Account");
        if (Files.notExists(accountFolder) || !Files.isDirectory(accountFolder)) {
            return new ArrayList<>();
        }

        try (var pathStream = Files.walk(accountFolder, 1)) {
            return pathStream
                    .filter(path -> !path.getFileName().toString().equals("Account") &&
                            !path.getFileName().toString().equals("SavedVariables"))
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        }
    }

    public boolean isValidWowFolder(File selectedDirectory) {
        try {
            boolean selectedExists = selectedDirectory.exists();
            boolean wowExecutableExists = selectedExists &&
                    (Files.exists(Paths.get(selectedDirectory.getAbsolutePath(), "WoW.exe")) ||
                            Files.exists(Paths.get(selectedDirectory.getAbsolutePath(), "WowT.exe")));

            Path interfaceFolder = Paths.get(selectedDirectory.getAbsolutePath(), "Interface");
            Path wtfFolder = Paths.get(selectedDirectory.getAbsolutePath(), "WTF");

            boolean interfaceAndWtfExists = selectedExists
                    && Files.exists(interfaceFolder)
                    && Files.isDirectory(interfaceFolder)
                    && Files.exists(wtfFolder)
                    && Files.isDirectory(wtfFolder);

            return selectedExists && wowExecutableExists && interfaceAndWtfExists;
        } catch (Exception e) {
            log.error("Error checking WoW folder validity", e);
            return false;
        }
    }

    public boolean isAddonInstalled(File selectedDirectory) {
        try {
            Path interfaceFolder = Paths.get(selectedDirectory.getAbsolutePath(), "Interface");
            Path addonFolder = Paths.get(selectedDirectory.getAbsolutePath(), "Interface", "AddOns");
            Path plimplomGambaFolder = Paths.get(selectedDirectory.getAbsolutePath(), "Interface", "AddOns", "plimplomGamba");

            return Files.exists(interfaceFolder) && Files.isDirectory(interfaceFolder)
                    && Files.exists(addonFolder) && Files.isDirectory(addonFolder)
                    && Files.exists(plimplomGambaFolder) && Files.isDirectory(plimplomGambaFolder);
        } catch (Exception e) {
            log.error("Error checking addon installation", e);
            return false;
        }
    }
}
