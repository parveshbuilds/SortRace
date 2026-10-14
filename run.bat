@echo off
title SortRace - Sorting Visualizer
cd /d "%~dp0"

if not exist out mkdir out

echo Compiling SortRace...
javac -d out src\Main.java

if errorlevel 1 (
    echo Compilation failed. Check Java installation.
    pause
    exit /b 1
)

echo Starting SortRace...
java -cp out Main
pause
