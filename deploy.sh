#!/usr/bin/env bash

WEBSPACE=/var/www/vhosts/elexis.ch/httpdocs/ungrad

P2=${WEBSPACE}/p2/features

mkdir -p ${P2}/${BUILD_NUMBER}

cp -R ungrad-p2site/target/repository/* ${P2}/${BUILD_NUMBER}

rm ${P2}/latest
ln -s ${BUILD_NUMBER} ${WEBSPACE}/p2/features/latest
