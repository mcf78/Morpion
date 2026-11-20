@echo off
setlocal
pushd "%~dp0"
where javac >nul 2>&1
if errorlevel 1 goto :sansjdk
where jar >nul 2>&1
if errorlevel 1 goto :sansjdk
if not exist "build\classes" mkdir "build\classes"
if not exist "dist" mkdir "dist"
javac -encoding UTF-8 -Xlint:all -d "build\classes" src\*.java
if errorlevel 1 goto :erreur
jar --create --file "dist\Morpion.jar" --main-class Gestion -C "build\classes" .
if errorlevel 1 goto :erreur
echo Compilation reussie : dist\Morpion.jar
popd
exit /b 0
:sansjdk
echo Un JDK Java 17 ou plus recent est necessaire. Verifie les commandes javac et jar.
:erreur
popd
exit /b 1
