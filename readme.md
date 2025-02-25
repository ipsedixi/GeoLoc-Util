# GeoLoc-Util

GeoLoc-Util is a project built for the Fetch SDET Take-Home Test.


## Prerequisites for running the project

You'll need Java (project built with Java 21.0.6).

If you aren't on version 21, you can use the JDK Install Guide ([Mac](https://docs.oracle.com/en/java/javase/21/install/installation-jdk-macos.html) | [Linux](https://docs.oracle.com/en/java/javase/21/install/installation-jdk-linux-platforms.html) | [Windows](https://docs.oracle.com/en/java/javase/21/install/installation-jdk-microsoft-windows-platforms.html)) or your SDK manager of choice to get on that version.

## Building the project

Use this command from the project root to build the project.

| OS            | Command                |
|---------------|------------------------|
| **Mac/Linux** | `./gradlew build`      |
| **Windows**   | `.\gradlew.bat build`  |


## Running the utility

You can call the utility using the JAR located at `app/build/libs/geoloc-util-all.jar`

Call it from the project root with your arguments of choice:

| OS            | Command                                                               |
|---------------|-----------------------------------------------------------------------|
| **Mac/Linux** | `java -jar app/build/libs/geoloc-util-all.jar "90210" "New York, NY"` |
| **Windows**   | `java -jar app\build\libs\geoloc-util-all.jar "90210" "New York, NY"` |


## Running the automated tests

Run them from the project root:

| OS            | Command                  |
|---------------|--------------------------|
| **Mac/Linux** | `./gradlew runTests`     |
| **Windows**   | `.\gradlew.bat runTests` |

Results of the tests can be found at `app/build/reports/tests/runTests/index.html`. Open it in your browser of choice.


## Examining the code

The utility is in `app/src/main/groovy/App.groovy`. The test code is in `app/src/test/groovy/GeoLocUtilIntegrationSpec.groovy`.