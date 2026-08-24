plugins {
    java
    `java-library`
    id("xyz.srnyx.gradle-galaxy") version "a8227b9"
    id("com.gradleup.shadow") version "9.6.1"
    id("me.modmuss50.mod-publish-plugin") version "675051c"
    id("io.papermc.hangar-publish-plugin") version "0.1.4"
    id("net.kyori.blossom") version "2.2.0"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.4.1" // For Blossom
}

group = "xyz.srnyx"
description = "General purpose API with tons of features"

galaxy {
    java {
        javaVersion = JavaVersion.VERSION_17
    }

    repository {
        add(SRNYX_RELEASES, SRNYX_SNAPSHOTS, ALESSIO_DP)
    }

    dependency {
        add {
            repositories.add(ALESSIO_DP)
            configurations = listOf("api")
            group = "net.byteflux"
            artifact = "libby-bukkit"
            version = "1.3.1"
            relocate("net.byteflux.libby")
        }
        add {
            repositories.addAll(SRNYX_RELEASES, SRNYX_SNAPSHOTS)
            configurations = listOf("api")
            group = "xyz.srnyx"
            artifact = "java-utilities"
            version = "0c7c450"
            relocate("xyz.srnyx.javautilities")
        }
    }

    minecraft {
        spigotAPI("1.8.8")
        folia = true

        dependency {
            optional {
                repositories.add(PLACEHOLDER_API)
                group = "me.clip"
                artifact = "placeholderapi"
                version = "2.12.2"

                pluginYml = "PlaceholderAPI"
                modrinth = "placeholderapi"
                hangar = "PlaceholderAPI"
            }
        }
        
        annoyingAPI.customRuntimeLibraries {
            configurations = listOf("compileOnlyApi", "testImplementation")
            relocate = false

            generateRuntimeLibraryEnum {
                relocateImports = false
            }

            library("annotations") { // Technically not runtime, but better for consumers to not have to specify it
                repositories.add(MAVEN_CENTRAL)
                group = "org.jetbrains"
                artifact = "annotations"
                version = "26.1.0"
            }
            library("semver4j") {
                repositories.add(MAVEN_CENTRAL)
                group = "org.semver4j"
                artifact = "semver4j"
                version = "6.0.0"
                relocate()

                action {
                    exclude("org.jspecify", "jspecify")
                }
            }
            library("xseries") {
                repositories.add(MAVEN_CENTRAL)
                group = "com.github.cryptomorin"
                artifact = "XSeries"
                version = "13.7.1"
                relocate("com.cryptomorin.xseries")
            }
            library("okaeri_configs_core") {
                repositories.add(SRNYX_SNAPSHOTS)
                group = "eu.okaeri"
                artifact = "okaeri-configs-core"
                version = "acd026c"
                relocate()

                library("okaeri_configs_yaml_bukkit") {
                    artifact = "okaeri-configs-yaml-bukkit"

                    library("okaeri_configs_serdes_bukkit") {
                        artifact = "okaeri-configs-serdes-bukkit"
                    }
                }
                library("okaeri_configs_serdes_commons") {
                    artifact = "okaeri-configs-serdes-commons"
                }
                library("okaeri_configs_validator_okaeri") {
                    artifact = "okaeri-configs-validator-okaeri"

                    dependency("okaeri_validator") {
                        repositories.add(OKAERI_RELEASES)
                        group = "eu.okaeri"
                        artifact = "okaeri-validator"
                        version = "2.0.5"
                        relocate()
                    }
                }
            }
            library("item_nbt_api") {
                repositories.add(CODE_MC)
                group = "de.tr7zw"
                artifact = "item-nbt-api"
                version = "2.15.7"
                relocate("de.tr7zw.changeme.nbtapi")
            }
            library("bstats_base") {
                repositories.add(MAVEN_CENTRAL)
                group = "org.bstats"
                artifact = "bstats-base"
                version = "3.2.1"
                relocate()

                library("bstats_bukkit") {
                    artifact = "bstats-bukkit"
                }
            }
            library("faststats_core") {
                repositories.addAll(FASTSTATS_RELEASES, FASTSTATS_SNAPSHOTS)
                group = "dev.faststats.metrics"
                artifact = "core"
                version = "0.29.4"
                relocate("dev.faststats")

                library("faststats_config") {
                    artifact = "config"
                }
                library("faststats_bukkit") {
                    artifact = "bukkit"
                    dependencies.addAll("faststats_config")
                }

                dependency("gson") {
                    repositories.add(MAVEN_CENTRAL)
                    group = "com.google.code.gson"
                    artifact = "gson"
                    version = "2.14.0"
                    relocate("com.google.gson")
                }
            }
            library("reflections") {
                repositories.add(MAVEN_CENTRAL)
                group = "org.reflections"
                artifact = "reflections"
                version = "0.10.2"
                relocate()

                dependency("javassist") {
                    repositories.add(MAVEN_CENTRAL)
                    group = "org.javassist"
                    artifact = "javassist"
                    version = "3.28.0-GA"
                    relocate("javassist.", "{package}.libs.javassist.")
                }
            }
            library("hikaricp") {
                repositories.add(MAVEN_CENTRAL)
                group = "com.zaxxer"
                artifact = "HikariCP"
                version = "7.1.0"
                relocate("com.zaxxer.hikari")
            }
            library("jooq") {
                repositories.add(MAVEN_CENTRAL)
                group = "org.jooq"
                artifact = "jooq"
                version = "3.19.36" // Keep on 3.19.x for Java 17 support (https://www.jooq.org/download/support-matrix-jdk#oss)
                relocate()

                dependency("r2dbc_spi") {
                    repositories.add(MAVEN_CENTRAL)
                    group = "io.r2dbc"
                    artifact = "r2dbc-spi"
                    version = "1.0.0.RELEASE"
                    relocate()

                    dependency("reactive_streams") {
                        repositories.add(MAVEN_CENTRAL)
                        group = "org.reactivestreams"
                        artifact = "reactive-streams"
                        version = "1.0.4"
                        relocate()
                    }
                }
            }
            library("h2") {
                repositories.add(MAVEN_CENTRAL)
                group = "com.h2database"
                artifact = "h2"
                version = "2.4.240"
                relocate("org.h2")
            }
            library("postgresql") {
                repositories.add(MAVEN_CENTRAL)
                group = "org.postgresql"
                artifact = "postgresql"
                version = "42.7.11"
                relocate()
            }
        }

        pluginYml {
            developerData(SRNYX)
            main = "${getPackage()}.AnnoyingPlugin"
        }

        platformPublishing {
            github("srnyx/annoying-api")
            hangar("AnnoyingAPI")
            modrinth("gzktm9GG")
            hangar("AnnoyingAPI")
            curseforge("728930")

            addAnnoyingApiDependency = false

            projectData("annoying-api")
        }
    }

    mavenPublishing {
        artifactId = "annoying-api"
        silenceMissingJavadocWarnings = true
        licenses.add(MIT)
        developers.add(SRNYX)

        textArtifact {
            classifier = "metadata"
            extension = "json"
            text = provider {
                val metadata = annoyingMetadata {
                    packageName = "${project.group}.annoyingapi"
                    javaVersion = java.javaVersion.get().majorVersion.toInt()
                    repositories.add(ALESSIO_DP)
                    runtimeLibraries = minecraft.annoyingAPI.customRuntimeLibraries.libraries
                    exclude("net.byteflux", "libby-bukkit")
                    exclude("xyz.srnyx", "java-utilities")
                }
                return@provider json {
                    prettyPrint = true
                    prettyPrintIndent = "  "
                }.encodeToString(metadata)
            }
        }

        publication {
            pom {
                url = "https://annoying-api.srnyx.com"
            }
        }
    }

    testing {
        jUnit("6.1.0")
        mockBukkit("3.9.0")
    }
}

tasks.shadowJar {
    exclude("META-INF/maven/**")
}

// Blossom (see java-templates module)
sourceSets.main {
    blossom.javaSources { property("annoying_api_version", version.toString()) }
}
