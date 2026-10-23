# Baron Client — Minecraft 1.21.11

A client-side Fabric mod project with a dark ClickGUI and starter HUD/module registry.

## Build status

GitHub Actions runs Gradle automatically on every push to `main` and can be started manually from the **Actions** tab. The compiled JAR is uploaded as the `baron-client-1.21.11` workflow artifact when the build succeeds.

## Toolchain

- Minecraft 1.21.11
- Java 21
- Fabric Loader 0.18.1
- Fabric API 0.141.6+1.21.11
- Fabric Loom 1.14.10
- Yarn 1.21.11+build.6

## Current foundation

- Right Shift opens the Baron Client GUI
- Module toggle buttons grouped by category in the starter registry
- FPS, coordinates and ping HUD
- Starter registry for PvP, HUD, visual and performance modules

**Important:** several registered modules are placeholders and do not yet implement their advertised behavior. This source has not been confirmed to compile until the GitHub Actions build completes.

## Local build

Install Java 21 and Gradle 9.2.1+, then run:

```bash
gradle build
```

The JAR should be created under `build/libs/`.

Minecraft is a trademark of Mojang Studios. Baron Client is not affiliated with Mojang.
