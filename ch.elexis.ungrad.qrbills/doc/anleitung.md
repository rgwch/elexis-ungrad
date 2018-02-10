# QR-Rechnungen

## Wozu?

Ab 2018 wurde in der Schweiz schrittweise der ISO 20022 Standard zur automatisierten Zahlungsverarbeitung eingeführt.
Für uns einfache Leute am wichtigsten sind erstens die Zahlungseingangs-Dateien, welche vom bisherigen Zeilenbasierten ESR-Format
auf ein XML-basiertes Format umgestellt wurden.
Elexis kann in der Version 3.4/Ungrad2018  solche Dateien bereits verarbeiten. Achtung: Mit früheren Elexis Versionen können Sie Zahlungen nur noch so lange automatisch verbuchen, bis Ihre Bank auf Iso 20022 umgestellt hat.

In einem zweiten Schritt, der auf Mitte/Ende 2018 angekündigt ist, sollen auch die Einzahlungsscheine geändert werden. Anstelle des Roten/rosa ESR-Teils soll ein auf beliebiges Papier druckbarer QR-Code kommen. Das ist zunächst mal eine Verbesserung: Es erspart uns, Drucker mit zwei Schächten anzuschaffen und zwei Sorten Papier vorzuhalten.

Allerdings ist der Aufbau des QR-Zahlteils ein wenig knifflig, und deshalb sind zum Zeitpunkt dieses Schreibens nur wenige Firmen und nur wenige Software-Lösungen bereits darauf eingerichtet.
 
 ## Installation

 Sie können ch.elexis.ungrad.qrbills über die Software-Installation in Elexis Ungrad 2018 oder Elexis 3.4 installieren.

 ## Konfiguration

 QrBills benötigt die IBAN Nummer Ihres Kontos. Gehen Sie auf Datei-Einstellungen - Abrechnungssysteme - QR-Rechnung und geben Sie die entsprechenden Daten ein.
 
 