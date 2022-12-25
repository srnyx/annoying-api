# Annoying API [![Release](https://jitpack.io/v/srnyx/annoying-api.svg)](https://jitpack.io/#xyz.srnyx/annoying-api)

General purpose API for my plugins. Just contains some utility classes and methods. *[Okay... it contains a tad bit more than that...](#developers)*

## Server Owners

Almost all of my plugins use AnnoyingAPI, however, they all come pre-packaged with it. **So, you don't need to install it separately.**

This may be different for other plugins that **aren't** mine, though. Just check their plugin pages / dependency lists to see if you need to install AnnoyingAPI separately.

### Download

- **Stable:** You can download the latest **stable** version at [Modrinth](https://modrinth.com/plugin/annoying-api), [Polymart](https://polymart.org/resource/3238), [Spigot](https://spigotmc.org/resources/106637), [Bukkit](https://dev.bukkit.org/projects/annoying-api), or [releases/latest](https://github.com/srnyx/annoying-api/releases/latest)
- **Snapshot:** You can download the latest **snapshot** version at [actions/workflows/build.yml](https://github.com/srnyx/annoying-api/actions/workflows/build.yml)

## Developers

You can find the Javadocs at [jitpack.io/com/github/srnyx/annoying-api/latest/javadoc/](https://jitpack.io/com/github/srnyx/annoying-api/latest/javadoc/) (wiki coming soon, hopefully)

### Importing

You can import the API using [Jitpack](https://jitpack.io/#xyz.srnyx/annoying-api). Make sure to replace `VERSION` with the version you want.

**RECOMMENDED:** If you don't want to require users to install AnnoyingAPI separately (and you want to be version-safe), you can shade it into your plugin. Each example below will include both shaded and non-shaded versions.

- **Gradle Kotlin DSL** (`build.gradle.kts`)**:**
  - Shaded (recommended):
  ```kotlin
  // Required plugins
  plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2" // https://github.com/johnrengelman/shadow/releases/latest
  }
  // Jitpack repository
  repositories {
    maven("https://jitpack.io")
  }
  // AnnoyingAPI dependency declaration
  dependencies {
      implementation("xyz.srnyx", "annoying-api", "VERSION")
  }
  // It's recommended to relocate the API to avoid conflicts
  tasks {
    build {
      relocate("xyz.srnyx.annoyingapi", "YOUR.PACKAGE.annoyingapi")
    }
  }
  ```
  - Non-shaded:
  ```kotlin
  // Required plugins
  plugins {
    java
  }
  // Jitpack repository
  repositories {
    maven("https://jitpack.io")
  }
  // AnnoyingAPI dependency declaration
  dependencies {
      compileOnly("xyz.srnyx", "annoying-api", "VERSION")
  }
  ```
- **Gradle Groovy** (`build.gradle`)**:**
  - Shaded (recommended):
  ```groovy
  // Required plugins
  plugins {
    id 'java'
    id 'com.github.johnrengelman.shadow' version '7.1.2' // https://github.com/johnrengelman/shadow/releases/latest
  }
  // Jitpack repository
  repositories {
      maven { url = 'https://jitpack.io' }
  }
  // AnnoyingAPI dependency declaration
  dependencies {
      implementation 'xyz.srnyx:annoying-api:VERSION'
  }
  // It's recommended to relocate the API to avoid conflicts
  shadowJar {
      relocate 'xyz.srnyx.annoyingapi', 'YOUR.PACKAGE.annoyingapi'
  }
  ```
  - Non-shaded:
  ```groovy
  // Required plugins
  plugins {
    id 'java'
  }
  // Jitpack repository
  repositories {
      maven { url = 'https://jitpack.io' }
  }
  // AnnoyingAPI dependency declaration
  dependencies {
      compileOnly 'xyz.srnyx:annoying-api:VERSION'
  }
  ```
- **Maven** (`pom.xml`)**:**
  - Shaded (recommended): I don't use Maven, so I don't know how to shade it. If you know how, please [open a PR](https://github.com/srnyx/annoying-api/pull/new).
  - Non-shaded:
  ```xml
  <project>
    <!-- Jitpack repository -->
    <repositories>
      <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
      </repository>
    </repositories>
    <!-- AnnoyingAPI dependency declaration -->
    <dependencies>
      <dependency>
        <groupId>xyz.srnyx</groupId>
        <artifactId>annoying-api</artifactId>
        <version>VERSION</version>
      </dependency>
    </dependencies>
  </project>
  ```
