package club.maxstats.commission.stathead.hypixel

import club.maxstats.commission.stathead.util.Async
import club.maxstats.commission.stathead.util.ChatColor
import com.mojang.realmsclient.gui.ChatFormatting
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ChatComponentText
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Future

object StatFetch {
    var API_KEY: String = ""
    val json = Json { ignoreUnknownKeys = true }

    /**
     * Forge doesn't like Future#get(), it results in a ByteBuffer exception
     * So the returned future is never used
     */
    fun fetchStats(player: EntityPlayer): Future<HypixelPlayer>? {
        if (API_KEY.isEmpty()) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText("${ChatColor.RED}API Key is not set, failed to fetch player stats for ${player.name}"))
            return null
        }
        return Async.async {
            val uuid = player.uniqueID
            try {
                val requestURL = "https://api.hypixel.net/player?key=$API_KEY&uuid=${uuid.toString().replace("-", "")}"
                with(URL(requestURL).openConnection() as HttpURLConnection) {
                    connect()

                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        StatWorld.statPlayers[player] =
                            StatPlayer(player.uniqueID.toString(), player.name, HypixelPlayer())

                        // not used
                        return@with HypixelPlayer()
                    }

                    val text = inputStream.bufferedReader().use { it.readText() }.also { disconnect() }
                    val decoded = json.decodeFromString<HypixelResponse>(text)

                    StatWorld.statPlayers[player] =
                            StatPlayer(player.uniqueID.toString(), player.name, decoded.player)

                    // not used
                    return@with decoded.player
                }
            } catch (ex: Exception) {
                ex.printStackTrace()

                StatWorld.statPlayers[player] =
                    StatPlayer(player.uniqueID.toString(), player.name, HypixelPlayer())
                return@async HypixelPlayer()
            }
        }
    }
}

@Serializable
data class HypixelResponse(
    val player: HypixelPlayer = HypixelPlayer()
)
@Serializable
data class HypixelPlayer(
    val achievements: HypixelAchievement = HypixelAchievement(),
    val stats: HypixelGeneral = HypixelGeneral(),
    val nicked: Boolean = false
)
@Serializable
data class HypixelGeneral(
    @SerialName("Bedwars") val bedwars: HypixelBedwars = HypixelBedwars(),
    @SerialName("SkyWars") val skywars: HypixelSkywars = HypixelSkywars()
)
@Serializable
data class HypixelAchievement(
    val bedwars_level: Int = 0
) {
    private val colors = arrayOf(
        ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.GREEN, ChatColor.AQUA, ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE
    )
    private val prestigeColors = arrayOf(
        ChatColor.WHITE, ChatColor.YELLOW, ChatColor.AQUA, ChatColor.GREEN, ChatColor.DARK_AQUA, ChatColor.RED,
        ChatColor.LIGHT_PURPLE, ChatColor.BLUE, ChatColor.DARK_PURPLE, ChatColor.DARK_GRAY
    )
    private fun getStarColor(star: Int): ChatColor {
        return when {
            star < 100 -> ChatColor.GRAY
            star < 200 -> ChatColor.WHITE
            star < 300 -> ChatColor.GOLD
            star < 400 -> ChatColor.AQUA
            star < 500 -> ChatColor.DARK_GREEN
            star < 600 -> ChatColor.DARK_AQUA
            star < 700 -> ChatColor.DARK_RED
            star < 800 -> ChatColor.LIGHT_PURPLE
            star < 900 -> ChatColor.BLUE
            else -> ChatColor.DARK_PURPLE
        }
    }
    fun formattedBedwarsLevel(): String {
        val starString = bedwars_level.toString()

        return if (bedwars_level < 1000) {
            "${getStarColor(bedwars_level)}$starString\u272B"
        } else {
            val colorAmount: Int
            val starUnicode: String

            when {
                bedwars_level < 1100 -> {
                    colorAmount = 7
                    starUnicode = "\u272A"
                    colors.fill(ChatColor.WHITE, 0, 4)
                }
                bedwars_level < 2000 -> {
                    colorAmount = 5
                    starUnicode = "\u272A"
                    when {
                        bedwars_level < 1200 -> colors.fill(ChatColor.WHITE, 0, 4)
                        bedwars_level < 1300 -> colors.fill(ChatColor.YELLOW, 0, 4)
                        bedwars_level < 1400 -> colors.fill(ChatColor.AQUA, 0, 4)
                        bedwars_level < 1500 -> colors.fill(ChatColor.GREEN, 0, 4)
                        bedwars_level < 1600 -> colors.fill(ChatColor.DARK_AQUA, 0, 4)
                        bedwars_level < 1700 -> colors.fill(ChatColor.RED, 0, 4)
                        bedwars_level < 1800 -> colors.fill(ChatColor.LIGHT_PURPLE, 0, 4)
                        bedwars_level < 1900 -> colors.fill(ChatColor.BLUE, 0, 4)
                        else -> colors.fill(ChatColor.DARK_PURPLE, 0, 4)
                    }
                } else -> {
                colorAmount = 6
                starUnicode = "\u269D"
                when {
                    bedwars_level < 2100 -> prestigeColors.copyInto(colors)
                    bedwars_level < 2200 -> { colors.fill(ChatColor.WHITE, 0, 2); colors.fill(ChatColor.YELLOW, 2, 4) }
                    bedwars_level < 2300 -> prestigeColors.copyInto(colors, 0, 0, 5)
                    bedwars_level < 2400 -> prestigeColors.copyInto(colors, 0, 1, 6)
                    bedwars_level < 2500 -> { colors.fill(ChatColor.AQUA, 0, 2); colors.fill(ChatColor.WHITE, 2, 4) }
                    bedwars_level < 2600 -> { colors.fill(ChatColor.WHITE, 0, 2); colors.fill(ChatColor.GREEN, 2, 4) }
                    bedwars_level < 2700 -> prestigeColors.copyInto(colors, 0, 2, 7)
                    bedwars_level < 2800 -> { colors.fill(ChatColor.YELLOW, 0, 2); colors.fill(ChatColor.WHITE, 2, 4) }
                    bedwars_level < 2900 -> prestigeColors.copyInto(colors, 0, 3, 8)
                    bedwars_level < 3000 -> prestigeColors.copyInto(colors, 0, 4, 9)
                    else -> prestigeColors.copyInto(colors, 0, 5, 10)
                }
            }
            }

            val starWave = (System.currentTimeMillis() % 850L / 850.0F * colorAmount).toLong()

            return buildString {
                append(colors[(starWave + 4).toInt() % colorAmount])
                append(starString[0])
                append(colors[(starWave + 3).toInt() % colorAmount])
                append(starString.substring(1, starString.length))
                append(starUnicode)
            }
        }
    }
}
@Serializable
data class HypixelBedwars(
    val final_kills_bedwars: Int = 0,
    val final_deaths_bedwars: Int = 0,
    val kills: Int = 0,
    val deaths: Int = 0
) {
    fun getFKDR(): String =
        "%.2f".format(final_kills_bedwars.toDouble() / final_deaths_bedwars.coerceAtLeast(1).toDouble())
    fun getKDR(): String =
        "%.2f".format(kills.toDouble() / deaths.coerceAtLeast(1).toDouble())
}
@Serializable
data class HypixelSkywars(
    val levelFormatted: String = "${ChatFormatting.GRAY}-",
    val kills: Int = 0,
    val deaths: Int = 0
) {
    fun getKDR(): String =
        "%.2f".format(kills.toDouble() / deaths.coerceAtLeast(1).toDouble())
}