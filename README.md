_Note: This project is published as an example use case for AGP's `gradle-api` artifact and is
not actually published to the Gradle Plugin Portal._

Sample plugin showing how Checkstyle can be configured for Android projects.

The plugin creates Checkstyle tasks for each Android sourceSet, as well as a parent task that
depends on each of the individual tasks so they can be run with a single command.

E.g. for a project with `debug` and `release` variants and no flavors, the following tasks would
be created:

```bash
Verification tasks
------------------
checkstyle - Parent task that runs all other Checkstyle tasks in the project
checkstyleAndroidTest - Run Checkstyle analysis for androidTest source set
checkstyleAndroidTestDebug - Run Checkstyle analysis for androidTestDebug source set
checkstyleAndroidTestRelease - Run Checkstyle analysis for androidTestRelease source set
checkstyleDebug - Run Checkstyle analysis for debug source set
checkstyleMain - Run Checkstyle analysis for main source set
checkstyleRelease - Run Checkstyle analysis for release source set
checkstyleTest - Run Checkstyle analysis for test source set
checkstyleTestDebug - Run Checkstyle analysis for testDebug source set
checkstyleTestRelease - Run Checkstyle analysis for testRelease source set
```

Each of these tasks are separately cacheable and have separate up-to-date checks. If a particular
source set doesn't have any files, the task will be skipped as `NO-SOURCE`.

## Usage

Build a snapshot with `./gradlew publishToMavenLocal`, then add a dependency with:
```groovy
plugins {
    id 'com.drewhannay.checkstyle-android' version '1.0.0'
}
```

Ensure that a Checkstyle configuration file exists at `config/checkstyle/checkstyle.xml` in your
root project. E.g.

```xml
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC
    "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
    "https://checkstyle.org/dtds/configuration_1_3.dtd">

<module name="Checker">
    <module name="JavadocPackage"/>
    <module name="TreeWalker">
        <module name="AvoidStarImport"/>
        <module name="ConstantName"/>
        <module name="EmptyBlock"/>
    </module>
</module>
```
