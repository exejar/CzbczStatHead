package club.maxstats.commission.stathead

import club.maxstats.commission.stathead.hypixel.StatFetch
import club.maxstats.commission.stathead.util.ChatColor
import kotlinx.serialization.encodeToString
import net.minecraft.client.Minecraft
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.ChatComponentText
import java.io.File

object SetApiKeyCommand: CommandBase() {
    val mc = Minecraft.getMinecraft()
    override fun getCommandName(): String = "setapikey"

    override fun getCommandUsage(sender: ICommandSender?): String = "Sets API Key for the mod to use"

    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        if (args == null || args.size > 1)
            mc.thePlayer.addChatMessage(ChatComponentText("${ChatColor.RED}Incorrect usage of command.\n${ChatColor.RED}/setapikey [api_key]"))
        else if (args.size == 1) {
            val apiKey = args[0]
            StatFetch.API_KEY = apiKey
            File("stathead.conf").writeText(StatFetch.json.encodeToString(Main.Config(apiKey)))
        }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true
}