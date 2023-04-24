plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    compileOnly("net.md-5", "bungeecord-api", "1.16-R0.4")
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
            from(components["java"])
        }
    }
}

// Relocate the bStats package
tasks {
    shadowJar {
        relocate("org.bstats", "xyz.srnyx.annoyingapi.bstats")
    }
}
