FROM opensuse:13.2
MAINTAINER Jens Reimann <jens.reimann@ibh-systems.com>

# import key

RUN zypper --non-interactive install wget
RUN wget http://download.eclipse.org/package-drone/PD-GPG-KEY
RUN rpm --import PD-GPG-KEY

# add repo

RUN zypper ar -f -n PackageDrone -g http://download.eclipse.org/package-drone/milestone/0.12/opensuse13 pdrone

# install

RUN zypper --non-interactive install org.eclipse.packagedrone.server

# Package drone is running on port 8080

EXPOSE 8080

CMD ["/usr/lib/package-drone-server/instance/server"]