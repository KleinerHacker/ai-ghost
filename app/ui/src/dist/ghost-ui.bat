@rem
@rem Start script for the AI Ghost UI.
@rem All application and dependency JARs live next to this script in "libs".
@rem
@echo off
setlocal

set APP_HOME=%~dp0

if defined JAVA_HOME (
  set JAVA_CMD="%JAVA_HOME%\bin\java.exe"
) else (
  set JAVA_CMD=java
)

%JAVA_CMD% --module-path "%APP_HOME%libs" --add-modules ALL-MODULE-PATH --module org.pcsoft.app.aighost.ui/org.pcsoft.app.aighost.app.LauncherKt %*

endlocal
