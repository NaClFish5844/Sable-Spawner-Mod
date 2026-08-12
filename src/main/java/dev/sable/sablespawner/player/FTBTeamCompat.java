package dev.sable.sablespawner.player;

public class FTBTeamCompat {
    // sync player score
    // onPlayerJoinServer, onPlayerJoinTeam
    // TeamTracker每次服务器启动时现取就行 FTB会做好一切的

    // 同一队伍的玩家按最高得分同步set 得分变动时全队一起变动
    // 离队时保持当前得分 但取消同步
}
