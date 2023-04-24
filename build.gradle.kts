plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

// Prevent java tasks from running for root project
tasks {
    classes { enabled = false }
    jar { enabled = false }
    assemble { enabled = false }
    testClasses { enabled = false }
    check { enabled = false }
    build { enabled = false }
}

subprojects {
    version = "2.1.1"
    group = "xyz.srnyx"

    apply(plugin = "java")
    apply(plugin = "com.github.johnrengelman.shadow")

    repositories {
        mavenCentral() // org.spigotmc:spigot, net.md-5:bungeecord-api (api)
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots") // org.spigotmc:spigot-api
        maven("https://oss.sonatype.org/content/repositories/snapshots") // org.spigotmc:spigot-api
    }

    dependencies {
        compileOnly("org.spigotmc", "spigot-api", "1.11-R0.1-SNAPSHOT")
        compileOnly("org.jetbrains", "annotations", "24.0.0")
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

        // Disable unnecessary tasks
        classes { enabled = false }
        jar { enabled = false }
        compileTestJava { enabled = false }
        processTestResources { enabled = false }
        testClasses { enabled = false }
        test { enabled = false }
        check { enabled = false }
    }
}
