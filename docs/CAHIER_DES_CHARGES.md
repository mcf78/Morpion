# Consignes du morpion

Source : [photo du sujet](consigne-originale.jpeg).

Le programme doit reprendre les éléments dessinés au tableau.

| Élément demandé | Ce que fait le jeu |
| --- | --- |
| Java Swing | Une application avec une fenêtre et des boutons. |
| Nom J1 et Nom J2 | Deux champs pour saisir les noms. |
| 2 joueurs ou ordinateur | Deux boutons radio pour choisir le mode. |
| Tableau de 9 boutons | Une grille de trois lignes et trois colonnes. |
| Listing des coups | Une zone de texte avec les noms et les coordonnées. |
| Sauvegarder | Enregistrer la partie dans un fichier. |
| Recharger | Reprendre la partie enregistrée. |
| Rejouer | Vider la grille et commencer une nouvelle partie. |
| Quitter | Fermer le programme. |
| Sans ou avec temps | Activer ou désactiver le chronomètre. |
| 10 secondes par coup | Le temps repart à chaque coup joué. |
| Défaite au temps | Le joueur qui dépasse le délai perd. |

## Règles

X commence. Les joueurs posent leur symbole dans une case vide, chacun leur tour. Trois symboles sur une ligne, une colonne ou une diagonale donnent la victoire. Une grille pleine sans gagnant donne un match nul.

## Choix simples

L'ordinateur joue O et choisit une case libre au hasard. La sauvegarde est un fichier texte. Le code utilise les classes, tableaux, boucles et composants Swing vus en cours. La classe `Gestion` est le point d'entrée : le jeu se lance dans IntelliJ avec le triangle vert de sa méthode `main`. Il n'y a pas de Maven, de Gradle ni de bibliothèque à installer.

## À vérifier

Essayer les deux modes, une victoire, un match nul, le temps écoulé et la sauvegarde/reprise. Les tests facultatifs se lancent dans IntelliJ avec le `main` des classes `TestPartie`, `TestServices` et `TestInterfaceMorpion`, dans `tests`. Le compte rendu des essais se trouve dans `VALIDATION.txt`.
