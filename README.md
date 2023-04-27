# Annoying API [![Release](https://jitpack.io/v/srnyx/annoying-api.svg)](https://jitpack.io/#xyz.srnyx/annoying-api)

General purpose API for my plugins. Just contains some utility classes and methods. *[Okay... it contains a tad bit more than that...](#developers)*

It's technically a framework, but I didn't know the difference when I was naming it, so oh well!

## Server Owners

All of my plugins use AnnoyingAPI, however, they all come pre-packaged with it. **So, you don't need to install it separately.**

This may be different for other plugins that **aren't** mine, though. Just check their plugin pages / dependency lists to see if you need to install AnnoyingAPI separately.

### Download (read above)

- **Stable:** You can download the latest **stable** version at [Modrinth](https://api.srnyx.com/modrinth), [Hangar](https://hangar.papermc.io/srnyx/AnnoyingAPI), [Polymart](https://api.srnyx.com/polymart), [BuiltByBit](https://api.srnyx.com/builtbybit), [Spigot](https://api.srnyx.com/spigot), [Bukkit](https://api.srnyx.com/bukkit), or [GitHub](https://api.srnyx.com/releases/latest)
- **Snapshot:** You can download the latest **snapshot** version at [GitHub Actions](https://api.srnyx.com/snapshot)

### Wiki

You can find the wiki at [api.srnyx.com/wiki](https://api.srnyx.com/wiki) which will contain important information about the API and the plugins that use it

## Developers

You can find the Javadocs at [api.srnyx.com/javadocs](https://api.srnyx.com/javadocs). The wiki above may also contain some helpful information

### Importing

You can import the API using [Jitpack](https://api.srnyx.com/jitpack). It's **HIGHLY** recommended to implement the API, all the examples below will implement it. Make sure to replace `VERSION` with the version you want.

- **Gradle Kotlin** (`build.gradle.kts`)**:**
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
- **Gradle Groovy** (`build.gradle`)**:**
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
* **Maven** (`pom.xml`)**:**
    * Shade plugin
  ```xml
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>3.4.1</version>
        <executions>
          <execution>
            <phase>package</phase>
            <goals>
              <goal>shade</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
        <!-- Exclude resources to avoid conflicts -->
          <filters>
            <filter>
              <artifact>xyz.srnyx:*</artifact>
              <excludes>
                <exclude>META-INF/*.MF</exclude>
                <exclude>plugin.yml</exclude>
              </excludes>
            </filter>
          </filters>
          <relocations>
          <!-- It's recommended to relocate the API to avoid conflicts -->
            <relocation>
                <pattern>xyz.srnyx.annoyingapi</pattern>
                <shadedPattern>YOUR.PACKAGE.annoyingapi</shadedPattern>
            </relocation>
          </relocations>
        </configuration>
      </plugin>
    </plugins>
  </build>
  ```
    * Jitpack repository
  ```xml
   <repositories>
        <repository>
            <id>jitpack</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
  ```
    * AnnoyingAPI dependency declaration
  ```xml
    <dependencies>
        <dependency>
            <groupId>xyz.srnyx</groupId>
            <artifactId>annoying-api</artifactId>
            <version>VERSION</version>
        </dependency>
    </dependencies>
  ```
