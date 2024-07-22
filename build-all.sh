#! /bin/bash

cd ../elexis-3-core
 mvn -V clean verify  -Dtycho.localArtifacts=ignore -DskipTests -Dmaterialize-products

cd ../elexis-3-base
 mvn -V clean verify  -Dtycho.localArtifacts=ignore -DskipTests 

cd ../elexis-ungrad-plugins
 mvn -V clean verify  -Dtycho.localArtifacts=ignore -DskipTests 
