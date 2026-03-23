@echo off
title Manim Studio
echo.
echo  Manim Studio
echo  ============
echo.

REM Check Python is available
python --version >nul 2>&1
if errorlevel 1 (
    echo  ERROR: Python not found on PATH
    echo  Install Python from https://python.org
    pause
    exit /b 1
)

REM Run setup if venv missing
if not exist "%~dp0engine\venv\Scripts\python.exe" (
    echo  First run detected — setting up engine...
    cd /d "%~dp0engine"
    python setup.py
    if errorlevel 1 (
        echo  Setup failed. Check errors above.
        pause
        exit /b 1
    )
)

REM Start everything
cd /d "%~dp0web"
npm run dev
