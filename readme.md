# GeoLoc-Util

GeoLoc-Util is a project built for the Fetch SDET Take-Home Test.


## Prerequisites for running the project

You'll need Java (project built with Java 21.0.6), Groovy (project built with Groovy 4.0.25) Gradle (project built with Gradle 8.12.1) to run this project. 

If you aren't on those versions, you can use an install guide below:
[Java Install Guide](https://www.geeksforgeeks.org/download-and-install-java-development-kit-jdk-on-windows-mac-and-linux/) or your JDK/SDK manager of choice
[Groovy Install Guide](https://groovy-lang.org/install.html) or your SDK manager of choice
[Gradle Install Guide](https://gradle.org/install/) or your package manager of choice


## Build the project (if necessary)

```Mac/Unix
./gradlew build
```

```Windows
.\gradlew.bat build
```


## Running the utility

You can call the utility using the JAR located at `GeoLoc-Util\app\build\libs\geoloc-util-all.jar`

Call it with your arguments of choice:

```Mac/Unix
java -jar app/build/libs/geoloc-util-all.jar "90210" "New York, NY"
```

```Windows
java -jar app\build\libs\geoloc-util-all.jar "90210" "New York, NY"
```


## Running the automated tests

```Mac/Unix
./gradlew runTests
```

```Windows
.\gradlew.bat runTests
```

You can include `--info` if you want more information as the tests run.

Results of the tests can be found at `app\build\reports\tests\runTests\index.html`. Open it in your browser of choice.


## Examining the code

The utility lives in `app\src\main\groovy\App.groovy`. The test code lives in `app\src\test\groovy\GeoLocUtilIntegrationSpec.groovy`.