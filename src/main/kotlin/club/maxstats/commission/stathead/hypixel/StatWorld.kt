package club.maxstats.commission.stathead.hypixel

import net.minecraft.entity.player.EntityPlayer

object StatWorld {
    val statPlayers: HashMap<EntityPlayer, StatPlayer> = HashMap()
}

data class StatPlayer(
    val uuid: String,
    val name: String,
    val stats: StatsGeneral
)