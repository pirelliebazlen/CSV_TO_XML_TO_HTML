package Etape1_Conversion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;


public final class Nettoyage {

    private Nettoyage() {
    }

    /// //////////////////////////////////////////////////////////////////////////
    public static boolean estManquant(String s) {
        return s == null || s.isBlank() || s.equals("nan") || s.equals("None");
    }

   /// ////////////////////////////////////////////////////////////////////////////////
    public static String annee(String s) {
        if (estManquant(s)) {
            return null;
        }
        s = s.trim();
        if (s.endsWith(".0")) {
            s = s.substring(0, s.length() - 2);
        }
        return s.matches("\\d{4}") ? s : null;
    }

    /// ///////////////////////////////////////////////////////////////////////////////////////
    public static String sexe(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        return (s.equals("M") || s.equals("F") || s.equals("O")) ? s : null;
    }

    /// //////////////////////////////////////////////////////////////////////////////////////////
    public static String texte(String s) {
        return estManquant(s) ? null : s.trim();
    }

    /// ////////////////////////////////////////////////////////////////
    public static String methodeProjection(String s) {
        if (s == null) {
            return null;
        }
        switch (s.trim()) {
            case "Manual review of DICOM fields":
                return "DICOM";
            case "resnet-50.t7":
                return "ResNet50";
            default:
                return null;
        }
    }

  /// //////////////////////////////////////////////////////////////////////////////
    public static List<String> liste(String s, boolean sansDoublons) {
        if (estManquant(s) || !s.trim().startsWith("[")) {
            return null;
        }
        Collection<String> resultat = sansDoublons ? new LinkedHashSet<>() : new ArrayList<>();
        extraireElements(s, 0, s.length(), resultat);
        return new ArrayList<>(resultat);
    }
    /// ///////////////////////////////////////////////////////////////////////////////////
    public static boolean estListePlate(String s) {
        if (estManquant(s)) {
            return false;
        }
        String t = s.trim();
        // Après le premier '[', on trouve '[' (ou nan) pour des phrases, une apostrophe pour du plat.
        int i = 1;
        while (i < t.length() && t.charAt(i) == ' ') {
            i++;
        }
        return i < t.length() && t.charAt(i) == '\'';
    }

    /// /////////////////////////////////////////////////////////////////////////////

    public static List<List<String>> listeDeListes(String s) {
        if (estManquant(s) || !s.trim().startsWith("[")) {
            return null;
        }

        if (estListePlate(s)) {
            List<String> termes = liste(s, false);
            List<List<String>> groupes = new ArrayList<>();
            List<String> groupe = new ArrayList<>();
            boolean dernierEstLoc = false;
            for (String terme : termes) {
                boolean estLoc = terme.startsWith("loc ");
                if (!estLoc && dernierEstLoc) {   // label après une loc -> nouveau groupe
                    groupes.add(groupe);
                    groupe = new ArrayList<>();
                }
                groupe.add(terme);
                dernierEstLoc = estLoc;
            }
            if (!groupe.isEmpty()) {
                groupes.add(groupe);
            }
            return groupes;
        }
        List<List<String>> phrases = new ArrayList<>();
        int profondeur = 0;
        int debut = -1;
        boolean dansApostrophes = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'') {
                dansApostrophes = !dansApostrophes;
            } else if (!dansApostrophes && c == '[') {
                profondeur++;
                if (profondeur == 2) {
                    debut = i;              // début d'une sous-liste
                }
            } else if (!dansApostrophes && c == ']') {
                if (profondeur == 2) {      // fin d'une sous-liste
                    List<String> phrase = new ArrayList<>();
                    extraireElements(s, debut, i, phrase);
                    phrases.add(phrase);
                }
                profondeur--;
            }
        }
        return phrases;
    }
    /////////////////////////////////////////////////////////////////////////////////////////////
    private static void extraireElements(String s, int debut, int fin, Collection<String> sortie) {
        int ouverture = -1;
        for (int i = debut; i < fin; i++) {
            if (s.charAt(i) == '\'') {
                if (ouverture < 0) {
                    ouverture = i;
                } else {
                    String element = s.substring(ouverture + 1, i).trim();
                    if (!element.isEmpty()) {
                        sortie.add(element);
                    }
                    ouverture = -1;
                }
            }
        }
    }
}
