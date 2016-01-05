```
This is a fork of version 0.1 of the original ADR plugin by Forsety and includes minor fixes necessary to work with the latest version of the Android Gradle plugin (currently 2.0.0-alpha3).
```

Android Drawable Resizer Gradle Plugin
======================================

This Gradle plugin will automatically resize Android drawables for lower screen densities, handling 
build types, flavors, source sets mapping and all your qualifiers. No more headache from sizing tons 
of images and storing them in your repository. Drawables are generated only when source drawable has 
changed and stored under the project 'build' folder.

Usage
-----

* Clone the project and run `gradle clean install`
* Add this to your Android project 'build.gradle' file
```groovy
buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath 'com.github.forsety:adr:0.1-psegina'
    }
}

apply plugin: 'com.github.forsety.adr'
```

_Note: Download and install [JDK8](http://www.oracle.com/technetwork/java/javase/downloads/index.html) if you haven't already.
Also set it as project default JDK and don't worry, your Android projects will build just fine._ 

Configuration
-------------

In your 'build.gradle' file add:
```groovy
adr {
    minDensity "mdpi"
    baseDensity "xxhdpi"
    generateTvDpi false
}
```
Example shows default configuration, so if it suits your needs - you don't need configuration at all.
