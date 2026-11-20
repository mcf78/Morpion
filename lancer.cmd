@echo off
setlocal
pushd "%~dp0"
call ".\compiler.cmd"
if errorlevel 1 goto :erreur
java -jar "dist\Morpion.jar"
if errorlevel 1 goto :erreur
popd
exit /b 0
:erreur
echo Le jeu n'a pas pu demarrer. Consulte le message ci-dessus.
pause
popd
exit /b 1
