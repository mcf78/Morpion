import java.util.Arrays;

/** Tests simples du moteur, sans bibliotheque externe. */
public class TestPartie {
    private static int controles;

    public static void main(String[] args) {
        testerVictoires();
        testerMatchNulEtRefus();
        testerHistoriqueEtRecommencer();
        testerCoordonnees();
        testerTemps();
        System.out.println("Partie : " + controles + " controles reussis.");
    }

    private static void testerVictoires() {
        int[][] victoiresX = {
            {0, 3, 1, 4, 2}, {3, 0, 4, 1, 5}, {6, 0, 7, 1, 8},
            {0, 1, 3, 2, 6}, {1, 0, 4, 2, 7}, {2, 0, 5, 1, 8},
            {0, 1, 4, 2, 8}, {2, 0, 4, 1, 6}
        };
        int[][] victoiresO = {
            {3, 0, 4, 1, 6, 2}, {0, 3, 1, 4, 6, 5}, {0, 6, 1, 7, 3, 8},
            {1, 0, 2, 3, 4, 6}, {0, 1, 2, 4, 3, 7}, {0, 2, 1, 5, 3, 8},
            {1, 0, 2, 4, 3, 8}, {0, 2, 1, 4, 3, 6}
        };
        for (int[] coups : victoiresX) testerVictoire(coups, 'X');
        for (int[] coups : victoiresO) testerVictoire(coups, 'O');
    }

    private static void testerVictoire(int[] coups, char gagnant) {
        Partie partie = jouer(coups);
        verifier(partie.estTerminee() && !partie.estMatchNul(), "Victoire attendue");
        verifier(partie.getGagnant() == gagnant, "Bon gagnant");
        for (int i = gagnant == 'X' ? 0 : 1; i < coups.length; i += 2) {
            verifier(partie.estCaseGagnante(coups[i] / 3, coups[i] % 3), "Case gagnante");
        }
        verifier(!partie.jouer(2, 2), "Coup refuse apres la victoire");
        verifier(partie.getNombreCoups() == coups.length, "Compteur conserve apres refus");
        verifier(Arrays.equals(partie.getHistorique(), coups), "Historique conserve apres refus");
        partie.perdreAuTemps();
        verifier(partie.getGagnant() == gagnant && !partie.estPerdueAuTemps(), "Victoire conservee");
    }

    private static void testerMatchNulEtRefus() {
        Partie partie = jouer(0, 1, 2, 4, 3, 5, 7, 6, 8);
        verifier(partie.estTerminee() && partie.estMatchNul(), "Match nul");
        verifier(partie.getGagnant() == Partie.VIDE, "Aucun gagnant au nul");
        verifier(!partie.jouer(0, 0) && partie.getNombreCoups() == 9, "Coup refuse apres le nul");
        partie.perdreAuTemps();
        verifier(partie.estMatchNul() && !partie.estPerdueAuTemps(), "Nul conserve apres expiration");
        partie = jouer(4);
        verifier(!partie.jouer(1, 1), "Case occupee refusee");
        verifier(partie.getCase(1, 1) == 'X' && partie.getNombreCoups() == 1, "Case et compteur conserves");
        verifier(partie.getJoueurCourant() == 'O', "Un refus ne change pas le joueur");
    }

    private static void testerHistoriqueEtRecommencer() {
        Partie partie = jouer(8, 0, 4);
        verifier(Arrays.equals(partie.getHistorique(), new int[] {8, 0, 4}), "Ordre des coups");
        int[] copie = partie.getHistorique();
        copie[0] = 7;
        verifier(partie.getHistorique()[0] == 8, "La copie ne modifie pas la partie");
        partie.perdreAuTemps();
        partie.recommencer();
        verifier(partie.getJoueurCourant() == 'X' && partie.getNombreCoups() == 0, "X recommence");
        verifier(!partie.estTerminee() && !partie.estPerdueAuTemps(), "Partie reactivee");
        verifier(partie.getGagnant() == Partie.VIDE && partie.getHistorique().length == 0, "Resultat efface");
        for (int i = 0; i < 9; i++) {
            verifier(partie.getCase(i / 3, i % 3) == Partie.VIDE, "Grille effacee");
            verifier(!partie.estCaseGagnante(i / 3, i % 3), "Aucune case gagnante");
        }
    }

    private static void testerCoordonnees() {
        Partie partie = new Partie();
        int[][] invalides = {{-1, 0}, {3, 0}, {0, -1}, {0, 3}};
        for (int[] position : invalides) {
            for (int operation = 0; operation < 3; operation++) {
                boolean refusee = false;
                try {
                    if (operation == 0) partie.jouer(position[0], position[1]);
                    if (operation == 1) partie.getCase(position[0], position[1]);
                    if (operation == 2) partie.estCaseGagnante(position[0], position[1]);
                } catch (IllegalArgumentException exception) {
                    refusee = true;
                }
                verifier(refusee, "Coordonnees invalides refusees");
            }
        }
        verifier(partie.getNombreCoups() == 0, "Pas de coup apres des coordonnees invalides");
    }

    private static void testerTemps() {
        Partie[] parties = {new Partie(), jouer(4)};
        for (Partie partie : parties) {
            char gagnant = partie.getJoueurCourant() == 'X' ? 'O' : 'X';
            int[] avant = partie.getHistorique();
            partie.perdreAuTemps();
            verifier(partie.estTerminee() && partie.estPerdueAuTemps(), "Defaite au temps");
            verifier(partie.getGagnant() == gagnant, "L'adversaire gagne au temps");
            verifier(!partie.jouer(0, 0), "Aucun coup apres expiration");
            verifier(Arrays.equals(partie.getHistorique(), avant), "Le temps n'ajoute aucun coup");
            for (int i = 0; i < 9; i++) {
                verifier(!partie.estCaseGagnante(i / 3, i % 3), "Le temps ne cree pas d'alignement");
            }
            partie.perdreAuTemps();
            verifier(partie.getGagnant() == gagnant, "Une seconde expiration ne change rien");
        }
    }

    private static Partie jouer(int... coups) {
        Partie partie = new Partie();
        for (int coup : coups) verifier(partie.jouer(coup / 3, coup % 3), "Coup autorise");
        return partie;
    }

    private static void verifier(boolean condition, String message) {
        controles++;
        if (!condition) throw new AssertionError(message);
    }
}
