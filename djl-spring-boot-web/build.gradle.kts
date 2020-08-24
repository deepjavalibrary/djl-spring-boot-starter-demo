import java.io.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot")
	id ("com.google.cloud.tools.jib") apply true
	kotlin("jvm")
	kotlin("plugin.spring")
}
val commitHash = ext.get("commitHash")

jib {
	from.image = "openjdk:13"
	to.image = "929819487611.dkr.ecr.us-east-1.amazonaws.com/djl-spring-boot-web"
	to.tags = setOf(version.toString().plus("-").plus(commitHash))
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation(project(":djl-spring-boot-common"))
	implementation("com.amazonaws:aws-java-sdk-s3:1.11.714") // TODO: this was not resolved through transitive dependency on djl-sping-boot-common
	implementation(project(":djl-spring-boot-model"))
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "12"
	}
}
