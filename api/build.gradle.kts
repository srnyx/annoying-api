plugins {
    `java-library`
    `maven-publish`
}

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi") // me.clip:placeholderapi
}

dependencies {
    compileOnly("net.md-5", "bungeecord-api", "1.16-R0.4")
    compileOnly("me.clip", "placeholderapi", "2.11.3")
    api("org.bstats", "bstats-bukkit", "3.0.0")
}

// Javadoc JAR task
java {
    withJavadocJar()
}

// Maven publishing for Jitpack
configure<PublishingExtension> {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.srnyx"
            artifact(tasks["shadowJar"])
            artifact(tasks["javadocJar"])
        }
    }
}

// Relocate the bStats package
tasks {
    shadowJar {
        relocate("org.bstats", "xyz.srnyx.annoyingapi.bstats")
    }
}
