plugins {
    java
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
    version = "2.0.0"
    group = "xyz.srnyx"

    apply(plugin = "java")

    repositories {
        mavenCentral() // org.spigotmc:spigot, net.md-5:bungeecord-api (api)
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots") // org.spigotmc:spigot-api
        maven("https://oss.sonatype.org/content/repositories/snapshots") // org.spigotmc:spigot-api
    }

    dependencies {
        compileOnly("org.spigotmc", "spigot-api", "1.11-R0.1-SNAPSHOT")
        compileOnly("org.jetbrains", "annotations", "23.0.0")
    }

    // Set Java version
    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks {
        // Text encoding
        compileJava {
            options.encoding = "UTF-8"
        }

        // Replace '${name}', '${version}', and '${website}' in 'plugin.yml'
        processResources {
            filesMatching("**/plugin.yml") {
                expand("name" to project.name, "version" to project.version)
            }
        }
    }
}
