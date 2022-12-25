plugins {
    `maven-publish`
}

dependencies {
    compileOnly("net.md-5", "bungeecord-api", "1.16-R0.4")
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
