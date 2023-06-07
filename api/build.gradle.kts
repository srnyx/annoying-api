plugins {
    `java-library`
    `maven-publish`
}

repositories {
    maven("https://repo.codemc.io/repository/maven-public") // de.tr7zw:item-nbt-api
}

dependencies {
    compileOnlyApi("org.jetbrains", "annotations", "24.0.0")
    api("org.bstats", "bstats-bukkit", "3.0.0")
    api("de.tr7zw", "item-nbt-api", "2.11.2") {
        exclude("de.tr7zw", "functional-annotations")
    }
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

// Relocate org.bstats and de.tr7zw.changeme.nbtapi
tasks {
    shadowJar {
        relocate("org.bstats", "xyz.srnyx.annoyingapi.libs.bstats")
        relocate("de.tr7zw.changeme.nbtapi", "xyz.srnyx.annoyingapi.libs.nbtapi")
    }
}
