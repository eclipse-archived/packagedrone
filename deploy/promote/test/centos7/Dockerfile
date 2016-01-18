FROM centos:7
MAINTAINER Jens Reimann <jens.reimann@ibh-systems.com>

# import key

RUN yum install -y wget yum-utils
RUN wget http://download.eclipse.org/package-drone/PD-GPG-KEY
RUN rpm --import PD-GPG-KEY

# add repository

COPY config.repo /
RUN yum-config-manager --add-repo=config.repo 

# install

RUN yum install -y org.eclipse.packagedrone.server

# Package drone is running on port 8080

EXPOSE 8080

CMD ["/usr/lib/package-drone-server/instance/server"]