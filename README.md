# DJL Spring Boot Demo

This repository contains example code demonstrating how to use [DJL](https://github.com/awslabs/djl) with Spring Boot and DJL Spring Boot Starter.
At present it covers MXNet based object detection inference with platform specific DJL libraries that could be consumed by using DJL Spring Boot Starter dependencies.

## Building Backend
Examples are based on gradle as the build tool. It is a multi-project build.
Before building please ensure that DJL Spring Boot starter BOM is in your local maven repository. More instructions are here: [https://github.com/awslabs/djl-spring-boot-starter](https://github.com/awslabs/djl-spring-boot-starter). You will need to check out this repository and run `./mvnw install`.

In order to build the DJL Spring Boot microservice run the following command:

    ./gradlew :djl-spring-boot-app:bootJar

The above will detect the os on the system where the build is running and use it for platform dependency resolution.

Platform specific builds (for CI): 

    ./gradlew :djl-spring-boot-app:bootJar -P osclassifier=linux-x86_64
    ./gradlew :djl-spring-boot-app:bootJar -P osclassifier=osx-x86_64
    ./gradlew :djl-spring-boot-app:bootJar -P osclassifier=win-x86_64
  
The produced artifacts will have the classifier in the name of the spring boot uber jar, e.g.

    djl-spring-boot-app/build/libs/djl-spring-boot-app-0.0.1-SNAPSHOT-linux-x86_64.jar
    djl-spring-boot-app/build/libs/djl-spring-boot-app-0.0.1-SNAPSHOT-osx-x86_64.jar


 ## Running Backend
 
 At present running the app requires the following:
  S3 bucket (djl-demo by default) specified in djl-spring-boot-app/src/main/resources/application.properties.
  The bucket is expected to have two prefixes: inbox (where input is located) and outbox (where results are placed)
  AWS_ACCESS_KEY and AWS_SECRET_KEY set as environment variables. IAM user is expected to have read permissions for inbox and write permission for outbox.
  
  macOS:
  
    java -jar djl-spring-boot-app/build/libs/djl-spring-boot-app-0.0.1-SNAPSHOT-osx-x86_64.jar
  
## Deploying to PCF

Make sure manifest.yml file contains valid credentials to allow access to S3. You can specify your own bucket in [application.properties] (djl-spring-boot-app/src/main/resources/application.properties) and [web application.properties](djl-spring-boot-web/src/main/resources/application.properties). 

    cf login
    cf push -f manifest.yml -p build/libs/djl-spring-boot-app-0.0.1-SNAPSHOT-linux-x86_64.jar

Check logs:

    cd logs djl-demo --recent

## Frontend Web Application
[Web Application](djl-spring-boot-web/README.md)

## License
This project is licensed under the Apache-2.0 License.


