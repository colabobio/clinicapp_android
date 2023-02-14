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
