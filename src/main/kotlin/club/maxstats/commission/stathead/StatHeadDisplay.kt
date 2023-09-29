package club.maxstats.commission.stathead

import club.maxstats.commission.stathead.hypixel.StatFetch
import club.maxstats.commission.stathead.hypixel.StatPlayer
import club.maxstats.commission.stathead.hypixel.StatWorld
import club.maxstats.commission.stathead.hypixel.StatsGeneral
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.scoreboard.Team
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future

class StatHeadDisplay {
    val cache: ConcurrentHashMap<UUID, String> = ConcurrentHashMap()
    val existedMoreThan5Seconds: MutableList<EntityPlayer> = mutableListOf()
    val timeCheck: MutableMap<UUID, Int> = mutableMapOf()
    val futureMap: MutableMap<EntityPlayer, Future<StatsGeneral>> = mutableMapOf()
    private val mc = Minecraft.getMinecraft()

    fun onTick() {
        for (player in mc.theWorld.playerEntities) {
            if (!existedMoreThan5Seconds.contains(player)) {
                if (!timeCheck.contains(player.uniqueID))
                    timeCheck[player.uniqueID] = 0

                val old = timeCheck[player.uniqueID]!!
                if (old > 100) {
                    if (!existedMoreThan5Seconds.contains(player))
                        existedMoreThan5Seconds += player
                } else if (!player.isInvisibleToPlayer(mc.thePlayer))
                    timeCheck[player.uniqueID] = old + 1

                if (shouldRender(player)) {
                    if (!StatWorld.statPlayers.contains(player)) {
                        val future = futureMap[player] ?: StatFetch.fetchStats(player) ?: continue

                        if (!futureMap.contains(player))
                            futureMap[player] = future

                        if (future.isDone) {
                            futureMap.remove(player)
                            StatWorld.statPlayers[player] = StatPlayer(
                                player.uniqueID.toString(),
                                player.name,
                                future.get()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun shouldRender(player: EntityPlayer?): Boolean {
        if (player == null || player.uniqueID.version() != 4)
            return false

        if (player.isInvisible)
            return false

        for (effect in player.activePotionEffects) {
            // invisibility
            if (effect.potionID == 14)
                return false
        }

        if (!renderFromTeam(player))
            return false

        if (player.riddenByEntity != null)
            return false

        return (!player.hasCustomName() || player.customNameTag.isNotEmpty()) && player.displayNameString.isNotEmpty()
                && existedMoreThan5Seconds.contains(player) && !player.isSneaking
    }

    private fun renderFromTeam(player: EntityPlayer): Boolean {
        if (player == mc.thePlayer) return true
        val team = player.team ?: return true
        val ourTeam = mc.thePlayer.team

        return when (team.nameTagVisibility) {
            Team.EnumVisible.NEVER -> false
            Team.EnumVisible.HIDE_FOR_OTHER_TEAMS -> ourTeam == null || team.isSameTeam(ourTeam)
            Team.EnumVisible.HIDE_FOR_OWN_TEAM -> ourTeam == null || !team.isSameTeam(ourTeam)
            Team.EnumVisible.ALWAYS -> true
            else -> true
        }
    }

    fun checkCacheSize() {
        val max = 150.coerceAtLeast(500)
        if (cache.size > max) {
            val safePlayers = mutableListOf<EntityPlayer>()
            for (player in mc.theWorld.playerEntities)
                if (existedMoreThan5Seconds.contains(player))
                    safePlayers += player

            existedMoreThan5Seconds.clear()
            existedMoreThan5Seconds.addAll(safePlayers)

            for (player in StatWorld.statPlayers.keys) {
                if (!safePlayers.contains(player))
                    StatWorld.statPlayers.remove(player)
            }
        }
    }

    fun onDelete() {
        StatWorld.statPlayers.clear()
        cache.clear()
        existedMoreThan5Seconds.clear()
    }
}