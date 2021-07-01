
dependencies {
	implementation("org.slf4j:slf4j-api:1.7.31")
	implementation("org.springframework:spring-core")
	implementation("org.springframework:spring-context")
	implementation("com.amazonaws:aws-java-sdk-s3:1.12.15")
	implementation("com.amazonaws:aws-java-sdk-sts:1.12.15")
	testImplementation("org.slf4j:slf4j-log4j12")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
