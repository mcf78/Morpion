"""Crée le rapport PDF du morpion avec ses captures et ses vrais liens."""
from pathlib import Path
import json
import pymupdf as fitz

BASE = Path(__file__).resolve().parents[1]
SORTIE = BASE / "output/pdf/Documentation_Morpion_MALLEM_Moncef.pdf"
IMAGES = BASE / "docs/images"
LIENS = json.loads((BASE / "docs/LIENS_PROJET.json").read_text(encoding="utf-8"))
LARGEUR, HAUTEUR = 595.276, 841.89
MARGE, ZONE = 50, 495.276
BLEU = (0.07, 0.16, 0.27)
VERT = (0.0, 0.45, 0.38)
GRIS = (0.36, 0.42, 0.48)
CLAIR = (0.94, 0.96, 0.97)
BLANC = (1, 1, 1)
FICHIERS_POLICES = {
    "texte": Path("C:/Windows/Fonts/segoeui.ttf"),
    "gras": Path("C:/Windows/Fonts/segoeuib.ttf"),
    "titre": Path("C:/Windows/Fonts/georgia.ttf"),
}
POLICES = {
    nom: fitz.Font(fontfile=str(path)) if path.exists() else fitz.Font("helv")
    for nom, path in FICHIERS_POLICES.items()
}
document = fitz.open()
document.set_metadata({
    "title": "Morpion Java Swing - Rapport de projet",
    "author": "MALLEM Moncef",
    "subject": "Paris Ynov Campus - 2026 / 2027",
})


def longueur(texte, taille=11, police="texte"):
    return POLICES[police].text_length(texte, fontsize=taille)


def lignes(texte, largeur, taille=11, police="texte"):
    resultat = []
    for paragraphe in texte.split("\n"):
        ligne = ""
        for mot in paragraphe.split():
            if ligne and longueur(ligne + " " + mot, taille, police) > largeur:
                resultat.append(ligne)
                ligne = ""
            while longueur(mot, taille, police) > largeur:
                limite = 1
                while longueur(mot[:limite + 1], taille, police) <= largeur:
                    limite += 1
                if ligne:
                    resultat.append(ligne)
                    ligne = ""
                resultat.append(mot[:limite])
                mot = mot[limite:]
            ligne = (ligne + " " + mot).strip()
        resultat.append(ligne)
    return resultat


def texte(page, contenu, y, x=MARGE, largeur=ZONE, taille=11,
          police="texte", couleur=BLEU, espace=10):
    for ligne in lignes(contenu, largeur, taille, police):
        if y + taille > 780:
            raise ValueError("Texte trop bas sur la page " + str(page.number + 1))
        page.insert_text((x, y + taille), ligne, fontsize=taille,
                         fontname=police, color=couleur)
        y += taille * 1.45
    return y + espace


def rectangle(page, x, y, largeur, hauteur, couleur=CLAIR):
    page.draw_rect(fitz.Rect(x, y, x + largeur, y + hauteur),
                   color=None, fill=couleur)


def page(titre, rubrique):
    p = document.new_page(width=LARGEUR, height=HAUTEUR)
    for nom, chemin in FICHIERS_POLICES.items():
        if chemin.exists():
            p.insert_font(fontname=nom, fontfile=str(chemin))
        else:
            p.insert_font(fontname=nom, fontbuffer=POLICES[nom].buffer)
    texte(p, rubrique.upper(), 40, taille=8, police="gras", couleur=VERT)
    texte(p, titre, 75, taille=28, police="titre")
    p.draw_line((MARGE, 797), (LARGEUR - MARGE, 797), color=(0.82, 0.87, 0.9))
    p.insert_text((MARGE, 817), "MALLEM Moncef / Morpion Java Swing",
                  fontsize=8, fontname="texte", color=GRIS)
    p.insert_text((LARGEUR - MARGE - 15, 817), str(p.number + 1).zfill(2),
                  fontsize=9, fontname="gras", color=BLEU)
    return p


def sous_titre(p, contenu, y):
    return texte(p, contenu, y, taille=13, police="gras")


def liste(p, elements, y):
    for element in elements:
        texte(p, "-", y, largeur=12, couleur=VERT, police="gras")
        y = texte(p, element, y, x=MARGE + 16, largeur=ZONE - 16, espace=10)
    return y


def tableau(p, entetes, donnees, largeurs, y, taille=10.5):
    for numero, rang in enumerate([entetes] + donnees):
        police = "gras" if numero == 0 else "texte"
        hauteur = max(len(lignes(t, l - 20, taille, police))
                       for t, l in zip(rang, largeurs)) * taille * 1.45 + 18
        if y + hauteur > 775:
            raise ValueError("Tableau trop long")
        x = MARGE
        for contenu, largeur in zip(rang, largeurs):
            rectangle(p, x, y, largeur, hauteur, BLEU if numero == 0 else
                      (CLAIR if numero % 2 else BLANC))
            texte(p, contenu, y + 8, x=x + 10, largeur=largeur - 20, taille=taille,
                  police=police, couleur=BLANC if numero == 0 else BLEU)
            x += largeur
        y += hauteur
    return y + 20


def capture(p, fichier, legende, y):
    chemin = IMAGES / fichier
    image = fitz.Pixmap(str(chemin))
    rapport = min(ZONE / image.width, 410 / image.height)
    largeur, hauteur = image.width * rapport, image.height * rapport
    x = MARGE + (ZONE - largeur) / 2
    p.insert_image(fitz.Rect(x, y, x + largeur, y + hauteur), filename=str(chemin))
    return texte(p, legende, y + hauteur + 10, taille=8.5, couleur=GRIS, espace=20)


# 1. Présentation
p = page("Morpion", "Projet Java Swing")
texte(p, "Un jeu simple, avec une fenêtre et des boutons.", 135, taille=15)
y = texte(p, "MALLEM Moncef\nParis Ynov Campus\nAnnée 2026 / 2027\n20/11/2026",
          205, taille=12, espace=25)
symboles = ["X", "X", "X", "O", "", "", "", "O", ""]
for i, symbole in enumerate(symboles):
    x = MARGE + (i % 3) * 78
    yy = 354 + (i // 3) * 78
    rectangle(p, x, yy, 70, 70)
    texte(p, symbole, yy + 11, x=x + 21, largeur=60, taille=34,
          couleur=VERT if symbole == "O" else BLEU)
texte(p, "Deux joueurs ou ordinateur\nSauvegarde et reprise\n10 secondes par coup en option",
      382, x=326, largeur=218, taille=12)
texte(p, "Le projet reprend la maquette du cours fournie en photo. Il utilise les "
      "composants Swing et les notions de Java vues en cours.", 635, taille=12)
texte(p, "Réalisé avec l'aide de Codex.", 724, taille=10, couleur=GRIS)

# 2. Sujet
p = page("Les consignes", "Le sujet")
y = texte(p, "La photo du tableau demande les éléments suivants.", 132)
y = tableau(p, ["Élément demandé", "Dans le programme"], [
    ("Java Swing", "Une fenêtre avec des composants standards."),
    ("Nom J1 et Nom J2", "Deux champs de texte."),
    ("2 joueurs / ordinateur", "Deux boutons radio."),
    ("9 boutons", "Une grille de trois lignes et trois colonnes."),
    ("Listing des coups", "Une zone de texte avec noms et coordonnées."),
    ("Sauvegarder / recharger", "Enregistrer et reprendre une partie."),
    ("Rejouer / quitter", "Nouvelle partie ou fermeture du jeu."),
    ("Sans / avec temps", "Choix du chronomètre."),
    ("10 secondes par coup", "Le joueur perd si son délai est dépassé."),
], [165, ZONE - 165], y + 8)
texte(p, "Source : docs/consigne-originale.jpeg.\nLe tableau complet des consignes est "
      "dans docs/CAHIER_DES_CHARGES.md.", y + 4, taille=10, couleur=GRIS)

# 3. Classes
p = page("Comprendre le code", "Les classes")
y = texte(p, "Chaque classe a un rôle simple.", 132)
y = tableau(p, ["Classe", "Rôle"], [
    ("Gestion", "Lance le jeu."),
    ("Morpion", "Affiche la fenêtre et répond aux clics."),
    ("Partie", "Garde la grille et vérifie les règles."),
    ("Ordinateur", "Choisit une case libre au hasard."),
    ("Sauvegarde", "Écrit et relit le fichier."),
], [145, ZONE - 145], y + 8)
y = sous_titre(p, "Quand on clique sur une case", y + 10)
y = liste(p, [
    "Le bouton donne sa position dans la grille.",
    "Le programme vérifie si la case est libre.",
    "Il pose X ou O et cherche un gagnant.",
    "Si la partie continue, il passe au joueur suivant.",
], y)
texte(p, "Le code utilise des variables, des tableaux, des boucles, des conditions "
      "et les événements des boutons.", y + 12)

# 4. Accueil
p = page("Préparer une partie", "Le jeu en images")
y = texte(p, "Saisir les noms, choisir les options, puis cliquer sur Démarrer.", 132)
y = capture(p, "01-accueil.png", "Accueil du jeu. Rendu du composant Swing réel hors écran.", y + 10)
liste(p, [
    "J1 utilise X. J2 ou l'ordinateur utilise O.",
    "X commence chaque nouvelle partie.",
    "Le chronomètre peut être activé ou désactivé.",
], y)

# 5. Partie
p = page("Jouer chacun son tour", "Le jeu en images")
y = texte(p, "Cliquer sur une case vide. Les coups sont ajoutés à l'historique.", 132)
y = capture(p, "02-partie.png", "Une partie en cours et la liste des coups.", y + 10)
liste(p, [
    "Une case occupée ne peut plus être jouée.",
    "L'historique indique le nom et les coordonnées de 1 à 3.",
    "Une grille pleine sans gagnant donne un match nul.",
], y)

# 6. Victoire
p = page("Gagner et rejouer", "Le jeu en images")
y = texte(p, "Trois symboles sur une ligne, une colonne ou une diagonale donnent la victoire.", 132)
y = capture(p, "03-victoire.png", "Exemple de victoire affichée par le programme.", y + 10)
liste(p, [
    "Le résultat apparaît à la fin de la partie.",
    "Il n'est plus possible de jouer après la fin.",
    "Rejouer vide la grille pour commencer une nouvelle partie.",
], y)

# 7. Ordinateur
p = page("Le mode ordinateur", "Le jeu en images")
y = texte(p, "L'humain joue X. L'ordinateur joue O juste après lui.", 132)
y = capture(p, "04-ordinateur.png", "Une partie contre l'ordinateur.", y + 10)
y = sous_titre(p, "Un choix au hasard", y)
texte(p, "Le programme cherche les cases libres, puis en choisit une au hasard. "
      "Cette solution est simple à comprendre. L'ordinateur peut gagner, perdre ou faire match nul.", y)

# 8. Chronomètre
p = page("Le chronomètre", "Le jeu en images")
y = texte(p, "Avec le temps, chaque joueur a dix secondes pour jouer son coup.", 132)
y = capture(p, "05-temps-ecoule.png", "Le délai est écoulé : le joueur actif a perdu.", y + 10)
liste(p, [
    "Le temps repart à dix secondes après un coup accepté.",
    "À zéro, le joueur actif perd et l'adversaire gagne.",
    "Le choix d'un fichier met le chronomètre en pause.",
], y)

# 9. Fichiers et essais
p = page("Sauvegarder et vérifier", "Les fichiers et les tests")
y = sous_titre(p, "Sauvegarder et recharger", 135)
y = texte(p, "Sauvegarder écrit la partie dans un fichier texte. Recharger permet de la reprendre.", y)
y = tableau(p, ["Ce qui est conservé", "Utilité"], [
    ("Noms et options", "Retrouver les mêmes joueurs et le même mode."),
    ("Liste des coups", "Reconstruire la grille et l'historique."),
    ("Temps restant", "Reprendre le délai enregistré."),
    ("Fin au temps", "Conserver une défaite au chronomètre."),
], [180, ZONE - 180], y + 6)
y = texte(p, "Le fichier est écrit en UTF-8 pour conserver les accents. Un fichier incorrect "
          "est refusé sans remplacer la partie courante.", y)
y = sous_titre(p, "Les vérifications", y + 15)
y = liste(p, [
    "Gagner avec X et O, faire match nul et refuser une case occupée.",
    "Vérifier que l'ordinateur joue une case libre.",
    "Sauvegarder, recharger et retrouver les mêmes coups.",
    "Vérifier le chronomètre et le bouton Rejouer.",
], y)
texte(p, "Commande : tester.cmd. Le résultat des essais est dans docs/VALIDATION.txt. "
      "Les dialogues de fichiers et la fermeture réelle de la fenêtre restent à essayer "
      "sur le poste de présentation.", y + 10, taille=10, couleur=GRIS)

# 10. Suivi et bilan
p = page("Organiser le projet", "GitHub et Trello")
y = texte(p, "Les tâches sont regroupées dans un tableau Kanban local, dans le style de "
          "Trello. Il permet de voir ce qui reste à faire et ce qui est terminé.", 132)
y = capture(p, "06-tableau-kanban.png", "Tableau Kanban local du projet. Aucun compte Trello nécessaire.", y + 8)
y = sous_titre(p, "GitHub", y)
lien = LIENS.get("github", "").strip()
if lien:
    depart = y
    y = texte(p, lien, y, taille=10, couleur=VERT)
    p.insert_link({"kind": fitz.LINK_URI,
                   "from": fitz.Rect(MARGE, depart, MARGE + ZONE, y), "uri": lien})
else:
    y = texte(p, "Le dépôt Git local est prêt. Le dépôt GitHub reste à créer après "
              "connexion du compte.", y, taille=10, couleur=GRIS)
y = sous_titre(p, "Répartition proposée", y + 5)
y = texte(p, "Participant 1 : grille, règles et ordinateur. Participant 2 : fenêtre, "
          "chronomètre et sauvegarde. Ensemble : tests, rapport et présentation.", y, taille=10.5)
texte(p, "Les rôles sont à attribuer aux participants réels.", y, taille=9, couleur=GRIS)

if len(document) != 10:
    raise ValueError("Le rapport doit contenir dix pages")
SORTIE.parent.mkdir(parents=True, exist_ok=True)
document.set_toc([
    [1, "Les consignes", 2], [1, "Le code", 3], [1, "Le jeu en images", 4],
    [1, "Sauvegarder et vérifier", 9], [1, "GitHub et Trello", 10],
])
document.save(SORTIE, garbage=4, deflate=True)
document.close()
verification = BASE / "tmp/pdfs/verification"
verification.mkdir(parents=True, exist_ok=True)
with fitz.open(SORTIE) as resultat:
    for i, p in enumerate(resultat):
        p.get_pixmap(matrix=fitz.Matrix(1.3, 1.3)).save(verification / f"page-{i+1:02d}.png")
print("Rapport créé :", SORTIE)
print("10 pages. Les captures, le tableau Kanban local et les liens sont intégrés.")
