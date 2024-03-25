## ClinicApp

Introduction
------------
The application uses Clean Architecture based on MVP.
The application is written entirely in Kotlin.

Android Jetpack is used as an Architecture components is Room, Work manager,Paging, Material design, ViewModel, and LiveData,

The application does network HTTP requests via Retrofit and GSON. For UI update and database operation we used RxJava library. 

Material design component used. The card view used to show list item in screen.

Getting Started
---------------
   
This project uses the Gradle build system. To build this project, use the
`gradlew build` command or use "Import Project" in Android Studio. 

But first, make sure to create the following files in the root of your project:

secrets.properties    
```
web_client_id="ID from Google Cloud"
```
    
and

keystore.properties 
```
keyAlias=
keyPassword=
storeFile=
storePassword=
```
    
Make sure that JDK used by Gradle in Android Studio is 17 or higher. To change the SDK version in Android Studio:

* Go to File, Settings, Build, Execution, Deployment
* Build tools, Gradle
* Change Gradle JDK from the dropdown. 

Finally, choose a new application ID under the app's Gradle build file, in the defaultConfig block:

```
    defaultConfig {
        applicationId "org.broadinstitute.clinicapp_test"
        ...
    }
```

This is needed because the package name in the Google Cloud Project (see below) must be unique, and 
equal to the application ID (the package name in the app's source can continue to be org.broadinstitute.clinicapp
as it can be different from the application ID).

Setting up the Cloud Console Project
---------------
1. Go to the following link - https://console.cloud.google.com/projectcreate
2. Enter the project name "ClinicApp" and set the location as "No Organization"
3. In a few seconds, you should be redirected to the "Cloud Overview" of the new project.
4. Click on the three lines in the top left corner and click on "Enabled APIs & Services" under "APIs & Services".
5. Click on "Enable APIs and Services"
6. Search for the "Google Drive API" and enable the first result.
7. Click "OAuth Consent Screen" from the options on the left.
8. Set up the OAuth Consent Screen page with the following configurations:
   * User type: External
   * App name: ClinicApp
   * User support email: developer’s email
   * Developer contact information: developer’s email
9. Search for and select the following scopes from the Scope page:
   * /auth/drive.appdata
   * /auth/drive.file
   * /auth/drive
   * /auth/drive.metadata
   * /auth/drive.readonly
10. Under Test Users, add the emails of any users that will have access to ClinicApp
11. Click "Credentials" from the options on the left.
12. Click "Create Credentials" and select "OAuth Client ID". Set up the Client ID with the following configurations:
    * Application type: Android
    * Name: (developer’s choice)
    * Package name: org.broadinstitute.clinicapp_test (same as the application ID in the app Gradle build file)
13. Find the SHA1key by the following steps:
    * Open the ClinicApp project in Android Studio
    * Click on "Gradle" on the far right
    * Click on the icon of a rectangle with a green triangle inside ("Execute Gradle Task")
    * Type "signingReport" and press enter.
    * The SHA1 key is the sequence generated in the console after "SHA1: "
14. Copy and paste the SHA1 key into the "SHA-1 certificate fingerprint" field under the Client ID page
15. Copy the Client ID under "Additional information" under "Credentials | OAuth 2.0 Client IDs" for the ClinicApp project in the Cloud Console
15. Paste the Client ID in the secrets.properties file, as the value for the web_client parameter

Libraries Used
--------------
* Foundation - Components for core system capabilities, Kotlin extensions  and automated testing.
  AppCompat - Degrade gracefully on older versions of Android.
  Android KTX - Write more concise, idiomatic Kotlin code.
  Test - An Android testing framework for unit and runtime UI tests.
  Architecture - A collection of libraries that help you design robust, testable, and maintainable apps.
  Start with classes for managing your UI component lifecycle and handling data persistence.
  Lifecycle - Create a UI that automatically responds to lifecycle events.
  Room - Database library to help execute sql query using ORM.
  AndroidX SQLite -  Implementation of the AndroidX SQLite interfaces via the Android framework APIs.
  LiveData - Build data objects that notify views when the underlying database changes.
  ViewModel - Store UI-related data that isn't destroyed on app rotations. Easily schedule asynchronous tasks for optimal execution.
  WorkManager - Scheduling mechanism

* Third party
  Retrofit 2 A configurable REST client.
  OkHttp 3 A type-safe HTTP client.
  GSON A Json - Object converter using reflection.
  RxJava - Observable pattern used to reflect change on UI
  SqlChiper - For database entity encryption

Application Development Environment
---------------
    Android Studio Android Studio Giraffe | 2022.3.1 Patch 2
    Target & Compiled Android SDK: 33
    OpenJDK 17 (Temurin)
