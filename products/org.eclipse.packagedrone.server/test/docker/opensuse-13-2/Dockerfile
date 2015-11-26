FROM opensuse:13.2

MAINTAINER Jens Reimann <jens.reimann@ibh-systems.com>

COPY tmp/org.eclipse.packagedrone.server.rpm /
RUN zypper --non-interactive --no-gpg-checks install org.eclipse.packagedrone.server.rpm

# Package drone is running on port 8080

EXPOSE 8080

CMD ["/usr/lib/package-drone-server/instance/server"]