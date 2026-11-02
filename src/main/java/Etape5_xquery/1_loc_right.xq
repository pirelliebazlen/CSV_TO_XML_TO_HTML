(: Requête 1 : nombre d'images dont l'étude contient la localisation "loc right".
   1) on cherche l'id de "loc right" dans le dictionnaire (ex. "LOC4")
   2) on garde les études dont le rapport cite cet id dans <localisations>
   3) on compte les images de ces études :)

let $idLocRight := db:get("padchest")//locDef[. = "loc right"]/@id
return count(db:get("padchest")//etude[rapport/localisations/loc/@ref = $idLocRight]/image)
