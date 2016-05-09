# docker file for testing APT installation from official download location

FROM ubuntu:16.04
MAINTAINER Jens Reimann <ctron@dentrassi.de>

# let debian install without prompts

ENV DEBIAN_FRONTEND noninteractive

# Refresh and install a few tools

RUN apt-get update ; apt-get -y install software-properties-common dpkg gdebi-core add-apt-key
RUN add-apt-repository "deb http://archive.ubuntu.com/ubuntu/ xenial universe multiverse" ; add-apt-repository "deb http://archive.ubuntu.com/ubuntu/ xenial-updates universe multiverse"
RUN apt-get update ; apt-get -y install openjdk-8-jre-headless

# import key

RUN add-apt-key 320E6224

# add repository

RUN add-apt-repository "deb http://download.eclipse.org/package-drone/milestone/0.13/ubuntu package-drone default"

# install

RUN apt-get update ; apt-get -y install org.eclipse.packagedrone.server

# Package drone is running on port 8080

EXPOSE 8080

CMD ["/usr/lib/package-drone-server/instance/server"]