package Etape3_parsing;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser DOM : charge TOUT le fichier en mémoire sous forme d'arbre,
 * puis on se promène dans l'arbre pour calculer les statistiques.
 *
 * Différence avec SAX :
 *   SAX -> le parser nous appelle pendant la lecture, il ne garde rien.
 *   DOM -> le parser lit tout, construit l'arbre complet, et nous le donne.
 *          On peut alors aller où on veut dans l'arbre... mais il faut
 *          que l'arbre entier tienne en mémoire.
 */
public class ParserDOM {

    private static int nbErreurs = 0;

    public static void main(String[] args) throws Exception {

        long debut = System.currentTimeMillis();

        // ============================================================
        // 1. Charger le document : le fichier devient un arbre en mémoire
        // ============================================================

        DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
        fabrique.setValidating(true);                         // vérifie avec la DTD
        fabrique.setIgnoringElementContentWhitespace(true);   // ignore les espaces d'indentation
        // Par défaut, le parser du JDK "triche" : il ne crée les objets de l'arbre
        // qu'au moment où on les visite (chargement différé). On désactive cette
        // optimisation pour avoir un vrai DOM, construit entièrement au chargement.
        fabrique.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);

        DocumentBuilder constructeur = fabrique.newDocumentBuilder();
        constructeur.setErrorHandler(new ErrorHandler() {
            public void warning(SAXParseException e) { }
            public void error(SAXParseException e) {
                nbErreurs++;
                if (nbErreurs <= 10) {
                    System.err.println("Erreur ligne " + e.getLineNumber() + " : " + e.getMessage());
                }
            }
            public void fatalError(SAXParseException e) throws SAXParseException {
                throw e;
            }
        });

        Document doc = constructeur.parse(new File("target/padchest.xml"));   // <-- TOUT est chargé ici

        long finChargement = System.currentTimeMillis();
        afficherMemoire("Après chargement de l'arbre");

        System.out.println("Validation DTD : "
                + (nbErreurs == 0 ? "document VALIDE" : nbErreurs + " erreur(s)"));

        // ============================================================
        // 2. Trouver l'id de "loc right" dans le dictionnaire
        // ============================================================

        // On cherche seulement DANS le dictionnaire (quelques centaines de nœuds),
        // pas dans tout le document (plusieurs millions de nœuds).
        String idLocRight = null;
        Element dictionnaire = (Element) doc.getElementsByTagName("dictionnaire").item(0);
        NodeList locDefs = dictionnaire.getElementsByTagName("locDef");
        for (int i = 0; i < locDefs.getLength(); i++) {
            Element locDef = (Element) locDefs.item(i);
            if (locDef.getTextContent().equals("loc right")) {
                idLocRight = locDef.getAttribute("id");
            }
        }
        // 3. Parcourir toutes les études
        int nbImagesLocRight = 0;
        Map<String, Integer> compteurLabels = new HashMap<>();
        Map<String, Integer> imagesParProjection = new HashMap<>();

        NodeList etudes = doc.getElementsByTagName("etude");

        for (int i = 0; i < etudes.getLength(); i++) {
            Element etude = (Element) etudes.item(i);
            Element rapport = (Element) etude.getElementsByTagName("rapport").item(0);

            // --- calcul 1 : le rapport contient-il "loc right" ?
            boolean aLocRight = false;
            Element localisations = premierEnfant(rapport, "localisations");
            if (localisations != null) {
                NodeList locs = localisations.getElementsByTagName("loc");
                for (int j = 0; j < locs.getLength(); j++) {
                    if (((Element) locs.item(j)).getAttribute("ref").equals(idLocRight)) {
                        aLocRight = true;
                    }
                }
            }

            // --- calcul 2 : les labels du rapport (pas ceux des <phrases>)
            Element labels = premierEnfant(rapport, "labels");
            if (labels != null) {
                NodeList listeLabels = labels.getElementsByTagName("label");
                for (int j = 0; j < listeLabels.getLength(); j++) {
                    String ref = ((Element) listeLabels.item(j)).getAttribute("ref");
                    compteurLabels.merge(ref, 1, Integer::sum);
                }
            }

            // --- calculs 1 et 3 : les images de l'étude
            NodeList images = etude.getElementsByTagName("image");
            for (int j = 0; j < images.getLength(); j++) {
                Element image = (Element) images.item(j);
                imagesParProjection.merge(image.getAttribute("projection"), 1, Integer::sum);
            }
            if (aLocRight) {
                nbImagesLocRight += images.getLength();
            }
        }

        // 4. Afficher les résultats
        System.out.println();
        System.out.println("1) Images avec la localisation 'loc right' : " + nbImagesLocRight);

        System.out.println();
        System.out.println("2) Top 10 des labels (nombre d'études) :");
        List<Map.Entry<String, Integer>> tri = new ArrayList<>(compteurLabels.entrySet());
        tri.sort((a, b) -> b.getValue() - a.getValue());
        for (int i = 0; i < 10 && i < tri.size(); i++) {
            String id = tri.get(i).getKey();
            // getElementById marche grâce à la DTD : elle dit que "id" est de type ID
            String terme = doc.getElementById(id).getTextContent();
            System.out.printf("   %2d. %-25s %d%n", i + 1, terme, tri.get(i).getValue());
        }

        System.out.println();
        System.out.println("3) Patients distincts : " + doc.getElementsByTagName("patient").getLength());
        System.out.println("   Images par projection :");
        for (Map.Entry<String, Integer> e : imagesParProjection.entrySet()) {
            System.out.printf("      %-14s %d%n", e.getKey(), e.getValue());
        }

        long fin = System.currentTimeMillis();
        System.out.println();
        System.out.println("Temps : chargement " + (finChargement - debut) + " ms, calculs "
                + (fin - finChargement) + " ms, total " + (fin - debut) + " ms");
    }

    /** Renvoie le premier enfant direct de 'parent' qui s'appelle 'nom', ou null. */
    private static Element premierEnfant(Element parent, String nom) {
        NodeList liste = parent.getElementsByTagName(nom);
        return liste.getLength() > 0 ? (Element) liste.item(0) : null;
    }

    private static void afficherMemoire(String moment) {
        Runtime rt = Runtime.getRuntime();
        System.gc();
        long utilise = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long max = rt.maxMemory() / (1024 * 1024);
        System.out.println(moment + " : ~" + utilise + " Mo utilisés (maximum autorisé : " + max + " Mo)");
    }
}