[Setup]
AppName=plimplomGamba
AppVersion={#AppVersion}
DefaultDirName={localappdata}\plimplomGamba
DisableDirPage=yes
OutputDir={#SourcePath}\dist
OutputBaseFilename=plimplomGamba_setup
; Set the icon for the installer exe
SetupIconFile={#SourcePath}\classes\icon.ico
; Set the icon in Add/Remove Programs
UninstallDisplayIcon={app}\icon.ico

[Files]
; Copy JAR file
Source: "{#SourcePath}\{#JarFile}"; DestDir: "{app}"; Flags: ignoreversion
; Copy custom runtime
Source: "{#SourcePath}\runtime\*"; DestDir: "{app}\runtime"; Flags: ignoreversion recursesubdirs createallsubdirs
; Copy the icon file
Source: "{#SourcePath}\classes\icon.ico"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
Name: "{commonprograms}\plimplomGamba"; Filename: "{app}\runtime\bin\javaw.exe"; Parameters: "-jar ""{app}\{#JarFile}"""; WorkingDir: "{app}"; IconFilename: "{app}\icon.ico"
Name: "{commondesktop}\plimplomGamba"; Filename: "{app}\runtime\bin\javaw.exe"; Parameters: "-jar ""{app}\{#JarFile}"""; WorkingDir: "{app}"; IconFilename: "{app}\icon.ico"