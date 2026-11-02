package Documents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Patient {

    private String xmlId;
    private String patientID;

    // Piège : un même patient n'a pas toujours la même année de naissance
    // (ni le même sexe) sur toutes ses images. On compte les valeurs vues
    // et on garde la plus fréquente.
    private Map<String, Integer> naissancesVues = new HashMap<>();
    private Map<String, Integer> sexesVus = new HashMap<>();

    private List<Etude> etudes = new ArrayList<>();

    public Patient(String xmlId, String patientID) {
        this.xmlId = xmlId;
        this.patientID = patientID;
    }

    public void noterNaissance(String annee) {
        if (annee != null) {
            naissancesVues.merge(annee, 1, Integer::sum);
        }
    }

    public void noterSexe(String sexe) {
        if (sexe != null) {
            sexesVus.merge(sexe, 1, Integer::sum);
        }
    }

    public String getXmlId() {
        return xmlId;
    }

    public String getPatientID() {
        return patientID;
    }

    /** Année la plus fréquente parmi les images du patient, ou null si jamais connue. */
    public String getPatientBirth() {
        return plusFrequent(naissancesVues);
    }

    /** Sexe le plus fréquent parmi les images du patient, ou null si jamais connu. */
    public String getPatientSex() {
        return plusFrequent(sexesVus);
    }

    public boolean aNaissanceIncoherente() {
        return naissancesVues.size() > 1;
    }

    public boolean aSexeIncoherent() {
        return sexesVus.size() > 1;
    }

    /** Valeur la plus fréquente ; en cas d'égalité, la plus petite (résultat reproductible). */
    private static String plusFrequent(Map<String, Integer> compteur) {
        String meilleur = null;
        int meilleurNombre = 0;
        for (Map.Entry<String, Integer> e : compteur.entrySet()) {
            if (e.getValue() > meilleurNombre
                    || (e.getValue() == meilleurNombre && e.getKey().compareTo(meilleur) < 0)) {
                meilleur = e.getKey();
                meilleurNombre = e.getValue();
            }
        }
        return meilleur;
    }

    public List<Etude> getEtudes() {
        return etudes;
    }

    public void ajouterEtude(Etude etude) {
        etudes.add(etude);
    }

    public Etude rechercherEtude(String studyID) {
        for (Etude etude : etudes) {
            if (etude.getStudyID().equals(studyID)) {
                return etude;
            }
        }
        return null;
    }
}
