plugins {
    id("com.google.osdetector") version "1.6.2"
    id ("com.google.cloud.tools.jib") apply true
    id("org.springframework.boot")
}

repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    mavenLocal()
}

val osclassifier : String? by project
val inferredClassifier: String = osclassifier?: osdetector.classifier

jib {
    from.image = "openjdk:13"
    to.image = "929819487611.dkr.ecr.us-east-1.amazonaws.com/djl-spring-boot-app"
    to.tags = setOf(version.toString().plus("-").plus(inferredClassifier))
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("ai.djl.spring:djl-spring-boot-starter-mxnet-${inferredClassifier}:0.0.1-SNAPSHOT")
    implementation("net.java.dev.jna:jna:5.3.0") // required override due to conflict with spring boot
    implementation(project(":djl-spring-boot-common"))
    implementation(project(":djl-spring-boot-model"))

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    // See: https://github.com/awslabs/djl/blob/master/mxnet/mxnet-engine/README.md for MXNet library selection
}
tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveClassifier.set(inferredClassifier)
}

tasks.withType<Test> {
    useJUnitPlatform()
}


