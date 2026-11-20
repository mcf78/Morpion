import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/** Rend les vrais composants Swing hors écran pour illustrer la documentation. */
public class CapturerInterface {
    public static void main(String[] args) throws Exception {
        Path dossier = Path.of("docs", "images");
        Files.createDirectories(dossier);
        SwingUtilities.invokeAndWait(() -> {
            HorlogeMorpion jeu = new HorlogeMorpion();
            try {
                ((JTextField) nomme(jeu, "nomX")).setText("Moncef");
                capturer(jeu, dossier.resolve("01-accueil.png"));
                cliquer(jeu, "rejouer");
                for (int index : new int[]{0, 4, 2, 1}) cliquer(jeu, "case-" + index);
                capturer(jeu, dossier.resolve("02-partie.png"));
                cliquer(jeu, "rejouer");
                for (int index : new int[]{0, 3, 1, 4, 2}) cliquer(jeu, "case-" + index);
                capturer(jeu, dossier.resolve("03-victoire.png"));
                cliquer(jeu, "ordinateur");
                cliquer(jeu, "rejouer");
                cliquer(jeu, "case-0");
                capturer(jeu, dossier.resolve("04-ordinateur.png"));
                // Rejouer conserve le mode courant. Pour modifier les options, on
                // termine cette manche de démonstration avant d'en lancer une autre.
                Path temporaire = Files.createTempFile(Path.of("build"), "demo-", ".morpion");
                Partie vide = new Partie();
                Sauvegarde.enregistrer(temporaire, vide, "Moncef", "Joueur 2", false, true, 10_000);
                jeu.charger(temporaire);
                Files.deleteIfExists(temporaire);
                jeu.maintenant += 10000;
                jeu.verifierChronometre();
                capturer(jeu, dossier.resolve("05-temps-ecoule.png"));
                System.out.println("5 captures de l'interface Swing enregistrées dans docs/images.");
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            } finally {
                jeu.arreter();
            }
        });
    }

    private static void capturer(Morpion jeu, Path chemin) throws Exception {
        jeu.setSize(800, 660);
        disposer(jeu);
        BufferedImage image = new BufferedImage(1600, 1320, BufferedImage.TYPE_INT_RGB);
        Graphics2D dessin = image.createGraphics();
        dessin.scale(2, 2);
        dessin.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        jeu.printAll(dessin);
        dessin.dispose();
        ImageIO.write(image, "png", chemin.toFile());
    }

    private static void disposer(Container parent) {
        parent.doLayout();
        for (Component composant : parent.getComponents()) {
            if (composant instanceof Container enfant) disposer(enfant);
        }
    }

    private static void cliquer(Container parent, String commande) {
        AbstractButton bouton = action(parent, commande);
        if (bouton == null || !bouton.isEnabled()) throw new AssertionError(commande);
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

    private static class HorlogeMorpion extends Morpion {
        private static final long serialVersionUID = 1L;
        long maintenant;

        @Override
        long heureActuelle() {
            return maintenant;
        }
    }
}
