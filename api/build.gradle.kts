import me.dkim19375.dkimgradle.enums.Repository
import me.dkim19375.dkimgradle.enums.maven

plugins {
    `java-library`
    `maven-publish`
}

repositories {
    maven(Repository.CODE_MC) // de.tr7zw:item-nbt-api
}

dependencies {
    compileOnlyApi("org.jetbrains", "annotations", "24.0.0")
    api("org.bstats", "bstats-bukkit", "3.0.0")
    api("de.tr7zw", "item-nbt-api", "2.11.3") {
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
            from(components["java"])
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
