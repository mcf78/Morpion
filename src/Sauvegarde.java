import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Enregistre les informations de la partie dans un fichier texte UTF-8. */
public class Sauvegarde {
    public static void enregistrer(Path fichier, Partie partie, String nomX, String nomO,
            boolean ordinateur, boolean chrono, long temps) throws IOException {
        if (partie == null) throw new IOException("La partie est absente.");
        verifierNom(nomX);
        verifierNom(nomO);
        verifierTemps(partie, chrono, temps);

        Properties donnees = new Properties();
        donnees.setProperty("version", "1");
        donnees.setProperty("nom.x", nomX);
        donnees.setProperty("nom.o", nomO);
        donnees.setProperty("mode.ordinateur", Boolean.toString(ordinateur));
        donnees.setProperty("mode.chronometre", Boolean.toString(chrono));
        donnees.setProperty("temps.restant.ms", Long.toString(temps));
        donnees.setProperty("fin.temps", Boolean.toString(partie.estPerdueAuTemps()));
        String coups = "";
        for (int index : partie.getHistorique()) {
            if (!coups.isEmpty()) coups += ",";
            coups += index;
        }
        donnees.setProperty("coups", coups);
        try (BufferedWriter sortie = Files.newBufferedWriter(fichier, StandardCharsets.UTF_8)) {
            donnees.store(sortie, "Morpion - sauvegarde version 1");
        }
    }

    public static Donnees charger(Path fichier) throws IOException {
        Properties donnees = new Properties();
        try (BufferedReader entree = Files.newBufferedReader(fichier, StandardCharsets.UTF_8)) {
            donnees.load(entree);
        } catch (IllegalArgumentException exception) {
            throw new IOException("Le fichier de sauvegarde est invalide.", exception);
        }
        String[] cles = {"version", "nom.x", "nom.o", "mode.ordinateur",
                "mode.chronometre", "temps.restant.ms", "fin.temps", "coups"};
        for (String cle : cles) {
            if (donnees.getProperty(cle) == null) {
                throw new IOException("Information absente : " + cle);
            }
        }
        if (!donnees.getProperty("version").equals("1")) {
            throw new IOException("Version de sauvegarde inconnue.");
        }
        String nomX = donnees.getProperty("nom.x");
        String nomO = donnees.getProperty("nom.o");
        verifierNom(nomX);
        verifierNom(nomO);
        boolean ordinateur = lireBooleen(donnees, "mode.ordinateur");
        boolean chrono = lireBooleen(donnees, "mode.chronometre");
        boolean perteAuTemps = lireBooleen(donnees, "fin.temps");
        long temps;
        try {
            temps = Long.parseLong(donnees.getProperty("temps.restant.ms"));
        } catch (NumberFormatException exception) {
            throw new IOException("Le temps restant est invalide.", exception);
        }

        // Rejouer les coups permet de retrouver la grille et le bon joueur.
        Partie partie = new Partie();
        String historique = donnees.getProperty("coups");
        if (!historique.isEmpty()) {
            String[] coups = historique.split(",", -1);
            if (coups.length > 9) throw new IOException("Il y a trop de coups.");
            for (String coup : coups) {
                if (!coup.matches("[0-8]")) throw new IOException("Une case est invalide.");
                int index = Integer.parseInt(coup);
                if (!partie.jouer(index / 3, index % 3)) {
                    throw new IOException("L'historique contient un coup interdit.");
                }
            }
        }
        if (perteAuTemps) {
            if (partie.estTerminee()) throw new IOException("La partie est deja terminee.");
            partie.perdreAuTemps();
        }
        verifierTemps(partie, chrono, temps);
        return new Donnees(partie, nomX, nomO, ordinateur, chrono, temps);
    }

    private static void verifierNom(String nom) throws IOException {
        if (nom == null || nom.isBlank() || nom.length() > 30) {
            throw new IOException("Un nom doit contenir de 1 a 30 caracteres.");
        }
        for (int i = 0; i < nom.length(); i++) {
            if (Character.isISOControl(nom.charAt(i))) {
                throw new IOException("Un nom ne peut pas contenir de retour a la ligne.");
            }
        }
    }

    private static boolean lireBooleen(Properties donnees, String cle) throws IOException {
        String valeur = donnees.getProperty(cle);
        if (!valeur.equals("true") && !valeur.equals("false")) {
            throw new IOException("Le choix " + cle + " est invalide.");
        }
        return Boolean.parseBoolean(valeur);
    }

    private static void verifierTemps(Partie partie, boolean chrono, long temps) throws IOException {
        if (temps < 0 || temps > 10_000) throw new IOException("Le temps doit etre entre 0 et 10000 ms.");
        if (!chrono && (temps != 10_000 || partie.estPerdueAuTemps())) {
            throw new IOException("Temps incoherent pour une partie sans chronometre.");
        }
        if (partie.estPerdueAuTemps() && (!chrono || temps != 0)) {
            throw new IOException("Une defaite au temps doit avoir un temps nul.");
        }
        if (chrono && !partie.estTerminee() && temps == 0) {
            throw new IOException("Une partie en cours doit avoir du temps restant.");
        }
    }

    /** Regroupe les informations lues avant de les donner a l'interface. */
    public static class Donnees {
        private final Partie partie;
        private final String nomX;
        private final String nomO;
        private final boolean ordinateur;
        private final boolean chrono;
        private final long temps;

        private Donnees(Partie partie, String nomX, String nomO, boolean ordinateur, boolean chrono, long temps) {
            this.partie = partie;
            this.nomX = nomX;
            this.nomO = nomO;
            this.ordinateur = ordinateur;
            this.chrono = chrono;
            this.temps = temps;
        }
        public Partie getPartie() { return partie; }
        public String getNomX() { return nomX; }
        public String getNomO() { return nomO; }
        public boolean isContreOrdinateur() { return ordinateur; }
        public boolean isChronometre() { return chrono; }
        public long getTempsRestantMs() { return temps; }
    }
}
