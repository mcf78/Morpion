import java.util.Random;

/** L'ordinateur choisit au hasard parmi les cases encore libres. */
public class Ordinateur {
    private static final Random hasard = new Random();

    public static int choisirCoup(Partie partie) {
        if (partie.estTerminee()) {
            return -1;
        }

        int[] casesLibres = new int[9];
        int nombre = 0;
        for (int i = 0; i < 9; i++) {
            if (partie.getCase(i / 3, i % 3) == Partie.VIDE) {
                casesLibres[nombre] = i;
                nombre++;
            }
        }
        // On retourne une position, sans placer le symbole dans la partie.
        if (nombre == 0) {
            return -1;
        }
        return casesLibres[hasard.nextInt(nombre)];
    }
}
