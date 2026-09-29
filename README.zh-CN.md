# SableSpawner 开发者使用文档

**简体中文** | [English](README.en.md)

> 版本：开发版（面向 NeoForge 1.21.1） | 更新：2026-09-29
> 适用于整合包作者 / 数据包作者。本文档描述的是**已实现功能**；规划中的舰队系统见更新计划文档。

---

## 1. 概述

**SableSpawner** 是 [Sable](https://github.com/ryanhcode) 太空物理模组生态的**附属模组**：读取数据包中配置的舰船蓝图，在**数据包配置的任意维度**（由属性的 `available_dimension` 与 `worldconfig` 共同决定）为玩家**自动刷出敌方船只**，并提供完整的生命周期管理：

- 按玩家独立预取、计时、刷出（互不阻塞）
- 世界等级系统（击杀得分 → 等级 → 越级过滤）
- 质量百分比驱动的战斗状态机（击沉残骸化 / FTL 撤离 / 超时消失）
- 碎片管理（分裂碎片 / 击沉残骸的自动追踪与定时清理）
- 玩家保护期（上线、换维度、重生、击沉后）

所有数值（刷怪参数、战斗阈值、世界等级、权重）均通过**数据包**配置，无需写代码即可调参。

## 2. 环境与依赖

| 依赖 | 版本要求 | 说明 |
|---|---|---|
| NeoForge | 1.21.1 | 前置 |
| Sable | >= 2.0.3 | 物理化子空间本体 |
| Sable Schematic API | >= 0.4.0 | 蓝图格式提供方（加载 / 放置）；**选装** |

`neoforge.mods.toml`：`neoforge` / `minecraft` / `sable` 为硬性依赖（缺失时游戏拒绝加载）；`sable_schematic_api` 为**选装**——缺失时模组正常启动，但蓝图加载停用（无船可放）。

## 3. 安装与数据包放置

0. 如果你下载了源码，想要自己编译：请将编译产物 `sablespawner-*.jar` 放入 `mods/` 
<br>
1. 确认 `sable` 已安装（必需）；`sable_schematic_api` 为选装（缺失时蓝图功能停用）
2. 数据包**不放入存档的 `datapacks/`**，而是放入**侧载目录**：

   ```
   gamedir/sablespawner/
   ```

   （版本隔离时为 `.minecraft/versions/<实例名>/sablespawner/`；服务器为服务端根目录下的 `sablespawner/`）

3. 首次启动会自动生成 `default.json`（全局默认配置），可直接编辑
4. 修改数据包后可用指令重载（`/sablespawner reload all`，需 OP 权限），无需重启

## 4. 快速开始（最小数据包）

```
sablespawner/
├── default.json                  ← 自动生成，可编辑
└── example/                      ← 容器（目录；也可以打包成 example.zip）
    ├── meta.json                 ← 包元数据
    └── data/
        ├── worldconfig/
        │   └── deepspace.json    ← 维度配置
        ├── properties/
        │   └── example.json      ← 刷怪配置
        └── blueprints/
            └── example.nbt       ← 舰船蓝图（压缩 nbt）
```

**meta.json**
```json
{
  "packname": "example"
}
```

**data/worldconfig/deepspace.json**（维度配置）
```json
{
  "dimension": "deepspace:space",
  "spawn_pattern": "space",
  "enemy_prefix": "[114514] ",
  "ally_prefix": "[1919810] "
}
```
> 注意：为了保证自由性，前缀的字符串是**直接**拼接上去的，如果你需要空格，就要自己加入

**data/properties/example.json**（刷怪配置）
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

放入后进入 `deepspace:space`，等待保护期结束即可看到敌舰。

## 5. 数据包格式详解

### 5.1 侧载与容器规则

| 规则 | 说明 |
|---|---|
| 侧载根 | `gamedir/sablespawner/`（唯一；`default.json` 只能放这里，不能放包内） |
| 容器 | 文件夹 或 `.zip`，放在侧载根下（任意深度均可） |
| 有效根 | 容器内**含 `meta.json` + `data/`** 的目录；可以给裸内容套一层文件夹 |
| 包边界 | 每个包 = 一个含 meta.json + data/ 的目录；包内不再分裂 |
| 一容器多包 | 一个目录 / 一个 zip 内可放多个包，各自独立加载 |
| 灵活结构 | 可直接压缩包内容 / 压缩含内容的文件夹 / 裸文件夹套层，均支持 |
| 嵌套分类 | `data/` 下可自由建子目录分类（蓝图、属性、配置都支持多级目录） |
| zip 内禁嵌 zip | 压缩包内出现 `.zip` → **整个包跳过** |
| 蓝图重名 | 包内蓝图**文件名必须唯一**（跨目录同名即违规，请拆包） |
| 扫描深度 | 不限深 **请勿放入zip炸弹** |

### 5.2 default.json（全局默认配置）

位于侧载根（`gamedir/sablespawner/default.json`），首次启动自动生成：

```json
{
  "levels": [0, 100, 200, 500, 1000, 114514],
  "enemy_prefix": "[ENEMY] ",
  "ally_prefix": "[ALLY] ",
  "neutral_prefix": "[NEUTRAL] "
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `levels` | int[] | 世界等级阈值（全局默认；可被 worldconfig 覆盖）。**分数 ≥ levels[i] → 等级 i+1**（从 1 级起） |
| `enemy_prefix` | string | 敌人名称前缀（IFF 判定、散船探测、重启清理都靠它） |
| `ally_prefix` | string | 友军名称前缀 |
| `neutral_prefix` | string | 中立名称前缀 |

### 5.3 worldconfig（维度配置）

`data/worldconfig/` 下每个 `.json` 配置**一个维度**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `dimension` | string | **是** | 维度 id（`namespace:path`，如 `deepspace:space`） |
| `spawn_pattern` | `space` / `ocean` / `land` | 否 | 刷新模式（预留字段，当前不参与刷怪逻辑） |
| `world_level` | int[] | 否 | 覆盖全局 `levels` |
| `enemy_prefix` | string | 否 | 覆盖全局前缀 |
| `ally_prefix` | string | 否 | 覆盖全局前缀 |
| `neutral_prefix` | string | 否 | 覆盖全局前缀 |

- 某维度没有对应配置时，回退到 `default.json` 的全局值（等级表、前缀）
- 缺失 / 非法 `dimension` 的文件会被跳过
- **维度与刷怪的关系**：是否刷怪由属性的 `available_dimension` / `available_world_level` 决定；无 worldconfig 的维度按全局 `levels` 计算等级

### 5.4 properties（蓝图属性）

`data/properties/` 下的 `.json`，每个文件描述一份蓝图。**顶层公共字段**：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `schematic_source` | `datapack` / `folder` | 是 | 蓝图来源：包内 / 磁盘库 |
| `source_mod_id` | string | 是 | 蓝图格式提供方，目前仅 `sable_schematic_api` |
| `schematic_name` | string | 是 | 蓝图文件名（含扩展名；**包内唯一**，如 `example.nbt`） |
| `sublevel_types` | string[] | 是 | 声明生成哪些类型：`enemy` / `ally` / `prefab`，可同时多个 |
| `sublevel_function` | `warship` / `cargo` | **是** | 舰船用途分类（查询过滤用）；缺失 / 非法会导致**整个文件跳过** |

> 一个文件可同时写 `enemy_property` / `ally_property` / `prefab_property` 多个块。

**enemy_property 块（刷怪参数）**：

| 字段 | 类型 | 默认 | 说明 |
|---|---|---|---|
| `available_world_level` | int[] | 空=不限 | 允许出现的世界等级**列表**（精确匹配：玩家当前等级在列表内才刷） |
| `available_dimension` | string[] | 空=不限 | 可出现的维度 id |
| `natural_spawn` | bool | false | 是否参与散船自然刷新（散船请设 `true`） |
| `weight` | int | 0 | 预取随机权重；≤0 不参与命中；**全 0 回退均匀随机** |
| `min_spawn_distance` | int | -1 | 刷出距离下限（格）。非法值按"未配置"处理（见下方兜底规则） |
| `max_spawn_distance` | int | -1 | 刷出距离上限（格）。同上 |
| `min_spawn_interval` | int | -1 | 刷怪间隔下限（tick）。同上 |
| `max_spawn_interval` | int | -1 | 刷怪间隔上限（tick）。同上 |
| `max_spawn_amount` | int | 1 | 单波数量上限（实际数量在 `1 ~ max` 间均匀随机）；非法 → 不刷 |
| `destroy_threshold` | int(%) | -1 | 击沉阈值：质量% ≤ 此值 → 判定击沉；**-1 / 非法 = 永不击沉** |
| `life_time` | int | -1 | 敌船存在时间（tick），超时移除；**-1 / 非法 = 永不超时** |
| `ftl_charge_threshold` | int(%) | -1 | FTL 撤离阈值：质量% ≤ 此值 → 开始充能；**与 duration 需同时配置，任一非法 = FTL 整体禁用** |
| `ftl_charge_duration` | int | -1 | FTL 充能时长（tick），充能完成 → 船只移除；同上 |
| `value` | int | 0 | 击沉奖励分数（驱动世界等级）；负值按 0 处理（不加分） |

**时间单位统一为 tick**（20 tick = 1 秒游戏时间）。

**"非法值"口径**：`0`、负数、`Integer.MAX_VALUE` 均视为未配置（禁用），不会拦截文件，只影响该字段行为。

**距离 / 间隔的区间兜底规则**（两个字段各自合法与否，自动整理）：

| 情况 | 结果区间 |
|---|---|
| 两端都合法 | `[较小值, 较大值]`（写反了自动颠倒） |
| 两端合法且相等 | `[值, 值+1]` |
| 仅 min 合法 | `[min, min+1]` |
| 仅 max 合法 | `[0, max]` |
| 两端都非法 | **不刷**（不生成预取票据） |

- 距离在区间内取高斯因子（均值 0.5、标准差 0.15，截断到 `[0,1]`）映射，结果落在区间内
- 间隔在 `[min, max-1]` 均匀随机（`min == max` 时取 `min`）

### 5.5 蓝图文件放置

- **datapack 源**：蓝图放包内 `data/blueprints/<schematic_name>`（旧目录名 `data/schematics/` 仍兜底扫描），必须为**压缩 nbt**（`.nbt`）
- **folder 源**：读取 `gamedir/Sable-Schematics/<schematic_name>`（Sable Schematic API 的蓝图库）
- 蓝图内容由对应 mod 解析；包内蓝图文件名唯一（见 5.1）
- **一个蓝图文件对应一艘船体（一个子空间）**：放置只会产生单个 sub-level；请勿把多艘船封装进同一蓝图文件

### 5.6 ally / prefab 块

当前为占位实现，刷出原语已就绪，等待玩法接入：

| 块 | 字段 | 说明 |
|---|---|---|
| `ally_property` | `placeholder` (bool) | 占位标记 |
| `prefab_property` | `price` (int) / `reusable` (bool) | 预制船价格 / 是否可重复使用 |

### 5.7 校验与防呆

加载时自动校验（违规打 `warn` 日志，含字段名）：

| 校验项 | 违规处理 |
|---|---|
| `meta.json` 缺 `packname` | 该容器跳过 |
| `schematic_source` / `source_mod_id` / `schematic_name` 缺失或非法 | **整个属性文件跳过** |
| `sublevel_types` 缺失 / 非法 | 不生成任何属性 |
| `sublevel_function` 缺失 / 非法 | **整个属性文件跳过** |
| `dimension` 缺失 / 非法 | 该 worldconfig 文件跳过 |
| `spawn_pattern` 非法 | 该字段按默认处理（警告） |
| `world_level` 未升序 / 有重复值 | 该字段按默认处理（警告） |
| 块内出现未知字段 | 警告并忽略（可暴露拼写错误） |
| 块内字段类型错误（如数字写成字符串） | 反序列化失败 → 该类型不生成 + 警告 |
| JSON 语法错误 | 该文件跳过 + error |
| 属性找不到对应蓝图 | 警告（该属性无蓝图可用） |
| 蓝图加载失败 | 警告（该蓝图保持引用态，使用时会重试） |
| 压缩包内嵌压缩包 | 整个包跳过 + error |
| 包内嵌套有效根 | 不识别（有效根即包边界，停止下探） |
| 字段级非法值（0 / 负数 / 超大） | 按"未配置"处理（禁用 / 不刷），不拦截文件 |

## 6. 服务器配置（config/sablespawner-server.toml）

| 配置项 | 默认 | 范围 | 说明 |
|---|---|---|---|
| `enemy_detection_distance` | 256 | 8~1048576 | 敌人探测距离（格）。玩家周围此范围内有敌人 → 不刷新 |
| `debris_despawn_time` | 2400 | -1~72000 | **短时碎片**（断裂碎片）的消失时间（tick）；-1 = 禁用清理 |
| `long_debris_despawn_time` | 24000 | -1~1728000 | **长时碎片**（击沉残骸）的消失时间（tick）；-1 = 禁用清理 |
| `scan_interval` | 100 | 10~172800 | 冷扫描间隔（tick）。请勿设置过低 |
| `player_protection_time` | 1200 | 10~172800 | 击沉 / 重生后的保护时间（tick） |
| `player_spawn_protection_time` | 2400 | 10~172800 | 登录 / 切换维度后的落地保护时间（tick） |
| `blueprint_cache_max_blocks` | 100000 | 10~2147483647 | 蓝图缓存阈值：方块数超出的大型蓝图**不进入内存缓存**（首次使用时现读，可能卡顿） |

## 7. 行为机制详解

### 7.1 调度架构

| 钩子 | 频率 | 门控 | 职责 |
|---|---|---|---|
| `callScan` | 每 `scan_interval` tick | 维度已加载 | 队列维护、过期清理（冷路径） |
| `callPer5Tick` | 每 5 tick | 维度已加载 + 有非旁观玩家 | 预取 → 到点检查 → 刷出、FTL 判定 |
| `callPerTick` | 每 tick | 同上 | 击沉判定、碎片收编、残骸/过期移除（热路径） |

玩家全部离开维度（但维度仍加载）时，**战斗状态冻结**（FTL 充能、质量变化暂停），但过期/残骸清理仍按游戏时间推进。

**维度控制器**：每个已加载维度一个控制器（不限定维度，完全数据驱动）。维度卸载时控制器**保留**（追踪状态不丢）；维度重载时刷新引用并逐个恢复追踪（`rebind`），未能恢复的条目清理。维度未加载期间控制器完全静默（冷热路径都不执行）。

**重启清理**：服务器启动、数据包加载完成后，名称含敌人前缀且不在追踪器中的无主敌船会被清理——服务器重启后残留的旧敌船不会永久滞留。

### 7.2 散船刷新流程（每玩家独立）

```
预取（玩家入队时）
  ├─ 过滤：available_dimension 含当前维度
  │        + available_world_level 含玩家当前等级
  │        + natural_spawn = true
  ├─ 按 weight 权重随机选型（全 0 回退均匀）
  └─ 构建票据（数量 / 距离 / 延迟随机；距离或间隔非法 → 不生成票据 = 不刷；每玩家独立队列，互不阻塞）

到点检查（每 5 tick）
  ├─ 玩家在线且在本维度
  ├─ 玩家周围无敌方（isEnemyNearby，前缀 + AABB）
  ├─ 玩家不在保护期
  └─ now ≥ 玩家出保护时刻 + 间隔随机值
     （间隔取 `[min, max-1]` 均匀；不在保护期视为"已出保护" → 立即刷；刷出时刻只锚定出保护时刻）

刷出
  ├─ 蓝图对象：经蓝图注册表获取（缓存的直取；大型引用态首次现读）
  ├─ 姿态生成：随机队形 + spacing（见 7.4）
  ├─ 全部点位空位检测（BoundBoxVacantDetection），失败重试 ≤3 次
  ├─ 成功 → 放置 + 命名（[前缀] 随机三字母三数字）→ 入追踪器
  └─ 清票据 → 下轮重新预取
```

**战力约束**：
- 每玩家最多只一次刷一波，数量为 `1 ~ max_spawn_amount`
- 附近有敌人不刷 

因此 散船总战力不会叠加，无需额外代码
平衡交给数据包作者

### 7.3 玩家保护期

| 触发 | 时长 |
|---|---|
| 登录服务器 | `player_spawn_protection_time` |
| 切换维度（任意维度） | `player_spawn_protection_time` |
| 重生 | `player_protection_time` |
| 击沉敌船 | `player_protection_time` |

保护期内该玩家**不会成为刷怪目标**，且已有的票据顺延（不丢弃）。

### 7.4 编队生成

单波随机 `amount ∈ [1, max_spawn_amount]`（均匀）：

- **距离**：`min/max_spawn_distance` 区间内的高斯映射（因子均值 0.5、标准差 0.15，截断到 `[0,1]`）→ 结果落在区间内
- **朝向**：随机旋转四元数，其 +Z 为编队平面法向量，也是队形圆心方向
- **队形**：随机选择（line / ring / triangle / cube / sphere / hemisphere；当前实现均走圆环，其余为 WIP）
- **圆环半径**：`r = spacing / (2·sin(π/amount))`
- **spacing**：蓝图包围盒最长边 × 1.1
- **船头**：全部朝向玩家
- 刷出失败重试时重新随机朝向与原点

### 7.5 战斗状态机

质量百分比 `mass% = 本体当前质量 / 初始总质量 × 100`（每 5 tick 更新；仅敌船，残骸不参与）：

```
mass% ≤ destroy_threshold          → 击沉：残骸化 + 加分(value) + 保护
mass% ≤ ftl_charge_threshold       → 开始 FTL 充能（需同时配置 duration）
充能中断（mass% 回升到阈值以上）   → 充能作废，下次再降重新计时
充能持续 ≥ ftl_charge_duration     → 船只移除（撤离）
存在时间 ≥ life_time               → 超时移除（未配置 = 永不超时）
残骸存在时间 ≥ long_debris_despawn_time → 残骸移除
```

- **击沉优先**：同一轮判定里击沉先于 FTL；被击沉的船立即残骸化，不再充能
- **残骸化**：击沉不直接删除实体，而是转为残骸并重置计时，走 `long_debris_despawn_time`（留出打捞时间）
- **击沉归属**：刷出时绑定的玩家；目标玩家离线时**仍然残骸化**，只是跳过加分与保护

**碎片追踪与清理（已实现）**

玩家挖船使 Sable 子空间分裂时，本 mod 会识别分裂产生的碎片并自动纳入追踪：

| 碎片来源 | 清理时间 |
|---|---|
| 击沉残骸（敌船被击沉后的船体） | `long_debris_despawn_time`（长时） |
| 分裂碎片（敌船 / 友船被挖开产生的断块） | `debris_despawn_time`（短时） |
| 玩家船只产生的碎片 | 不追踪、不清理（玩家资产保护） |

- 碎片再分裂时，成因 / 归属 / 来源名自动继承（碎片链）
- 玩家自己的船（未被追踪）分裂出的碎片不会被收编，也不会被清理
- **无需关闭 Sable 的子空间分裂**（`sub_level_splitting` 保持默认即可）

### 7.6 世界等级

- 分数：`value` 累计（玩家数据持久化）
- 等级：`levels` 阈值表映射（1 起）；`levels` 来源为 `default.json`，可被维度配置的 `world_level` 覆盖；无 worldconfig 的维度同样按全局 `levels` 计算
- 作用：预取过滤（`available_world_level`），低等级不会刷出高级船
- 分数消费（商店）预留了扣分接口

## 8. 调试指令

需要 OP（权限等级 2）。聊天反馈随客户端语言显示中文 / 英文；`debug` 输出一律写入日志。

```
/sablespawner
├── reload all | datapack | blueprint      重载（all = 数据包 + 蓝图）
├── worldconfig getLevels | getPrefix      当前维度的配置（等级表 / 前缀）
├── defaultconfig getLevels | getPrefix    全局默认配置（default.json）
├── player [player]                        省略 [player] = 自己
│   ├── protection getExpireTime | set | setTime <tick> | remove
│   └── score get | add <amount> | set <amount> | sub <amount>
└── debug
    ├── getRegistry blueprintRegistry | propertyBlueprintMap | datapackRegistry
    │                propertyManager | worldconfigManager | all
    ├── getRunTime  controllers | playerTracker
    │                spawnQueue [dim] | subLevelTracker [dim]
    └── runtime    spawnEnemy <packname> <name> | forceFlushSpawnQueue [dim]
```

- `getRegistry`：输出数据包域 / 蓝图域注册表（`all` 一次性输出全部 5 个）；
- `getRunTime`：输出运行时数据——控制器状态、玩家追踪、刷怪队列、子空间追踪（敌方 / 友军 / 碎片三张表 + 容器子空间视图）；
- `runtime`：手动刷一艘敌船（指定包名与属性名，用于测试数据包）、强制清空并重建指定维度的刷怪队列；
- `[dim]` 为维度选择器（可 Tab 补全），省略 = 执行者所在维度；
- 敌船输出含生成时间、总 / 剩余质量（百分比）、剩余存在时间（`life_time` 剩余，未配置时显示负值）、FTL 充能剩余；碎片输出含成因（击沉残骸 / 分裂）、归属（敌 / 友 / 玩家）、来源名、存在时间与剩余清理时间；容器子空间视图列出容器内全部子空间及其追踪状态（排查用）。

## 9. 常见问题

| 现象 | 排查 |
|---|---|
| 进维度没有敌人 | ① 数据包是否放对位置（**侧载目录 `gamedir/sablespawner/`**，不是存档 `datapacks/`）② 容器内是否有 `meta.json` + `data/` ③ `natural_spawn` 是否 `true` ④ `available_dimension` 是否含当前维度 ⑤ `available_world_level` 是否含玩家当前等级 ⑥ 保护期未过（默认 2400t） ⑦ `min/max_spawn_interval` 或 `min/max_spawn_distance` 是否非法（≤0 / 超大 = 不刷） ⑧ 玩家等级不在 `available_world_level` 列表内（精确匹配） |
| 日志报 "未找到属性 [x] 对应的蓝图：y.nbt" | `schematic_name` 与包内蓝图文件名不一致，或蓝图文件缺失（datapack 源需在 `data/blueprints/`） |
| 日志报 "压缩包内不允许嵌套压缩包" | zip 里含 zip（含分类目录里的），请移除内层 zip |
| 日志报 "Failed to resolve blueprint" | 蓝图加载失败：文件损坏 / 不是对应 mod 的蓝图格式 |
| 大型敌舰第一次出现时卡顿 | 大蓝图超出 `blueprint_cache_max_blocks` 阈值不缓存，首次现读；可调高阈值（占内存） |
| 敌船击沉后没消失、也没掉落 | 正常：击沉转为残骸（可打捞），`long_debris_despawn_time` 后消失 |
| 敌船回血后撤离没有继续 | 正常：充能中断会作废重置，再次降到阈值以下才重新充能 |
| 服务器重启后敌船不见了 | 正常：容器就绪时会清理残留的无主敌船（重启清理） |
| 玩家一下线就崩服务器 | 旧版本 bug（击沉归属离线判空），已修复，确认 mod 为最新构建 |
| 敌方产生的碎片一直不消失 | 碎片到时会自动清理（见 7.5）：断裂碎片走 `debris_despawn_time`，击沉残骸走 `long_debris_despawn_time`；配置为 -1 时禁用清理 |
| 刷怪间隔好像没生效 | 确认 `min/max_spawn_interval` 已配置；间隔取 `[min, max-1]` 均匀，锚定的是**出保护时刻**，不在保护期会立即刷 |
| 改了数据包没反应 | 执行 `/sablespawner reload all` 重载；仍无反应再按上面几条排查 |

## 10. 未来规划（未实现）

- **事件舰队系统**：舰队构成（型号+数量）+ 编队树（子队形 / 自动包围盒 / 盒套盒）+ 舰队奖励分，设计已定稿（见 `SableSpawner-更新计划.md`），落地时间未定
- **事件化重载**：reload 的事件化触发（指令版已实现：`/sablespawner reload`）
- **蓝图格式内部解析**：不再依赖 `sable_schematic_api` 的 API（当前蓝图加载 / 放置仍经其接口，仅支持该格式）
- **DataTag**：以可扩展的数据标签替代枚举常量
- 一次性蓝图物品（右键放置消耗）、蓝图锚（快照修补）、预警系统、可视化编辑 / 自动导出

## 11. 文件索引（源码）

```
src/main/java/dev/sablespawner/
├── SableSpawner.java             主类（静态句柄、事件与配置注册）
├── SableSpawnerConfig.java       服务器配置项
├── manager/
│   ├── datapack/                 数据包域：侧载扫描 / 加载 / 校验 / 注册表 / 查询
│   ├── blueprint/                蓝图域：扫描 / 解析 / 加载 / 注册表 / 查询（四组件）
│   └── DataTag.java              数据标签（预留）
├── player/                       玩家状态（保护期、分数、等级）
├── registry/                     注册与指令（SableSpawnerCommands：调试指令）
├── spawn/
│   ├── GlobalControl.java        调度器（冷/热路径门控、控制器生命周期）
│   ├── EnemyControl.java         每维度控制（刷新流程、战斗状态机、残骸化、碎片收编）
│   ├── Spawner.java              刷出原语（空位检测、蓝图获取、放置、命名）
│   └── session/
│       ├── spawnqueue/           预取队列（SpawnQueue / SpawnTicket / SpawnTicketBuilder）
│       └── tracker/              子空间追踪（SubLevelTracker 基类 + 敌 / 友 / 碎片三域）
│           ├── entry/            追踪条目（SubLevelEntry 基类 + 敌 / 友 / 碎片三态）
│           └── query/            链式只读查询（三域查询器）
└── util/                         工具（包围盒 / 文件 IO / 哈希 / 队形生成）
```
