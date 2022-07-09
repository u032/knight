val ktorVersion = "2.0.3"
val kotlinVersion = "1.6.21"
val logbackVersion = "1.2.11"
val firestoreVersion = "3.3.0"
val dotenvVersion = "6.3.1"
val isDevelopment = true

plugins {
    application
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.serialization") version "1.4.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "wiki.chess"
version = "1.0.0"

application {
    mainClass.set("wiki.chess.ApplicationKt")

    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-java-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("com.google.cloud:google-cloud-firestore:$firestoreVersion")
    implementation("io.github.cdimascio:dotenv-kotlin:$dotenvVersion")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}

tasks {
    shadowJar {
        archiveBaseName.set("knight")
        archiveClassifier.set("")
        archiveVersion.set("")
    }
}
