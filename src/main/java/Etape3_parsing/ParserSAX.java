package Etape3_parsing;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Parser SAX : lit padchest.xml du début à la fin, UNE seule fois,
 * vérifie qu'il respecte la DTD et calcule les 3 calcule statistiques de Mr.
 *
 * Principe de SAX : on ne demande rien au parser, c'est LUI qui nous appelle
 * à chaque fois qu'il rencontre quelque chose :
 *   startElement(...)  -> "j'ouvre une balise"
 *   characters(...)    -> "voici du texte"
 *   endElement(...)    -> "je ferme une balise"
 */
public class ParserSAX extends  DefaultHandler{

    // 1. Les compteurs (les résultats qu'on veut calculer)
    private int nbPatients = 0;
    private int nbImagesLocRight = 0;
    private Map<String, Integer> imagesParProjection = new HashMap<>();
    private Map<String, Integer> compteurLabels = new HashMap<>();
    private int nbErreurs = 0;

     //2. Le dictionnaire : id -> terme   ("L1" -> "normal")
    private Map<String, String> termes = new HashMap<>(); //valeur
    private String idLocRight = null; //id de locRight trouvé


    //Permet de savoir où SAX en est car elle ne retient rien
    private String idEnCours = null;   //id du labelDef/locDef dont on lit le texte
    private StringBuilder texte = new StringBuilder();
    private boolean dansLabels=false;
    private boolean dansLocalisations = false;
    private boolean etudeALocRight = false;  //etude en cours contient loc-right


    //4 methodes SAX

    @Override
    public void startElement(String uri, String localName, String nom, Attributes attributes)
    {
        switch (nom)
        {
            case "labelDef":
            case "locDef":
                idEnCours = attributes.getValue("id");
                texte.setLength(0);
                break;
            case "patient":  //CALCUL 3
                nbPatients++;
                break;

            case "etude":
                etudeALocRight=false;   //nouvelle étude : on remet le drapeau à faux
                break;

            case "labels":
                dansLabels = true;
                break;

            case "localisations":
                dansLocalisations= true;
                break;

            case "label":  //CALCULE 2
                if(dansLabels)
                {
                    String ref = attributes.getValue("ref");
                    compteurLabels.merge(ref, 1, Integer::sum);  //je recupere a ref et j'incrementes son compteur ex-> l1=2 l2=4
                }
                break;

            case "loc":
                if(dansLocalisations && attributes.getValue("ref").equals(idLocRight))
                {
                    etudeALocRight = true;
                }
                break;

            case "image" :
                String projection = attributes.getValue("projection");
                imagesParProjection.merge(projection, 1, Integer::sum);
                if(etudeALocRight)
                {
                    nbImagesLocRight++;
                }
                break;

        }
    }



    @Override
    public void  characters(char[] ch, int debut, int longueur)  //permet de mettre ens tout le token decouper par SAX
    {
        if(idEnCours!=null)
        {
            texte.append(ch, debut, longueur);
        }

    }

    @Override
    public void endElement(String uri, String localName, String nom)
    {

        switch (nom) {

            case "labelDef":
            case "locDef":
                String terme = texte.toString();
                termes.put(idEnCours, terme);
                if(terme.equals("loc right"))
                {
                    idLocRight = idEnCours;
                }
                idEnCours = null;
                break;

            case "labels":
                dansLabels = false;
                break;

            case "localisations":
                dansLocalisations = false;
                break;
        }

    }

    //5. Les erreurs de validation (le document ne respecte pas la DTD)

    @Override
    public void error(SAXParseException e)
    {
        if (nbErreurs <= 10) {
            System.err.println("Erreur ligne " + e.getLineNumber() + " : " + e.getMessage());
        }
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        throw e;   // document mal formé : on arrête tout
    }

    // 6. Affichage des résultats

    private void afficherResultats()
    {
        System.out.println("Validation DTD : " + (nbErreurs == 0 ? "document VALIDE" : nbErreurs + " erreur(s)"));
        System.out.println();
        System.out.println("1) Images avec la localisation 'loc right' : " + nbImagesLocRight);
        System.out.println();

        List<Map.Entry<String, Integer>> tri = new ArrayList<>(compteurLabels.entrySet());
        tri.sort((a, b) -> b.getValue() - a.getValue());
        System.out.println("2) Top 10 des labels (nombre d'études) :");
        for (int i = 0; i < 10 && i < tri.size(); i++) {
            String id = tri.get(i).getKey();
            System.out.printf("%2d. %-25s %d%n", i + 1, termes.get(id), tri.get(i).getValue());

        }
        System.out.println();
        System.out.println("3) Patients distincts : " + nbPatients);
        System.out.println("   Images par projection :");
        for (Map.Entry<String, Integer> e : imagesParProjection.entrySet()) {
            System.out.printf(" %-14s %d%n", e.getKey(), e.getValue());
        }
    }

    public static void main(String[] args) throws Exception {

        long debut = System.currentTimeMillis();

        SAXParserFactory fabrique = SAXParserFactory.newInstance();
        fabrique.setValidating(true);  // => vérifie le document avec la DTD du <!DOCTYPE>

        //pour xsd

        /*fabrique.setNamespaceAware(true);
        fabrique.setSchema( javax.xml.validation.SchemaFactory
                        .newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI)
                        .newSchema(new File("src/main/resources/XSD.xsd"))
        );*/

        SAXParser parser = fabrique.newSAXParser();

        ParserSAX handler = new ParserSAX();
        parser.parse(new File("target/padchest.xml"), handler);   // SAX lit tout et appelle le handler

        handler.afficherResultats();

        long duree = System.currentTimeMillis() - debut;

        Runtime rt = Runtime.getRuntime();
        long memoireMo = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        System.out.println();
        System.out.println("Temps : " + duree + " ms   |   Mémoire utilisée : ~" + memoireMo + " Mo");
    }




}
