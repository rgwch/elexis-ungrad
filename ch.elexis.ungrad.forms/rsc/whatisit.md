Dieses Verzeichnis enthält einige Beispiel-Schablonen für den Formularprozessor.

Schablonen können im Html, Pug oder PDF-Format sein. 

## HTML

Pug kann nur verwendet werden, wenn ein Programm zur Umwandlung von .pug nach .html im System installiert ist, und wird dann als HTML-Schablone behandelt.

Gleich wie bei Textschablonen können Felder vorabgefüllt werden, z.B. [Patient.Name] usw. Wenn ein Feld mit [Adressat...] existiert, wird ein
Adressabfrage-Dialog geöffnet.

Nach der automatischen Abfüllung der Standardfelder werden Eingabefelder evaluiert. Diese sind mit dem html-Attribut data-input deklariert.

Beispiel:

<div data-input="Bemerkungen"></div>

würde ein Eingabefeld mit dem Titel "Bemerkungen" erstellen, und das, was dort eingegeben wird, in der Ausgabedatei an diese Stelle eintragen.

Der Dateititel wird entweder dem <title></title>-Tag, oder dem <h1></h1>-Tag entnommen, oder einem beliebigen Tag mit dem Attribut data-doctitle.

Beispiel:

<h1 data-doctitle="blahblah">Blahblubb</h1>"

Würde dem Dokument den Titel "blahblah" geben (da data-doctitle eine höhere Präferenz hat, als h1)
