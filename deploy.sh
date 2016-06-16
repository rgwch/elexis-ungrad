#!/usr/bin/env bash

P2=${WEBSPACE}/p2/features

mkdir -p ${P2}/${BUILD_NUMBER}

cp -R ungrad-p2site/target/repository/* ${P2}/${BUILD_NUMBER}

rm ${P2}/latest
ln -s ${P2}/${BUILD_NUMBER} ${P2}/latest
