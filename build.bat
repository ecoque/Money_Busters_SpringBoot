@echo off
chcp 65001 >nul
title Money Busters - JAR Build Script
color 0A

echo ============================================
echo   Money Busters - JAR Olusturma Araci
echo ============================================
echo.

REM Proje dizinine git
cd /d "%~dp0"

echo [1/3] Onceki build temizleniyor...
echo.

REM Eski JAR ve EXE dosyalarini temizle
if exist "MoneyBusters.exe" del "MoneyBusters.exe"

echo [2/3] Maven ile JAR derleniyor (testler atlaniyor)...
echo      Bu islem biraz zaman alabilir...
echo.

call mvnw.cmd clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    color 0C
    echo.
    echo ============================================
    echo   HATA: JAR olusturma basarisiz oldu!
    echo   Lütfen hata mesajlarini kontrol edin.
    echo ============================================
    pause
    exit /b 1
)

echo.
echo [3/3] Kontrol ediliyor...

if exist "target\MoneyBusters.jar" (
    color 0A
    echo.
    echo ============================================
    echo   BASARILI! JAR dosyasi olusturuldu:
    echo   target\MoneyBusters.jar
    echo ============================================
    echo.
    echo   Sonraki adim: Launch4j ile EXE olusturun
    echo   Config dosyasi: launch4j-config.xml
    echo ============================================
) else (
    color 0C
    echo.
    echo ============================================
    echo   HATA: JAR dosyasi bulunamadi!
    echo ============================================
)

echo.
pause
