# Morpion en Java Swing

Projet de MALLEM Moncef, Paris Ynov Campus, année 2026 / 2027. Réalisé avec l'aide de Codex à partir de la [photo du sujet](docs/consigne-originale.jpeg).

## Lancer le jeu

Installer Java JDK 17 ou plus récent, puis double-cliquer sur `lancer.cmd`.

Dans IntelliJ : ouvrir le dossier `Morpion`, choisir le JDK et lancer `Gestion.main()`.

## Jouer

1. Saisir les noms des joueurs.
2. Choisir **2 joueurs** ou **Ordinateur**.
3. Choisir **Sans** ou **Avec** chronomètre, puis cliquer sur **Démarrer**.
4. Cliquer à tour de rôle sur une case vide. X commence.
5. Aligner trois symboles pour gagner. Une grille pleine sans gagnant donne un match nul.

En mode ordinateur, le joueur utilise X. L'ordinateur joue O et choisit une case libre au hasard.

Avec le chronomètre, chaque joueur a **10 secondes pour jouer son coup**. Si le temps est écoulé, il perd.

Les coups apparaissent dans l'historique avec le nom et les coordonnées, de 1 à 3.

Les noms et les options sont modifiables avant de démarrer et après la fin de la partie.

## Les boutons

| Bouton | Utilité |
| --- | --- |
| Sauvegarder | Enregistrer la partie dans un fichier. |
| Recharger | Reprendre une partie enregistrée. |
| Rejouer | Recommencer avec une grille vide. |
| Quitter | Fermer le jeu. |

La sauvegarde conserve les noms, les options, les coups et le temps restant. Les accents sont conservés avec le format UTF-8.

## Les fichiers Java

| Classe | Rôle |
| --- | --- |
| `Gestion` | Lance le programme. |
| `Morpion` | Affiche la fenêtre et réagit aux clics. |
| `Partie` | Contient la grille et les règles. |
| `Ordinateur` | Choisit une case libre au hasard. |
| `Sauvegarde` | Enregistre et relit le fichier. |

Le jeu utilise uniquement Java et Swing, sans bibliothèque à ajouter.

## Vérifier et rendre le projet

- `tester.cmd` lance les tests.
- `compiler.cmd` crée `dist/Morpion.jar`.
- `java -jar dist/Morpion.jar` lance le JAR.

Documentation : [consignes](docs/CAHIER_DES_CHARGES.md), [organisation](docs/ORGANISATION.md), [soutenance](docs/SOUTENANCE.md) et [rapport PDF](output/pdf/Documentation_Morpion_MALLEM_Moncef.pdf).

Les sources et la documentation sont publiées sur [GitHub](https://github.com/mcf78/Morpion).

Les tâches sont suivies sur le [tableau Trello Morpion Java Swing](https://trello.com/b/pguzsi6Y/morpion-java-swing), qui est privé. Une [capture réelle du tableau Trello](docs/images/06-trello-reel.png) a été fournie et accompagne le projet. Les rôles du binôme restent à attribuer aux participants réels.
