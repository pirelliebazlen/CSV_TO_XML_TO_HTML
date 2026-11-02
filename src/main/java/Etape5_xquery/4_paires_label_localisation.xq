(: Requête 4 : les paires (label, localisation) les plus fréquentes,
   extraites de LabelsLocalizationsBySentence (<phrases>).
   Dans chaque phrase, chaque label est associé à chaque localisation de la même phrase :
     <phrase><label ref="L2"/><loc ref="LOC1"/><loc ref="LOC4"/></phrase>
     -> (L2, LOC1) et (L2, LOC4) :)

let $doc := db:get("padchest")
let $top :=
  for $phrase in $doc//phrases/phrase
  for $l in $phrase/label
  for $c in $phrase/loc
  group by $paire := $l/@ref || " " || $c/@ref     (: une clé par paire, ex. "L2 LOC1" :)
  let $refs := tokenize($paire, " ")
  order by count($l) descending
  return <paire label="{$doc//labelDef[@id = $refs[1]]}"
                localisation="{$doc//locDef[@id = $refs[2]]}"
                nombre="{count($l)}"/>
return subsequence($top, 1, 10)
