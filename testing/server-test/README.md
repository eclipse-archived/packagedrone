This project performs an integration test of the Eclipse Package Drone™ server.

It does require:

 * That you did run `mvn clean package` on the main project (packagedrone.git)
 * That you did run `mvn clean install` on the "secondary" project (packagedrone.git/secondary). You may need it to run with `-Dgpg.skip=true`
 * TCP Port 8081 is available
 
Afterwards you may need to perform "Maven" -> "Update Project..." (Alt+F5) on this project.