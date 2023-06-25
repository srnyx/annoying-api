import xyz.srnyx.gradlegalaxy.utility.implementationRelocate


description = "Plugin to test/give an example use of AnnoyingAPI"

dependencies {
    implementationRelocate(project, project(":AnnoyingAPI", "shadow"), "xyz.srnyx.annoyingapi")
}

// Fix compileJava task
tasks.compileJava {
    dependsOn(":AnnoyingAPI:jar")
}
