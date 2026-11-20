import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/** Scenarios sur les vrais boutons Swing, sans ouvrir de fenetre. */
public class TestInterfaceMorpion {
    private static int controles;

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                testerReglesEtRejouer();
                testerNomsEtModes();
                testerChronometre();
                testerFichiersEtOrdinateur();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
        System.out.println("TestInterfaceMorpion OK : " + controles
                + " controles (boutons, noms, temps, fichiers et ordinateur).");
    }

    private static void testerReglesEtRejouer() {
        Morpion jeu = new Morpion();
        try {
            verifier(casesJouables(jeu) == 0, "Pas de coup avant Demarrer.");
            verifier(!action(jeu, "sauvegarder").isEnabled(), "Pas de sauvegarde avant Demarrer.");
            champ(jeu, "nomX").setText("Moncef");
            champ(jeu, "nomO").setText("Alice");
            cliquer(jeu, "rejouer");
            verifier(casesJouables(jeu) == 9, "Les neuf cases sont disponibles.");
            verifier(!champ(jeu, "nomX").isEnabled(), "Options bloquees pendant la partie.");
            coups(jeu, 0, 3, 1, 4, 2);
            verifier(label(jeu, "statut").contains("Moncef a gagné"), "Victoire de X.");
            verifier(casesJouables(jeu) == 0, "Aucun coup apres la victoire.");
            verifier(historique(jeu).contains("(1, 3)"), "Coordonnees humaines dans le journal.");
            String avant = historique(jeu);
            action(jeu, "case-8").doClick(0);
            verifier(historique(jeu).equals(avant), "Un clic apres la fin reste sans effet.");

            cliquer(jeu, "rejouer");
            verifier(casesJouables(jeu) == 9 && label(jeu, "statut").contains("Moncef (X)"), "Rejouer remet X au debut.");
            coups(jeu, 0, 3, 1, 4, 8, 5);
            verifier(label(jeu, "statut").contains("Alice a gagné"), "Victoire de O.");
            cliquer(jeu, "rejouer");
            coups(jeu, 0, 1, 2, 4, 3, 5, 7, 6, 8);
            verifier(label(jeu, "statut").equals("Match nul !"), "Match nul.");
            verifier(casesJouables(jeu) == 0, "Grille bloquee apres le nul.");
        } finally {
            jeu.arreter();
        }
    }

    private static void testerNomsEtModes() {
        Morpion jeu = new Morpion();
        try {
            champ(jeu, "nomO").setText("Alice");
            cliquer(jeu, "deux-joueurs");
            verifier(champ(jeu, "nomO").getText().equals("Alice"), "Recliquer le mode conserve le nom.");
            cliquer(jeu, "ordinateur");
            verifier(!champ(jeu, "nomO").isEnabled(), "Nom ordinateur non modifiable.");
            cliquer(jeu, "ordinateur");
            cliquer(jeu, "deux-joueurs");
            verifier(champ(jeu, "nomO").getText().equals("Alice"), "Retour au nom humain.");
            champ(jeu, "nomX").setText("W".repeat(35));
            cliquer(jeu, "rejouer");
            verifier(champ(jeu, "nomX").getText().equals("W".repeat(30)), "Nom limite a trente caracteres.");
            verifier(label(jeu, "statut").contains("W".repeat(30)), "Nom complet dans le statut.");
            coups(jeu, 0, 3, 1, 4, 2);
            champ(jeu, "nomX").setText("\u2003\u2003");
            cliquer(jeu, "rejouer");
            verifier(champ(jeu, "nomX").getText().equals("Joueur 1"), "Nom blanc remplace par defaut.");
        } finally {
            jeu.arreter();
        }
    }

    private static void testerChronometre() {
        HorlogeMorpion jeu = new HorlogeMorpion();
        try {
            cliquer(jeu, "avec-chrono");
            cliquer(jeu, "rejouer");
            jeu.maintenant = 9_999;
            jeu.verifierChronometre();
            verifier(casesJouables(jeu) == 9, "Toujours actif juste avant dix secondes.");
            verifier(label(jeu, "temps").contains("1 s"), "Secondes arrondies au superieur.");
            cliquer(jeu, "case-0");
            verifier(label(jeu, "temps").contains("10 s"), "Dix secondes pour le joueur suivant.");
            jeu.maintenant += 10_000;
            // Le clic doit verifier la limite meme avant le prochain tick.
            action(jeu, "case-1").doClick(0);
            verifier(label(jeu, "statut").contains("Joueur 1 a gagné"), "Le joueur O perd a dix secondes.");
            verifier(label(jeu, "detail").contains("Temps dépassé"), "Cause de fin affichee.");
            verifier(((JButton) action(jeu, "case-1")).getText().isEmpty(), "Coup hors delai refuse.");
            verifier(casesJouables(jeu) == 0, "Partie bloquee apres expiration.");
            cliquer(jeu, "sans-chrono");
            cliquer(jeu, "rejouer");
            jeu.maintenant += 60_000;
            jeu.verifierChronometre();
            verifier(casesJouables(jeu) == 9, "Pas de delai en mode sans chrono.");
        } finally {
            jeu.arreter();
        }
    }

    private static void testerFichiersEtOrdinateur() throws IOException {
        Path dossier = Files.createTempDirectory("morpion-interface-");
        Path fichier = dossier.resolve("partie.morpion");
        Path mauvais = dossier.resolve("invalide.morpion");
        HorlogeMorpion source = new HorlogeMorpion();
        HorlogeMorpion cible = new HorlogeMorpion();
        try {
            champ(source, "nomO").setText("Zoé");
            cliquer(source, "avec-chrono");
            cliquer(source, "rejouer");
            coups(source, 0, 4);
            source.maintenant = 3_590;
            source.sauvegarder(fichier);
            verifier(Sauvegarde.charger(fichier).getTempsRestantMs() == 6_410, "Temps en millisecondes enregistre.");
            cible.maintenant = 100_000;
            cible.charger(fichier);
            verifier(historique(cible).equals(historique(source)), "Historique restaure.");
            verifier(champ(cible, "nomO").getText().equals("Zoé"), "Nom accentue restaure.");
            verifier(casesJouables(cible) == 7 && label(cible, "temps").contains("7 s"), "Grille et temps repris.");

            String avant = historique(cible);
            Files.writeString(mauvais, "fichier incomplet", StandardCharsets.UTF_8);
            try {
                cible.charger(mauvais);
                throw new AssertionError("Le fichier invalide doit etre refuse.");
            } catch (IOException attendu) {
                verifier(historique(cible).equals(avant), "Fichier invalide laisse la partie intacte.");
            }
            cible.maintenant += 6_409;
            cible.verifierChronometre();
            verifier(casesJouables(cible) == 7, "Actif a une milliseconde de la fin.");
            cible.maintenant++;
            cible.verifierChronometre();
            verifier(label(cible, "statut").contains("Zoé a gagné"), "Expiration du temps repris.");
            cible.sauvegarder(fichier);
            source.charger(fichier);
            verifier(label(source, "detail").contains("Temps dépassé"), "Fin au temps rechargeable.");

            cliquer(source, "rejouer");
            source.maintenant += 10_000;
            source.sauvegarder(fichier);
            Sauvegarde.Donnees lue = Sauvegarde.charger(fichier);
            verifier(lue.getPartie().estPerdueAuTemps() && lue.getTempsRestantMs() == 0,
                    "Sauvegarder a la limite constate la defaite.");

            // L'ordinateur repond directement, sans attente artificielle.
            cliquer(source, "ordinateur");
            cliquer(source, "sans-chrono");
            cliquer(source, "rejouer");
            cliquer(source, "case-0");
            verifier(casesJouables(source) == 7, "Un clic humain est suivi d'un coup ordinateur.");
            verifier(historique(source).contains("Ordinateur (O)"), "Coup ordinateur dans le journal.");
            verifier(label(source, "statut").contains("(X)"), "Tour rendu a l'humain.");

            Partie tourO = new Partie();
            tourO.jouer(0, 0);
            Sauvegarde.enregistrer(fichier, tourO, "Moncef", "Ordinateur", true, false, 10_000);
            cible.charger(fichier);
            verifier(casesJouables(cible) == 7, "Reprise d'un tour O declenche son coup.");
            verifier(((JButton) action(cible, "case-0")).getText().equals("X"), "Coup humain conserve.");
            verifier(label(cible, "statut").contains("Moncef (X)"), "Tour X apres reprise ordinateur.");
        } finally {
            source.arreter();
            cible.arreter();
            Files.deleteIfExists(fichier);
            Files.deleteIfExists(mauvais);
            Files.deleteIfExists(dossier);
        }
    }

    /** Une horloge de test : avancer le nombre remplace l'attente reelle. */
    private static class HorlogeMorpion extends Morpion {
        private static final long serialVersionUID = 1L;
        long maintenant;
        @Override long heureActuelle() { return maintenant; }
    }

    private static void coups(Morpion jeu, int... positions) {
        for (int position : positions) cliquer(jeu, "case-" + position);
    }

    private static void cliquer(Morpion jeu, String commande) {
        AbstractButton bouton = action(jeu, commande);
        verifier(bouton != null && bouton.isEnabled(), "Commande disponible : " + commande);
        bouton.doClick(0);
    }

    private static AbstractButton action(Container parent, String commande) {
        for (Component composant : parent.getComponents()) {
            if (composant instanceof AbstractButton bouton && commande.equals(bouton.getActionCommand())) return bouton;
            if (composant instanceof Container enfant) {
                AbstractButton trouve = action(enfant, commande);
                if (trouve != null) return trouve;
            }
        }
        return null;
    }

    private static Component nomme(Container parent, String nom) {
        for (Component composant : parent.getComponents()) {
            if (nom.equals(composant.getName())) return composant;
            if (composant instanceof Container enfant) {
                Component trouve = nomme(enfant, nom);
                if (trouve != null) return trouve;
            }
        }
        return null;
    }

    private static JTextField champ(Morpion jeu, String nom) { return (JTextField) nomme(jeu, nom); }
    private static String label(Morpion jeu, String nom) { return ((JLabel) nomme(jeu, nom)).getText(); }
    private static String historique(Morpion jeu) { return ((JTextArea) nomme(jeu, "historique")).getText(); }
    private static int casesJouables(Morpion jeu) {
        int nombre = 0;
        for (int i = 0; i < 9; i++) if (action(jeu, "case-" + i).isEnabled()) nombre++;
        return nombre;
    }
    private static void verifier(boolean condition, String message) {
        controles++;
        if (!condition) throw new AssertionError(message);
    }
}
