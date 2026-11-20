/**
 * Regles d'une partie de morpion a deux joueurs, sans dependance graphique.
 * Les lignes et les colonnes sont numerotees de 0 a 2.
 */
public class Partie {
    public static final char VIDE = ' ';

    private final char[][] grille = new char[3][3];
    private final boolean[][] casesGagnantes = new boolean[3][3];
    private final int[] historique = new int[9];
    private char joueurCourant;
    private char gagnant;
    private int nombreCoups;
    private boolean perdueAuTemps;

    public Partie() {
        recommencer();
    }

    public char getCase(int ligne, int colonne) {
        verifierCoordonnees(ligne, colonne);
        return grille[ligne][colonne];
    }

    public char getJoueurCourant() {
        return joueurCourant;
    }

    public char getGagnant() {
        return gagnant;
    }

    public boolean estTerminee() {
        return gagnant != VIDE || nombreCoups == 9;
    }

    public boolean estMatchNul() {
        return nombreCoups == 9 && gagnant == VIDE;
    }

    public int getNombreCoups() {
        return nombreCoups;
    }

    /** Retourne une copie des cases jouees, numerotees de 0 a 8. */
    public int[] getHistorique() {
        int[] copie = new int[nombreCoups];
        System.arraycopy(historique, 0, copie, 0, nombreCoups);
        return copie;
    }

    /** Le joueur courant perd si la partie est encore en cours. */
    public void perdreAuTemps() {
        if (!estTerminee()) {
            gagnant = joueurCourant == 'X' ? 'O' : 'X';
            perdueAuTemps = true;
        }
    }

    public boolean estPerdueAuTemps() {
        return perdueAuTemps;
    }

    /**
     * Pose le symbole du joueur courant si le coup est autorise.
     * Un refus ne change aucun element de la partie.
     */
    public boolean jouer(int ligne, int colonne) {
        verifierCoordonnees(ligne, colonne);
        if (estTerminee() || grille[ligne][colonne] != VIDE) {
            return false;
        }

        grille[ligne][colonne] = joueurCourant;
        historique[nombreCoups] = ligne * 3 + colonne;
        nombreCoups++;
        chercherAlignements();

        if (!estTerminee()) {
            joueurCourant = joueurCourant == 'X' ? 'O' : 'X';
        }
        return true;
    }

    /** Remet la grille a zero. Le joueur X commence toujours. */
    public void recommencer() {
        for (int ligne = 0; ligne < 3; ligne++) {
            for (int colonne = 0; colonne < 3; colonne++) {
                grille[ligne][colonne] = VIDE;
                casesGagnantes[ligne][colonne] = false;
            }
        }
        joueurCourant = 'X';
        gagnant = VIDE;
        nombreCoups = 0;
        perdueAuTemps = false;
    }

    public boolean estCaseGagnante(int ligne, int colonne) {
        verifierCoordonnees(ligne, colonne);
        return casesGagnantes[ligne][colonne];
    }

    private void chercherAlignements() {
        for (int index = 0; index < 3; index++) {
            marquerAlignement(index, 0, index, 1, index, 2);
            marquerAlignement(0, index, 1, index, 2, index);
        }
        marquerAlignement(0, 0, 1, 1, 2, 2);
        marquerAlignement(0, 2, 1, 1, 2, 0);
    }

    private void marquerAlignement(int ligne1, int colonne1,
                                   int ligne2, int colonne2,
                                   int ligne3, int colonne3) {
        if (grille[ligne1][colonne1] == joueurCourant
                && grille[ligne2][colonne2] == joueurCourant
                && grille[ligne3][colonne3] == joueurCourant) {
            gagnant = joueurCourant;
            casesGagnantes[ligne1][colonne1] = true;
            casesGagnantes[ligne2][colonne2] = true;
            casesGagnantes[ligne3][colonne3] = true;
        }
    }

    private void verifierCoordonnees(int ligne, int colonne) {
        if (ligne < 0 || ligne >= 3 || colonne < 0 || colonne >= 3) {
            throw new IllegalArgumentException("La ligne et la colonne doivent etre comprises entre 0 et 2.");
        }
    }
}
