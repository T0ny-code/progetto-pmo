@echo off
rem Compila ed esegue il gioco su Windows. Serve il JDK 21 (non solo il runtime).
rem L'equivalente per Linux e Mac e' gioca.sh, accanto a questo file.
setlocal enabledelayedexpansion

rem Si lavora sempre nella cartella dello script, cosi' funziona anche
rem facendoci doppio clic da Esplora risorse.
cd /d "%~dp0"

where javac >nul 2>nul
if errorlevel 1 (
    echo.
    echo Non trovo javac. Serve il JDK 21, non solo il runtime Java:
    echo con il solo runtime il gioco non si puo' compilare.
    echo Controlla con:  javac -version
    echo.
    pause
    exit /b 1
)

if not exist bin mkdir bin

rem L'elenco dei sorgenti finisce in un file di argomenti, un percorso per riga.
rem Due accortezze, tutt'e due necessarie:
rem  - fra virgolette, altrimenti una cartella con uno spazio nel nome
rem    spezzerebbe il percorso in due;
rem  - con le barre in avanti, perche' nel file di argomenti di javac la barra
rem    rovesciata e' un carattere di escape e sparisce: C:\Users\Tonino\...
rem    arriverebbe a javac come C:UsersTonino... Le barre in avanti su Windows
rem    vanno benissimo.
if exist sorgenti.txt del sorgenti.txt
for /r src %%f in (*.java) do (
    set "percorso=%%f"
    echo "!percorso:\=/!">>sorgenti.txt
)

javac -encoding UTF-8 -d bin @sorgenti.txt
if errorlevel 1 (
    echo.
    echo Compilazione fallita.
    echo.
    pause
    exit /b 1
)
del sorgenti.txt

rem javac copia solo i .class: i fogli di sprite vanno messi accanto a mano,
rem altrimenti il gioco non li trova nel classpath. Eclipse invece lo fa da se'.
xcopy /e /i /y /q src\risorse bin\risorse >nul

java -cp bin gdr.grafica.Avvio

rem Se il gioco si chiude per un errore, la finestra resta aperta a mostrarlo.
if errorlevel 1 pause

endlocal
