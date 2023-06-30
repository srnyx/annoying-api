import xyz.srnyx.gradlegalaxy.enums.Repository
import xyz.srnyx.gradlegalaxy.enums.repository
import xyz.srnyx.gradlegalaxy.utility.setupMC
import xyz.srnyx.gradlegalaxy.utility.spigotAPI


plugins {
    java
    id("xyz.srnyx.gradle-galaxy") version "1.1.0" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "xyz.srnyx.gradle-galaxy")
    apply(plugin = "com.github.johnrengelman.shadow")

    setupMC("xyz.srnyx", "3.1.0")
    spigotAPI("1.8.8")
    repository(Repository.PLACEHOLDER_API)
    dependencies.compileOnly("me.clip", "placeholderapi", "2.11.3")
}
