description = "AnnoyingAPI"
version = "1.0.0"
group = "xyz.srnyx"

plugins {
    java
    `maven-publish`
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // org.spigotmc:spigot-api
    mavenCentral() // org.spigotmc:spigot-api
}

dependencies {
    compileOnly("org.spigotmc", "spigot-api", "1.19.2-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains", "annotations", "23.0.0")
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}

java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
    withJavadocJar()
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.srnyx"
            artifactId = "annoyingapi"
            from(components["java"])
        }
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    processResources {
        inputs.property("version", project.version)
        filesMatching("**/plugin.yml") {
            expand("version" to project.version)
        }
    }
}