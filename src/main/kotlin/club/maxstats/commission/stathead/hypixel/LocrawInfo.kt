package club.maxstats.commission.stathead.hypixel

import club.maxstats.commission.stathead.util.Async
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.concurrent.TimeUnit

object LocrawHandler {
    private val json = Json { ignoreUnknownKeys = true }
    private val mc = Minecraft.getMinecraft()
    private var tick = 0
    private var limboLoop = 0

    private var sentCommand = false
    private var sendPermitted = false

    fun queueUpdate(interval: Long) {
        sendPermitted = true
        Async.schedule({
           if (sendPermitted) {
               mc.thePlayer.sendChatMessage("/locraw")
           }
        }, interval, TimeUnit.MILLISECONDS)
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        tick++
        if (tick % 20 == 0) {
            tick = 0
            if (StatWorld.isHypixel && !sentCommand) {
                queueUpdate(500)
                sentCommand = true
            }
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        StatWorld.locrawInfo = null
        sendPermitted = false
        sentCommand = false
        limboLoop = 0
    }

    @SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = true)
    fun onMessageReceived(event: ClientChatReceivedEvent) {
        if (!sentCommand) return
        val chat = event.message.unformattedText

        if (!chat.startsWith("{") || !chat.endsWith("}")) {
            if (chat.contains("You are sending too many commands! Please try again in a few seconds."))
                queueUpdate(4900)
            return
        }

        try {
            val parsed = json.decodeFromString<LocrawInfo>(chat)
            event.isCanceled = true

            if (5 > limboLoop && parsed.gametype == GameType.LIMBO) {
                sentCommand = false
                limboLoop++
                queueUpdate(1000)
            } else
                StatWorld.locrawInfo = parsed
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}

@Serializable
data class LocrawInfo(
    val server: String = "",
    val gametype: GameType = GameType.UNKNOWN,
    val lobbyname: String = "",
    val mode: String = "",
    val map: String = ""
) {
    fun inGame(): Boolean = mode.isNotEmpty() && mode != "lobby"
    override fun toString(): String =
        "Server: $server, Game Type: $gametype, Lobby Name: $lobbyname, Mode: $mode, Map: $map"
}

class GameTypeSerializer: KSerializer<GameType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("GameType", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): GameType {
        val gameTypeString = decoder.decodeString()
        return GameType.entries.firstOrNull { it.gameType == gameTypeString } ?: GameType.UNKNOWN
    }
    override fun serialize(encoder: Encoder, value: GameType) {
        encoder.encodeString(value.gameType)
    }
}

enum class GameType(val gameType: String) {
    ARCADE_GAMES("ARCADE"),
    BEDWARS("BEDWARS"),
    BLITZ_SG("SURVIVAL_GAMES"),
    BUILD_BATTLE("BUILD_BATTLE"),
    CLASSIC_GAMES("LEGACY"),
    COPS_AND_CRIMS("MCGO"),
    DUELS("DUELS"),
    HOUSING("HOUSING"),
    LIMBO("LIMBO"),
    MAIN("MAIN"),
    MEGA_WALLS("WALLS3"),
    MURDER_MYSTERY("MURDER_MYSTERY"),
    PIT("PIT"),
    PROTOTYPE("PROTOTYPE"),
    SKYBLOCK("SKYBLOCK"),
    SKYWARS("SKYWARS"),
    SMASH_HEROES("SUPER_SMASH"),
    SPEED_UHC("SPEED_UHC"),
    TNT_GAMES("TNTGAMES"),
    UHC_CHAMPIONS("UHC"),
    UNKNOWN(""),
    WARLORDS("BATTLEGROUND")
}