[![Build Status](https://ci.codemc.org/buildStatus/icon?job=BentoBoxWorld/AutoIslandPurge)](https://ci.codemc.org/job/BentoBoxWorld/job/AutoIslandPurge/)

# AutoIslandPurge

## About

An addon of BentoBox to purge islands automatically. Islands are purged with specific inactive days.
It lazily purges the islands so that it doesn't affect the server performance. 
It doesn't free up the world file size, just give some empty spaces for new islands.


## How to use

1. Place the addon jar in the addons folder of the BentoBox plugin.
2. Restart the server
3. The addon will create a data folder and inside the folder will be a config.yml
4. Edit the config.yml how you want.
5. Restart the server if you make a change

## Rules for purging
An island will become eligible for deletion if the following is true:

1. The island is not the spawn island
2. The island is not purge protected (you can protect islands from being purged with the purge protect admin command)
3. The island must be owned
4. The member size of the island must be less than the number given in the config. Default 1 (a single-player island).
5. The player has not been online for the number of days given in the config. Default 7. See below.

## Config

Example:

```
enabled-modes:
- bskyblock
- aoneblock
offline-days-until-purge: 7
purge-island-member-size: 1
check-islands-ticks: 300
ticks-per-island-deleted: 10
forced-spawn-location: false
spawn-location:
  world: world
  x: 0
  y: 60
  z: 0
  pitch: 0
  yaw: 0
```
### enabled-modes
This is a list of the game modes that AutoIslandPurge will operate on.

### offline-days-until-purge
The number of days a player must have been offline before the island is wiped.

### purge-island-member-size
The maximum number of members an island can have before it is not eligible for purging.

### check-islands-ticks
How often islands should be checked to see if an island needs deleting. One tick is 1/20s.

### ticks-per-island-deleted
How often an island should be deleted if there are more than one queued.

### spawn-location
Where players should be teleported if their island was deleted while they were logged off.

### forced-spawn-location
If true, then players will be teleported to the spawn location if their island was deleted while they were logged off.
If false, then they will instead be teleported to the location of the spawn island in the world they were in when they logged off.
They are only teleported to the spawn location if that world does not have a spawn island.
