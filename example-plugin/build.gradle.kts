import xyz.srnyx.gradlegalaxy.data.AdventureDependency
import xyz.srnyx.gradlegalaxy.utility.adventure
import xyz.srnyx.gradlegalaxy.utility.implementationRelocate


description = "Plugin to test/give an example use of AnnoyingAPI"
adventure(dependencies = AdventureDependency.getDefaultAnnoyingSpigot())

dependencies {
    implementationRelocate(project, project(":AnnoyingAPI", "shadow"), "xyz.srnyx.annoyingapi")
}

// Fix compileJava task
tasks.compileJava {
    dependsOn(":AnnoyingAPI:jar")
}
