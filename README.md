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
    
    
```
secrets.properties
web_client_id="ID from Google Cloud"
```
    
and
    
```
keystore.properties
keyAlias=
keyPassword=
storeFile=
storePassword=
```
    
Also, make sure that JDK used by Gradle in Android Studio is 17 or higher. To change the SDK version in Android Studio:

* Go to File, Settings, Build, Execution, Deployment
* Build tools, Gradle
* Change Gradle JDK from the dropdown. 
   
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
    Android Studio 3.4.1
    Target & Compiled Android SDK: 28
    Java 1.8

Setting up the Cloud Console Project
---------------
1. Go to the following link - https://console.cloud.google.com/projectcreate
2. Enter the project name “ClinicApp” and set the location as “No Organization”
3. In a few seconds, you should be redirected to the “Cloud Overview” of the new project.
4. Click on the three lines in the top left corner and click on “Enabled APIs & Services” under “APIs & Services”.
5. Click on “Enable APIs and Services”
6. Search for the “Google Drive API” and enable the first result.
7. Click “OAuth Consent Screen” from the options on the left.
8. Set up the OAuth Consent Screen page with the following configurations:
   a) User type: External
   b) App name: ClinicApp
   c) User support email: developer’s email
   d) Developer contact information: developer’s email
9. Search for and select the following scopes from the Scope page:
   a) /auth/drive.appdata
   b) /auth/drive.file
   c) /auth/drive
   d) /auth/drive.metadata
   e) /auth/drive.metadata.readonly
   f) /auth/drive.readonly
10. Under Test Users, add the emails of any users that will have access to ClinicApp
11. Click “Credentials” from the options on the left.
12. Click “Create Credentials” and select “OAuth Client ID”. Set up the Client ID with the following configurations:
13. Application type: Android
14. Name: (developer’s choice)
15. Package name: org.broadinstitute.clinicapp
16. Find the SHA1key by the following steps:
17. Open the ClinicApp project in Android Studio
18. Click on “Gradle” on the far right
19. Click on the icon of a rectangle with a green triangle inside (“Execute Gradle Task”
20. Type “signingReport” and press enter.
21. The SHA1 key is the sequence generated in the console after “SHA1: ”
22. Copy and paste the SHA1 key into the Client ID
