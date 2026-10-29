#!/usr/bin/env bash
# Compila ed esegue il gioco. Serve un ambiente grafico.
set -e
cd "$(dirname "$0")"

mkdir -p bin
javac -encoding UTF-8 -d bin $(find src -name '*.java')

# javac copia solo i .class: i fogli di sprite vanno messi accanto a mano,
# altrimenti il gioco non li trova nel classpath. Eclipse invece lo fa da se'.
cp -r src/risorse bin/

java -cp bin gdr.grafica.Avvio
