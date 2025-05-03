[Setup]
AppName=plimplomGamba
AppVersion={#AppVersion}
DefaultDirName={localappdata}\plimplomGamba
DisableDirPage=yes
OutputDir={#SourcePath}\dist
OutputBaseFilename=plimplomGamba_setup

[Files]
; Copy JAR file
Source: "{#SourcePath}\{#JarFile}"; DestDir: "{app}"; Flags: ignoreversion
; Copy custom runtime
Source: "{#SourcePath}\runtime\*"; DestDir: "{app}\runtime"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{commonprograms}\plimplomGamba"; Filename: "{app}\runtime\bin\javaw.exe"; Parameters: "-jar ""{app}\{#JarFile}"""; WorkingDir: "{app}"
Name: "{commondesktop}\plimplomGamba"; Filename: "{app}\runtime\bin\javaw.exe"; Parameters: "-jar ""{app}\{#JarFile}"""; WorkingDir: "{app}"
