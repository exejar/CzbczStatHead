package club.maxstats.commission.stathead.hypixel

import java.util.UUID

object StatWorld {
    val statPlayers: HashMap<UUID, StatPlayer> = HashMap()
    var locrawInfo: LocrawInfo? = null
    var isHypixel: Boolean = false
    val gameMode: GameType get() = locrawInfo?.gametype ?: GameType.UNKNOWN
    val inGame: Boolean get() = locrawInfo?.inGame() ?: false
}
data class StatPlayer(
    val uuid: String,
    val name: String,
    val stats: HypixelPlayer
)