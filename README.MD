## OSM-Scotlandyard

## Vorraussetzungen
1. Java
2. Maven
3. OSM-Datei (in .osm.pbf Format), die den gewünschten Kartenvisualisierungsbereich abdeckt. Diese Dateien lassen sich zum Beispiel durch [Geofabrik](https://download.geofabrik.de/) beziehen. Eine Beispieldatei [greater-london.osm.pbf](geofiles/greater-london.osm.pbf) im Ordner [geofiles](geofiles), deckt den Bereich Greater London ab. 

#### Greater London
![Greater London](https://user-images.githubusercontent.com/45538729/158072585-34aece11-f732-4088-9b77-bae192392934.png)

## Programm kompilieren
1. Terminal öffnen
2. In den Ordner des Projekts navigieren
3. Shell script [compileApplication.sh](./compileApplication.sh) durch Eingabe von `chmod +x compileApplication.sh` als ausführbar kennzeichnen
4. Shell script [compileApplication.sh](./compileApplication.sh), das die Kompilation automatisiert, durch Eingabe von `sh compileAppliction.sh` starten

## Programm starten
1. Terminal öffnen
2. In den Ordner des Projekts navigieren
3. Shell script [startProgramm.sh](startProgramm.sh) durch Eingabe von `chmod +x startProgramm.sh` als ausführbar kennzeichnen
4. Shell script [startProgramm.sh](startProgramm.sh), das den Start des Programms automatisiert, durch Eingabe von `sh startProgramm.sh` starten

## Programm Nutzung
1. Nach Aufforderung den Pfad zur OSM-Datei Datei angeben.
2. Nach Aufforderung eine beliebige Adresse eingeben, die innerhalb des Abdeckungsbereichs der zuvor bereit gestellten OSM-Datei liegt. Beispiele hierfür sind `London bigben`, `2 Lincoln Memorial Circle NW`, `Stuttgart pfaffenwaldring 30`. Für detailliertere Informationen siehe [MapBox Geocoding](https://docs.mapbox.com/help/getting-started/geocoding/).
3. Bei einer erfolgreichen Adresseingabe, wird auf Basis der Routen öffentlicher Verkehrsmittel die Grundlage des Spielbretts erstellt.
4. Nach öffnen der GUI, durch einen Klick auf den Button `Spiel beginnen`, das Spiel starten.

