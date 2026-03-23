@echo off
echo Stopping Manim Studio...
taskkill /F /IM python.exe /T >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq Manim Studio" /T >nul 2>&1
echo Done.
timeout /t 2 >nul
