package club.maxstats.commission.stathead

import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

@Mod(modid="czbczstathead", useMetadata=true)
class Main {
    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(this)
    }

    private val mc = Minecraft.getMinecraft()
    private val display = StatHeadDisplay()
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START || mc.theWorld == null)
            return

        if (!mc.isGamePaused && mc.thePlayer != null)
            display.onTick()
    }
}