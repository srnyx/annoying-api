dependencies {
    implementation(project(":AnnoyingAPI", configuration = "shadow"))
}

tasks {
    // Relocate the AnnoyingAPI package
    shadowJar {
        relocate("xyz.srnyx.annoyingapi", "xyz.srnyx.annoyingexample.annoyingapi")
    }

    // Fix compileJava task
    compileJava {
        dependsOn(":AnnoyingAPI:jar")
    }
}
