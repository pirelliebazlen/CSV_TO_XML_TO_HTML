(: Requête 2 : les 10 labels les plus fréquents, avec leur nombre d'apparitions.
   On compte les <label> de <labels> (une fois par étude), PAS ceux de <phrases>,
   exactement comme le parser Java. :)

let $doc := db:get("padchest")
let $top :=
  for $label in $doc//rapport/labels/label
  group by $ref := $label/@ref               (: un groupe par label :)
  order by count($label) descending           (: $label = tous les <label> du groupe :)
  return <label nom="{$doc//labelDef[@id = $ref]}" nombre="{count($label)}"/>
return subsequence($top, 1, 10)               (: les 10 premiers :)
