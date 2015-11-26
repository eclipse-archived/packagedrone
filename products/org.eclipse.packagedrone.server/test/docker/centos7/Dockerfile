FROM centos:7

MAINTAINER Jens Reimann <ctron@dentrassi.de>

COPY tmp/org.eclipse.packagedrone.server.rpm /
RUN yum -y install org.eclipse.packagedrone.server.rpm

# Package drone is running on port 8080

EXPOSE 8080

CMD ["/usr/lib/package-drone-server/instance/server"]