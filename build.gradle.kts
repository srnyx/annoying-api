import me.dkim19375.dkimgradle.enums.Repository
import me.dkim19375.dkimgradle.enums.maven
import me.dkim19375.dkimgradle.util.spigotAPI

plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.github.dkim19375.dkim-gradle") version "1.2.0"
}

subprojects {
    group = "xyz.srnyx"
    version = "3.0.1"

    apply(plugin = "java")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "io.github.dkim19375.dkim-gradle")

    repositories {
        maven(Repository.MAVEN_CENTRAL, Repository.SPIGOT, Repository.SONATYPE_SNAPSHOTS_OLD, Repository.PLACEHOLDER_API)
    }

    dependencies {
        compileOnly(spigotAPI("1.8.8"))
        compileOnly("me.clip", "placeholderapi", "2.11.3")
    }

    // Set Java version
    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks {
        // Make 'gradle build' run 'gradle shadowJar'
        build {
            dependsOn("shadowJar")
        }

        // Remove '-all' from the JAR file name
        shadowJar {
            archiveClassifier.set("")
        }

        // Text encoding
        compileJava {
            options.encoding = "UTF-8"
        }

        // Replace '${name}' and '${version}' in resource files
        processResources {
            inputs.property("name", project.name)
            inputs.property("version", version)
            filesMatching("**/*.yml") {
                expand("name" to project.name, "version" to version)
            }
        }
    }
}
