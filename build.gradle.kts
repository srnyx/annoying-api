plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
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
    version = "3.0.1"
    group = "xyz.srnyx"

    apply(plugin = "java")
    apply(plugin = "com.github.johnrengelman.shadow")

    repositories {
        mavenCentral() // org.spigotmc:spigot, net.md-5:bungeecord-api (api)
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots") // org.spigotmc:spigot-api
        maven("https://oss.sonatype.org/content/repositories/snapshots") // org.spigotmc:spigot-api
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi") // me.clip:placeholderapi
        maven("https://repo.codemc.io/repository/maven-public") // de.tr7zw:item-nbt-api (api)
    }

    dependencies {
        compileOnly("org.spigotmc", "spigot-api", "1.8.8-R0.1-SNAPSHOT")
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
