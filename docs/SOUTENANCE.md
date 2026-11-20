# Présenter le morpion

## Avant de passer

Lancer `tester.cmd`, puis ouvrir le jeu avec `lancer.cmd`. Préparer un dossier pour enregistrer la sauvegarde.

## Démonstration proposée

1. Montrer les deux champs de noms et les boutons radio.
2. Jouer en mode deux joueurs et montrer l'historique.
3. Faire gagner X : les options deviennent de nouveau modifiables.
4. Choisir le mode ordinateur, puis Rejouer. Il joue O dans une case libre choisie au hasard.
5. Sauvegarder cette partie, puis la recharger.
6. Finir la manche, choisir Avec chronomètre et cliquer Rejouer. Attendre dix secondes pour montrer la défaite au temps.
7. Montrer les classes Java et les résultats des tests.

Pour faire gagner X en mode deux joueurs, cliquer dans cet ordre : `(1,1), (2,1), (1,2), (2,2), (1,3)`.

## Expliquer le code simplement

- `Gestion` lance le jeu.
- `Morpion` affiche les champs et boutons et répond aux clics.
- `Partie` garde la grille et vérifie le gagnant.
- `Ordinateur` choisit une case libre au hasard.
- `Sauvegarde` écrit et relit le fichier.

Un clic donne une position. Le programme vérifie si la case est libre, pose X ou O, cherche un gagnant et passe au joueur suivant.

## Bilan

Expliquer les tableaux, les boucles, les classes et les événements Swing utilisés. Présenter les tâches et contributions réelles. Le projet a été réalisé avec l'aide de Codex ; préciser ce qui a été compris et vérifié personnellement.
