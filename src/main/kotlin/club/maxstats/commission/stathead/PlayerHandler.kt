package club.maxstats.commission.stathead

import club.maxstats.commission.stathead.hypixel.StatFetch
import club.maxstats.commission.stathead.hypixel.StatWorld
import club.maxstats.commission.stathead.hypixel.HypixelPlayer
import club.maxstats.commission.stathead.hypixel.StatPlayer
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.scoreboard.Team

object PlayerHandler {
    private val mc = Minecraft.getMinecraft()

    fun onTick() {
        checkCacheSize()
        for (player in mc.theWorld.playerEntities) {
            if (shouldRender(player)) {
                if (!StatWorld.statPlayers.contains(player)) {
                    val isNicked = player.uniqueID.version() == 1

                    StatWorld.statPlayers[player] = StatPlayer(
                        player.uniqueID.toString(),
                        player.name,
                        HypixelPlayer(nicked = isNicked)
                    )
                    if (!isNicked)
                        StatFetch.fetchStats(player)
                }
            }
        }
    }

    fun shouldRender(player: EntityPlayer?): Boolean {
        if (!StatWorld.isHypixel || !StatWorld.inGame)
            return false

        if (player == null || (player.uniqueID.version() != 4 && player.uniqueID.version() != 1))
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

        return (!player.hasCustomName() || player.customNameTag.isNotEmpty())
                && player.displayNameString.isNotEmpty() && !player.isSneaking
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

    private fun checkCacheSize() {
        val max = 500
        if (StatWorld.statPlayers.size > max) {
            val safePlayers = mutableListOf<EntityPlayer>()
            for (player in mc.theWorld.playerEntities)
                safePlayers += player

            for (player in StatWorld.statPlayers.keys) {
                if (!safePlayers.contains(player))
                    StatWorld.statPlayers.remove(player)
            }
        }
    }

    fun onDelete() {
        StatWorld.statPlayers.clear()
    }
}