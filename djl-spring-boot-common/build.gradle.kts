
dependencies {
	implementation("org.slf4j:slf4j-api:1.7.30")
	implementation("org.springframework:spring-core")
	implementation("org.springframework:spring-context")
	implementation("com.amazonaws:aws-java-sdk-s3:1.11.714")
	testImplementation("org.slf4j:slf4j-log4j12")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
