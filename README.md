# Chat Plus

![Static Badge](https://img.shields.io/badge/Minecraft_version-1.19%2B_%7C_1.20%2B_%7C_1.21%2B-red?style=flat)  
<a href="https://modrinth.com/mod/chatplus">
  <img src="https://img.shields.io/badge/Modrinth-Chat_Plus-%234e910e?style=flat" />
</a>

A simple Fabric mod that adds Bukkit-style color codes (using "&") and "[item]" display in chat.

It can be installed on the **client** or **server**:
- Install on the **server only** if you want players to send styled messages in-game
- Install on the **host client** (the one creating the LAN multiplayer session) for LAN multiplayer functionality
- Install on the **client** for single-player functionality



Compatibility has been implemented for these mods: Styled Chat, Dynmap<br>
_* Other mods may work without explicit support. Report compatibility requests via [GitHub Issues](https://github.com/CPTProgrammer/ChatPlus)._

## Screenshots

![image](https://github.com/CPTProgrammer/ChatPlus/assets/46586216/dcd2ca4b-79e9-4692-a8de-02fddfe4a392)<br>
**^^^ Display the item in the main hand**

![image](https://github.com/CPTProgrammer/ChatPlus/assets/46586216/b0449371-35aa-451b-ab9c-af464f6c597d)<br>
**^^^ Colorful Text**

![image](https://github.com/CPTProgrammer/ChatPlus/assets/46586216/dc3f0451-cbea-4610-8575-ea5da4e97f0f)<br>
**^^^ Display items in slots**

![image](https://github.com/user-attachments/assets/c725d9f7-04d1-4b40-ad13-f09c54d35942)<br>
**^^^ Escape character `&`**



## Minecraft Versions

| Development Version | Compatible Versions |
| ------------------- | ------------------- |
| 1.19                | 1.19                |
| 1.19.1              | 1.19.1 - 1.19.2     |
| 1.19.3              | 1.19.3 - 1.19.4     |
| 1.20                | 1.20 - 1.20.2       |
| 1.20.3              | 1.20.3 - 1.20.6     |
| 1.21                | 1.21 - 1.21.5       |



## Development

#### Environment

- Java 23 or higher
- (Optional) Java IDE with Manifold support (e.g., IntelliJ IDEA)<br>
  _* If using IntelliJ IDEA, the Manifold plugin should be installed_

#### Switching Versions

Set `minecraft_version` in `gradle.properties`, or append `-Pmc=x.x.x` parameter to Gradle commands (e.g., `./gradlew build -Pmc=1.20.3`) to switch Minecraft versions for development.

> **Note for IDE users:**
>
> After modifying `gradle.properties` or `./properties/*.properties`, remember to reload the Gradle project (or click "Load Gradle Changes" button)

**Cross-version Testing**

To test mod compatibility with newer Minecraft versions:

- Method 1:
  - Set `minecraft_version` to target version, and configure matching `test_fabric_api_version` (e.g., `0.121.0+1.21.5`)
  - Reload project to download dependencies (for IDE users)
  - Place additional test mods in `./run/mods`
  - Run Gradle task `runClient` or `runServer` (_May encounter unexpected launch issues - if this occurs, try Method 2_)
- Method 2: Build JAR file and deploy to `mods` folder of the target Minecraft version client/server

> **Note:**
>
> The `minecraft_version` can be a version not explicitly listed in `./properties/*.properties` files. The `settings.gradle` script will automatically select the nearest compatible properties configuration.

#### Build

Run one of the following commands to build the JAR:

```bash
# Build the JAR for the currently configured Minecraft version
./gradlew build  # Windows: .\gradlew build

# Build for a different Minecraft version, e.g.
./gradlew build -Pmc=1.20.3
```

To build JARs for all Minecraft versions defined in `./properties`:

```bash
./gradlew buildAll  # Windows: .\gradlew buildAll
```

> **Note:**
>
> All compiled JARs are output to `./build/libs`



## Contributors

<a href="https://github.com/CPTProgrammer/ChatPlus/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=CPTProgrammer/ChatPlus" />
</a>

#### Special Thanks

- [@SAGUMEDREAM](https://github.com/SAGUMEDREAM) for advice on mixin development.
- [Distant-Horizons-Team/Distant Horizons](https://gitlab.com/distant-horizons-team/distant-horizons/) for inspiring the build configuration design.

