
dependencies {
	implementation("jakarta.validation:jakarta.validation-api:2.0.2")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
