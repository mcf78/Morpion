import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/** Quelques scenarios pour verifier l'ordinateur et les fichiers. */
public class TestServices {
    private static int controles;

    public static void main(String[] args) throws IOException {
        testerOrdinateur();
        Path dossier = Files.createTempDirectory("morpion-tests-");
        Path fichier = dossier.resolve("partie.morpion");
        try {
            testerSauvegardes(fichier);
            testerFichiersInvalides(fichier);
        } finally {
            Files.deleteIfExists(fichier);
            Files.deleteIfExists(dossier);
        }
        System.out.println("TestServices OK : " + controles + " controles (ordinateur et sauvegarde).");
    }

    private static void testerOrdinateur() {
        Partie partie = jouer(0, 4, 1);
        int[] avant = partie.getHistorique();
        // Le resultat est aleatoire : on verifie la regle, pas une position precise.
        for (int i = 0; i < 20; i++) {
            int choix = Ordinateur.choisirCoup(partie);
            verifier(choix >= 0 && choix < 9, "Une position existe.");
            verifier(partie.getCase(choix / 3, choix % 3) == Partie.VIDE, "La case choisie est libre.");
        }
        verifier(Arrays.equals(avant, partie.getHistorique()), "Choisir ne modifie pas la partie.");
        verifier(partie.getJoueurCourant() == 'O', "Choisir ne change pas le joueur.");
        verifier(Ordinateur.choisirCoup(jouer(0, 3, 1, 4, 2)) == -1, "Pas de coup apres une victoire.");
        verifier(Ordinateur.choisirCoup(jouer(0, 1, 2, 4, 3, 5, 7, 6, 8)) == -1, "Pas de coup apres un nul.");
        partie.perdreAuTemps();
        verifier(Ordinateur.choisirCoup(partie) == -1, "Pas de coup apres une defaite au temps.");
        partie = jouer(0, 1, 2, 4, 3, 5, 7, 6);
        verifier(Ordinateur.choisirCoup(partie) == 8, "La derniere case libre est choisie.");
    }

    private static void testerSauvegardes(Path fichier) throws IOException {
        for (boolean ordinateur : new boolean[]{false, true}) {
            for (boolean chrono : new boolean[]{false, true}) {
                Partie partie = jouer(0, 4, 2);
                long temps = chrono ? 4_321 : 10_000;
                Sauvegarde.enregistrer(fichier, partie, "Moncef", "Zoé", ordinateur, chrono, temps);
                Sauvegarde.Donnees lue = Sauvegarde.charger(fichier);
                verifier(lue.getNomX().equals("Moncef") && lue.getNomO().equals("Zoé"), "Noms et accents conserves.");
                verifier(lue.isContreOrdinateur() == ordinateur && lue.isChronometre() == chrono, "Modes conserves.");
                verifier(lue.getTempsRestantMs() == temps, "Temps restant conserve.");
                comparer(partie, lue.getPartie());
            }
        }
        Partie victoire = jouer(0, 3, 1, 4, 2);
        Sauvegarde.enregistrer(fichier, victoire, "X", "O", false, false, 10_000);
        comparer(victoire, Sauvegarde.charger(fichier).getPartie());
        Partie nul = jouer(0, 1, 2, 4, 3, 5, 7, 6, 8);
        Sauvegarde.enregistrer(fichier, nul, "X", "O", false, true, 500);
        comparer(nul, Sauvegarde.charger(fichier).getPartie());
        Partie tropLent = jouer(0, 4, 1);
        tropLent.perdreAuTemps();
        Sauvegarde.enregistrer(fichier, tropLent, "X", "Ordinateur", true, true, 0);
        comparer(tropLent, Sauvegarde.charger(fichier).getPartie());
        verifier(Sauvegarde.charger(fichier).getTempsRestantMs() == 0, "Temps nul conserve apres expiration.");
        try {
            Sauvegarde.enregistrer(fichier, new Partie(), " ", "O", false, false, 10_000);
            throw new AssertionError("Le nom vide doit etre refuse.");
        } catch (IOException attendu) {
            controles++;
        }
    }

    private static void testerFichiersInvalides(Path fichier) throws IOException {
        Sauvegarde.enregistrer(fichier, jouer(0, 4, 1), "X", "O", false, true, 1_234);
        String valide = Files.readString(fichier, StandardCharsets.UTF_8);
        refuser(fichier, "fichier incomplet");
        refuser(fichier, valide.replace("version=1", "version=2"));
        refuser(fichier, valide.replace("mode.ordinateur=false", "mode.ordinateur=oui"));
        refuser(fichier, valide.replace("nom.x=X", "nom.x="));
        refuser(fichier, valide.replace("nom.x=X", "nom.x=\\n"));
        refuser(fichier, valide.replace("nom.x=X", "nom.x=" + "X".repeat(31)));
        refuser(fichier, valide.replace("coups=0,4,1", "coups=0,0"));
        refuser(fichier, valide.replace("coups=0,4,1", "coups=0,3,1,4,2,5"));
        refuser(fichier, valide.replace("coups=0,4,1", "coups=9"));
        refuser(fichier, valide.replace("coups=0,4,1", "coups=0,"));
        refuser(fichier, valide.replace("temps.restant.ms=1234", "temps.restant.ms=0"));
        refuser(fichier, valide.replace("temps.restant.ms=1234", "temps.restant.ms=-1"));
        refuser(fichier, valide.replace("temps.restant.ms=1234", "temps.restant.ms=10001"));
        refuser(fichier, valide.replace("temps.restant.ms=1234", "temps.restant.ms=inconnu"));
        refuser(fichier, valide.replace("fin.temps=false", "fin.temps=true"));
        refuser(fichier, valide.replace("mode.chronometre=true", "mode.chronometre=false"));
        Sauvegarde.enregistrer(fichier, jouer(0, 3, 1, 4, 2), "X", "O", false, true, 500);
        String terminee = Files.readString(fichier, StandardCharsets.UTF_8);
        refuser(fichier, terminee.replace("fin.temps=false", "fin.temps=true"));
    }

    private static void refuser(Path fichier, String texte) throws IOException {
        Files.writeString(fichier, texte, StandardCharsets.UTF_8);
        try {
            Sauvegarde.charger(fichier);
            throw new AssertionError("Le fichier invalide doit etre refuse.");
        } catch (IOException attendu) {
            controles++;
        }
    }

    private static Partie jouer(int... coups) {
        Partie partie = new Partie();
        for (int index : coups) {
            if (!partie.jouer(index / 3, index % 3)) throw new AssertionError("Scenario incorrect.");
        }
        return partie;
    }

    private static void comparer(Partie attendue, Partie lue) {
        verifier(Arrays.equals(attendue.getHistorique(), lue.getHistorique()), "Historique conserve.");
        verifier(attendue.getJoueurCourant() == lue.getJoueurCourant(), "Joueur conserve.");
        verifier(attendue.getGagnant() == lue.getGagnant(), "Gagnant conserve.");
        verifier(attendue.estTerminee() == lue.estTerminee(), "Fin conservee.");
        verifier(attendue.estMatchNul() == lue.estMatchNul(), "Nul conserve.");
        verifier(attendue.estPerdueAuTemps() == lue.estPerdueAuTemps(), "Expiration conservee.");
        for (int i = 0; i < 9; i++) {
            verifier(attendue.getCase(i / 3, i % 3) == lue.getCase(i / 3, i % 3), "Case conservee.");
        }
    }

    private static void verifier(boolean condition, String message) {
        controles++;
        if (!condition) throw new AssertionError(message);
    }
}
