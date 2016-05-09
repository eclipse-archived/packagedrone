FROM ubuntu:16.04

MAINTAINER Jens Reimann <ctron@dentrassi.de>

# Set this to ensure debconf works

ENV DEBIAN_FRONTEND noninteractive

# Install "add-apt-repository

RUN apt-get update ; apt-get -y install software-properties-common dpkg gdebi-core

# Enable universe and multiverse

RUN add-apt-repository "deb http://archive.ubuntu.com/ubuntu/ xenial universe multiverse" ; add-apt-repository "deb http://archive.ubuntu.com/ubuntu/ xenial-updates universe multiverse"

# Install OpenJDK 8

RUN apt-get update ; apt-get -y install openjdk-8-jre-headless

# Install package drone ... from local source

COPY org.eclipse.packagedrone.server.deb org.eclipse.packagedrone.server.deb
RUN gdebi -n org.eclipse.packagedrone.server.deb

# Package drone is running on port 8080

EXPOSE 8080

CMD ["/usr/lib/package-drone-server/instance/server"]