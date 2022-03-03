plugins {
    id("com.google.osdetector") version "1.6.2"
    id ("com.google.cloud.tools.jib") apply true
    id("org.springframework.boot")
}

repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
}

val osclassifier : String? by project
val inferredClassifier: String = osclassifier?: osdetector.classifier
val timestamp = System.currentTimeMillis()
val commitHash = ext.get("commitHash")
val versionTags = generateVersionTag()

jib {
    from.image = "adoptopenjdk/openjdk13:debian"
    to.image = "929819487611.dkr.ecr.us-east-1.amazonaws.com/djl-spring-boot-app"
    to.tags = versionTags
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    //implementation("ai.djl.spring:djl-spring-boot-starter-mxnet-${inferredClassifier}:0.11-SNAPSHOT")
    implementation("ai.djl.spring:djl-spring-boot-starter-pytorch-auto:0.15")
    implementation(project(":djl-spring-boot-common"))
    implementation(project(":djl-spring-boot-model"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
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

fun generateVersionTag() : Set<String> {
    project.logger.lifecycle("Version tag: ".plus(commitHash))
    return  setOf(version.toString().plus("-").plus(inferredClassifier).plus("-").plus(commitHash))
}

