# Testing

Die Test-Pyramide ist wie folgt definiert

1. Unit tests
2. Functional tests
3. Docker tests
4. System tests

## Unit tests

Die Businesslogik einiger _Custom Tasks_ ist in eigenen Bibliotheken ausgelagert (z.B ili2pg, CSV-Import, ...), der Task ist dabei "bloss" eine einfache Hülle. Für diese Tasks werden keine _Unit Tests_ erstellt. 

Für _Unit Tests_ sollen nur lokale, d.h. embedded Datenbanken verwendet werden, z.B. _Derby_. Müssen Funktionalitäten geprüft werden, die explizit eine PostgreSQL-Datenbank voraussetzen, werden diese in den _Functional Tests_ (als kompletter Gradle-Task) geprüft. Für reine GIS-/Geometrie-Funktionaliäten kann [H2GIS](http://www.h2gis.org/) verwendet werden (__noch zu prüfen__). Auf spezielle Testprofile o.ä. für die Unittests _mit_ externen DB-Abhängigkeiten wird verzichtet, da das die Sache nochmals verkomplizieren würde.

Die _Unit tests_ werden mit dem Befehl `./gradlew test` resp. beim Erstellen der Binaries `./gradlew build` ausgeführt.

## Functional tests

Die _Functional Tests_ testen einen ganzen GRETL-Task oder mehrere Tasks. Es werden ganze Gradle-Jobs mit [TestKit](https://docs.gradle.org/current/userguide/test_kit.html) ausgeführt und getestet. Es wird TestKit verwendet, da es sich um die gängige Art handelt, wie Gradle Plugins getestet werden. Die Verwendung von TestKit hat zur Folge, dass die Jobs nicht für die _Docker tests_ verwendet werden können:

Das Laden von Plugins via DSL erwartet neben der Plugin-ID auch immer eine Plugin-Version. Beim Testen selber mit TestKit darf/kann man keine Version verwenden. Die _Docker Tests_, die das fertige Endprodukt prüfen sollen, rufen jedoch ganz normal einen Gradle-Befehl auf (in einem Docker-Container). In diesem Fall muss zwingend beim Laden des Plugins eine Version angegeben werden.

Anmerkung: Zum jetzigen Zeitpunkt verwenden sämtliche (AGI-)Jobs die alte Syntax zum Laden von Plugins (im Buildscript Block).

Quellcode und Gradle-Jobs liegen im SourceSet `src/functionalTest/java` resp. `src/functionalTest/jobs`. Die _Functional Tests_ benötigen eine PostgreSQL/PostGIS-Datenbank. Diese wird als Docker-Image mit Gradle vor den Tests hochgefahren. Nach Abschluss der Tests wird der Container wieder gestoppt. Das Dockerfile befindet sich im Verzeichnis `tooling/test-database-pg/`. Die Datenbank ist wie folgt erreichbar:

* hostname: localhost
* port: 5432
* database: gretl
* username: ddluser
* password: ddluser
* jdbc-url: jdbc:postgresql:gretl

Die Gradle-Tasks für die Funktionstest sind in der Datei `gradle/functional-test.gradle` gespeichert.

Die _Functional Tests_ werden mit dem Befehl `./gradlew functionalTest -Pgretltest_dburi_pg=jdbc:postgresql:gretl`. DB-User und -Passwort sind in den `build.gradle`-Dateien codiert.

__TODO__: Ebenfalls in den Funktionstests werden Tasks mit Abhängigkeiten von weiteren Gradle-Plugins gestestet (Download, SSH, ...).

### Erstellen von Funktionstests

Ein Funktionstest benötigt eine JUnit-Klasse (z.B. `src/functionalTest/java/ch/so/agi/gretl/jobs/ShpValidatorTest`) und dazugehörige Gradle-Jobs, d.h. `build.gradle`-Dateien. Diese befinden sich in einem Ordner pro Job (d.h. pro `build.gradle`) im Verzeichnis `src/functionalTest/jobs/`. Zu jeder JUnit-Klasse können ein oder mehrere Jobs gehören. Dazugehörige Daten (z.B. CSV, Shapefiles etc.) liegen ebenfalls im dazugehörigen Jobs-Verzeichnis.

### Oracle-Datenbank

Falls für die Funktionstest ebenfalls eine Oracle-Datenbank gebraucht wird, kann diese ebenfalls als Docker-Container bereitgestellt werden. Die Suche nach einem praktikablen Image erweist sich als schwierig. Brauchbar scheinen:

* https://hub.docker.com/r/pengbai/docker-oracle-12c-r1/ 
* https://hub.docker.com/r/alexeiled/docker-oracle-xe-11g/ 

Wobei ersteres Images hier verwendet wird. Insbesondere die teilweise extrem langsamen Startup-Zeiten verunmöglichen die Verwendung beinahe. Docker-Befehl: `docker run -it  --name test-oracle -p 1521:1521 pengbai/docker-oracle-12c-r1docker run -it --name test-oracle -p 1521:1521 pengbai/docker-oracle-12c-r1`.

* hostname: localhost
* port: 1521
* sid: xe
* username: system
* password: oracle
* jdbc-url: jdbc:oracle:thin:@localhost:1521:xe

## Docker tests
TODO

Im inttest/jobs Directory sind die Gradle build-Skripts für einzelne Testfälle und im
inttest/src/testIntegration die Test Klassen dazu.

Lokal können die Jobs direkt (aber ohne Validierung der Ergebnisse!) ausgeführt werden mit:
"cd inttest/jobs/iliValidator"
"../../gradlew --init-script ../init.gradle --project-dir jobPath
parameter..."

Tests (also inkl. Validierung) werden im inttest Projekt ausgeführt.
"cd inttest"
"./gradlew testIntegration"

## System Tests
TODO 

## Reporting

Die Testreports liegen im jeweilingen Unterverzeichnis von `build/reports/tests/`.

