package club.maxstats.commission.stathead

import club.maxstats.commission.stathead.hypixel.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.minecraft.client.Minecraft
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.RenderPlayerEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.io.File

@Mod(modid="czbczstathead", useMetadata=true)
class Main {
    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        val file = File("stathead.conf").also {
            if (it.createNewFile())
                it.writeText(Json.encodeToString(Config()))
        }

        val config = StatFetch.json.decodeFromString<Config>(file.readText())
        StatFetch.API_KEY = config.apiKey

        ClientCommandHandler.instance.registerCommand(SetApiKeyCommand)
        ClientCommandHandler.instance.registerCommand(ToggleSecondStatCommand)
        MinecraftForge.EVENT_BUS.register(this)
        MinecraftForge.EVENT_BUS.register(LocrawHandler)
    }

    private val mc = Minecraft.getMinecraft()
    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START || mc.theWorld == null)
            return

        if (!mc.isGamePaused && mc.thePlayer != null)
            PlayerHandler.onTick()

        StatWorld.isHypixel = mc.currentServerData?.serverIP?.contains("hypixel.net") ?: false
    }

    @SubscribeEvent
    fun render(event: RenderPlayerEvent.Pre) {
        val player = event.entityPlayer
        val playerStats = StatWorld.statPlayers[player.uniqueID]?.stats ?: return

        if (PlayerHandler.shouldRender(player)) {
            val scoreboard = player.worldScoreboard

            val offset =
                if (scoreboard.getObjectiveInDisplaySlot(2) != null) 1.8
                else if (player.uniqueID == mc.thePlayer.uniqueID) 0.0
                else 0.9

            TagRenderer.renderName(event.renderer, playerStats, player, event.x, event.y + offset * 0.3, event.z)
        }
    }

    @Serializable
    data class Config(
        val apiKey: String = ""
    )
}