package club.maxstats.commission.stathead.hypixel

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.mojang.realmsclient.gui.ChatFormatting
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ChatComponentText
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.Future

object StatFetch {
    var API_KEY: String = ""
    val json = Json { ignoreUnknownKeys = true }
    fun fetchStats(player: EntityPlayer): Future<StatsGeneral>? {
        if (API_KEY.isEmpty()) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText("API Key is not set, failed to fetch player stats for ${player.name}"))
            return null
        }
        return AsyncFetch.async {
            val uuid = player.uniqueID
            try {
                val requestURL = "https://api.hypixel.net/player?key=$API_KEY&uuid=${uuid.toString().replace("-", "")}"
                with(URL(requestURL).openConnection() as HttpURLConnection) {
                    connect()

                    if (responseCode != HttpURLConnection.HTTP_OK)
                        return@with StatsGeneral()

                    return@with json.decodeFromStream<StatsGeneral>(inputStream).also { disconnect() }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                return@async StatsGeneral()
            }
        }
    }
}

object AsyncFetch {
    private val executor = Executors.newCachedThreadPool(ThreadFactoryBuilder().setNameFormat("czbczstathead-%d").build())
    fun async(callable: () -> StatsGeneral): Future<StatsGeneral> = executor.submit(callable)
}

@Serializable
data class StatsGeneral(
    val achievements: StatsAchievement = StatsAchievement(),
    val bedwars: StatsBedwars = StatsBedwars(),
    val skywars: StatsSkywars = StatsSkywars(),
)
@Serializable
data class StatsAchievement(
    val bedwars_level: Int = 0
)
@Serializable
data class StatsBedwars(
    val final_kills: Int = 0,
    val final_deaths: Int = 0,
    val kills: Int = 0,
    val deaths: Int = 0
)
@Serializable
data class StatsSkywars(
    val levelFormatted: String = "${ChatFormatting.GRAY}-",
    val kills: Int = 0,
    val deaths: Int = 0
)