
package Etape1_Conversion;

import Documents.Patient;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ConvertToXML {

    //private static final Path CSV_PATH = Path.of("src/main/resources/PADCHEST_chest_x_ray_images_labels_160K_01.02.19.csv" );
    private static final Path CSV_PATH = Path.of("src/main/resources/PadChest_test_5_enregistrements.csv" );
    private static final Path XML_PATH = Path.of("target/exemple.xml");

    //private static final Path XML_PATH = Path.of("target/padchest.xml");

    public static void convertir() throws IOException {

        // 1. Lire et regrouper le CSV en objets Java.
        System.out.println("2.ConvertTo .convertir()");
        RegroupeurPatients regroupeur = new RegroupeurPatients();

        List<Patient> patients = regroupeur.regrouper(CSV_PATH);


        System.out.println( "Nombre de patients regroupés : " + patients.size() );

        // 2. Générer le fichier XML.
        CreationXML generateur = new CreationXML();

        generateur.ecrireXML(patients, XML_PATH);

        System.out.println( "XML créé : " + XML_PATH.toAbsolutePath()
        );
    }
}
