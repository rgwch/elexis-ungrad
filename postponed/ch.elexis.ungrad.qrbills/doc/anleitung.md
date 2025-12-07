# QR-Rechnungen

## Wozu?

Ab 2018 wurde in der Schweiz schrittweise der ISO 20022 Standard zur automatisierten Zahlungsverarbeitung eingeführt.
Relevante Änderungen betreffen einerseits die Zahlungseingangs-Dateien, welche vom bisherigen zeilenbasierten ESR-Format
auf ein XML-basiertes Format umgestellt wurden.

Elexis kann seit der Version 3.4/Ungrad2018  solche Dateien bereits verarbeiten. .

In einem zweiten Schritt, wurden auch die Einzahlungsscheine geändert. Anstelle des roten/rosa ESR-Teils kommt ein auf Normalpapier druckbarer QR-Code. Achtung: Das bedeutet nicht, dass man kein Spezialpapier meht braucht. Der Standard sieht vor, dass man perforiertes Papier zum Abtrennen des Zahlteils verwenden muss (Siehe: <https://www.paymentstandards.ch/dam/downloads/style-guide-de.pdf> ).

Nur wenn man die Rechnung als PDF versendet, darf der Empfänger sie auf Normalpapier ausdrucken. In diesem Fall muss dann aber drauf stehen: "Vor der Einzahlung abzutrennen", damit der Kunde das tut, bevor er den Einzahlungsschein zur Post bringt. Sofern er nicht sowieso den QR Code gleich am Bildschirm mit der Banking-App scannt.

Elexis Ungrad kann seit Version 2018 standardkonforme QR-Codes auf PDF ausgeben, die ggf. ausgedruckt oder als Mail versendet werden können.

*Achtung*: Das Datenformat wurde geändert. Die mit Elexis Ungrad 2018 erstellbaren QR-Codes sind nicht mehr gültig. Aktuell gilt das Format 2.0. Sie müssen also zumindest das QR-Plugin auf Ungrad2022 updaten, falls Sie schon eine frühere Version verwendet haben. 
 
## Installation

 Sie können ch.elexis.ungrad.qrbills über die Software-Installation in Elexis Ungrad-2022 installieren.
 Ausserdem muss das Feature "Elexis Swiss OpenSource Feature" aus der Gruppe "Basispakete" installiert sein.

## Konfiguration

 QrBills benötigt die QR-IBAN Nummer Ihres Kontos, die Sie bei Ihrer Bank verlangen müssen. Das ist nicht dieselbe IBAN, wie die, die Sie normalerweise nutzen, und die Sie auch weiterhin für Überweisungen benötigen. Gehen Sie auf Datei-Einstellungen - Abrechnungssysteme - QR-Rechnung und geben Sie die entsprechenden Daten für Ihre Bank (auf die blaue Schrift klicken und auswählen) und die IBAN ein.
 
![settings.jpg](/ungrad/images/qr_bills_settings.jpg)

Beachten Sie, dass die derzeitige Spezifikation QR-Rechnung von ISO 20022 nur IBAN-Nummern mit CH und LI Präfix zulässt.

Die Zeilen für die Vorlagen können Sie erst mal leer lassen, ich werde weiter unten darauf eingehen. Wenn diese Felder leer sind, verwendet Elexis Standardvorlagen, die bereits einigermassen brauchbar sind.
 
Sie können dieses Plugin zur Rechnungsausgabe für jedes andere Abrechnungssystem verwenden:
 
![settings2.jpg](/ungrad/images/qr_bills_settings2.jpg)
 
 
## Nutzung
 
 Wählen Sie bei der Rechnungsausgabe das Ziel "Rechnung mit QR-Code". 
 
![output.jpg](/ungrad/images/qr_bills_output.jpg)
 
Für jede Rechnung werden fünf Dateien geschrieben: 

* nummer.xml - Die XML-Datei, die Sie z.B. ans Trust Center senden können

* nummer_rf.html - Der Rückforderungsbeleg als HTML-Datei

* nummer_rf.pdf - Der Rückforderungsbeleg als PDF-Datei

* nummer_qr.html - Der Einzahlungsschein als HTML-Datei

* nummer_qr.pdf - Der Einzahlungsschein als PDF-Datei

Die HTML Dateien sind Zwischenprodukte, die normalerweise gleich wieder gelöscht werden, so dass Sie am Ende nur die XML-Datei im Verzeichnis für XMLs und die zwei PDF-Dateien im Verzeichnis für PDFs haben.

Die zwei darunterliegenden Checkboxen entscheiden darüber, welche Seiten überhaupt ausgegeben werden. Normalerweise sind das beide.

Wenn Sie das Feld darunter ankreuzen, wird Elexis versuchen, zur Ausgabe geeignete Drucker zu finden, und Ihnen die Auswahl anzuzeigen. Wenn Sie "Direkt ausdrucken" ankreuzen, dann wird keine Auswahl angezeigt, sondern die Rechnungen werden direkt auf den Standarddrucker ausgegeben.

Wenn Sie PDF löchen ankreuzen, dann werden die Dateien nach erfolgreichem Ausdruck gelöscht. Achtung: Elexis kann nicht wirklich erkennen, ob der Ausdruck erfolgreich war, sondern nur die Rückmeldung des Druckers auswerten.

Die direkte Druckausgabe funktioniert nicht auf allen Systemen. Sie können aber immer die PDFS manuell ausdurcken. Mit Tools wie etwa dem Acrobat Reader lässt sich das auch recht komfortabel gestalten.

Die unterste Checkbox gibt Ihnen die Möglichkeit, die HTML-Zwischendateien aufzubewahren, so dass Sie sie analysieren können, wenn die Rechnungen etwa nicht so aussehen, wie erwartet. 




## Mögliche Probleme
 
Der Ausdruck einer PDF-Datei ist nicht immer perfekt. Häufig spielen uns "intelligente" Optimierungen einen Streich. So versuchen manche Druckertreiber, die Seite zu verkleinern, damit sie in den Druckbereich (Seite ohne Ränder) des Druckers passt. Dies darf im Fall der QR-Rechnung aber nicht passieren, da die Grösse und Position aller Elemente innerhalb enger Grenzen vorgeschrieben ist. Kontrollieren Sie das, indem Sie nach einem Probeausdruck nachmessen, ob der "Zahlteil" wirklich Format A6 ist (148x105mm), und ob er wirklich in der rechten unteren Ecke der Seite (nicht des Druckbereichs!) platziert ist.
 
Wenn die Positionierung und Grösse nicht stimmt, damm kommen automatische Verarbeitungssysteme damit nicht zurecht, was für Sie zu erhöhten Verarbeitungskosten führen kann.

Lesen Sie dann den QR Code mit irgendeiner QR-Applikation oder Banking-Applikation auf Ihrem Smartphone ein und prüfen Sie, ob die Rechnungsdaten korrekt eingelesen werden. 

Senden Sie drei Testrechnungen an Ihre Bank zur Prüfung, ob die QR-Zahlteile korrekt sind.

## Vorlagen

Sie können das Aussehen der Rechnungen und Mahnungen selbst bestimmen. Nur das Aussehen des Zahlungs-Abschnitts ist genau vorgeschrieben. 

Wir verwenden hier HTML-Dateien als Vorlagen. Sie können solche Dateien mit einem gewöhnlichen Texteditor oder einem speziellen HTML-Editor verändern. An sich kann beim Experimentieren nicht viel kaputtgehen. Schlimmstenfalls wird nichts mehr oder eine ungültige Datei ausgedruckt. Gehen Sie dann einfach wieder zur Ausgangsdatei zurück.

Es ist empfehlenswert, dass Sie zunächst die [Vorlage](../rsc/qrbill_template_v4.html) herunterladen und z.B. als 'qr-rechnung.html' irgendwo abspeichern.

Wenn Sie HTML nicht gewohnt sind, wird Ihnen diese Datei reichlich seltsam vorkommen. Sie sind aber auf der sicheren Seite, wenn Sie in dieser Vorlage zunächst nur diesen Teil hier verändern (Sie müssen dazu ziemlich weit, bis ins unterste Drittel, hinunterscrollen):

```html
 <!-- *******************************************************************************************************
      User-modifiable part starts here 
      ********************************************************************************************************* -->
    <div id="header">
      <h1>Praxis [Mandant.Titel] [Mandant.Vorname] [Mandant.Name]</h1>
      <span>[Mandant.Strasse] [Mandant.Plz] [Mandant.Ort] Tel.: [Mandant.Telefon1] e-mail: [Mandant.E-Mail]</span>
      <hr />

    </div>
    <div id="date">
      [Mandant.Ort], [Datum.heute]
    </div>

    <div id="sender">
      [Mandant.Titel] [Mandant.Vorname] [Mandant.Name]
      <br /> Facharzt für [Mandant.TarmedSpezialität]
      <br /> ZSR-Nr. [Mandant.KSK]
    </div>

    <div id="address">
      [Adressat.Anschrift]
      <br />
    </div>

    <div id="bill_summary">
      <table>
        <tr>
          <td colspan="2">
            Für: [Patient.Name] [Patient.Vorname], [Patient.Geburtsdatum]
          </td>
        </tr>
        <tr>
          <td width="100%">
            Rechnungs-Nummer:
          </td>
          <td>
            [Rechnung.RnNummer]
          </td>
        </tr>
        <tr>
          <td>Rechnungs-Datum:</td>
          <td>[Rechnung.RnDatum]</td>
        </tr>
        <tr>
          <td>Behandlungen von:</td>
          <td>[Rechnung.RnDatumVon]</td>
        </tr>
        <tr>
          <td>Behandlungen bis:</td>
          <td>[Rechnung.RnDatumBis]</td>
        </tr>

      </table>
    </div>
    <div id="maintext">
      <h1>Honorar-Rechnung</h1>
      <p>Diese Seite ist für Ihre Unterlagen bestimmt. Bitte senden Sie den beiliegenden Rückerstattungs-Beleg an
        Ihre Krankenkasse.
        Bitte um Begleichung innert 30 Tagen.
      </p>
    </div>

    <!-- ***************************************************************************************** 
      User-modifiable part ends here 
     ********************************************************************************************* -->
```


Verändern Sie den Text nach Ihrem Geschmack, laden Sie sie in einen Web-Browser und machen Sie einen Ausdruck. Prüfen Sie genau, ob die Rechnung wie erwartet aussieht, und messen Sie auch den unteren Abschnitt nochmals aus. Er muss 105mm hoch sein, und der QR-Code sollte 46mm messen.

Wenn alles nach Ihren Wünschen aussieht, kopieren Sie die fertige qr-rechnung.html nach qr-mahnung-1.html und ändern Sie dort nur die Zeilen, die bei der Zahlungserinnerung anders aussehen sollen. Verfahren Sie gleich für qr-mahnung-2.html und qr-mahnung-3.html. Konfigurieren Sie dann das QR Plugin wie eingangs gezeigt, so dass es auf diese Vorlagen zurückgreift.

Die zweite benötigte Vorlage ist die [Formalisierte Tarmed-Rechnung](../rsc/tarmed44_page1.html). Diese sollten Sie wirklich nur ändern, wenn Sie dehr genau wissen, was Sie tun.

