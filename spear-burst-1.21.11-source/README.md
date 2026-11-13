# Spear Burst — experimental source, Minecraft 1.21.11

This is a standalone Meteor addon source project, not a compiled or game-tested JAR.

Hold use with a spear while aiming at a nearby living entity. Once charged and within
the trigger range, it applies one horizontal velocity burst and zeros velocity at
the end of that client tick. Release use before another burst. Approach normally;
it does not fly toward distant targets. Keep Trouser Streak SpearKill and other
velocity-changing modules disabled while testing this module.

The requested speed is capped by available distance to avoid passing the target.
Friends, spectators, obstructed paths, and targets outside range are ignored.
Server position corrections are respected. Braking also runs on module disable.
Stopping is at client-tick granularity, not literally instantaneous. Ordinary input,
gravity and server knockback can move you again afterward.

This cannot promise instant kills or invisibility to observers. A short movement can
still be visible, and the server determines damage and accepted movement. Braking
may reduce effective spear damage; only a real server test can establish behavior.

## Build

Install JDK 21 and Gradle 9.2.1, then run `gradle build` in this folder.
The resulting installable file should be `build/libs/spear-burst-0.1.0.jar`.
Put it alongside Meteor for Minecraft 1.21.11 in your client mods folder, restart,
and enable `spear-burst` in the Spear Burst category.

Alternatively upload this project to your own GitHub repository and run its included
Build workflow from the Actions tab. Download the JAR artifact after a successful run.
This workflow has not been run here. Snapshot dependencies require network access
and can change or become unavailable.

## Validation status

JSON and project structure checked. Compilation and in-game tests were unavailable:
the authoring environment has Java 8 only and cannot resolve dependency hosts.
Before normal use, test a charged spear against a stationary mob, then a moving
target; verify one burst per press, braking, releasing/rearming, disabling during
a burst, obstruction checks, and the view from a second client.

## Reference

API names, build coordinates and charge timing were checked against:
https://github.com/etianl/Trouser-Streak/tree/eec0e52255d05d4a8d686cc7d1d21093fa901a5f
particularly its SpearKill.java by etianl and Kimtaeho. This project implements a
separate module and does not redistribute Trouser Streak's classes or assets.
