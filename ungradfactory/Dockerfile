FROM debian:13

ARG BRANCH=ungrad-2026

RUN apt-get update && apt-get install -y git openjdk-21-jdk-headless maven\
  && mkdir /opt/elexisfactory && mkdir -p /opt/artifacts && cd /opt/elexisfactory \
  && git clone -b $BRANCH https://github.com/rgwch/elexis-3-core \
  && git clone -b $BRANCH https://github.com/rgwch/elexis-3-base \
  && git clone -b $BRANCH https://github.com/rgwch/elexis-ungrad

ADD build.sh /opt/elexisfactory/build.sh