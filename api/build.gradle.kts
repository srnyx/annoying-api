plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    compileOnlyApi("org.jetbrains", "annotations", "24.0.0")
    api("org.bstats", "bstats-bukkit", "3.0.0")
    api("de.tr7zw", "item-nbt-api", "2.11.2")
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
        relocate("de.tr7zw", "xyz.srnyx.annoyingapi.nbt")
    }
}
