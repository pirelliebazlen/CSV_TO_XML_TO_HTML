package Etape1_Conversion;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Lit le CSV PadChest et donne chaque enregistrement (36 champs) au "traitement".
 *
 * Le fichier contient deux sortes de lignes :
 *
 *   NORMALE :   8,3137...png,0,...,[],[]
 *
 *   EMBALLÉE :  "0,2053...png,0,...,""['normal']"",[],[]"
 *               -> toute la ligne est entre guillemets, et les " de l'intérieur
 *                  sont doublés ("").
 *
 * Et parfois, un enregistrement est coupé sur 2 lignes (ou plus) :
 *               "12,...,""['C0262950' 'C0448034'"
 *               " 'C0920882' 'C0025066']"""
 */
public class LectureCSV {

    private final Path cheminCSV;
    public LectureCSV(Path cheminCSV) {

        this.cheminCSV = cheminCSV;
    }
/// ////////////////////////////////////////////////////////////////////////////////////
    public void lire(Consumer<String[]> traitement) throws IOException //
    {
        System.out.println("3.LectureCSV .lire");
        int nbEnregistrements = 0;
        int nbRejets = 0;

        try (BufferedReader lecteur = Files.newBufferedReader(cheminCSV, StandardCharsets.UTF_8)) {

            lecteur.readLine(); // 1re ligne = en-tête, on l'ignore

            String enCours = null; // enregistrement en train d'être reconstruit
            String ligne;

            while ((ligne = lecteur.readLine()) != null) {

                // ÉTAPE 1 : si la ligne est emballée, on la déballe.
                ligne = deballer(ligne);

                // ÉTAPE 2 : on la colle à la suite de l'enregistrement en cours.
                if (enCours == null) {
                    enCours = ligne;
                } else {
                    enCours = enCours + "\n" + ligne;
                }

                // ÉTAPE 3 : nombre impair de guillemets = un champ n'est pas fermé,
                //           l'enregistrement continue sur la ligne suivante.
                if (compterGuillemets(enCours) % 2 != 0) {
                    continue;
                }

                // ÉTAPE 4 : l'enregistrement est complet, on le découpe en champs.
                String[] champs = decouper(enCours);
                enCours = null;

                if (champs.length == 36) {
                    nbEnregistrements++;
                    traitement.accept(champs);
                } else {
                    nbRejets++;
                    //System.err.println("Rejeté (" + champs.length + " champs au lieu de 36)");
                }
            }
        }

        //System.out.println("Enregistrements lus : " + nbEnregistrements);
        //System.out.println("Rejetés             : " + nbRejets);
    }

    /// //////////////////////////////////////////////////////////////////////////////
    private static String deballer(String ligne) {
        System.out.println("deballer");
        if (ligne.length() >= 2 && ligne.startsWith("\"") && ligne.endsWith("\"")) {
            String interieur = ligne.substring(1, ligne.length() - 1);
            return interieur.replace("\"\"", "\"");
        }
        return ligne;
    }
    /// ////////////////////////////////////////////////////////////////////////////////
    private static int compterGuillemets(String texte) {
        System.out.println("compterGuillemets");
        int n = 0;
        for (int i = 0; i < texte.length(); i++) {
            if (texte.charAt(i) == '"') {
                n++;
            }
        }
        return n;
    }

    /// ////////////////////////////////////////////////////////////////////////
    private static String[] decouper(String enregistrement) {
        List<String> champs = new ArrayList<>();
        StringBuilder champ = new StringBuilder();
        boolean dansGuillemets = false;

        for (int i = 0; i < enregistrement.length(); i++) {
            char c = enregistrement.charAt(i);

            if (c == '"') {
                dansGuillemets = !dansGuillemets;    // on entre ou on sort des guillemets
            } else if (c == ',' && !dansGuillemets) {
                champs.add(champ.toString());        // fin du champ
                champ.setLength(0);
            } else {
                champ.append(c);                     // caractère normal
            }
        }
        champs.add(champ.toString());                // le dernier champ

        return champs.toArray(new String[0]);
    }

    /// /////////////////////////////////////////////////////////////////////////////
    public static String valeur(String[] valeurs, int index) {
        if (index >= valeurs.length || valeurs[index] == null) {
            return "";
        }
        return valeurs[index].trim();
    }
}