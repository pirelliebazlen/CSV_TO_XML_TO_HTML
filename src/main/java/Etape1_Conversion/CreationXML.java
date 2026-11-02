package Etape1_Conversion;

import Documents.Etude;
import Documents.Image;
import Documents.Patient;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class CreationXML {

    // terme -> id XML  (LinkedHashMap : ordre d'apparition conservé)
    private final Map<String, String> idsLabels = new LinkedHashMap<>();
    private final Map<String, String> idsLocs = new LinkedHashMap<>();

    public void ecrireXML(List<Patient> patients, Path cheminXML) throws IOException {

        // 1er passage : construire le dictionnaire des labels et localisations.
        construireDictionnaire(patients);

        Files.createDirectories(cheminXML.getParent());

        try (BufferedWriter w = Files.newBufferedWriter(cheminXML, StandardCharsets.UTF_8)) {

            w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            w.write("<!DOCTYPE padchest SYSTEM \"../src/main/java/Etape2_Structure/DTD.dtd\">\n");
            w.write("<padchest>\n");

            ecrireDictionnaire(w);

            // 2e passage : les patients.
            for (Patient patient : patients) {
                ecrirePatient(w, patient);
            }

            w.write("</padchest>\n");
        }

        afficherStatistiques(patients);
    }

    // /////////////// dictionnaire////////////////////////////////////////////////

    private void construireDictionnaire(List<Patient> patients) {
        for (Patient p : patients) {
            for (Etude e : p.getEtudes()) {
                List<String> labels = Nettoyage.liste(e.getLabels(), true);
                if (labels != null) {
                    labels.forEach(this::enregistrerLabel);
                }
                List<String> locs = Nettoyage.liste(e.getLocalizations(), true);
                if (locs != null) {
                    locs.forEach(this::enregistrerLoc);
                }
                // Les phrases contiennent parfois des termes absents de Labels.
                List<List<String>> phrases =
                        Nettoyage.listeDeListes(e.getLabelsLocalizationsBySentence());
                if (phrases != null) {
                    for (List<String> phrase : phrases) {
                        for (String terme : phrase) {
                            if (estLocalisation(terme)) {
                                enregistrerLoc(terme);
                            } else {
                                enregistrerLabel(terme);
                            }
                        }
                    }
                }
            }
        }
    }

    /// ///////////////////////////////////////////////////////////////////////////////////////////////
    private static boolean estLocalisation(String terme) {
        return terme.startsWith("loc ");
    }
    /// //////////////////////////////////////////////////////////////////////////////////////////
    private void enregistrerLabel(String terme) {
        idsLabels.computeIfAbsent(terme, t -> "L" + (idsLabels.size() + 1));
    }
    /// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void enregistrerLoc(String terme) {
        idsLocs.computeIfAbsent(terme, t -> "LOC" + (idsLocs.size() + 1));
    }
    /// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void ecrireDictionnaire(BufferedWriter w) throws IOException {
        w.write("  <dictionnaire>\n");
        for (Map.Entry<String, String> e : idsLabels.entrySet()) {
            w.write("    <labelDef id=\"" + e.getValue() + "\">" + echapper(e.getKey()) + "</labelDef>\n");
        }
        for (Map.Entry<String, String> e : idsLocs.entrySet()) {
            w.write("    <locDef id=\"" + e.getValue() + "\">" + echapper(e.getKey()) + "</locDef>\n");
        }
        w.write("  </dictionnaire>\n");
    }

    //////////////////////////patients///////////////////////////////////////////////////////////////

    private void ecrirePatient(BufferedWriter w, Patient p) throws IOException {
        w.write("  <patient id=\"" + p.getXmlId() + "\""
                + attribut("patientID", p.getPatientID())
                + attribut("naissance", p.getPatientBirth())   // absent si inconnue
                + attribut("sexe", p.getPatientSex())          // absent si inconnu
                + ">\n");

        for (Etude e : p.getEtudes()) {
            w.write("    <etude id=\"" + e.getXmlId() + "\"" + attribut("studyID", e.getStudyID()) + ">\n");
            ecrireRapport(w, e);
            for (Image i : e.getImages()) {
                w.write("      <image"
                        + attribut("fichier", i.getImageID())
                        + attribut("dossier", i.getImageDir())
                        + attribut("projection", i.getProjection())
                        + attribut("pediatrique", i.getPediatric())
                        + attribut("methodeProjection", Nettoyage.methodeProjection(i.getMethodProjection()))
                        + "/>\n");
            }
            w.write("    </etude>\n");
        }

        w.write("  </patient>\n");
    }
    /// ///////////////////////////////////////////////////////////////////////////////////////////////
    private void ecrireRapport(BufferedWriter w, Etude e) throws IOException {
        w.write("      <rapport" + attribut("reportID", e.getReportID())
                + attribut("methode", e.getMethodLabel()) + ">\n");

        String texte = Nettoyage.texte(e.getReport());
        if (texte != null) {
            w.write("        <texte>" + echapper(texte) + "</texte>\n");
        }

        ecrireRefs(w, "labels", Nettoyage.liste(e.getLabels(), true));
        ecrireRefs(w, "localisations", Nettoyage.liste(e.getLocalizations(), true));

        List<List<String>> phrases = Nettoyage.listeDeListes(e.getLabelsLocalizationsBySentence());
        if (phrases != null) {
            String decoupage = Nettoyage.estListePlate(e.getLabelsLocalizationsBySentence())
                    ? "reconstruit" : "original";
            w.write("        <phrases decoupage=\"" + decoupage + "\">\n");
            for (List<String> phrase : phrases) {
                StringBuilder sb = new StringBuilder("          <phrase>");
                for (String terme : phrase) {
                    sb.append(ref(terme));
                }
                sb.append("</phrase>\n");
                w.write(sb.toString());
            }
            w.write("        </phrases>\n");
        }

        ecrireCuis(w, "cuisLabels", Nettoyage.liste(e.getLabelCUIS(), false));
        ecrireCuis(w, "cuisLocalisations", Nettoyage.liste(e.getLocalizationsCUIS(), false));

        w.write("      </rapport>\n");
    }

    /// ///////////////////////////////////////////////////////////////////////////////////////////////
    private void ecrireRefs(BufferedWriter w, String conteneur, List<String> termes) throws IOException {
        if (termes == null) {
            return;
        }
        StringBuilder sb = new StringBuilder("        <" + conteneur + ">");
        for (String terme : termes) {
            sb.append(ref(terme));
        }
        sb.append("</").append(conteneur).append(">\n");
        w.write(sb.toString());
    }
    /// ///////////////////////////////////////////////////////////////////////////////////////////
    private String ref(String terme) {
        return estLocalisation(terme)
                ? "<loc ref=\"" + idsLocs.get(terme) + "\"/>"
                : "<label ref=\"" + idsLabels.get(terme) + "\"/>";
    }

    /// ///////////////////////////////////////////////////////////////////////////////////////////////////
    private void ecrireCuis(BufferedWriter w, String conteneur, List<String> cuis) throws IOException {
        if (cuis == null) {
            return;
        }
        StringBuilder sb = new StringBuilder("        <" + conteneur + ">");
        for (String cui : cuis) {
            sb.append("<cui>").append(echapper(cui)).append("</cui>");
        }
        sb.append("</").append(conteneur).append(">\n");
        w.write(sb.toString());
    }

    /////////////////////////////utilitaires/////////////////////////////////

    private String attribut(String nom, String valeur) {
        if (valeur == null || valeur.isEmpty()) {
            return "";
        }
        return " " + nom + "=\"" + echapper(valeur) + "\"";
    }

   /// ////////////////////////////////////////////////////////////
    private String echapper(String texte) {
        if (texte == null) {
            return "";
        }
        return texte.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
    /// ////////////////////////////////////////////////////////////////

    private void afficherStatistiques(List<Patient> patients) {
        long naissanceInco = patients.stream().filter(Patient::aNaissanceIncoherente).count();
        long sexeInco = patients.stream().filter(Patient::aSexeIncoherent).count();
        long sansNaissance = patients.stream().filter(p -> p.getPatientBirth() == null).count();
        long sansSexe = patients.stream().filter(p -> p.getPatientSex() == null).count();
        System.out.println("Dictionnaire : " + idsLabels.size() + " labels, " + idsLocs.size() + " localisations");
        System.out.println("Patients avec plusieurs années de naissance : " + naissanceInco);
        System.out.println("Patients avec plusieurs sexes               : " + sexeInco);
        System.out.println("Patients sans année de naissance            : " + sansNaissance);
        System.out.println("Patients sans sexe connu                    : " + sansSexe);
    }
}
