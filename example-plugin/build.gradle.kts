dependencies {
    implementation(project(":AnnoyingAPI"))
}

// Relocate the AnnoyingAPI package
tasks {
    shadowJar {
        relocate("xyz.srnyx.annoyingapi", "xyz.srnyx.annoyingexample.annoyingapi")
    }
}
