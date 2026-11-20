import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** Point d'entrée, comme dans le convertisseur réalisé en cours. */
public class Gestion {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Morpion jeu = new Morpion();
            JFrame fenetre = new JFrame("TP Jeu Morpion 2027");
            fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            fenetre.setContentPane(jeu);
            fenetre.setResizable(false);
            fenetre.pack();
            fenetre.setLocationRelativeTo(null);
            fenetre.setVisible(true);
        });
    }
}
