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
        if (args == null || args.isEmpty() || args.size > 1)
            mc.thePlayer.addChatMessage(ChatComponentText("${ChatColor.GOLD}[StatHead] ${ChatColor.RED}Incorrect usage of command. /setapikey [api_key]"))
        else if (args.size == 1) {
            mc.thePlayer.addChatMessage(ChatComponentText("${ChatColor.GOLD}[StatHead] ${ChatColor.GREEN}Successfully set API key."))
            val apiKey = args[0]
            StatFetch.API_KEY = apiKey
            File("stathead.conf").writeText(StatFetch.json.encodeToString(Main.Config(apiKey)))
        }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true
}

object ToggleSecondStatCommand: CommandBase() {
    val mc = Minecraft.getMinecraft()
    var toggled = false

    override fun getCommandName(): String = "togglekdr"

    override fun getCommandUsage(sender: ICommandSender?): String = "Toggles FKDR/KDR from being rendered above heads"

    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        toggled = !toggled
        mc.thePlayer.addChatMessage(ChatComponentText("${ChatColor.GOLD}[StatHead] ${ChatColor.GREEN}Toggled Show FKDR to $toggled"))
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true
}