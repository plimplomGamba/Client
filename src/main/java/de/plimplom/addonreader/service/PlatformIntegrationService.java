package de.plimplom.addonreader.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
public class PlatformIntegrationService {
    private static final String APP_NAME = "plimplomGamba";

    /**
     * Sets whether the application should start with Windows.
     *
     * @param enabled true to enable auto-start, false to disable
     * @return true if the operation was successful, false otherwise
     */
    public boolean setStartWithWindows(boolean enabled) {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return setStartWithWindowsOnWindows(enabled);
        } else if (os.contains("mac")) {
            throw new NotImplementedException("Auto-start on macOS is not yet implemented");
        } else if (os.contains("linux") || os.contains("unix")) {
            throw new NotImplementedException("Auto-start on macOS is not yet implemented");
        } else {
            log.warn("Auto-start not supported on this operating system: {}", os);
            return false;
        }
    }

    private boolean setStartWithWindowsOnWindows(boolean enabled) {
        try {
            String appPath = getApplicationPath();
            Path startupFolder = getWindowsStartupFolder();
            Path shortcutPath = startupFolder.resolve(APP_NAME + ".lnk");

            if (enabled) {
                // Create shortcut in startup folder
                // Using PowerShell to create a shortcut
                String command = String.format(
                        "powershell.exe -Command \"$WshShell = New-Object -ComObject WScript.Shell; " +
                                "$Shortcut = $WshShell.CreateShortcut('%s'); " +
                                "$Shortcut.TargetPath = '%s'; " +
                                "$Shortcut.Save()\"",
                        shortcutPath, appPath);

                Process process = Runtime.getRuntime().exec(command);
                int exitCode = process.waitFor();

                return exitCode == 0;
            } else {
                // Remove shortcut from startup folder
                if (Files.exists(shortcutPath)) {
                    Files.delete(shortcutPath);
                }
                return true;
            }
        } catch (Exception e) {
            log.error("Failed to {} auto-start on Windows", enabled ? "enable" : "disable", e);
            return false;
        }
    }

    private Path getWindowsStartupFolder() {
        String appData = System.getenv("APPDATA");
        return Paths.get(appData, "Microsoft", "Windows", "Start Menu", "Programs", "Startup");
    }

    private String getApplicationPath() {
        try {
            // Get the location of the JAR file
            String jarPath = PlatformIntegrationService.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                // Windows paths need to be adjusted
                jarPath = jarPath.substring(1).replace("/", "\\");
            }

            if (jarPath.endsWith(".jar")) {
                // Running from JAR file
                return "java -jar \"" + jarPath + "\"";
            } else {
                // Running from IDE or exploded JAR
                String javaHome = System.getProperty("java.home");
                String javaBin = javaHome +
                        (System.getProperty("os.name").toLowerCase().contains("win") ?
                                "\\bin\\java.exe" : "/bin/java");

                return "\"" + javaBin + "\" -cp \"" + jarPath + "\" " +
                        "de.plimplom.addonreader.app.PlimplomGambaClient";
            }
        } catch (Exception e) {
            log.error("Failed to determine application path", e);
            return "";
        }
    }
}
