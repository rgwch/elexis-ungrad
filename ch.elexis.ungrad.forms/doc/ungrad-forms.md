# Ungrad Formulare

Dieses Plugin erlaubt das einfache und schnelle Ausfüllen von Formularen im HTML-, PUG- und PDF-Format ([MedForms](http://medforms.ch)). Es kann damit das jeweilige Textplugin ergänzen oder je nach Anspruch auch ersetzen. Im Unterschoed zum Textplugin muss kein externes Programm genutzt werden, wodurch die Abläufe stabiler und schneller werden. Dafür kann man das Aussehen des Dokuments nur in der Vorlage festlegen und gibt beim Ausfüllen nur reinen Text ohne spezielle Formatierung ein.

## Prinzip

Formulare werden in einem zu definierenden Order vorgehalten. Bei Auswahl eines Formulars werden ausfüllbare Felder angezeigt, und beim Abspeichern wieder ins Formular integriert. Das fertige Formular wird dann als PDF-Datei in einem ebenfalls zu definierenden Ausgangs-Ordner abgelegt. Dieser Speicherort kann auch das Dokumentenverzeichnis sein. Innerhalb des Speicherordners wird das Dokument wie folgt abgelegt:

`speicherort/t/Testperson_Armeswesen_01.02.1955/A_2022-11-01-Zuweisung_Eisenbeiss-Peter.pdf`

(Dies wäre ein Formular mit dem Titel "Zuweisung", welches am 1.11.2022 an einen Peter Eisenbeiss geschickt würde und Testperson Armeswesen betrifft.)

## Einrichtung

Am besten gehen Sie von den [Beispielformularen](https://github.com/rgwch/elexis-ungrad/tree/master/ch.elexis.ungrad.forms/rsc) aus. Kopieren Sie diese Formulare und die beiden .css-Dateien in ein anderes Verzeichnis, z.B. IhreDokumente/elexis/schablonen.

Wenn Sie Vorlagen in der Beschreibungssprache [pug](https://pugjs.org/api/getting-started.html) nutzen wollen, benötigen Sie ein Progamm, das Pug nach html umwandeln kann. Wenn Ihnen das nichts sagt, macht das auch nichts, man kann genauso gut mit html-Formularen arbeiten. Als Beispiel finden Sie "AnmeldungDu.html", das einfach die compilierte Version von AnmeldungDu.pug ist, und das Sie als Ausgangspunkt für eigene HTML-Formulare verwenden können-.

Gehen Sie auf Datei-Einstellungen-Datenaustausch-Forms und geben Sie die Verzeichnisse, und -falls vorhanden- das Pug-Programm ein,

![Settings](settings.png)

## Verwendung: HTML Formulare

Öffnen Sie die View "Forms View" und klicken Sie auf das grüne "+" Symbol, um eine Liste der Vorlagen zu erhalten.

![Auswahl](choose.png)

Doppelklick auf die gewünschte Vorlage öffnet die Formular-Sicht (In diesem Beispiel ergibt AnmeldungDu.hml und AnmeldungDu.pug exakt dasselbe Formular; Sie brauchen nur eine davon):

![Formular](formview.png)

Zum Abschluss klicken Sie auf das Druckersymbol rechts oben, um die PDF-Sicht zum Ausdrucken oder Mailen zu erhalten:

![PDF-Version](pdf.png)

Zusammenfassung: Sie brauchen:

_Entweder:_ AnmeldungDu.pug _und_ a4.pug _und_ styles_a4.css (alle in Ihrem Vorlagen-Verzeichnis)

_Oder:_ AnmeldungDu.html (die alles obige enthält)

Diese Vorlage enthält Felder, die in der Formularsicht gefüllt werden. Klick auf das Druckersymbol führt dann Formular und Inhalt zusammen und erstellt eine HTNL- und eine PDF- Datei. Mehr über den Aufbau der Formulare und die Definition der Felder können Sie [hier](../rsc/whatisit.md) nachlesen.

Wenn Sie in der Listenansicht auf eine existierende Datei doppelklicken, wird sie wieder in der Formularansicht geöffnet und kann geändert werden. Ein so erstelltes PDF überschreibt das früher erstellte Dokument.

## Verwendung: Medforms Formulare

Das Plugin verarbeitet auch [Medforms](https://www.medforms.ch/) Formulare. Das sind PDF Dateien mit Standardkonformen Formularfeldern. Ungrad Forms kann die Felder, die den aktuelll ausgewählten Patienten betreffen, vorab abfüllen. In diesem Fall wird nicht die Formularsicht in der Forms-View geöffnet, sondern direkt der PDF-Viewer mit dem Vorabgefüllten Formular:

![Medforms](physio.png)

Da es allerdings kein frei zugängliches API für Medforms-Dateien gibt, beruht dieses Ausfüllen auf einfachen Analysen einiger Formulare. Geben Sie doch eine Rückmeldung, falls Ihre Formulare nicht funktionieren.

## Dank

Die Verarbeitung von HTML- und PDF-Dateien wird ermöglicht durch:

* [JSoup](https://jsoup.org/)
* [Open HTML to PDF](https://github.com/danfickle/openhtmltopdf)
* [Apache PDFBox](https://pdfbox.apache.org/)
