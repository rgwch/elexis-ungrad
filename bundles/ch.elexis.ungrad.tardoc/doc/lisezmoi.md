# Assistant de facturation Tardoc

## À quoi cela sert-il ?

Le tarif Tardoc apporte ses propres défis : nouvelles positions tarifaires, facturation à la minute pour les prestations temporelles, exigences pour les prestations d'action.

Ce plugin facilite une facturation correcte.

## Pour qui ?

Actuellement, le plugin ne fonctionne que pour les titulaires de la dignité 3010 (Spécialistes en médecine interne générale)

## Utilisation :

Lorsque vous commencez une nouvelle consultation, cliquez sur le bouton "Démarrer". Cela facture automatiquement la position CA.00.0010 et le chronomètre commence à tourner. Après 5 minutes, la position CA.00.0030 est facturée en plus et compte une minute à la fois, jusqu'à un maximum de 15 minutes.

Lorsque vous facturez une position d'action, le minuteur s'arrête. Dans le texte de consultation, les examens prescrits pour cette prestation d'action sont prédéfinis et doivent ensuite être complétés manuellement.

## Limitations

Bien que le plugin puisse être configuré à l'aide du fichier rsc/config.json, il n'a été testé jusqu'à présent que pour les positions de base du médecin de famille.
De plus, config.json est très sensible à la syntaxe. Après avoir apporté des modifications, assurez-vous de vérifier avec un validateur JSON qu'il est correct.

## Vibe Coding

Ce plugin a été créé avec l'aide significative de l'IA (Claude Sonnet 4.5)
