@echo off
setlocal

set "APP_DIR=%~dp0DesktopClient"
if not exist "%APP_DIR%\src\Main.java" (
  echo ERROR: DesktopClient sources not found at "%APP_DIR%\src".
  exit /b 1
)

pushd "%APP_DIR%" >nul

set "SRC=src"
set "OUT=out"
set "LIB=lib"
set "CP=%LIB%\mssql-jdbc-12.6.1.jre8.jar;%LIB%\javafx\javafx-base-25-win.jar;%LIB%\javafx\javafx-graphics-25-win.jar;%LIB%\javafx\javafx-controls-25-win.jar"

where javac >nul 2>nul
if errorlevel 1 (
  echo ERROR: javac not found. Install a JDK and ensure it is on PATH.
  popd >nul
  exit /b 1
)

where java >nul 2>nul
if errorlevel 1 (
  echo ERROR: java not found. Install a JDK/JRE and ensure it is on PATH.
  popd >nul
  exit /b 1
)

if not exist "%OUT%" mkdir "%OUT%"

javac -cp "%CP%" -sourcepath "%SRC%" -d "%OUT%" "%SRC%\Main.java"
if errorlevel 1 (
  popd >nul
  exit /b 1
)

java --enable-native-access=ALL-UNNAMED -cp "%OUT%;%CP%" Main

popd >nul
endlocal

