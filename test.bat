@echo off
REM Maven Wrapper Shortcut
REM Bu dosyayi projenin root klasorune koy ve "test.bat" olarak calistir

cd /d "%~dp0"
call mvnw.cmd clean test

pause
