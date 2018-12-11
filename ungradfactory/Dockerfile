FROM maven:3.6-jdk-8-slim

RUN apt-get update && apt-get install -y git \
  && mkdir /opt/elexisfactory && mkdir -p /opt/artifacts && cd /opt/elexisfactory \
  && git clone https://github.com/rgwch/elexis-3-core \
  && git clone https://github.com/rgwch/elexis-3-base \
  && git clone https://github.com/rgwch/elexis-ungrad \
  && cd elexis-3-core \
  && mvn -V clean verify -Dtycho.localArtifacts=ignore -Dmaven.test.skip=true -P all-archs  \
  && cd ../elexis-3-base \
  && mvn -V clean verify -Dtycho.localArtifacts=ignore -Dmaven.test.skip=true \
  && cd ../elexis-ungrad \
  && mvn -V clean verify -Dtycho.localArtifacts=ignore -Dmaven.test.skip=true 


ADD build.sh /opt/elexisfactory/build.sh
CMD ["/opt/elexisfactory/build.sh"]
