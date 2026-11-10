@echo off
title Lanzador General de Microservicios Marketplace

echo =======================================================
echo 0. Deteniendo todos los microservicios en ejecucion...
echo =======================================================
powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-CimInstance Win32_Process -Filter \"Name='java.exe'\" | Where-Object { $_.CommandLine -match '(ms-[A-Za-z0-9]+|api-gateway)-0\.0\.1-SNAPSHOT\.jar' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }; for ($port = 8080; $port -le 8090; $port++) { Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique | ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue } }; exit 0"
timeout /t 2 /nobreak > nul

echo.
echo =======================================================
echo 1. Compilando proyectos con Maven (Generando .jar)...
echo =======================================================
call mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] La compilacion con Maven fallo. Revisa el codigo.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo =======================================================
echo 2. Iniciando Contenedor de MySQL en Docker...
echo =======================================================
docker compose up -d

echo.
echo Esperando 5 segundos a que el motor de MySQL este 100%% operativo...
timeout /t 5 /nobreak > nul
echo.
echo =======================================================
echo 3. Levantando API Gateway y Swagger (api-gateway)...
echo =======================================================
start "API-GATEWAY (Port 8080)" cmd /k "java -jar api-gateway\target\api-gateway-0.0.1-SNAPSHOT.jar"

echo.
echo =======================================================
echo Swagger centralizado: http://localhost:8080/swagger-ui/index.html
echo =======================================================

echo.
echo =======================================================
echo 4. Levantando Microservicios Base (ms-rol y ms-categoria)...
echo =======================================================
start "MS-ROL (Port 8082)" cmd /k "java -jar ms-rol\target\ms-rol-0.0.1-SNAPSHOT.jar"
start "MS-CATEGORIA (Port 8085)" cmd /k "java -jar ms-categoria\target\ms-categoria-0.0.1-SNAPSHOT.jar"

timeout /t 6 /nobreak > nul

echo.
echo =======================================================
echo 5. Levantando Microservicios Secundarios (ms-usuario y ms-item)...
echo =======================================================
start "MS-USUARIO (Port 8081)" cmd /k "java -jar ms-usuario\target\ms-usuario-0.0.1-SNAPSHOT.jar"
start "MS-ITEM (Port 8083)" cmd /k "java -jar ms-item\target\ms-item-0.0.1-SNAPSHOT.jar"

timeout /t 6 /nobreak > nul

echo.
echo =======================================================
echo 6. Levantando Microservicio de Ventas (ms-venta)...
echo =======================================================
start "MS-VENTA (Port 8087)" cmd /k "java -jar ms-venta\target\ms-venta-0.0.1-SNAPSHOT.jar"

timeout /t 6 /nobreak > nul

echo.
echo =======================================================
echo 7. Levantando Microservicios de Pago y Envio (ms-pago y ms-envio)...
echo =======================================================
start "MS-PAGO (Port 8089)" cmd /k "java -jar ms-pago\target\ms-pago-0.0.1-SNAPSHOT.jar"
start "MS-ENVIO (Port 8090)" cmd /k "java -jar ms-envio\target\ms-envio-0.0.1-SNAPSHOT.jar"

timeout /t 6 /nobreak > nul

echo.
echo =======================================================
echo 8. Levantando Microservicios Consumidores (ms-carrito, ms-chat y ms-valoracion)...
echo =======================================================
start "MS-CARRITO (Port 8084)" cmd /k "java -jar ms-carrito\target\ms-carrito-0.0.1-SNAPSHOT.jar"
start "MS-CHAT (Port 8086)" cmd /k "java -jar ms-chat\target\ms-chat-0.0.1-SNAPSHOT.jar"
start "MS-VALORACION (Port 8088)" cmd /k "java -jar ms-valoracion\target\ms-valoracion-0.0.1-SNAPSHOT.jar"

timeout /t 6 /nobreak > nul

pause