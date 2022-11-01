Dieses Verzeichnis enthält einige Beispiel-Schablonen für den Formularprozessor.

Schablonen können im Html, Pug oder PDF-Format sein. 

## HTML

Pug kann nur verwendet werden, wenn ein Programm zur Umwandlung von .pug nach .html im System installiert ist, und wird dann als HTML-Schablone behandelt.

Gleich wie bei Textschablonen können Felder vorabgefüllt werden, z.B. [Patient.Name] usw. Wenn ein Feld mit [Adressat...] existiert, wird ein Adressabfrage-Dialog geöffnet.

### Eingabefelder

Nach der automatischen Abfüllung der Standardfelder werden Eingabefelder evaluiert. Diese sind mit dem html-Attribut data-input deklariert.

Beispiel:

```html
<div data-input="Bemerkungen"></div>
```

würde ein Eingabefeld mit dem Titel "Bemerkungen" erstellen, und das, was dort eingegeben wird, in der Ausgabedatei an diese Stelle eintragen.

### Dateititel und Name der Ausgabedatei

Der Dateititel wird entweder dem <title></title>-Tag, oder dem <h1></h1>-Tag entnommen, oder einem beliebigen Tag mit dem Attribut data-doctitle.

Beispiel:

```html
<h1 data-doctitle="blahblah">Blahblubb</h1>"
```


Würde dem Dokument den Titel "blahblah" geben (da data-doctitle eine höhere Präferenz hat, als h1)

Der Titel wird Teil des Dateinamens beim speichern. Beispiel: `2022-10-31_Dateititel_AdressatName_Andressat_Vorname.pdf`

### Anrede

Für Nicht-Standard-Anreden kann ein Element data-anrede="true" verwendet werden. Damn wird das Feld "Bemerkung" des Adressaten nach einer Zeichenfolge wie: :Anrede:Hallo Altes Haus!: durchsucht, und falls gefunden in dieses Feld gesetzt. 

Beispiel:

```html
p(data-anrede="true") Liebe[Adressat:mw:r/ ] [Adressat.Vorname]
```

Wenn obige Zeichenfolge in "Bemerkungen" des Adressaten "Hans Elexikus" gefunden wird, wird "Hallo Altes Haus" geschrieben, ansonsten "Lieber Hans".


## Medforms

Das Plugin kann auch [Medforms](http://medforms.ch)-Formulare mit den Daten des aktuell ausgewählten Patienten befüllen und zum Weiterbearbeiten anzeigen. 
PDF-Formulare im Schablonen-Order werden zusammen mit den HTML-Schalblonen zur Auswahl angeboten und automatisch als Medforms-Formulare behandelt.
