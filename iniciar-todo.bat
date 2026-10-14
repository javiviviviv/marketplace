@echo off
title Lanzador General de Microservicios Marketplace
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
docker compose up -d mysql

echo.
echo Esperando 20 segundos a que el motor de MySQL este 100%% operativo...
timeout /t 20 /nobreak > nul

echo.
echo =======================================================
echo 3. Levantando Microservicios Base (ms-rol y ms-categoria)...
echo =======================================================
start "MS-ROL (Port 8082)" cmd /k "java -jar ms-rol\target\ms-rol-0.0.1-SNAPSHOT.jar"
start "MS-CATEGORIA (Port 8085)" cmd /k "java -jar ms-categoria\target\ms-categoria-0.0.1-SNAPSHOT.jar"

timeout /t 6 /nobreak > nul

echo.
echo =======================================================
echo 4. Levantando Microservicios Secundarios (ms-usuario y ms-item)...
echo =======================================================
start "MS-USUARIO (Port 8081)" cmd /k "java -jar ms-usuario\target\ms-usuario-0.0.1-SNAPSHOT.jar"
start "MS-ITEM (Port 8083)" cmd /k "java -jar ms-item\target\ms-item-0.0.1-SNAPSHOT.jar"

timeout /t 6 /nobreak > nul

echo.
echo =======================================================
echo 5. Levantando Microservicio Consumidor (ms-carrito)...
echo =======================================================
start "MS-CARRITO (Port 8084)" cmd /k "java -jar ms-carrito\target\ms-carrito-0.0.1-SNAPSHOT.jar"

echo.
echo =======================================================
echo [OK] ¡Proceso finalizado! Las ventanas permaneceran abiertas.
echo =======================================================
pause