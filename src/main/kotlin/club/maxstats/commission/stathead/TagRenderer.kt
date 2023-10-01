package club.maxstats.commission.stathead

import club.maxstats.commission.stathead.hypixel.GameType
import club.maxstats.commission.stathead.hypixel.StatWorld
import club.maxstats.commission.stathead.hypixel.HypixelPlayer
import club.maxstats.commission.stathead.util.ChatColor
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.entity.RenderPlayer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import java.awt.Color

object TagRenderer {
    private val mc = Minecraft.getMinecraft()
    fun renderName(
        renderer: RenderPlayer,
        playerAPI: HypixelPlayer,
        player: EntityPlayer,
        renderX: Double,
        renderY: Double,
        renderZ: Double
    ) {
        val fontRenderer = renderer.fontRendererFromRenderManager
        val renderManager = renderer.renderManager
        val f = 1.6f
        val scale = 0.016666668f * f
        val xMultiplier = if (mc.gameSettings.thirdPersonView == 2) -1 else 1

        GlStateManager.pushMatrix()

        GL11.glNormal3f(0f, 1f, 0f)
        GlStateManager.translate(renderX, renderY + player.height + 0.5, renderZ)
        GlStateManager.rotate(-renderManager.playerViewY, 0f, 1f, 0f)
        GlStateManager.rotate(renderManager.playerViewX * xMultiplier, 1f, 0f, 0f)
        GlStateManager.scale(-scale, -scale, -scale)

        GlStateManager.disableLighting()
        GlStateManager.depthMask(false)
        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)

        if (playerAPI.nicked)
            renderTag(fontRenderer, "${ChatColor.RED}NICKED")
        else {
            val stats: Pair<String, String> = when(StatWorld.gameMode) {
                GameType.SKYWARS -> "Level: ${playerAPI.stats.skywars.levelFormatted}" to "KDR: ${playerAPI.stats.skywars.getKDR()}"
                GameType.BEDWARS -> "Star: ${playerAPI.achievements.formattedBedwarsLevel()}" to "FKDR: ${playerAPI.stats.bedwars.getFKDR()}"
                else -> "" to ""
            }
            renderTag(fontRenderer, stats.second)
            GlStateManager.translate(0f, -9.5f, 0f)
            renderTag(fontRenderer, stats.first)
        }

        GlStateManager.depthMask(true)
        GlStateManager.enableDepth()
        GlStateManager.enableLighting()
        GlStateManager.disableBlend()
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.popMatrix()
    }

    private fun renderTag(
        renderer: FontRenderer,
        string: String
    ) {
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer

        val width = renderer.getStringWidth(string) / 2

        GlStateManager.disableTexture2D()
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldRenderer.pos(-width - 1.0, -1.0, 0.0).color(0f, 0f, 0f, 0.25f).endVertex()
        worldRenderer.pos(-width - 1.0, 8.0, 0.0).color(0f, 0f, 0f, 0.25f).endVertex()
        worldRenderer.pos(width + 1.0, 8.0, 0.0).color(0f, 0f, 0f, 0.25f).endVertex()
        worldRenderer.pos(width + 1.0, -1.0, 0.0).color(0f, 0f, 0f, 0.25f).endVertex()
        tessellator.draw()

        GlStateManager.enableTexture2D()
        renderString(renderer, string, -width)
    }
    private fun renderString(
        renderer: FontRenderer,
        string: String,
        x: Int
    ) {
        GlStateManager.disableDepth()
        GlStateManager.depthMask(false)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 0.2f)
        renderer.drawString(string, x, 0, Color.gray.rgb)

        GlStateManager.color(1f, 1f, 1f, 1f)

        GlStateManager.enableDepth()
        GlStateManager.depthMask(true)
        GlStateManager.color(1f, 1f, 1f, 0.5f)
        renderer.drawString(string, x, 0, Color.white.rgb)

        GlStateManager.disableDepth()
        GlStateManager.depthMask(false)
    }
}