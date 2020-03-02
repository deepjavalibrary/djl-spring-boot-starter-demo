plugins {
    java
    id("org.springframework.boot") version "2.2.4.RELEASE" apply false
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id ("com.google.cloud.tools.jib") version "2.1.0" apply false
    kotlin("jvm") version "1.3.61" apply false
    kotlin("plugin.spring") version "1.3.61" apply false
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "io.spring.dependency-management")
    apply(plugin  = "java")
    group = "com.aws.samples"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    java.sourceCompatibility=JavaVersion.VERSION_12
    java.targetCompatibility=JavaVersion.VERSION_12

    dependencyManagement {
        imports {
            mavenBom("org.springframework:spring-framework-bom:5.2.3.RELEASE")
        }
    }
}
