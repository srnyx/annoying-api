description = "AnnoyingAPI"
version = "1.1.8"
group = "xyz.srnyx"

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // org.spigotmc:spigot
    maven("https://oss.sonatype.org/content/repositories/snapshots/") // org.spigotmc:spigot
    mavenCentral() // net.md-5:bungeecord-api
}

dependencies {
    compileOnly("org.spigotmc", "spigot-api", "1.11-R0.1-SNAPSHOT")
    compileOnly("net.md-5", "bungeecord-api", "1.16-R0.4")
    compileOnly("org.jetbrains", "annotations", "23.0.0")
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}

plugins {
    java
    `maven-publish`
}

// Allow compileOnly dependencies to be used in tests
configurations {
    testCompileOnly {
        extendsFrom(configurations.compileOnly.get())
    }
}

// Set Java version and Javadoc
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
}

// Maven publishing for Jitpack
configure<PublishingExtension> {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.srnyx"
            from(components["java"])
        }
    }
}

tasks {
    // Text encoding
    compileJava {
        options.encoding = "UTF-8"
    }

    // Replace version in plugin.yml
    @Suppress("UnstableApiUsage")
    withType<ProcessResources> {
        inputs.property("version", project.version)
        filesMatching("**/plugin.yml") {
            expand("version" to project.version)
        }
    }

    // Create test JAR
    assemble {
        dependsOn("testJar")
    }
    register<Jar>("testJar") {
        archiveClassifier.set("test")
        from(sourceSets["test"].output)
    }
}