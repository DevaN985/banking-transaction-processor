@echo off
setlocal
cd /d "%~dp0"

echo ========================================
echo Banking Transaction Processor
echo ========================================
echo.
echo Building required modules and starting the application...
echo Database mode: %BANKING_DB_MODE%
echo.

call mvn -f banking-bootstrap\pom.xml spring-boot:run
set "APP_EXIT_CODE=%ERRORLEVEL%"

echo.
if not "%APP_EXIT_CODE%"=="0" (
    echo Application stopped with an error. Review the messages above.
) else (
    echo Application stopped.
)
pause
exit /b %APP_EXIT_CODE%
