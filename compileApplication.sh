#!/bin/bash
javac -d ScotlandYardv2.4/bin ScotlandYardv2.4/src/game/*.java ScotlandYardv2.4/src/i18n/*.java
cd ScotlandYardv2.4/bin
jar cfe sy.jar game/PlayGame Translation_de.properties Translation.properties game/ i18n/
cd ../..
mvn clean install