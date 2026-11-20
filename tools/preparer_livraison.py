"""Crée une archive autonome du projet, avec le JAR et le dossier PDF.

Exécuter après compiler.cmd et après génération du PDF :
    python tools/preparer_livraison.py
Python n'est pas nécessaire pour jouer au morpion.
"""

from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile


BASE = Path(__file__).resolve().parents[1]
DESTINATION = BASE.parent / "Morpion_livraison.zip"


def main():
    requis = [
        BASE / "dist" / "Morpion.jar",
        BASE / "output" / "pdf" / "Documentation_Morpion_MALLEM_Moncef.pdf",
        BASE / "README.md",
        BASE / "compiler.cmd",
        BASE / "lancer.cmd",
        BASE / "tester.cmd",
        BASE / ".gitignore",
        BASE / "Morpion.iml",
    ]
    for fichier in requis:
        if not fichier.is_file():
            raise FileNotFoundError(f"Livrable manquant : {fichier}")

    fichiers = set(requis)
    for dossier in ("src", "tests", "tools", "docs"):
        fichiers.update(
            fichier for fichier in (BASE / dossier).rglob("*")
            if fichier.is_file() and "__pycache__" not in fichier.parts
        )

    with ZipFile(DESTINATION, "w", ZIP_DEFLATED, compresslevel=9) as archive:
        for fichier in sorted(fichiers):
            relatif = fichier.relative_to(BASE).as_posix()
            archive.write(fichier, "Morpion/" + relatif)

    with ZipFile(DESTINATION) as archive:
        erreur = archive.testzip()
        if erreur:
            raise RuntimeError(f"Fichier endommagé dans l'archive : {erreur}")
        assert len(archive.namelist()) == len(fichiers)
        assert all("/.git/" not in nom and "/build/" not in nom for nom in archive.namelist())
    print(f"Archive vérifiée : {DESTINATION.name} ({len(fichiers)} fichiers)")


if __name__ == "__main__":
    main()
