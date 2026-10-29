# Progetto GDR

Gioco di ruolo 2D a visuale dall'alto in Java 21 e Swing. Due zone esterne
collegate da un passaggio, movimento a griglia con animazione fluida, due
casette e un ospedale, erba alta con incontri casuali, battaglia a turni con
quattro elementi.

## Come eseguire

    ./gioca.sh     compila ed apre la finestra di gioco (Linux e Mac)
    gioca.bat      lo stesso su Windows: doppio clic, oppure da cmd

Da Eclipse: `gdr.grafica.Avvio` come Java Application.

Servono solo JDK 21 e bash. Nessuna dipendenza esterna per giocare; per i test
serve JUnit 5, che Eclipse porta con se'.

## I test

I test stanno in `test/`, nei package che rispecchiano quelli di `src/`, e usano
**JUnit 5**. Da Eclipse: tasto destro sulla cartella `test` > Run As > JUnit
Test. Il progetto e' gia' configurato — `.classpath` dichiara `test` come
cartella di sorgenti e aggiunge la libreria JUnit 5 — quindi non c'e' niente da
installare.

Sono 112 prove in 11 classi, e coprono la logica di gioco: la griglia e le
direzioni, il movimento a caselle, la lettura delle mappe testuali, la coerenza
dei passaggi fra zone, la formula del danno, la tabella di efficacia, la
macchina a turni con i suoi tre esiti, la regola della tregua negli incontri,
l'esplorazione e le fasi della partita.

Due aiutanti stanno in `test/gdr/prova/`:

- `CasualeDiProva` e' una sorgente casuale che restituisce i valori che le si
  dicono. E' il motivo per cui la logica riceve un `Casuale` dall'esterno invece
  di chiamare `Math.random()`: cosi' una battaglia si fa accadere esattamente
  come serve alla prova, e il risultato e' lo stesso a ogni esecuzione.
- `MappeDiProva` costruisce un mondo minimo di due zone e qualche creatura dai
  numeri tondi. Le prove non usano il contenuto vero del gioco tranne dove e'
  proprio quello a essere messo alla prova, altrimenti ribilanciare una specie
  farebbe fallire prove che parlano di tutt'altro.

Niente di tutto questo entra nel gioco: `gioca.sh` e `gioca.bat` compilano solo
`src/`, quindi chi vuole soltanto giocare non ha bisogno di JUnit.

Tre prove esistono perche' altrettanti difetti sono stati trovati eseguendo il
gioco, e servono a impedire che tornino: la porta che si annuncia una volta
sola, il personaggio che non deve camminare da solo dopo una battaglia, e il
passo che non deve essere attribuito alla zona sbagliata.

## Comandi

    frecce o WASD    camminare, e scorrere il menu in battaglia
    Z, Invio, spazio confermare la scelta in battaglia

Sbattendo contro la porta dell'ospedale, l'edificio centrale col tetto chiaro e
la croce azzurra, il personaggio entra per tre secondi e riesce con la creatura
curata. Le altre due porte non fanno niente: gli interni sono fuori scope.

## Struttura

    src/gdr/nucleo/    Posizione, Direzione, Casuale, CasualeStandard
    src/gdr/mondo/     TipoTile, Passaggio, Zona, LettoreZona, Mondo
    src/gdr/entita/    MovimentoGriglia, Giocatore
    src/gdr/battaglia/ Elemento, Mossa, Specie, Creatura, CalcolatoreDanno,
                       AzioneBattaglia, EventoBattaglia, Esito, Battaglia
    src/gdr/incontri/  VoceIncontro, TabellaIncontri, GeneratoreIncontri
    src/gdr/partita/   FaseGioco, Partita, EventoPartita,
                       Esplorazione, EventoEsplorazione
    src/gdr/dati/      ZoneIniziali (le mappe), Bestiario, Repertorio
    src/gdr/grafica/   Avvio, FinestraGioco, PannelloGioco, CicloDiGioco,
                       GestoreInput, Telecamera, VeloTransizione, Riquadri,
                       Tavolozza, Testi, RenderizzatoreEsplorazione,
                       RenderizzatoreBattaglia, AnimatoreBattaglia,
                       ParteEdificio, DisegnatoreTile, DisegnatoreEdificio,
                       FoglioSprite, DisegnatorePersonaggio, DisegnatoreCreatura
    src/risorse/       terreno.png, personaggio.png, creature.png
                       (i tre fogli di sprite)
    test/gdr/...       le prove JUnit 5, nei package che rispecchiano src/
    test/gdr/prova/    CasualeDiProva, MappeDiProva (aiutanti delle prove)

## Simboli delle mappe

    .   sentiero            calpestabile
    ,   erba bassa          calpestabile
    *   erba alta           calpestabile, genera incontri
    A   albero              non calpestabile
    ~   acqua               non calpestabile
    >   passaggio           calpestabile, porta a un'altra zona
    E   edificio            non calpestabile
    P   porta               non calpestabile (gli interni sono fuori scope)
    O   ospedale            non calpestabile
    H   porta dell'ospedale non calpestabile, cura la creatura

## Gli invarianti del mondo

Il costruttore di `Mondo` verifica la coerenza delle mappe e solleva
un'eccezione se qualcosa non torna, quindi una mappa mal collegata si manifesta
subito all'avvio invece di diventare un vicolo cieco scoperto giocando. I
controlli sono:

- ogni passaggio punta a una zona esistente;
- la casella d'arrivo e' dentro la zona di destinazione ed e' calpestabile;
- la casella d'arrivo non e' a sua volta un passaggio (niente rimbalzi);
- la zona di destinazione ha un passaggio che riporta indietro.

Sono controlli che costano una volta sola, all'avvio, e trasformano un errore di
battitura nelle mappe testuali in un messaggio esplicito invece che in un
comportamento strano.

## Il movimento a griglia

La posizione logica e' sempre una casella intera. `MovimentoGriglia` tiene il
passo in corso (casella di partenza, di arrivo, progresso fra 0 e 1) e non
conosce la mappa: decidere se un passo e' lecito spetta a `Esplorazione`, che ha
la zona sottomano.

`avanza(secondi)` fa scorrere il tempo sul passo in corso, e un aggiornamento
conclude al massimo un passo: il tempo che resta dopo la fine di un passo si
scarta. Prima veniva riversato nel passo successivo, e un aggiornamento poteva
concluderne due. Costava due difetti — un passo della radura risolto con la
mappa del bosco, e un passo appena avviato che sopravviveva alla battaglia e
faceva camminare il personaggio di una casella da solo — mentre non faceva
guadagnare niente: con il passo fisso di 1/60 di secondo e quattro caselle al
secondo una casella dura esattamente quindici aggiornamenti.

Per disegnare si usano `colonnaInterpolata()` e `rigaInterpolata()`, che danno
caselle frazionarie da moltiplicare per la dimensione del tile.

## Il confine con la grafica

`Esplorazione.aggiorna(secondi, comando)` riceve un `Optional<Direzione>` (la
direzione premuta, vuoto se nessuna) e restituisce una lista di
`EventoEsplorazione`:
passo completato, porta toccata, zona cambiata. La logica non emette suoni, non
scrive testo e non disegna: dice solo cosa e' successo. `Partita` legge quegli
eventi, li traduce in `EventoPartita` e li passa alla vista.

Nessun package di logica importa `java.awt` o `javax.swing`: la regola vale per
`nucleo`, `mondo`, `entita`, `battaglia`, `incontri`, `partita` e `dati`, e solo
`gdr.grafica` ha il permesso di disegnare. Anche dentro `gdr.grafica` cinque
classi non importano AWT: tre per scelta dichiarata, perche' sono aritmetica
pura (`Telecamera`, `VeloTransizione`, `ParteEdificio`), e due perche' non
disegnano (`Testi`, che tiene solo stringhe, e `AnimatoreBattaglia`, che conta
secondi).

Verificarlo e' immediato:

    grep -rl "import java.awt\|import javax.swing" src/ | grep -v gdr/grafica

Se non stampa niente, la regola e' rispettata.

## La grafica

Il disegno avviene su una tela di 240x176 pixel logici, poi ingrandita di un
fattore intero con interpolazione a pixel netti. E' questo che da' l'aspetto
anni '90 senza sfocature, e permette di ridimensionare la finestra senza che i
pixel diventino di dimensioni diverse fra loro.

Tutto quello che si vede sulla mappa viene da tre file PNG in `src/risorse`,
caricati da `FoglioSprite` attraverso il classpath:

    terreno.png       256x64, riquadri da 16x16, quattro righe:
                      riga 0  sentiero, erba, erba alta della radura, erba alta
                              del bosco, albero, passaggio, e i ciuffi che
                              passano davanti al personaggio (radura e bosco)
                      riga 1  i sedici fotogrammi dell'acqua
                      riga 2  le basi degli edifici e le sovrapposizioni
                      riga 3  i bordi laterali, in tre colori
    personaggio.png   48x64, riquadri da 16x16: tre fasi di camminata per
                      colonna, quattro direzioni per riga (NORD, SUD, OVEST,
                      EST, nell'ordine di Direzione)
    creature.png      160x32, cinque riquadri da 32x32, uno per specie,
                      nell'ordine della lista NOMI in DisegnatoreCreatura

Sostituire un disegno vuol dire sostituire il riquadro nel PNG, senza toccare il
codice. Aggiungere una specie vuol dire aggiungere un riquadro in coda al foglio
e il suo nome in fondo a `DisegnatoreCreatura.NOMI`: sono le due sole cose da
tenere allineate.

Tutte le caselle dello stesso terreno sono identiche. Prima ognuna aveva i suoi
sassolini e i suoi ciuffi, ricavati dalle proprie coordinate; adesso il disegno
sta nell'immagine, e per riavere varieta' si aggiungono riquadri al foglio, non
codice.

L'acqua invece si muove, e il movimento non e' nel PNG: sono sedici fotogrammi,
e il codice sceglie quale mostrare con

    Math.floorMod((long) (tempo * 7.0) + riga * 3L, 16)

Le righe diverse sono sfasate di proposito, altrimenti il laghetto pulserebbe
tutto insieme. Un giro completo dura poco piu' di due secondi.

In `Tavolozza` restano solo i colori di quello che e' ancora disegnato con
`Graphics2D`: la schermata di battaglia, i riquadri di testo e lo sfondo.

`gioca.sh` copia `src/risorse` dentro `bin` dopo la compilazione, perche' `javac`
sposta solo i `.class`. Eclipse invece lo fa da se'.

## Il testo

`Riquadri` disegna le cornici e il testo, con un font logico
(`Font.SANS_SERIF`) a corpo 9. Un font logico esiste su qualunque macchina,
mentre una famiglia installata potrebbe non esserci e il testo cambierebbe
aspetto da un computer all'altro.

L'antialiasing e' disattivato di proposito: la tela viene ingrandita di un
fattore intero a pixel netti, quindi le sfumature di grigio dei bordi
diventerebbero blocchi di grigio. A corpo 9 le lettere restano distinguibili,
accenti compresi.

In `Riquadri.scrivi` la `y` e' il bordo superiore del testo, non la linea di
base: i richiami passano coordinate di riquadro, non di tipografia.

`Testi` traduce gli eventi in italiano. La logica non contiene nessuna frase da
mostrare: tradurre il gioco vorrebbe dire toccare solo quella classe.

## Il ciclo di gioco

`CicloDiGioco` e' un solo ciclo a passo fisso di 1/60 di secondo: accumula il
tempo realmente trascorso e fa avanzare la logica a passi costanti, con un tetto
massimo perche' una finestra rimasta ferma qualche secondo non recuperi
centinaia di passi in una volta (equivarrebbe a teletrasportare il giocatore).

## Gli edifici

Le case sono disegnate nella mappa testuale come blocchi di `E` con una `P`
sulla fila in basso, esattamente come tutto il resto del terreno. Che aspetto
abbia una singola casella lo ricava `ParteEdificio` contando i vicini: se sopra
ci sono meno di due caselle di edificio e' tetto, altrimenti muro; le finestre
vanno a colonne alterne, mai sulla porta.

Cosi' non esiste una seconda struttura dati da tenere allineata alla mappa, e
allargare una casa e' solo questione di scrivere piu' `E`.

`ParteEdificio` resta anche con i tile, perche' il conteggio dei vicini serve
comunque: con le immagini cambia solo cosa se ne fa `DisegnatoreEdificio`, che
invece di dipingere sceglie quale riquadro mettere. I pezzi si compongono: muro,
poi la finestra o la porta, poi lo zoccolo, poi i bordi laterali. Tenerli
separati costa una sovrapposizione per pezzo invece di un riquadro per ogni
combinazione, e l'ordine conta — lo zoccolo passa davanti alla porta negli
ultimi due pixel.

I bordi laterali vanno per ultimi e sono colonne intere, quindi negli angoli
coprono i due pixel dello zoccolo e il pixel di sottogronda: sei pixel per
edificio, che si vedono solo fermando l'immagine e ingrandendola. Per toglierli
servirebbero sei riquadri di bordo in piu', uno per ogni pezzo su cui il bordo
puo' cadere, e non ne valeva la pena.

Ogni porta sta sulla fila piu' in basso della casa, con davanti una casella
calpestabile: irrilevante adesso che gli interni sono fuori scope, ma e' la
condizione da rispettare il giorno in cui le porte si apriranno.

## L'ospedale

Sbattere contro una porta produce un evento invece di non fare niente, ed e'
quell'evento che permette alla porta dell'ospedale di curare senza che esistano
interni. Durante i tre secondi di visita il personaggio semplicemente non viene
disegnato, l'input e' ignorato e un riquadro dice cosa sta succedendo.

L'evento esce al primo contatto e non piu' finche' si resta premuti contro la
porta. Senza questa regola l'evento usciva sessanta volte al secondo e, appena
finita la visita, si rientrava subito: tenendo premuto si veniva curati in
ciclo ogni tre secondi. Basta girarsi o lasciare il tasto per poter rientrare.

Il contatto lo ricorda il campo `portaInContatto` di `Esplorazione`, e c'e' un
dettaglio che costa quattro righe in `Partita`: durante la visita
`Esplorazione.aggiorna` non viene chiamato, quindi un tasto lasciato *dentro*
l'ospedale non passerebbe da nessuna parte e non si riuscirebbe piu' a
rientrare. Per questo la visita passa comunque il comando all'esplorazione, con
`osservaSenzaMuovere`, che aggiorna il contatto senza spostare nessuno.

Il tetto e' chiaro con una croce azzurra a cavallo delle due caselle centrali.
Non una croce rossa su bianco: quello e' l'emblema della Croce Rossa, protetto
da convenzione internazionale, e non si usa a piacere.

## La battaglia

Quattro elementi: FUOCO, ACQUA, ERBA, ELETTRO. Il triangolo classico fuoco batte
erba, erba batte acqua, acqua batte fuoco, piu' l'elettro forte sull'acqua e
neutro per il resto.

    FUOCO    batte ERBA     debole a ACQUA
    ACQUA    batte FUOCO    debole a ERBA, ELETTRO
    ERBA     batte ACQUA    debole a FUOCO
    ELETTRO  batte ACQUA    debole a nessuno

L'elettro senza debolezze e' un'asimmetria voluta. Prima l'erba lo batteva ed
era resistente ai suoi attacchi, cosi' ogni elemento aveva il suo contro. Ma
l'unica creatura del giocatore e' d'elettro e attacca solo con quell'elemento,
e nessuna creatura selvatica e' d'elettro: quella debolezza non bilanciava
niente, puniva solo il giocatore. Contro Verzino, d'erba, sommava meta' danno
inflitto e danno doppio subito, e la misura dava zero vittorie su ventimila.

La regola per un eventuale quinto elemento e' che abbia almeno un bersaglio; la
debolezza serve solo se quell'elemento puo' finire sia al giocatore sia agli
avversari.

`Battaglia` e' sincrona e istantanea: riceve un'`AzioneBattaglia`, risolve il
turno intero e restituisce la lista di `EventoBattaglia`. Al ritorno lo stato e'
gia' quello finale. Non conosce millisecondi, non attende, non produce frasi.

La sorgente casuale e' iniettata: `Battaglia` riceve un `Casuale` e non chiama
mai `Math.random()`, quindi il caso si puo' sostituire dall'esterno.

Le mosse non hanno punti uso: si usano quante volte si vuole. Ne avevamo messi,
ma con trenta cariche e la cura gratuita all'ospedale non si esaurivano mai. Un
meccanismo che non entra in gioco e' solo codice in piu' e un numero a schermo
da spiegare, quindi e' stato rimosso del tutto invece di essere lasciato spento.

## La messa in scena della battaglia

La logica risolve il turno tutto insieme e restituisce gli eventi con i PS gia'
aggiornati. `AnimatoreBattaglia` li consuma uno alla volta con le proprie durate
e tiene dei valori *mostrati* distinti da quelli reali: la barra dei PS scende
gradualmente verso un numero gia' fissato. Quanto resta in scena ciascun evento
lo dice `Testi.durata`, accanto alle frasi. Finche' ha qualcosa in scena il
giocatore non puo' scegliere.

## Le fasi della partita

`Partita` e' la radice dello stato: tiene insieme mondo, giocatore, creatura,
incontri e battaglia, e sa in che fase siamo fra ESPLORAZIONE, DENTRO_OSPEDALE e
BATTAGLIA. E' logica pura: non disegna, non legge tasti, non conosce Swing.

In battaglia il tempo del mondo non scorre: l'azione passa da
`scegliInBattaglia`. Quando la battaglia finisce, `Partita` non si chiude da
sola: aspetta che la vista chiami `chiudiBattaglia`, perche' la logica sa cosa
succede ma non quando l'animazione e' terminata.

## Gli incontri

La probabilita' e' per passo compiuto nell'erba alta, non al secondo: cosi' non
dipende da quanto e' veloce la camminata ne' da quanti fotogrammi al secondo
gira il gioco. Con il 12% per passo e la tregua imposta dal generatore viene un
incontro ogni dieci caselle circa.

La tregua serve a non far partire una battaglia appena chiusa la precedente:
senza, il gioco sembra rotto. Dopo un incontro i due passi successivi sono
sicuri; il terzo puo' gia' farne partire un altro.

Le creature selvatiche sono quattro, e dentro una zona escono tutte con la
stessa probabilita'. Nel bosco ci sono tutte e quattro; nella radura sono tre,
perche' Verzino ne resta fuori: la radura e' la zona in cui si impara a
giocare, e lo scontro piu' lungo sta meglio nella seconda zona.

La prima versione della radura aveva due sole specie con Rugiado al 75%: in
dieci incontri se ne vedevano nove uguali e la zona sembrava avere un solo
mostro. Le due creature d'acqua, Rugiado e Marezzo, condividono elemento e
mosse ma hanno statistiche opposte e sagome opposte, tonda contro piatta: due
specie dello stesso tipo non rendono gli scontri identici.

## Il bilanciamento, misurato

I numeri di gioco non sono stimati: una simulazione fuori dal progetto, usata
come strumento di sviluppo, ha giocato ventimila battaglie per specie pescando
gli avversari dalle tabelle vere delle zone. Con Scintillo di livello 5, contro
avversari di livello 5:

    avversario   vittorie   turni   colpi miei / suoi
    Rugiado      99,9%      5,0     4 / 8
    Marezzo      99,5%      4,2     4 / 7
    Fuligno      76,9%      6,2     5 / 5
    Verzino      72,5%      8,1     7 / 8

E per livello dell'avversario, dal 2 al 6:

                 lv2    lv3    lv4    lv5    lv6
    Rugiado      100%   100%   100%   100%    93%
    Marezzo      100%   100%   100%    99%    94%
    Fuligno      100%   100%   100%    73%    23%
    Verzino      100%   100%   100%    69%    47%

Verzino e' lo scontro piu' lungo del gioco, otto turni contro i quattro degli
altri, ma si vince: e' quello impegnativo, non quello impossibile. Al livello 6
il piu' duro diventa Fuligno, che ha l'attacco piu' alto del bestiario.

Il bilanciamento e' stato corretto due volte, e ogni volta lo ha scoperto la
misura, non la lettura del codice.

La prima: con una crescita di un decimo per livello, una creatura selvatica di
livello 2 aveva quasi gli stessi PS del compagno di livello 5, e Verzino vinceva
il 100% degli scontri anche nella radura. La crescita e' passata a un quinto per
livello.

La seconda: Verzino restava imbattibile comunque, zero vittorie su ventimila,
perche' l'erba resisteva all'elettro e lo colpiva al doppio mentre Scintillo non
ha nessun altro elemento con cui attaccare. Provare ad abbassargli le
statistiche o ad ammorbidire i moltiplicatori lasciava il risultato sotto l'1%:
la causa era la coppia di regole nella tabella dei tipi, non le statistiche.
Sono state tolte entrambe e le statistiche di Verzino portate in linea con le
altre specie; solo le due cose insieme portano lo scontro al 75% di vittorie in
sette turni e mezzo, sui livelli con cui il bosco lo fa comparire. Il livello
pesa molto: al 4 si vince sempre, al 5 nel 73% dei casi, al 6 e' una monetina.
Nella stessa zona la battaglia piu' rischiosa non e' nemmeno la sua, ma quella
contro Fuligno: il 67%, in sei turni.

## Due difetti che solo l'esecuzione ha trovato

### Un tasto, una sola azione

Le frecce su e giu' servono sia a camminare sia a scorrere il menu di battaglia,
e le due cose erano registrate come due associazioni separate sullo stesso
tasto. Ma una `InputMap` associa una combinazione a una sola azione: la seconda
registrazione aveva sostituito la prima, e il personaggio non andava piu' ne' su
ne' giu'. Compilava senza un solo avviso, e si vedeva solo giocando.

La correzione e' una sola azione per tasto che fa entrambi i lavori, in
`GestoreInput`. E' la trappola da ricordare di Swing: se un tasto deve servire a
due scopi, va registrato una volta sola.

### Due tasti, una sola direzione

Ogni direzione ha due tasti, la freccia e la lettera di WASD, e l'elenco dei
tasti premuti conteneva direzioni invece di tasti. Tenendo giu' la freccia su e
sfiorando `W`, il rilascio di `W` cancellava l'unica voce NORD dell'elenco e il
personaggio si fermava con la freccia ancora premuta. Adesso l'elenco contiene i
nomi dei tasti e la direzione si ricava dall'ultimo ancora giu'.

Nello stesso punto: la pressione si conta una volta sola, perche' il sistema
operativo la ripete finche' il tasto resta giu' e la ripetizione rimetteva in
coda il tasto vecchio, togliendo la precedenza a quello premuto dopo.

### Un rilascio che non arrivava

Una `InputMap` confronta la combinazione **esatta**, modificatori compresi. Se
mentre si rilascia la freccia e' giu' anche shift, control o alt, `released UP`
non scatta: il tasto resta segnato come premuto e il personaggio cammina da
solo finche' non lo si preme e rilascia di nuovo senza modificatori. Verificato
rifacendo la stessa ricerca che fa Swing. Ora ogni rilascio e' registrato anche
nelle tre varianti con modificatore.

Resta un limite che non si chiude da qui: le scorciatoie valgono finche' la
finestra ha il fuoco, quindi un rilascio avvenuto mentre si e' passati a
un'altra finestra non arriva comunque.
