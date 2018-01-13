#! /bin/bash

mvn -V clean verify -Dtycho.localArtifacts=ignore -Dmaven.test.skip=true
