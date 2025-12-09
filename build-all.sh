#! /bin/bash

set -o errexit

cd ../elexis-3-core
 git pull pinas ungrad-2026
 mvn -V clean verify  -Dtycho.localArtifacts=ignore -DskipTests -Dmaterialize-products

cd ../elexis-3-base
 git pull pinas ungrad-2026
 mvn -V clean verify  -Dtycho.localArtifacts=ignore -DskipTests 

cd ../elexis-ungrad-plugins
 git pull pinas ungrad-2026
 mvn -V clean verify  -Dtycho.localArtifacts=ignore -DskipTests 

