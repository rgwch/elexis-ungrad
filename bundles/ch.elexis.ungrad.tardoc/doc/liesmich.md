# Tardoc Abrechnungshilfe

## Wozu? 

Der Tardoc-Tarif bringt seine eigenen Herausforderungen: Neue Tarifpositionen, minutengenaue Abrechnung bei Zeitleistungen, Vorgaben bei Handlungsleistungen.

Dieses Plugin erleichtert die korrekte Abrechnung.

## Für wen?

Im Moment funktioniert das Plugin nur für Inhaber der Dinität 3010 (Fachärztinnen und Fachärzte für Allgemeine Innere Medizin)

## Bedienung:

Wenn Sie eine neue Konsultation beginnen, klicken Sie auf den "Start"-Button. Damit wird automatisch die Position CA.00.0010 verrechnet und die Stoppuhr beginnt zu laufen. Nach 5 Minuten wird zusätzlich die Position CA.00.0030 verrechnet und jede Minute eins hochhgezählt, bis maximal15 Minuten.

Wenn Sie eine Handlungsposition verrechnen, wird der Timer gestoppt. Im Konsultationstext werden die für diese Handlungsleistung vorgeschriebenen Untersuchungen vorgegeben und müssen dann noch von Hand ergänzt werden.

## Einschränkungen

Zwar kann das Plugin mittels der Datei rsc/config.json konfiguriert werden, aber getestet ist es bisher nur für die hausärztlichen Basis-Positionen. 
Auch ist config.json sehr Syntax-Sensibel. Prüfen Sie nach einer Änderung unbedingt mit einem JSON-Validator, ob sie korrekt ist.

## Vibe Coding

Dieses Plugin entstand mit wesentlicher Hilfe von KI (Claude Sonnet 4.5)

