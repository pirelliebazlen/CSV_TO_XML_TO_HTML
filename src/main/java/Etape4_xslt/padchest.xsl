<?xml version="1.0" encoding="UTF-8"?>
<!--
    padchest.xsl : transforme padchest.xml en site web HTML (XSLT 3.0, Saxon-HE).

    Résultat :
      index.html           -> statistiques + liens vers les pages
      patients-1.html ...  -> les patients, 500 par page (sinon une seule page
                              ferait des dizaines de Mo et le navigateur ramerait)
-->
<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="xs">

    <xsl:output method="html" html-version="5" encoding="UTF-8" indent="no"/>

    <!-- Nombre de patients par page (modifiable en ligne de commande : parPage=1000) -->
    <xsl:param name="parPage" as="xs:integer" select="500"/>

    <!-- Index sur le dictionnaire : key('terme', 'L1') renvoie <labelDef id="L1">normal</labelDef> -->
    <xsl:key name="terme" match="labelDef | locDef" use="@id"/>

    <!-- Nombre total de pages -->
    <xsl:variable name="nbPages" select="xs:integer(ceiling(count(/padchest/patient) div $parPage))"/>

    <!--    1. Point de départ : la racine du document -> index.html -->
    <xsl:template match="/">
        <html lang="fr">
            <xsl:call-template name="entete">
                <xsl:with-param name="titre" select="'PadChest - Accueil'"/>
            </xsl:call-template>
            <body>
                <h1>PadChest : Rapport medicaux </h1>
                <!-- Chiffres clés -->
                <div class="cartes">
                    <div class="carte"><b><xsl:value-of select="count(//patient)"/></b>patients</div>
                    <div class="carte"><b><xsl:value-of select="count(//etude)"/></b>études</div>
                    <div class="carte"><b><xsl:value-of select="count(//image)"/></b>images</div>
                </div>

                <!-- Images par projection : on regroupe les images par valeur de @projection -->
                <h2>Images par projection</h2>
                <table class="petit">
                    <tr><th>Projection</th><th>Images</th></tr>
                    <xsl:for-each-group select="//image" group-by="@projection">
                        <xsl:sort select="count(current-group())" order="descending"/>
                        <tr>
                            <td><xsl:value-of select="current-grouping-key()"/></td>
                            <td class="nombre"><xsl:value-of select="count(current-group())"/></td>
                        </tr>
                    </xsl:for-each-group>
                </table>

                <!-- Top 10 des labels : on regroupe les <label> des rapports par @ref -->
                <h2>Les 10 labels les plus fréquents</h2>
                <table class="petit">
                    <tr><th>#</th><th>Label</th><th>Études</th></tr>
                    <xsl:for-each-group select="//rapport/labels/label" group-by="@ref">
                        <xsl:sort select="count(current-group())" order="descending"/>
                        <xsl:if test="position() le 10">
                            <tr>
                                <td><xsl:value-of select="position()"/></td>
                                <td><xsl:value-of select="key('terme', current-grouping-key())"/></td>
                                <td class="nombre"><xsl:value-of select="count(current-group())"/></td>
                            </tr>
                        </xsl:if>
                    </xsl:for-each-group>
                </table>

                <!-- Liens vers les pages de patients -->
                <h2>Liste des patients</h2>
                <xsl:call-template name="navigation">
                    <xsl:with-param name="pageCourante" select="0"/>
                </xsl:call-template>
            </body>
        </html>

        <!-- Les pages de patients : on découpe les patients en paquets de $parPage -->
        <xsl:for-each-group select="/padchest/patient"
                            group-adjacent="(position() - 1) idiv $parPage">
            <xsl:variable name="numero" select="current-grouping-key() + 1"/>
            <!-- xsl:result-document = écrire un AUTRE fichier de sortie -->
            <xsl:result-document href="patients-{$numero}.html">
                <xsl:call-template name="pagePatients">
                    <xsl:with-param name="numero" select="$numero"/>
                    <xsl:with-param name="patients" select="current-group()"/>
                </xsl:call-template>
            </xsl:result-document>
        </xsl:for-each-group>
    </xsl:template>


    <!-- =============================================================
         2. Une page de patients
         ============================================================= -->
    <xsl:template name="pagePatients">
        <xsl:param name="numero"/>
        <xsl:param name="patients"/>
        <html lang="fr">
            <xsl:call-template name="entete">
                <xsl:with-param name="titre" select="concat('PadChest - Patients page ', $numero)"/>
            </xsl:call-template>
            <body>
                <p><a href="index.html">← Accueil</a></p>
                <h1>Patients - page <xsl:value-of select="$numero"/> / <xsl:value-of select="$nbPages"/></h1>
                <xsl:call-template name="navigation">
                    <xsl:with-param name="pageCourante" select="$numero"/>
                </xsl:call-template>

                <table>
                    <tr>
                        <th>Patient</th><th>Naissance</th><th>Sexe</th>
                        <th>Étude</th><th>Méthode</th><th>Labels</th><th>Localisations</th><th>Images</th>
                    </tr>
                    <xsl:apply-templates select="$patients"/>
                </table>
            </body>
        </html>
    </xsl:template>


    <!-- =============================================================
         3. Un patient : une ligne par étude.
            Les colonnes du patient sont fusionnées (rowspan) sur toutes ses études.
         ============================================================= -->
    <xsl:template match="patient">
        <xsl:variable name="nbEtudes" select="count(etude)"/>
        <xsl:for-each select="etude">
            <tr>
                <!-- Les infos du patient seulement sur la 1re ligne -->
                <xsl:if test="position() = 1">
                    <td rowspan="{$nbEtudes}" class="patient"><xsl:value-of select="../@id"/></td>
                    <td rowspan="{$nbEtudes}"><xsl:value-of select="(../@naissance, '?')[1]"/></td>
                    <td rowspan="{$nbEtudes}"><xsl:value-of select="(../@sexe, '?')[1]"/></td>
                </xsl:if>
                <xsl:apply-templates select="."/>
            </tr>
        </xsl:for-each>
    </xsl:template>


    <!-- =============================================================
         4. Une étude : les cellules de la ligne
         ============================================================= -->
    <xsl:template match="etude">
        <td>
            <xsl:value-of select="@id"/>
            <!-- <details> : le texte du rapport se déplie au clic, sans JavaScript -->
            <xsl:if test="rapport/texte">
                <details>
                    <summary>rapport</summary>
                    <xsl:value-of select="rapport/texte"/>
                </details>
            </xsl:if>
        </td>
        <td><xsl:value-of select="rapport/@methode"/></td>
        <td>
            <!-- ref="L1" -> on va chercher le mot dans le dictionnaire -->
            <xsl:for-each select="rapport/labels/label">
                <span class="label"><xsl:value-of select="key('terme', @ref)"/></span>
            </xsl:for-each>
        </td>
        <td>
            <xsl:for-each select="rapport/localisations/loc">
                <span class="loc"><xsl:value-of select="substring-after(key('terme', @ref), 'loc ')"/></span>
            </xsl:for-each>
        </td>
        <td>
            <xsl:for-each select="image">
                <span class="image" title="{@fichier}"><xsl:value-of select="@projection"/></span>
            </xsl:for-each>
        </td>
    </xsl:template>

    <!--  5. Morceaux réutilisés : <head> + style, et la barre de pages -->
    <xsl:template name="entete">
        <xsl:param name="titre"/>
        <head>
            <meta charset="UTF-8"/>
            <meta name="viewport" content="width=device-width, initial-scale=1"/>
            <title><xsl:value-of select="$titre"/></title>
            <style>
                body    { font-family: system-ui, sans-serif; margin: 2rem; color: #1f2933; background: #f7f8fa; }
                h1      { color: #1d4e89; }
                h2      { margin-top: 2rem; color: #1d4e89; }
                a       { color: #1d4e89; }
                table   { border-collapse: collapse; background: white; width: 100%; font-size: 0.9rem; }
                th, td  { border: 1px solid #d9dee5; padding: 0.4rem 0.6rem; text-align: left; vertical-align: top; }
                th      { background: #1d4e89; color: white; position: sticky; top: 0; }
                table.petit { width: auto; min-width: 20rem; }
                td.nombre   { text-align: right; font-variant-numeric: tabular-nums; }
                td.patient  { font-weight: bold; background: #eef3f9; }
                .cartes { display: flex; gap: 1rem; flex-wrap: wrap; }
                .carte  { background: white; border: 1px solid #d9dee5; border-radius: 8px; padding: 1rem 1.5rem; }
                .carte b { display: block; font-size: 1.8rem; color: #1d4e89; }
                .label, .loc, .image { display: inline-block; border-radius: 4px; padding: 0 0.35rem; margin: 0.1rem; }
                .label  { background: #e3edf9; }
                .loc    { background: #e6f4ea; }
                .image  { background: #fdf0d5; cursor: help; }
                details summary { cursor: pointer; color: #52606d; font-size: 0.8rem; }
                .pages  { display: flex; gap: 0.3rem; flex-wrap: wrap; margin: 1rem 0; }
                .pages a, .pages span { padding: 0.2rem 0.5rem; border: 1px solid #d9dee5; border-radius: 4px; background: white; text-decoration: none; }
                .pages span { background: #1d4e89; color: white; }
            </style>
        </head>
    </xsl:template>

    <xsl:template name="navigation">
        <xsl:param name="pageCourante"/>
        <div class="pages">
            <xsl:for-each select="1 to $nbPages">
                <xsl:choose>
                    <xsl:when test=". = $pageCourante"><span><xsl:value-of select="."/></span></xsl:when>
                    <xsl:otherwise><a href="patients-{.}.html"><xsl:value-of select="."/></a></xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </div>
    </xsl:template>

</xsl:stylesheet>
