
package Etape1_Conversion;

import Documents.Etude;
import Documents.Image;
import Documents.Patient;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static Etape1_Conversion.LectureCSV.valeur;

public class RegroupeurPatients {

    // Indices dans record.values()
    private static final int IMAGE_ID = 1;
    private static final int IMAGE_DIR = 2;
    private static final int STUDY_ID = 4;
    private static final int PATIENT_ID = 5;
    private static final int PATIENT_BIRTH = 6;
    private static final int PATIENT_SEX = 7;
    private static final int PROJECTION = 9;
    private static final int METHOD_PROJECTION = 10;
    private static final int PEDIATRIC = 11;
    private static final int REPORT_ID = 28;
    private static final int REPORT = 29;
    private static final int METHOD_LABEL = 30;
    private static final int LABELS = 31;
    private static final int LOCALIZATIONS = 32;
    private static final int LABELS_BY_SENTENCE = 33;
    private static final int LABEL_CUIS = 34;
    private static final int LOCALIZATION_CUIS = 35;

    private int compteurPatients = 0;
    private int compteurEtudes = 0;
    private int compteurImages = 0;

    public List<Patient> regrouper(Path cheminCSV)  throws IOException
    {

        Map<String, Patient> patients = new LinkedHashMap<>();

        LectureCSV lecteur = new LectureCSV(cheminCSV);

        lecteur.lire(v -> traiterEnregistrement(v, patients));

        return new ArrayList<>(patients.values());
    }

    private void traiterEnregistrement( String[] v, Map<String, Patient> patients)
    {
        String patientID = valeur(v, PATIENT_ID);
        String studyID = valeur(v, STUDY_ID);

        if (patientID.isBlank() || studyID.isBlank()) {
            System.err.println(   "PatientID ou StudyID manquant : enregistrement ignoré.");
            return;
        }

        // 1. Retrouver ou créer le patient.
        Patient patient = patients.get(patientID);

        if (patient == null)
        {
            patient = new Patient("P" + (++compteurPatients), patientID);
            patients.put(patientID, patient);
        }


        patient.noterNaissance(Nettoyage.annee(valeur(v, PATIENT_BIRTH)));
        patient.noterSexe(Nettoyage.sexe(valeur(v, PATIENT_SEX)));

        // 2. Retrouver ou créer l'étude de ce patient.
        Etude etude = patient.rechercherEtude(studyID);

        if (etude == null) {
            etude = new Etude( "S" + (++compteurEtudes),
                    studyID,
                    valeur(v, REPORT_ID),
                    valeur(v, REPORT),
                    valeur(v, METHOD_LABEL),
                    valeur(v, LABELS),
                    valeur(v, LOCALIZATIONS),
                    valeur(v, LABELS_BY_SENTENCE),
                    valeur(v, LABEL_CUIS),
                    valeur(v, LOCALIZATION_CUIS)
            );

            patient.ajouterEtude(etude);
        }

        // 3. Créer l'image correspondant à l'enregistrement.
        Image image = new Image(
                "I" + (++compteurImages),
                valeur(v, IMAGE_ID),
                valeur(v, IMAGE_DIR),
                valeur(v, PROJECTION),
                valeur(v, PEDIATRIC),
                valeur(v, METHOD_PROJECTION)
        );

        etude.ajouterImage(image);
    }
}
