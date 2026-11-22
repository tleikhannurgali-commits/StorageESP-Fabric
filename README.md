# StorageESP Fabric 1.21.11 — Pulse Visuals compatibility port

**Status:** source port only. NOT yet built with Gradle or tested in Minecraft.
This is an independent Fabric mod; it does not modify Pulse or bypass launch restrictions.

- Minecraft 1.21.11, Fabric loader (Pulse 1.21.11 uses Fabric)
- Fabric API 0.141.3+1.21.11, Java 21
- Keybind Right Ctrl: GUI. H/J/K/O toggle, M mark.
- Existing source features retained: storage/player ESP, anti-invis, loot tracker,
  warden timer scanner, radar, marks, void map, traces, price tags.
- All renderers use data available to the local client; no claims about FunTime bypass.
- Original GUI double-blur crash was removed.

## Build through GitHub (recommended)
Create a **new, separate repository** (do not upload into Forge project). Upload
ALL contents of this directory, including hidden `.github` folder.
Actions will build and upload a JAR if the source compiles. If red, open compile
logs and adjust Fabric APIs. This source has not been compiled in a Fabric toolchain.

## Installation after successful build
Remove Forge StorageESP from the Fabric/Pulse mods folder.
Put the Fabric JAR and Fabric API for Minecraft 1.21.11 in the **correct profile's** mods folder.
Pulse may restrict external mods; having a Fabric JAR does not guarantee it accepts it.
To view all GUI cards, set Minecraft GUI Scale to 2 at 1920x1080 resolution.

## Known limitations
The port uses vanilla Gizmos for outlines and a SoundManager mixin for traces.
Pulse's custom rendering or mod loading may interfere. Test offline first.
