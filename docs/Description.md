# Description

## Generate code from api spec
```shell
# export JAVA_POST_PROCESS_FILE="/usr/local/bin/clang-format -i"
# maven generate-resources
```

## Git
### Add existing project directory to github project
```shell
git init
git add .
git commit -m "my commit"
git remote set-url origin git@github.com:username/repo.git
git push origin main
```
## OpenAPI
See 
- https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-maven-pluginsee 
- https://apitools.dev/swagger-parser/online/# 

## H2
Start h2 console in seperate session
```shell
# java -jar ~/.m2/repository/com/h2database/h2/1.4.200/h2-1.4.200.jar
```

## Generate code from OpenAPI Spec
- See https://www.north-47.com/knowledge-base/generate-spring-boot-rest-api-using-swagger-openapi/
## JPA
See
- https://attacomsian.com/blog/spring-data-jpa-many-to-many-mapping
- https://www.javaguides.net/2019/08/spring-boot-hibernate-many-to-many-example.html


## Requests

```shell
curl -H "Accept: */*" -H "Content-Type: application/problem+json,application/json" -H "x-api-key: 0af5cbf4-b6b1-405a-b7b2-53934f03af48" https://service.pre.omgevingswet.overheid.nl/publiek/catalogus/api/opvragen/v3/conceptschemas?uri=http%3A%2F%2Fregelgeving.omgevingswet.overheid.nl%2Fid%2Fconceptscheme%2FRegelgeving&gepubliceerdDoor=https%3A%2F%2Fstandaarden.overheid.nl%2Fowms%2Fterms%2FMinisterie_van_Binnenlandse_Zaken_en_Koninkrijksrelaties&geldigOp=2021-04-14&page=1&pageSize=50&_expandScope=collecties&_expandScope=concepten

curl -v -X GET -H "Accept: */*" -H "Content-Type: application/problem+json,application/json" -H "x-api-key: 0af5cbf4-b6b1-405a-b7b2-53934f03af48" https://service.pre.omgevingswet.overheid.nl/publiek/catalogus/api/opvragen/v3/conceptschemas?geldigOp="2021-04-14"&page="1"&pageSize="50"&_expandScope=collecties

curl -v -H 'Accept: */*' -H 'Content-Type: application/problem+json,application/json' -H 'x-api-key: 0af5cbf4-b6b1-405a-b7b2-53934f03af48' 'https://service.pre.omgevingswet.overheid.nl/publiek/catalogus/api/opvragen/v3/conceptschemas?geldigOp=2021-04-14&page=1&pageSize=50&_expandScope=collecties&_expandScope=concepten'

```

## Maven
```shell 
export JAVA_POST_PROCESS_FILE="/usr/local/bin/clang-format -i"
mvn -Dopenapi.generator.maven.plugin.generateAliasAsModel=true clean test 
```

## Checks

```sql
select count(*), cs.id as conceptschema_id, c.id as collectie_id from conceptschema cs, concept c where c.conceptschema_id = cs.id  group by cs.id, c.id order by cs.id, c.id;

select 'collectie per conceptschema' as title, count(*), sub.conceptschema_id
from
    (select count(*), cs.id as conceptschema_id, c.id as collectie_id from conceptschema cs, collectie c where c.conceptschema_id = cs.id  group by cs.id, c.id order by cs.id, c.id) sub
group by sub.conceptschema_id
order by sub.conceptschema_id
;

select 'concept per conceptschema'  as title, count(*), sub.conceptschema_id
from 
    (select count(*), cs.id as conceptschema_id, c.id as collectie_id from conceptschema cs, concept c where c.conceptschema_id = cs.id  group by cs.id, c.id order by cs.id, c.id) sub
group by sub.conceptschema_id
order by sub.conceptschema_id
;

-- waardelijsten
select naam, titel, versie, eigenaar from waardelijst where versie = '1.0.9';
select w.naam, w.titel, w.versie, c.naam from waardelijst w, waardelijst_waarde ww, concept c where w.versie = '1.0.9' and ww.waardelijst_id = w.id and ww.concept_id = c.id order by w.naam, c.naam;

```
