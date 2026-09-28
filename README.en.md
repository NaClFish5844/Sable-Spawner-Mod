# SableSpawner Developer Guide

[简体中文](README.zh-CN.md) | **English**

> Version: Development Build (for NeoForge 1.21.1) | Updated: 2026-09-29
> For modpack authors / datapack authors. This document covers **implemented features**; the planned fleet system is described in the update plan document.

---

## 1. Overview

**SableSpawner** is an **addon** for the [Sable](https://github.com/ryanhcode) space physics mod ecosystem: it reads ship blueprints configured in datapacks and **automatically spawns enemy ships** for players **in any dimension configured by datapacks** (decided by the property's `available_dimension` together with `worldconfig`), providing full lifecycle management:

- Per-player independent prefetching, timing, and spawning (non-blocking)
- World level system (kill score → level → filtering by level)
- Mass-percentage driven combat state machine (wreck conversion / FTL retreat / timed despawn)
- Debris management (automatic tracking and scheduled cleanup of split debris and wrecks)
- Player protection periods (join, dimension change, respawn, after a kill)

All values (spawn parameters, combat thresholds, world levels, weights) are configured through **datapacks** — no code needed to tune them.

## 2. Environment & Dependencies

| Dependency | Version | Notes |
|---|---|---|
| NeoForge | 1.21.1 | Prerequisite |
| Sable | >= 2.0.3 | The sub-level physics core |
| Sable Schematic API | >= 0.4.0 | Blueprint format provider (load / place); **optional** |

`neoforge.mods.toml`: `neoforge` / `minecraft` / `sable` are hard dependencies (the game refuses to load without them); `sable_schematic_api` is **optional** — the mod starts normally when it is missing, but blueprint loading is disabled (no ships to spawn).

## 3. Installation & Datapack Placement

0. If you downloaded the source and want to compile it yourself: put the build artifact `sablespawner-*.jar` into `mods/`
<br>
1. Make sure `sable` is installed (required); `sable_schematic_api` is optional (blueprint features are disabled if missing)
2. Datapacks do **not** go into the world's `datapacks/` — they go into the **sideload directory**:

   ```
   gamedir/sablespawner/
   ```

   (With version isolation: `.minecraft/versions/<instance>/sablespawner/`; on a server: `sablespawner/` under the server root)

3. On first launch, `default.json` (global default config) is generated automatically and can be edited directly
4. After changing datapacks, reload with `/sablespawner reload all` (OP required) — no restart needed

## 4. Quick Start (Minimal Datapack)

```
sablespawner/
├── default.json                  ← generated automatically, editable
└── example/                      ← container (a folder; can also be zipped as example.zip)
    ├── meta.json                 ← pack metadata
    └── data/
        ├── worldconfig/
        │   └── deepspace.json    ← dimension config
        ├── properties/
        │   └── example.json      ← spawn config
        └── blueprints/
            └── example.nbt       ← ship blueprint (compressed NBT)
```

**meta.json**
```json
{
  "packname": "example"
}
```

**data/worldconfig/deepspace.json** (dimension config)
```json
{
  "dimension": "deepspace:space",
  "spawn_pattern": "space",
  "enemy_prefix": "[114514] ",
  "ally_prefix": "[1919810] "
}
```
> Note: to keep things flexible, prefixes are concatenated **directly** — add spaces yourself if you need them.

**data/properties/example.json** (spawn config)
```json
{
  "schematic_source": "datapack",
  "source_mod_id": "sable_schematic_api",
  "schematic_name": "example.nbt",
  "sublevel_types": ["enemy"],
  "sublevel_function": "warship",

  "enemy_property": {
    "available_world_level": [1, 2, 3],
    "available_dimension": ["deepspace:space"],
    "natural_spawn": true,
    "weight": 10,
    "min_spawn_distance": 64,
    "max_spawn_distance": 128,
    "min_spawn_interval": 1200,
    "max_spawn_interval": 1800,
    "max_spawn_amount": 1,
    "destroy_threshold": 10,
    "life_time": 3600,
    "ftl_charge_threshold": 50,
    "ftl_charge_duration": 1200,
    "value": 10
  }
}
```

Once placed, enter `deepspace:space` and wait for the protection period to end — enemy ships will appear.

## 5. Datapack Format Reference

### 5.1 Sideload & Container Rules

| Rule | Description |
|---|---|
| Sideload root | `gamedir/sablespawner/` (the only one; `default.json` must live here, not inside a pack) |
| Container | A folder or `.zip` placed under the sideload root (any depth) |
| Valid root | A directory **containing `meta.json` + `data/`** inside a container; bare content may be wrapped in one folder |
| Pack boundary | Each pack = one directory with `meta.json` + `data/`; no further splitting inside a pack |
| Multiple packs per container | One folder / one zip may hold several packs, each loaded independently |
| Flexible layouts | Zip the pack content / zip a folder containing it / plain folder with an extra layer — all supported |
| Nested categories | `data/` may contain freely nested subfolders (blueprints, properties, configs all support multi-level folders) |
| No nested zips | A `.zip` inside a zip → **the whole pack is skipped** |
| Blueprint name clashes | Blueprint **file names must be unique within a pack** (same name in different folders is a violation — split the pack) |
| Scan depth | Unlimited — **do not include zip bombs** |

### 5.2 default.json (Global Default Config)

Located at the sideload root (`gamedir/sablespawner/default.json`), generated on first launch:

```json
{
  "levels": [0, 100, 200, 500, 1000, 114514],
  "enemy_prefix": "[ENEMY] ",
  "ally_prefix": "[ALLY] ",
  "neutral_prefix": "[NEUTRAL] "
}
```

| Field | Type | Description |
|---|---|---|
| `levels` | int[] | World level thresholds (global default; can be overridden by worldconfig). **score ≥ levels[i] → level i+1** (starting from level 1) |
| `enemy_prefix` | string | Enemy name prefix (used for IFF checks, roaming ship detection, and restart cleanup) |
| `ally_prefix` | string | Ally name prefix |
| `neutral_prefix` | string | Neutral name prefix |

### 5.3 worldconfig (Dimension Config)

Each `.json` under `data/worldconfig/` configures **one dimension**:

| Field | Type | Required | Description |
|---|---|---|---|
| `dimension` | string | **Yes** | Dimension id (`namespace:path`, e.g. `deepspace:space`) |
| `spawn_pattern` | `space` / `ocean` / `land` | No | Spawn pattern (reserved field, currently unused by spawn logic) |
| `world_level` | int[] | No | Overrides the global `levels` |
| `enemy_prefix` | string | No | Overrides the global prefix |
| `ally_prefix` | string | No | Overrides the global prefix |
| `neutral_prefix` | string | No | Overrides the global prefix |

- If a dimension has no config, it falls back to the global values (level table, prefixes) from `default.json`
- Files with a missing / invalid `dimension` are skipped
- **Dimensions and spawning**: whether ships spawn is decided by the property's `available_dimension` / `available_world_level`; dimensions without a worldconfig use the global `levels`

### 5.4 properties (Blueprint Properties)

Each `.json` under `data/properties/` describes one blueprint. **Top-level common fields**:

| Field | Type | Required | Description |
|---|---|---|---|
| `schematic_source` | `datapack` / `folder` | Yes | Blueprint source: in-pack / on-disk library |
| `source_mod_id` | string | Yes | Blueprint format provider; currently only `sable_schematic_api` |
| `schematic_name` | string | Yes | Blueprint file name (with extension; **unique within the pack**, e.g. `example.nbt`) |
| `sublevel_types` | string[] | Yes | Declares which types to generate: `enemy` / `ally` / `prefab`, multiple allowed |
| `sublevel_function` | `warship` / `cargo` | **Yes** | Ship role (used for query filtering); missing / invalid → **the whole file is skipped** |

> One file may contain `enemy_property` / `ally_property` / `prefab_property` blocks at the same time.

**enemy_property block (spawn parameters)**:

| Field | Type | Default | Description |
|---|---|---|---|
| `available_world_level` | int[] | empty = any | **List** of allowed world levels (exact match: spawns only when the player's current level is in the list) |
| `available_dimension` | string[] | empty = any | Allowed dimension ids |
| `natural_spawn` | bool | false | Whether to take part in roaming natural spawning (set `true` for roaming ships) |
| `weight` | int | 0 | Prefetch random weight; ≤0 never selected; **all-zero falls back to uniform random** |
| `min_spawn_distance` | int | -1 | Minimum spawn distance (blocks). Invalid values are treated as "not configured" (see fallback rules below) |
| `max_spawn_distance` | int | -1 | Maximum spawn distance (blocks). Same as above |
| `min_spawn_interval` | int | -1 | Minimum spawn interval (ticks). Same as above |
| `max_spawn_interval` | int | -1 | Maximum spawn interval (ticks). Same as above |
| `max_spawn_amount` | int | 1 | Maximum ships per wave (the actual amount is uniform random in `1 ~ max`); invalid → no spawn |
| `destroy_threshold` | int(%) | -1 | Destruction threshold: mass% ≤ this value → destroyed; **-1 / invalid = never destroyed** |
| `life_time` | int | -1 | Enemy ship lifetime (ticks), removed on timeout; **-1 / invalid = never times out** |
| `ftl_charge_threshold` | int(%) | -1 | FTL retreat threshold: mass% ≤ this value → charging starts; **must be configured together with duration; either invalid = FTL fully disabled** |
| `ftl_charge_duration` | int | -1 | FTL charge duration (ticks); when charging completes the ship is removed; same as above |
| `value` | int | 0 | Score reward for destroying it (drives world levels); negative values count as 0 (no score) |

**All time units are in ticks** (20 ticks = 1 second of game time).

**"Invalid value" definition**: `0`, negative numbers, and `Integer.MAX_VALUE` are all treated as "not configured" (disabled). They do not block the file — only that field's behavior is affected.

**Distance / interval range fallback rules** (each of the two fields is normalized independently):

| Case | Resulting range |
|---|---|
| Both ends valid | `[smaller, larger]` (swapped values are auto-corrected) |
| Both valid and equal | `[value, value+1]` |
| Only min valid | `[min, min+1]` |
| Only max valid | `[0, max]` |
| Both invalid | **No spawn** (no prefetch ticket is created) |

- Distance is mapped inside the range through a Gaussian factor (mean 0.5, std 0.15, clamped to `[0,1]`), and the result falls inside the range
- Interval is uniform random in `[min, max-1]` (when `min == max` it takes `min`)

### 5.5 Blueprint File Placement

- **datapack source**: put blueprints in the pack under `data/blueprints/<schematic_name>` (the legacy folder `data/schematics/` is still scanned as a fallback); must be a **compressed NBT** file (`.nbt`)
- **folder source**: reads `gamedir/Sable-Schematics/<schematic_name>` (the Sable Schematic API blueprint library)
- Blueprint contents are parsed by the corresponding mod; blueprint file names must be unique within a pack (see 5.1)

### 5.6 ally / prefab Blocks

Currently placeholder implementations — the spawn primitive is ready, waiting for gameplay integration:

| Block | Fields | Description |
|---|---|---|
| `ally_property` | `placeholder` (bool) | Placeholder flag |
| `prefab_property` | `price` (int) / `reusable` (bool) | Prefab ship price / whether it is reusable |

### 5.7 Validation & Guard Rails

Validation runs on load (violations are logged as `warn`, with the field name):

| Check | Violation handling |
|---|---|
| `meta.json` missing `packname` | The container is skipped |
| `schematic_source` / `source_mod_id` / `schematic_name` missing or invalid | **The whole property file is skipped** |
| `sublevel_types` missing / invalid | No properties are generated |
| `sublevel_function` missing / invalid | **The whole property file is skipped** |
| `dimension` missing / invalid | The worldconfig file is skipped |
| `spawn_pattern` invalid | The field is treated as default (warning) |
| `world_level` not ascending / duplicate values | The field is treated as default (warning) |
| Unknown field inside a block | Warning and ignored (helps catch typos) |
| Wrong field type (e.g. a number written as a string) | Deserialization fails → that type is not generated + warning |
| JSON syntax error | The file is skipped + error |
| Property has no matching blueprint | Warning (no blueprint available for this property) |
| Blueprint load failure | Warning (the blueprint stays in reference state and is retried on use) |
| Nested zip inside a zip | The whole pack is skipped + error |
| Nested valid root inside a pack | Not recognized (the valid root is the pack boundary; scanning stops there) |
| Field-level invalid values (0 / negative / oversized) | Treated as "not configured" (disabled / no spawn); the file is not blocked |

## 6. Server Config (config/sablespawner-server.toml)

| Option | Default | Range | Description |
|---|---|---|---|
| `enemy_detection_distance` | 256 | 8~1048576 | Enemy detection distance (blocks). If enemies are within this range of a player → no spawn |
| `debris_despawn_time` | 2400 | -1~72000 | Despawn time (ticks) for **short-lived debris** (split fragments); -1 = disable cleanup |
| `long_debris_despawn_time` | 24000 | -1~1728000 | Despawn time (ticks) for **long-lived debris** (wrecks); -1 = disable cleanup |
| `scan_interval` | 100 | 10~172800 | Cold scan interval (ticks). Do not set it too low |
| `player_protection_time` | 1200 | 10~172800 | Protection time (ticks) after a kill / respawn |
| `player_spawn_protection_time` | 2400 | 10~172800 | Landing protection time (ticks) after joining / changing dimension |
| `blueprint_cache_max_blocks` | 100000 | 10~2147483647 | Blueprint cache threshold: large blueprints with more blocks are **not cached in memory** (read on first use; may cause a hiccup) |

## 7. Behavior & Mechanics

### 7.1 Scheduling Architecture

| Hook | Frequency | Gating | Responsibility |
|---|---|---|---|
| `callScan` | every `scan_interval` ticks | dimension loaded | Queue maintenance, expiry cleanup (cold path) |
| `callPer5Tick` | every 5 ticks | dimension loaded + non-spectator players present | Prefetch → due check → spawn, FTL checks |
| `callPerTick` | every tick | same as above | Destruction check, debris collection, wreck/expiry removal (hot path) |

When all players leave a dimension (but the dimension stays loaded), **combat state freezes** (FTL charging, mass changes pause), while expiry / wreck cleanup still advances with game time.

**Dimension controller**: one controller per loaded dimension (not limited to specific dimensions, fully data-driven). When a dimension unloads, the controller is **kept** (tracking state is not lost); when the dimension reloads, references are refreshed and tracking is restored entry by entry (`rebind`); entries that cannot be restored are dropped. While the dimension is unloaded, the controller is completely silent (neither cold nor hot path runs).

**Restart cleanup**: after server start and datapack loading, ownerless enemy ships whose names contain the enemy prefix and which are not in the tracker are cleaned up — enemy ships left over from a server restart do not linger forever.

### 7.2 Roaming Ship Spawn Flow (per player, independent)

```
Prefetch (when a player joins the queue)
  ├─ Filter: available_dimension contains the current dimension
  │        + available_world_level contains the player's current level
  │        + natural_spawn = true
  ├─ Randomly pick a type by weight (all-zero falls back to uniform)
  └─ Build a ticket (amount / distance / delay randomized; invalid distance or interval → no ticket = no spawn; per-player queue, non-blocking)

Due check (every 5 ticks)
  ├─ Player online and in this dimension
  ├─ No enemies near the player (isEnemyNearby, prefix + AABB)
  ├─ Player not under protection
  └─ now ≥ protection-end time + random interval
     (interval is uniform in `[min, max-1]`; "not under protection" counts as "protection already ended" → spawn immediately; the spawn time is anchored only to the protection-end time)

Spawn
  ├─ Blueprint object: fetched from the blueprint registry (cached ones directly; large reference-state ones read on first use)
  ├─ Pose generation: random formation + spacing (see 7.4)
  ├─ Vacancy check for all positions (BoundBoxVacantDetection), retry ≤5 times on failure
  ├─ Success → place + name ([prefix] random 3 letters + 3 digits) → add to tracker
  └─ Clear ticket → re-prefetch next round
```

**Combat power constraint**:
- Each player spawns at most one wave at a time, with `1 ~ max_spawn_amount` ships
- No spawn while enemies are nearby

So the total roaming combat power never stacks — no extra code needed.
Balance is up to datapack authors.

### 7.3 Player Protection

| Trigger | Duration |
|---|---|
| Joining the server | `player_spawn_protection_time` |
| Changing dimension (any) | `player_spawn_protection_time` |
| Respawn | `player_protection_time` |
| Destroying an enemy ship | `player_protection_time` |

While protected, the player **cannot be targeted for spawning**, and existing tickets are postponed (not discarded).

### 7.4 Formation Generation

Each wave randomly picks `amount ∈ [1, max_spawn_amount]` (uniform):

- **Distance**: Gaussian mapping inside `min/max_spawn_distance` (factor mean 0.5, std 0.15, clamped to `[0,1]`) → the result falls inside the range
- **Orientation**: a random rotation quaternion whose +Z is the formation plane normal, and also the direction toward the formation center
- **Formation**: randomly chosen (line / ring / triangle / cube / sphere / hemisphere; the current implementation always uses a ring, the rest are WIP)
- **Ring radius**: `r = spacing / (2·sin(π/amount))`
- **spacing**: longest edge of the blueprint bounding box × 1.1
- **Ship heading**: all face the player
- On spawn-retry, orientation and origin are randomized again

### 7.5 Combat State Machine

Mass percentage `mass% = current self mass / initial total mass × 100` (updated every 5 ticks; enemy ships only, wrecks do not take part):

```
mass% ≤ destroy_threshold          → destroyed: convert to wreck + score (value) + protection
mass% ≤ ftl_charge_threshold       → start FTL charging (duration must also be configured)
charging interrupted (mass% back above the threshold) → charge invalidated, re-times when it drops again
charging for ≥ ftl_charge_duration → ship removed (retreat)
lifetime ≥ life_time               → removed on timeout (not configured = never times out)
wreck lifetime ≥ long_debris_despawn_time → wreck removed
```

- **Destruction takes priority**: within the same check, destruction is evaluated before FTL; a destroyed ship immediately becomes a wreck and stops charging
- **Wreck conversion**: destruction does not delete the entity directly; it turns into a wreck and resets the timer, using `long_debris_despawn_time` (leaving time for salvage)
- **Destruction attribution**: the player bound at spawn time; if the target player is offline, the ship **still becomes a wreck**, only score and protection are skipped

**Debris Tracking & Cleanup (implemented)**

When a player mines a ship and Sable splits a sub-level, the mod recognizes the resulting debris and tracks it automatically:

| Debris source | Cleanup time |
|---|---|
| Wreck (hull of a destroyed enemy ship) | `long_debris_despawn_time` (long-lived) |
| Split debris (fragments of enemy / ally ships broken by mining) | `debris_despawn_time` (short-lived) |
| Debris from player ships | Not tracked, not cleaned up (player asset protection) |

- When debris splits again, cause / side / source name are inherited automatically (debris chain)
- Debris split from a player's own (untracked) ship is neither collected nor cleaned up
- **Sable's sub-level splitting does not need to be disabled** (leave `sub_level_splitting` at its default)

### 7.6 World Level

- Score: accumulated from `value` (persisted per player)
- Level: mapped from the `levels` threshold table (starting at 1); `levels` comes from `default.json` and can be overridden per dimension by `world_level`; dimensions without a worldconfig also use the global `levels`
- Effect: prefetch filtering (`available_world_level`) — low-level players never get high-level ships
- Score spending (shops) has a deduct-score API reserved

## 8. Debug Commands

OP required (permission level 2). Chat feedback is shown in Chinese / English based on the client language; `debug` output always goes to the log.

```
/sablespawner
├── reload all | datapack | blueprint      Reload (all = datapacks + blueprints)
├── worldconfig getLevels | getPrefix      Current dimension config (level table / prefixes)
├── defaultconfig getLevels | getPrefix    Global default config (default.json)
├── player [player]                        Omit [player] = yourself
│   ├── protection getExpireTime | set | setTime <tick> | remove
│   └── score get | add <amount> | set <amount> | sub <amount>
└── debug
    ├── getRegistry blueprintRegistry | propertyBlueprintMap | datapackRegistry
    │                propertyManager | worldconfigManager | all
    ├── getRunTime  controllers | playerTracker
    │                spawnQueue [dim] | subLevelTracker [dim]
    └── runtime    spawnEnemy <packname> <name> | forceFlushSpawnQueue [dim]
```

- `getRegistry`: prints the datapack / blueprint registries (`all` prints all 5 at once);
- `getRunTime`: prints runtime data — controller states, player tracking, spawn queue, sub-level tracking (enemy / ally / debris tables + container sub-level view);
- `runtime`: manually spawn an enemy ship (pack name + property name, for testing datapacks), force-clear and rebuild the spawn queue of a dimension;
- `[dim]` is a dimension selector (Tab-completable); omitted = the executor's dimension;
- Enemy output includes spawn time, total / remaining mass (percentage), remaining lifetime (`life_time` remaining, negative when not configured), FTL charge remaining; debris output includes cause (wreck / split), side (enemy / ally / player), source name, lifetime and remaining cleanup time; the container sub-level view lists all sub-levels in the container together with their tracking state (for troubleshooting).

## 9. FAQ

| Symptom | Troubleshooting |
|---|---|
| No enemies after entering a dimension | ① Is the datapack in the right place (**sideload directory `gamedir/sablespawner/`**, not the world's `datapacks/`)? ② Does the container have `meta.json` + `data/`? ③ Is `natural_spawn` `true`? ④ Does `available_dimension` contain the current dimension? ⑤ Does `available_world_level` contain the player's current level? ⑥ Protection period not over (2400t by default)? ⑦ Are `min/max_spawn_interval` or `min/max_spawn_distance` invalid (≤0 / oversized = no spawn)? ⑧ Player level not in the `available_world_level` list (exact match)? |
| Log: "no blueprint for property [x]: y.nbt" | `schematic_name` does not match the blueprint file name, or the file is missing (datapack source requires `data/blueprints/`) |
| Log: "nested zips are not allowed" | A zip inside a zip (including in category folders) — remove the inner zip |
| Log: "Failed to resolve blueprint" | Blueprint failed to load: corrupted file / not a blueprint of the corresponding mod |
| Large enemy ship causes a hiccup on first appearance | Large blueprints above `blueprint_cache_max_blocks` are not cached and are read on first use; raise the threshold (uses memory) |
| Enemy ship does not disappear or drop after being destroyed | Normal: destruction converts it into a wreck (salvageable); it disappears after `long_debris_despawn_time` |
| Retreat stops after the ship regenerates mass | Normal: an interrupted charge is invalidated and must drop below the threshold again |
| Enemy ships disappear after a server restart | Normal: leftover ownerless enemy ships are cleaned up when the container is ready (restart cleanup) |
| Server crashes when a player disconnects | Old-version bug (null check on offline destruction attribution); fixed — make sure you use the latest build |
| Enemy debris never disappears | Debris is cleaned up automatically (see 7.5): split fragments use `debris_despawn_time`, wrecks use `long_debris_despawn_time`; setting -1 disables cleanup |
| Spawn interval seems to have no effect | Make sure `min/max_spawn_interval` is configured; the interval is uniform in `[min, max-1]` and anchored to the **protection-end time**; outside protection it spawns immediately |
| Datapack changes have no effect | Run `/sablespawner reload all`; if it still does not work, check the items above |

## 10. Future Plans (Not Implemented)

- **Event fleet system**: fleet composition (models + counts) + formation tree (sub-formations / auto bounding boxes / box-in-box) + fleet bonus score. Design is finalized (see `SableSpawner-更新计划.md`); landing time TBD
- **Event-driven reload**: event-based reload triggering (the command version is implemented: `/sablespawner reload`)
- **Native blueprint parsing**: drop the dependency on the `sable_schematic_api` API (currently blueprint loading / placement still goes through its interface, and only that format is supported)
- **DataTag**: replace enum constants with extensible data tags
- One-time blueprint items (consumed on right-click placement), blueprint anchors (snapshot patching), warning system, visual editing / auto export

## 11. Source File Index

```
src/main/java/dev/sablespawner/
├── SableSpawner.java             Main class (static handles, event & config registration)
├── SableSpawnerConfig.java       Server config options
├── manager/
│   ├── datapack/                 Datapack domain: sideload scanning / loading / validation / registry / query
│   ├── blueprint/                Blueprint domain: scanning / parsing / loading / registry / query (four components)
│   └── DataTag.java              Data tags (reserved)
├── player/                       Player state (protection, score, level)
├── registry/                     Registration & commands (SableSpawnerCommands: debug commands)
├── spawn/
│   ├── GlobalControl.java        Scheduler (cold/hot-path gating, controller lifecycle)
│   ├── EnemyControl.java         Per-dimension control (spawn flow, combat state machine, wreck conversion, debris collection)
│   ├── Spawner.java              Spawn primitives (vacancy check, blueprint fetching, placement, naming)
│   └── session/
│       ├── spawnqueue/           Prefetch queue (SpawnQueue / SpawnTicket / SpawnTicketBuilder)
│       └── tracker/              Sub-level tracking (SubLevelTracker base + enemy / ally / debris domains)
│           ├── entry/            Tracking entries (SubLevelEntry base + enemy / ally / debris states)
│           └── query/            Chainable read-only queries (three domain queriers)
└── util/                         Utilities (bounding boxes / file IO / hashing / formation generation)
```
