@echo off
setlocal enabledelayedexpansion

:: ============================================================================
:: IMS PRO - Windows Developer Task Runner (make.bat)
:: ============================================================================

:: Locate project directory containing mvnw.cmd
set "PROJECT_DIR=%~dp0"
if exist "%PROJECT_DIR%ims\mvnw.cmd" (
    set "WORKDIR=%PROJECT_DIR%ims"
) else if exist "%PROJECT_DIR%mvnw.cmd" (
    set "WORKDIR=%PROJECT_DIR%"
) else (
    echo [ERROR] Could not locate mvnw.cmd in %PROJECT_DIR% or %PROJECT_DIR%ims.
    exit /b 1
)

:: Get command argument (default to help if empty)
set "COMMAND=%~1"
if "%COMMAND%"=="" set "COMMAND=help"

:: Route to command target
if /i "%COMMAND%"=="help" goto :help
if /i "%COMMAND%"=="build" goto :build
if /i "%COMMAND%"=="package" goto :package
if /i "%COMMAND%"=="test" goto :test
if /i "%COMMAND%"=="run" goto :run
if /i "%COMMAND%"=="start" goto :run
if /i "%COMMAND%"=="clean" goto :clean
if /i "%COMMAND%"=="docker-build" goto :docker_build
if /i "%COMMAND%"=="docker-up" goto :docker_up
if /i "%COMMAND%"=="docker-down" goto :docker_down
if /i "%COMMAND%"=="docker-restart" goto :docker_restart
if /i "%COMMAND%"=="docker-logs" goto :docker_logs
if /i "%COMMAND%"=="swagger" goto :swagger
if /i "%COMMAND%"=="app" goto :app

echo [ERROR] Unknown command: %COMMAND%
echo Run 'make.bat help' to see available commands.
exit /b 1

:: ============================================================================
:: Targets
:: ============================================================================

:help
echo.
echo ============================================================================
echo   IMS PRO - Developer Command Center (make.bat)
echo ============================================================================
echo.
echo   Usage: make ^<command^>  or  .\make.bat ^<command^>
echo.
echo   Maven ^& Application Commands:
echo     make build           - Build the application JAR (skips tests)
echo     make test            - Execute full test suite (15 unit ^& integration tests)
echo     make package         - Run tests and package into production JAR
echo     make run             - Run Spring Boot application locally (mvnw spring-boot:run)
echo     make clean           - Clean Maven target build artifacts
echo.
echo   Docker ^& Container Commands:
echo     make docker-build    - Build Docker images via docker-compose
echo     make docker-up       - Start application and MySQL containers in background
echo     make docker-down     - Stop and remove containers and network
echo     make docker-restart  - Restart all running Docker containers
echo     make docker-logs     - Follow live container logs
echo.
echo   Browser Shortcuts:
echo     make swagger         - Open Swagger UI in default browser (http://localhost:8080/swagger-ui.html)
echo     make app             - Open Web App in default browser (http://localhost:8080)
echo.
echo   Information:
echo     make help            - Show this command reference
echo.
echo ============================================================================
exit /b 0

:build
echo [INFO] Building IMS application (skipping tests)...
pushd "%WORKDIR%"
call .\mvnw.cmd clean package -DskipTests
set "EXIT_CODE=%ERRORLEVEL%"
popd
exit /b %EXIT_CODE%

:package
echo [INFO] Running tests and packaging IMS application...
pushd "%WORKDIR%"
call .\mvnw.cmd clean package
set "EXIT_CODE=%ERRORLEVEL%"
popd
exit /b %EXIT_CODE%

:test
echo [INFO] Executing automated test suite...
pushd "%WORKDIR%"
call .\mvnw.cmd test
set "EXIT_CODE=%ERRORLEVEL%"
popd
exit /b %EXIT_CODE%

:run
echo [INFO] Starting IMS Spring Boot application...
pushd "%WORKDIR%"
call .\mvnw.cmd spring-boot:run
set "EXIT_CODE=%ERRORLEVEL%"
popd
exit /b %EXIT_CODE%

:clean
echo [INFO] Cleaning target build artifacts...
pushd "%WORKDIR%"
call .\mvnw.cmd clean
set "EXIT_CODE=%ERRORLEVEL%"
popd
exit /b %EXIT_CODE%

:docker_build
echo [INFO] Building Docker images...
pushd "%WORKDIR%"
docker compose build
set "EXIT_CODE=%ERRORLEVEL%"
popd
exit /b %EXIT_CODE%

:docker_up
echo [INFO] Starting IMS containers with Docker Compose...
pushd "%WORKDIR%"
docker compose up -d
set "EXIT_CODE=%ERRORLEVEL%"
popd
if %EXIT_CODE% EQU 0 (
    echo [INFO] Containers started! Access Swagger at http://localhost:8080/swagger-ui.html
) else (
    echo [ERROR] Failed to start Docker containers.
)
exit /b %EXIT_CODE%

:docker_down
echo [INFO] Stopping IMS containers...
pushd "%WORKDIR%"
docker compose down
set "EXIT_CODE=%ERRORLEVEL%"
popd
exit /b %EXIT_CODE%

:docker_restart
echo [INFO] Restarting IMS containers...
pushd "%WORKDIR%"
docker compose restart
set "EXIT_CODE=%ERRORLEVEL%"
popd
exit /b %EXIT_CODE%

:docker_logs
echo [INFO] Following Docker container logs (Ctrl+C to stop)...
pushd "%WORKDIR%"
docker compose logs -f
set "EXIT_CODE=%ERRORLEVEL%"
popd
exit /b %EXIT_CODE%

:swagger
echo [INFO] Opening Swagger UI...
start http://localhost:8080/swagger-ui.html
exit /b 0

:app
echo [INFO] Opening IMS Web Application...
start http://localhost:8080
exit /b 0
