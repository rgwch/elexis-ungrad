# Assistente di fatturazione Tardoc

## A cosa serve?

La tariffa Tardoc presenta le sue proprie sfide: nuove posizioni tariffarie, fatturazione al minuto per le prestazioni temporali, requisiti per le prestazioni d'azione.

Questo plugin facilita una fatturazione corretta.

## Per chi?

Attualmente, il plugin funziona solo per i titolari della dignità 3010 (Specialisti in medicina interna generale)

## Utilizzo:

Quando si inizia una nuova consultazione, fare clic sul pulsante "Avvia". Questo fattura automaticamente la posizione CA.00.0010 e il cronometro inizia a funzionare. Dopo 5 minuti, viene fatturata inoltre la posizione CA.00.0030 e conta un minuto alla volta, fino a un massimo di 15 minuti.

Quando si fattura una posizione d'azione, il timer si ferma. Nel testo della consultazione, gli esami prescritti per questa prestazione d'azione sono predefiniti e devono quindi essere completati manualmente.

## Limitazioni

Sebbene il plugin possa essere configurato utilizzando il file rsc/config.json, è stato finora testato solo per le posizioni base del medico di famiglia.
Inoltre, config.json è molto sensibile alla sintassi. Dopo aver apportato modifiche, assicurarsi di verificare con un validatore JSON che sia corretto.

## Vibe Coding

Questo plugin è stato creato con un aiuto significativo dell'IA (Claude Sonnet 4.5)
