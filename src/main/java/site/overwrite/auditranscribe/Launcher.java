/*
 * Launcher.java
 *
 * Created on 2022-02-10
 * Updated on 2022-02-12
 *
 * Description: Contains the application launcher class.
 *
 * Note:
 *  - An additional Launcher class is needed to be able to start the JavaFX application from a JAR file.
 *
 * References:
 *  - https://stackoverflow.com/questions/53533486/how-to-open-javafx-jar-file-with-jdk-11
 *  - https://stackoverflow.com/questions/52653836/maven-shade-javafx-runtime-components-are-missing/52654791#52654791
 */

package site.overwrite.auditranscribe;

public class Launcher {

    public static void main(String[] args) {
        MainApplication.main(args);
    }
}
