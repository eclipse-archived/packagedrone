FROM ubuntu:15.10

MAINTAINER Jens Reimann <ctron@dentrassi.de>

# Set this to ensure debconf works

ENV DEBIAN_FRONTEND noninteractive

# Install "add-apt-repository

RUN apt-get update ; apt-get -y install software-properties-common dpkg

# Enable universe and multiverse

RUN add-apt-repository "deb http://archive.ubuntu.com/ubuntu/ wily universe multiverse" ; add-apt-repository "deb http://archive.ubuntu.com/ubuntu/ wily-updates universe multiverse"

# Install OpenJDK 8

RUN apt-get update ; apt-get -y install openjdk-8-jre-headless

# Install package drone

RUN apt-key adv --keyserver hkp://keys.gnupg.net --recv-keys 97A336A9320E6224 ; sh -c "echo deb http://repo.dentrassi.de/apt package-drone main > /etc/apt/sources.list.d/PackageDrone.list"
RUN apt-get update ; apt-get -y install org.eclipse.packagedrone.server

# Package drone is running on port 8080

EXPOSE 8080

CMD ["/usr/lib/package-drone-server/instance/server"]