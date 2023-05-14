plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "org.example"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.6.2")

    implementation("org.jetbrains.exposed:exposed-core:0.39.2")
    implementation("org.jetbrains.exposed:exposed-dao:0.39.2")
    runtimeOnly("org.jetbrains.exposed:exposed-jdbc:0.39.2")

    implementation(group = "org.xerial", name = "sqlite-jdbc", version = "3.25.2")
    implementation(group = "mysql", name = "mysql-connector-java", version = "8.0.28")
    implementation("com.zaxxer:HikariCP:5.0.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}