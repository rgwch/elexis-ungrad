#! /bin/bash

CORE=file:~/dev/elexis-3.12-builds/core-repository
BASE=file:~/dev/elexis-3.12-builds/base-repository
TARGET=https://download.elexis.info/elexis/target/2023-09-java17/
mvn -V clean verify -Dtycho.localArtifacts=ignore -Dmaven.test.skip=true -DTARGET=$TARGET -DCORE=$CORE -DBASE=$BASE

