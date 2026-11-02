package Etape4_xslt;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.nio.file.Files;


public class TransformationXSLT {

    public static void main(String[] args) throws Exception {

        File xml  = new File("target/padchest.xml");                   // le XML à transformer
        File xsl  = new File("src/main/java/Etape4_xslt/padchest.xsl"); // la feuille de style
        File html = new File("site/index.html");                        // la page principale

        if (!xml.exists()) {
            System.err.println("Fichier introuvable : " + xml.getAbsolutePath());
            return;
        }

        Files.createDirectories(html.getParentFile().toPath());         //pour créer le dossier site/

        long debut = System.currentTimeMillis();

        // On demande explicitement Saxon (le XSLT intégré à Java ne connaît que XSLT 1.0).
        TransformerFactory fabrique =
                TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);

        Transformer transformateur = fabrique.newTransformer(new StreamSource(xsl));
        transformateur.transform(new StreamSource(xml), new StreamResult(html));
        // Les pages patients-N.html (xsl:result-document) sont écrites à côté de index.html.

        System.out.println("Site créé en " + (System.currentTimeMillis() - debut)
                + " ms : " + html.getAbsolutePath());
    }
}