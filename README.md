# Annoying API [![Release](https://jitpack.io/v/srnyx/annoying-api.svg)](https://jitpack.io/#xyz.srnyx/annoying-api)

General purpose API for my plugins. Just contains some utility classes and methods.

## Server Owners

All (or at least most) of my plugins will require you to install Annoying API. Full releases of my plugins will **only** use stable versions of Annoying API, so you'll never need to download the snapshot versions *(unless explicitly stated)*.

### Download

- **Stable:** You can download the latest *(stable)* version at [Modrinth](https://modrinth.com/plugin/annoying-api), [Spigot](https://spigotmc.org/resources/106637), [Bukkit](https://dev.bukkit.org/projects/annoying-api), or [releases/latest](https://github.com/srnyx/annoying-api/releases/latest)
- **Snapshot:** You can download the latest *(snapshot)* version at [actions/workflows/build.yml](https://github.com/srnyx/annoying-api/actions/workflows/build.yml)

## Developers

You can find the Javadocs at [jitpack.io/com/github/srnyx/annoying-api/latest/javadoc/](https://jitpack.io/com/github/srnyx/annoying-api/latest/javadoc/)

### Importing

You can import the API using [Jitpack](https://jitpack.io/#xyz.srnyx/annoying-api). Make sure to replace `VERSION` with the version you want.

- **Gradle Groovy** (`build.gradle`)**:**
```gradle
repositories {
    maven { url = 'https://jitpack.io' }
}
dependencies {
    compileOnly 'xyz.srnyx:annoying-api:VERSION'
}
```
- **Gradle Kotlin DSL** (`build.gradle.kts`)**:**
```kotlin
repositories {
    maven("https://jitpack.io")
}
dependencies {
    compileOnly("xyz.srnyx", "annoying-api", "VERSION")
}
```
- **Maven** (`pom.xml`)**:**
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
<dependencies>
  <dependency>
    <groupId>xyz.srnyx</groupId>
    <artifactId>annoying-api</artifactId>
    <version>VERSION</version>
  </dependency>
</dependencies>
```
