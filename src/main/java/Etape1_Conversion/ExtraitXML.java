package Etape1_Conversion;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Crée extrait.xml : le début de padchest.xml, avec seulement les 200 premières images
 * (1 image = 1 enregistrement du CSV). Le fichier complet est trop lourd pour un mail.
 *
 * Méthode : on recopie padchest.xml ligne par ligne en comptant les <image .../>.
 * À la 200e, on referme les balises encore ouvertes et on s'arrête.
 */
public class ExtraitXML {
    private  static final int NB_IMAGES = 200;
    private static final Path SOURCE = Path.of("target/padchest.xml");
    private static final Path EXTRAIT = Path.of("extrait.xml");

    public static void main(String[] args) throws  IOException
    {
        int nbImages = 0;
        try(BufferedReader lecteur = Files.newBufferedReader(SOURCE, StandardCharsets.UTF_8);
            BufferedWriter ecrivain = Files.newBufferedWriter(EXTRAIT, StandardCharsets.UTF_8))
        {
            String ligne;
            while((ligne = lecteur.readLine())!=null)
            {
                if(ligne.startsWith("<!DOCTYPE"))
                {
                    ligne = ligne.replace("../src/", "src/"); //extrait.xml est a la racine je ne doit pas remonter mais aller directement dans src

                }
                ecrivain.write(ligne);
                ecrivain.newLine();

                if(ligne.trim().startsWith("<image "))
                {
                    nbImages++;

                    if(nbImages == NB_IMAGES)
                    {
                        ecrivain.write("    </etude>");
                        ecrivain.newLine();
                        ecrivain.write("    </patient>");
                        ecrivain.newLine();
                        ecrivain.write("    </padchest>");
                        ecrivain.newLine();
                        break;

                    }
                }
            }
        }
        System.out.println("extrait.xml créé avec " + nbImages + " images : "+ EXTRAIT.toAbsolutePath());
    }
}
