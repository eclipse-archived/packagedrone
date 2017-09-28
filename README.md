# Eclipse Package Drone™

Package Drone is a software artifact repository with an initial focus on OSGi.
The system can be enhanced by plugins to also support other types of artifacts
and repository interfaces.

Eclipse Package Drone™ is a projected hosted by the Eclipse Foundation. It is open source and licensed under the EPL.

## Building

In order to re-build Package Drone you will need Maven 3.3+ and Java 8.

Build the P2 target environment first:

    mvn install -f runtime/pom.xml

The build the main project:

    mvn install

Build the secondary artifacts:

    mvn install -f secondary/pom.xml -Dgpg.skip

Building the target environment is only required once, or after the target
environment has been updated. The secondary artifacts rebuilds parts of
Package Drone in way it can be distributed on Maven Central.

## More Information

Also see:
 * The project page at Eclipse – https://eclipse.org/package-drone
 * The GitHub repository – https://github.com/eclipse/packagedrone
 * The blog – http://packagedrone.org
  * List of features – http://packagedrone.org/features/
 * The demo system – https://thedrone.packagedrone.org
 * The wiki at Eclipse – https://wiki.eclipse.org/PackageDrone
 * The Mattermost channel - https://mattermost.eclipse.org/eclipse/channels/package-drone
 * Talk @ EclipseCon 2015 "Managing OSGi artifacts with Package Drone" - https://www.youtube.com/watch?v=CTPYBqOxXz4
