# DJL Spring Boot Demo

This repository contains example code demonstrating how to use the [Deep Java Library](https://github.com/awslabs/djl) (DJL) with Spring Boot and the DJL Spring Boot Starter.
It covers MXNet-based object detection inference with platform specific DJL libraries that can be consumed using DJL Spring Boot Starter dependencies.

## Building the backend
Examples use Gradle as the build tool. It is a multi-project build.
Before building, ensure that the DJL Spring Boot starter BOM is in your local maven repository. For more information, see the [DJL Spring Boot Starter repo](https://github.com/awslabs/djl-spring-boot-starter). You need to check out this repository and run `./mvnw install`.

To build the DJL Spring Boot microservice, run the following command:

    ./gradlew :djl-spring-boot-app:bootJar

This command detects the operating system on the system where the build is running and uses it for platform dependency resolution.

Platform specific builds (for CI): 

    ./gradlew :djl-spring-boot-app:bootJar -P osclassifier=linux-x86_64
    ./gradlew :djl-spring-boot-app:bootJar -P osclassifier=osx-x86_64
    ./gradlew :djl-spring-boot-app:bootJar -P osclassifier=win-x86_64
  
The produced artifacts will have the classifier in the name of the spring boot uber jar, e.g.

    djl-spring-boot-app/build/libs/djl-spring-boot-app-0.0.1-SNAPSHOT-linux-x86_64.jar
    djl-spring-boot-app/build/libs/djl-spring-boot-app-0.0.1-SNAPSHOT-osx-x86_64.jar


 ## Running the backend
 
Running the app requires the following:
  The S3 bucket (`djl-demo` by default) must be specified in `djl-spring-boot-app/src/main/resources/application.properties`.
  The bucket is expected to have two prefixes: `inbox`, where input is located, and `outbox`, where results are placed.
  `AWS_ACCESS_KEY` and `AWS_SECRET_KEY` set as environment variables for region `us-east-1`. 
  The IAM user must have read permissions for the `inbox` and write permissions for the `outbox`.
  
  Run the following command to start the backend based on the created JAR file, sample command for macOS:
      
    java -jar djl-spring-boot-app/build/libs/djl-spring-boot-app-0.0.1-SNAPSHOT-osx-x86_64.jar
  
  Alternatively you can use Gradle for execution, sample command for Windows:   
  
    gradlew :djl-spring-boot-app:bootRun -P osclassifier=win-x86_64
  
## Deploying to PCF

Make sure your manifest.yml file contains valid S3 access credentials. You can specify your own bucket in [application.properties] (djl-spring-boot-app/src/main/resources/application.properties) and [web application.properties](djl-spring-boot-web/src/main/resources/application.properties). 

Use the following command to deploy:

    cf login
    cf push -f manifest.yml -p build/libs/djl-spring-boot-app-0.0.1-SNAPSHOT-linux-x86_64.jar

Check the logs:

    cd logs djl-demo --recent

## Frontend Web Application
[Web Application](djl-spring-boot-web/README.md)

## License
This project is licensed under the Apache-2.0 License.


