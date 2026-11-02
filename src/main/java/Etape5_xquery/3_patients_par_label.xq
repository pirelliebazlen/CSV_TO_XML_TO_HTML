(: Requête 3 : pour un label donné en paramètre, la liste des patients concernés,
   triés par année de naissance.
   Le paramètre est "external" : on peut le changer dans BaseX sans toucher la requête
   (sinon il vaut "pneumonia"). :)

declare variable $label external := "pneumonia";

let $doc := db:get("padchest")
let $id  := $doc//labelDef[. = $label]/@id
let $patients := $doc//patient[etude/rapport/labels/label/@ref = $id]
return
  <patients label="{$label}" nombre="{count($patients)}">{
    for $p in $patients
    order by $p/@naissance empty greatest     (: sans année -> à la fin :)
    return <patient id="{$p/@id}" patientID="{$p/@patientID}"
                    naissance="{$p/@naissance}" sexe="{$p/@sexe}"/>
  }</patients>
