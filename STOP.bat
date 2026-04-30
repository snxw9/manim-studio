@echo off
echo Stopping Manim Studio...
taskkill /F /IM python.exe /T >nul 2>&1
taskkill /F /FI "WINDOWTITLE eq Manim Studio" /T >nul 2>&1

echo Clearing render cache...
if exist "%~dp0engine\media_cache" (
    del /Q /S "%~dp0engine\media_cache\*" >nul 2>&1
)
if exist "%~dp0engine\outputs" (
    del /Q /S "%~dp0engine\outputs\*.mp4" >nul 2>&1
    del /Q /S "%~dp0engine\outputs\*.gif" >nul 2>&1
    del /Q /S "%~dp0engine\outputs\*.webm" >nul 2>&1
)

echo Done. LaTeX cache preserved for faster future renders.
timeout /t 2 >nul
