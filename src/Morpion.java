import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.*;

/** Interface construite avec les composants et événements Swing du cours. */
public class Morpion extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    private Partie partie = new Partie();
    private final JButton[] cases = new JButton[9];
    private final JTextField nomX = new JTextField("Joueur 1");
    private final JTextField nomO = new JTextField("Joueur 2");
    private final JRadioButton deuxJoueurs = new JRadioButton("2 joueurs", true);
    private final JRadioButton ordinateur = new JRadioButton("Ordinateur");
    private final JRadioButton sansChrono = new JRadioButton("Sans", true);
    private final JRadioButton avecChrono = new JRadioButton("Avec : 10 secondes");
    private final JTextArea historique = new JTextArea();
    private final JLabel statut = new JLabel("Choisis les options, puis clique Démarrer.");
    private final JLabel detail = new JLabel("X commence. Il faut aligner trois symboles.");
    private final JLabel temps = new JLabel("Sans chronomètre");
    private final JLabel compteur = new JLabel("0 / 9 coups");
    private final JButton rejouer = new JButton("Démarrer");
    private final JButton sauver = new JButton("Sauvegarder");
    private final Timer minuterie = new Timer(100, this);
    private boolean commencee;
    private boolean contreOrdinateur;
    private boolean chronometre;
    private boolean suspendu;
    private long tempsRestantMs = 10000;
    private long finTour;
    private String joueurX = "Joueur 1";
    private String joueurO = "Joueur 2";
    private String dernierNomHumain = "Joueur 2";

    public Morpion() {
        // Les positions fixes reprennent le convertisseur réalisé en cours.
        setLayout(null);
        setPreferredSize(new Dimension(800, 660));
        setBackground(new Color(245, 247, 250));
        JLabel titre = new JLabel("TP Jeu Morpion 2027");
        placer(titre, 20, 15, 760, 35);
        titre.setFont(new Font("SansSerif", Font.BOLD, 24));
        placer(statut, 20, 58, 760, 25);
        statut.setFont(new Font("SansSerif", Font.BOLD, 17));
        placer(detail, 20, 85, 760, 22);
        placer(new JLabel("Nom J1 (X)"), 20, 120, 90, 28);
        placer(nomX, 110, 120, 250, 28);
        placer(new JLabel("Nom J2 (O)"), 20, 160, 90, 28);
        placer(nomO, 110, 160, 250, 28);
        nomX.setName("nomX");
        nomO.setName("nomO");
        nomX.setDisabledTextColor(Color.DARK_GRAY);
        nomO.setDisabledTextColor(Color.DARK_GRAY);
        statut.setName("statut");
        detail.setName("detail");
        temps.setName("temps");
        historique.setName("historique");

        ButtonGroup mode = new ButtonGroup();
        mode.add(deuxJoueurs);
        mode.add(ordinateur);
        placer(deuxJoueurs, 410, 120, 110, 28);
        placer(ordinateur, 530, 120, 180, 28);
        deuxJoueurs.setActionCommand("deux-joueurs");
        ordinateur.setActionCommand("ordinateur");
        deuxJoueurs.addActionListener(this);
        ordinateur.addActionListener(this);
        ButtonGroup choixTemps = new ButtonGroup();
        choixTemps.add(sansChrono);
        choixTemps.add(avecChrono);
        placer(new JLabel("Chronomètre :"), 410, 160, 110, 28);
        placer(sansChrono, 520, 160, 70, 28);
        placer(avecChrono, 590, 160, 180, 28);
        sansChrono.setActionCommand("sans-chrono");
        avecChrono.setActionCommand("avec-chrono");

        placer(new JLabel("Historique des coups (ligne, colonne)"), 20, 210, 350, 25);
        historique.setEditable(false);
        historique.setLineWrap(true);
        historique.setWrapStyleWord(true);
        historique.setFont(new Font("SansSerif", Font.PLAIN, 14));
        placer(new JScrollPane(historique), 20, 240, 340, 310);
        JPanel grille = new JPanel(new GridLayout(3, 3, 6, 6));
        for (int i = 0; i < 9; i++) {
            cases[i] = new JButton("");
            cases[i].setFont(new Font("SansSerif", Font.BOLD, 44));
            cases[i].setActionCommand("case-" + i);
            cases[i].addActionListener(this);
            grille.add(cases[i]);
        }
        placer(grille, 410, 210, 350, 350);
        placer(compteur, 20, 555, 340, 25);
        placer(temps, 410, 568, 350, 25);
        ajouterBouton(sauver, "sauvegarder", 20, 130);
        ajouterBouton(new JButton("Recharger"), "recharger", 160, 130);
        ajouterBouton(rejouer, "rejouer", 410, 170);
        ajouterBouton(new JButton("Quitter"), "quitter", 590, 170);
        actualiser();
    }

    private void placer(Component composant, int x, int y, int largeur, int hauteur) {
        composant.setBounds(x, y, largeur, hauteur);
        composant.setFont(new Font("SansSerif", Font.PLAIN, 14));
        add(composant);
    }

    private void ajouterBouton(JButton bouton, String commande, int x, int largeur) {
        placer(bouton, x, 608, largeur, 32);
        bouton.setActionCommand(commande);
        bouton.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent evenement) {
        if (evenement.getSource() == minuterie) {
            verifierChronometre();
            return;
        }
        String commande = evenement.getActionCommand();
        if (commande.startsWith("case-")) {
            verifierChronometre();
            int index = Integer.parseInt(commande.substring(5));
            if (commencee && !suspendu && !(contreOrdinateur && partie.getJoueurCourant() == 'O')
                    && partie.jouer(index / 3, index % 3)) {
                apresCoup();
                jouerOrdinateur();
            }
        } else if (commande.equals("rejouer")) {
            demarrer();
        } else if (commande.equals("sauvegarder")) {
            dialogueFichier(true);
        } else if (commande.equals("recharger")) {
            dialogueFichier(false);
        } else if (commande.equals("quitter")) {
            arreter();
            Window fenetre = SwingUtilities.getWindowAncestor(this);
            if (fenetre != null) fenetre.dispose();
        } else if (commande.equals("ordinateur") || commande.equals("deux-joueurs")) {
            if (ordinateur.isSelected() && nomO.isEnabled()) {
                dernierNomHumain = nomO.getText();
                nomO.setText("Ordinateur");
            } else if (deuxJoueurs.isSelected() && !nomO.isEnabled()) {
                nomO.setText(dernierNomHumain);
            }
            nomO.setEnabled(!ordinateur.isSelected());
        }
    }

    private void demarrer() {
        arreter();
        joueurX = preparerNom(nomX.getText(), "Joueur 1");
        contreOrdinateur = ordinateur.isSelected();
        joueurO = contreOrdinateur ? "Ordinateur" : preparerNom(nomO.getText(), "Joueur 2");
        nomX.setText(joueurX);
        nomO.setText(joueurO);
        chronometre = avecChrono.isSelected();
        partie = new Partie();
        commencee = true;
        suspendu = false;
        lancerTour(10000);
        actualiser();
    }

    private String preparerNom(String texte, String defaut) {
        String nom = texte.strip();
        if (nom.isEmpty()) return defaut;
        return nom.substring(0, Math.min(nom.length(), 30));
    }

    void jouerOrdinateur() {
        verifierChronometre();
        if (!commencee || suspendu || !contreOrdinateur || partie.estTerminee()
                || partie.getJoueurCourant() != 'O') return;
        int index = Ordinateur.choisirCoup(partie);
        verifierChronometre();
        if (!partie.estTerminee() && index >= 0 && partie.jouer(index / 3, index % 3)) apresCoup();
    }

    private void apresCoup() {
        if (partie.estTerminee()) arreter();
        else lancerTour(10000);
        actualiser();
    }

    // Une date de fin évite de rallonger le tour si un tick arrive en retard.
    private void lancerTour(long delaiMs) {
        tempsRestantMs = delaiMs;
        finTour = heureActuelle() + delaiMs;
        if (commencee && chronometre && !suspendu && !partie.estTerminee()) minuterie.start();
    }

    long heureActuelle() {
        return System.currentTimeMillis();
    }

    private long restant() {
        if (!chronometre) return 10000;
        if (suspendu || partie.estTerminee()) return tempsRestantMs;
        return Math.max(0, finTour - heureActuelle());
    }

    void verifierChronometre() {
        if (!commencee || suspendu || !chronometre || partie.estTerminee()) return;
        tempsRestantMs = restant();
        if (tempsRestantMs == 0) {
            partie.perdreAuTemps();
            arreter();
            actualiser();
        } else afficherTemps();
    }

    private void suspendre() {
        if (suspendu) return;
        verifierChronometre();
        arreter();
        suspendu = true;
    }

    private void reprendre() {
        suspendu = false;
        lancerTour(tempsRestantMs);
        actualiser();
        jouerOrdinateur();
    }

    public void arreter() {
        minuterie.stop();
    }

    void sauvegarder(Path fichier) throws IOException {
        if (!commencee) throw new IOException("Démarre une partie avant de sauvegarder.");
        boolean dejaSuspendu = suspendu;
        suspendre();
        try {
            Sauvegarde.enregistrer(fichier, partie, joueurX, joueurO,
                    contreOrdinateur, chronometre, tempsRestantMs);
        } finally {
            if (!dejaSuspendu) reprendre();
        }
    }

    void charger(Path fichier) throws IOException {
        boolean dejaSuspendu = suspendu;
        suspendre();
        try {
            Sauvegarde.Donnees donnees = Sauvegarde.charger(fichier);
            partie = donnees.getPartie();
            joueurX = donnees.getNomX();
            joueurO = donnees.getNomO();
            contreOrdinateur = donnees.isContreOrdinateur();
            chronometre = donnees.isChronometre();
            tempsRestantMs = donnees.getTempsRestantMs();
            nomX.setText(joueurX);
            nomO.setText(joueurO);
            ordinateur.setSelected(contreOrdinateur);
            deuxJoueurs.setSelected(!contreOrdinateur);
            avecChrono.setSelected(chronometre);
            sansChrono.setSelected(!chronometre);
            if (!contreOrdinateur) dernierNomHumain = joueurO;
            commencee = true;
        } finally {
            if (!dejaSuspendu) reprendre();
        }
    }

    private void dialogueFichier(boolean enregistrer) {
        suspendre();
        try {
            JFileChooser choix = new JFileChooser();
            int reponse = enregistrer ? choix.showSaveDialog(this) : choix.showOpenDialog(this);
            if (reponse != JFileChooser.APPROVE_OPTION) return;
            Path fichier = choix.getSelectedFile().toPath();
            if (enregistrer && java.nio.file.Files.exists(fichier)) {
                int accord = JOptionPane.showConfirmDialog(this, "Remplacer ce fichier ?",
                        "Sauvegarde", JOptionPane.YES_NO_OPTION);
                if (accord != JOptionPane.YES_OPTION) return;
            }
            if (enregistrer) sauvegarder(fichier);
            else charger(fichier);
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        } finally {
            reprendre();
        }
    }

    private void actualiser() {
        boolean enCours = commencee && !partie.estTerminee();
        nomX.setEnabled(!enCours);
        nomO.setEnabled(!enCours && !ordinateur.isSelected());
        deuxJoueurs.setEnabled(!enCours);
        ordinateur.setEnabled(!enCours);
        sansChrono.setEnabled(!enCours);
        avecChrono.setEnabled(!enCours);
        sauver.setEnabled(commencee);
        rejouer.setText(commencee ? "Rejouer" : "Démarrer");
        if (commencee) {
            if (partie.estMatchNul()) statut.setText("Match nul !");
            else if (partie.estTerminee()) statut.setText(nom(partie.getGagnant()) + " a gagné !");
            else statut.setText("Au tour de " + nom(partie.getJoueurCourant()) + " (" + partie.getJoueurCourant() + ")");
            detail.setText(partie.estPerdueAuTemps() ? "Temps dépassé : le joueur actif a perdu."
                    : "Une case occupée ne peut plus être jouée.");
        }
        for (int i = 0; i < 9; i++) {
            char symbole = partie.getCase(i / 3, i % 3);
            cases[i].setText(symbole == Partie.VIDE ? "" : String.valueOf(symbole));
            cases[i].setEnabled(enCours && symbole == Partie.VIDE
                    && !(contreOrdinateur && partie.getJoueurCourant() == 'O'));
            cases[i].setBackground(partie.estCaseGagnante(i / 3, i % 3) ? new Color(200, 235, 210) : Color.WHITE);
        }
        String texte = "";
        int[] coups = partie.getHistorique();
        for (int i = 0; i < coups.length; i++) {
            char symbole = i % 2 == 0 ? 'X' : 'O';
            texte += (i + 1) + ". " + nom(symbole) + " (" + symbole + ") : ("
                    + (coups[i] / 3 + 1) + ", " + (coups[i] % 3 + 1) + ")\n";
        }
        if (partie.estPerdueAuTemps()) texte += "Temps dépassé pour " + nom(partie.getJoueurCourant());
        historique.setText(texte);
        compteur.setText(partie.getNombreCoups() + " / 9 coups");
        afficherTemps();
    }

    private String nom(char symbole) {
        return symbole == 'X' ? joueurX : joueurO;
    }

    private void afficherTemps() {
        temps.setText(!chronometre || !commencee ? "Sans chronomètre"
                : "Temps restant : " + ((tempsRestantMs + 999) / 1000) + " s");
    }
}
