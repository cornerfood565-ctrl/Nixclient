# Baron Client — Minecraft 1.21.11

A client-side Fabric mod foundation with a black modern ClickGUI, PvP HUD utilities, and visual customization.

## Build on GitHub
Open the **Actions** tab and run **Build Baron Client**. It compiles with Java 21 and Gradle on a GitHub-hosted runner, then uploads the remapped JAR as a workflow artifact.

## Local build
Install Java 21 and Gradle 9.2 or newer, then run `gradle build`. The JAR is written to `build/libs/`.

## Controls
- Right Shift: open Baron Client GUI

## Status
The initial source includes the module registry, black ClickGUI, FPS/coordinates/ping HUD, and registered module placeholders for the next implementation pass. Not every registered module is fully implemented yet.

Minecraft is a trademark of Mojang Studios. Baron Client is not affiliated with Mojang.