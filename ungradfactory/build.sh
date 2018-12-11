#! /bin/bash

######### Elexis Core
cd /opt/elexisfactory
rm -rf dist
if [ -d "/opt/elexisfactory/elexis-3-core" ]
then
  echo "pull elexis-3-core"
  cd elexis-3-core
  git pull
  cd ..
else
  echo clone elexis-3-core
  git clone https://github.com/rgwch/elexis-3-core
fi
cd /opt/elexisfactory/elexis-3-core
mvn clean install -Dmaven.test.skip=true -Pall-archs
mkdir /opt/elexisfactory/dist
cp -r /opt/elexisfactory/elexis-3-core/ch.elexis.core.p2site/target/products/* /opt/elexisfactory/dist

######### Elexis Base
cd /opt/elexisfactory
if [ -d "/opt/elexisfactory/elexis-3-base" ]
then
  echo "pull elexis-3-base"
  cd elexis-3-base
  git pull
  cd ..
else
  echo clone elexis-3-base
  git clone https://github.com/rgwch/elexis-3-base
fi
cd /opt/elexisfactory/elexis-3-base
mvn clean install -Dmaven.test.skip=true
mkdir /opt/elexisfactory/dist/base-b2site
cp -r /opt/elexisfactory/elexis-3-base/ch.elexis.base.p2site/target/repository/* /opt/elexisfactory/dist/base-p2site

######### Elexis Ungrad
cd /opt/elexisfactory
if [ -d "/opt/elexisfactory/elexis-ungrad" ]
then
  echo "pull elexis-ungrad"
  cd elexis-ungrad
  git pull
  cd ..
else
  echo clone elexis-ungrad
  git clone https://github.com/rgwch/elexis-ungrad
fi
cd /opt/elexisfactory/elexis-ungrad
mvn clean install -Dmaven.test.skip=true
mkdir /opt/elexisfactory/dist/ungrad-p2site
cp -r /opt/elexisfactory/elexis-ungrad/ungrad-p2site/target/repository/* /opt/elexisfactory/dist/ungrad-p2site
