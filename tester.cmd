@echo off
setlocal
pushd "%~dp0"
call ".\compiler.cmd"
if errorlevel 1 goto :erreur
if not exist "build\tests" mkdir "build\tests"
javac -encoding UTF-8 -Xlint:all -cp "build\classes" -d "build\tests" tests\*.java
if errorlevel 1 goto :erreur
for %%T in (TestPartie TestServices TestInterfaceMorpion) do (
    java -Djava.awt.headless=true -cp "build\classes;build\tests" %%T
    if errorlevel 1 goto :erreur
)
popd
exit /b 0
:erreur
popd
exit /b 1
