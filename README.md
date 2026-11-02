## Présentation générale du projet

Dans le cadre du developpement de mon projet xml , j'étais emmené à prendre un grand fichier de données médicales au format CSV, de le transformer en un document XML bien organisé, de vérifier sa structure,
d'effectuer des statistiques et enfin de présenter les informations sur une page web.

Pour arriver à cette fin , j'ai utilisé plusieurs technologies (Demander dans le cahier de charge):

| Technologie |                           Rôle                           |
|:------------|:--------------------------------------------------------:|
| CSV         |         Fichier contenant les données de départ.         |
| Java        |         Lire, nettoyer et convertir les données          |
| XML         |      Organiser les données sous forme hiérarchique       |
| DTD         |          Définir les règles de structure du XML          |
| XSD         |      Définir les règles de validation plus précises      |
| SAX/DOM     |      Parser pour Lire et analyser les document XML       |
| XSLT        |             Transformer le XML en page HTML              |
| Saxon       |            Exécuter les transformations XSLT             |
| BaseX       |         Stocker et interroger le XML avec XQuery         |
| XQuery      | Effectuer des recherches et des statistiques dans le XML |
|             |                                                          |

## Le fonctionnement global
 
### Etape 1: conversion CSV → XML (Java)

j'ai d'abord créé les classes qui représentent les données (Document):
-`Patient` : un patient et la liste de ses études;
- `Etude` : le rapport et la liste de ses images ;
- `Image` : une image (1 enregistrement du CSV = 1 image)

La conversion se fait en 3 parties:

1. **Lecture du CSV** (`LectureCSV`) : le fichier est lu ligne par ligne , "à la main",
    sans bibliothèque.
2. **Regroupement** (`RegroupeurPatients`) : les lignes d'un même patient ne se suivent pas,
    donc je garde tout en mémoire dans une `Map` (patient -> études -> image) avant d'ecrire 
3. **Écriture du XML** (`CreationXML`) : chaque valeur est nettoyée (`Nettoyage`) puis écrite avec les
    caractères spéciaux échappés (&, <, >, ").

Résultat : `target/padchest.xml` (160 861 images, 67 625 patients).

### Etape 2 : Structure du document xml (DTD et XSD)

La DTD définit la structure : quels éléments existent , dans quel ordre , avec quels attributs.
La XSD va plus loin : types (année , patterns), énumérations, et `key` / `keyref` pour controler les
références.

### Etape 3 : Parsing (SAX et DOM)

Les deux parseurs java valident le xml avec la DTD , XSD et nous permet d'effectuer des statistiques
- **SAX** lit le fichier en flux, sans le garder en mémoire (~120 Mo).
- **DOM** charge tout l'arbre en mémoire (~540 Mo) et plante avec `-Xmx512m`

### Étape 4 : site web (XSLT + Saxon)

J'ai écrit une feuille de style `padchest.xsl` qui décrit les règles de transformation
du XML en HTML (par exemple : « pour chaque patient, écrire une ligne de tableau »).
Ces règles sont exécutées par Saxon-HE, un programme Java qui comprend XSLT 3.0.
Elle produit `site/index.html` (statistiques) et 136 pages de patients.

### Étape 5 : base de données (BaseX + XQuery)

Le XML est importé dans BaseX (base `padchest`) et interrogé avec 4 requêtes XQuery.
Les requêtes 1 et 2 donnent les mêmes résultats que les parsers Java.

## USE CASE

# Pour l'étape 1 :
Lancer `Etape1_Conversion.Main`. Le fichier `target/padchest.xml` est créé.
Lancer ensuite `Etape1_Conversion.ExtraitXML` pour créer `extrait.xml`
(les 200 premières images).

# Pour l'étape 2 : 
Rien à lancer. La DTD (`Etape2_Structure/DTD.dtd`) et la XSD
(`Etape2_Structure/XSD.xsd`) sont utilisées par les parsers de l'étape 3.
Le XML est relié à la DTD par la ligne `<!DOCTYPE padchest SYSTEM "...">`.


# Pour l'etape 3 :
Lancer `Etape3_parsing.ParserSAX`, puis `Etape3_parsing.ParserDOM`.
Chacun valide le XML puis affiche les 3 statistiques.
La validation DTD est activée par le réglage :

    fabrique.setValidating(true);

et pour XSD :

    setNamespaceAware(true); ,
    .setSchema( javax.xml.validation.SchemaFactory
                  .newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI)
                         .newSchema(new File("src/main/resources/XSD.xsd"))


# Pour l'étape 4
Lancer `Etape4_xslt.TransformationXSLT`. Le dossier `site/` est créé.
Ouvrir `site/index.html` dans un navigateur() (Click droit sur index.html -> Open in -> Browser -> firefox )
(ou le copier dans un serveur Apache / BaseX, voir plus haut)


# Pour l'étape 5

Télécharger BaseX (version 13) sur basex.org, puis ouvrir BaseX GUI.
Créer la base : Database → New → choisir `target/padchest.xml`, nom de la base `padchest`.
Ouvrir les fichiers `Etape5_xquery/*.xq` et les exécuter avec ▶

