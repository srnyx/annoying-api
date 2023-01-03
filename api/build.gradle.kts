plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    compileOnly("net.md-5", "bungeecord-api", "1.16-R0.4")
    compileOnlyApi("org.jetbrains:annotations:23.1.0")
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
