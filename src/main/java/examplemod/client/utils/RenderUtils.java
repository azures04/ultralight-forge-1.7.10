package examplemod.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

import java.awt.*;
import java.util.Random;

public class RenderUtils {
    private RenderUtils() {}

    /**
     * Allows you to easily draw textures to the screen.
     * If you are drawing without a gui screen active make sure to fix your
     * x & y coordinates using the ScaledResolution class
     * @param resourceLocation The resource location of the texture
     * @param x
     * @param y
     * @param width The width used to draw the texture
     * @param height The height used to draw the texture
     * @param rgbaCombined Color as combined RGBA to tint the texture with
     */
    public static void drawTexture(ResourceLocation resourceLocation, double x, double y, int width, int height, int rgbaCombined) {
        if (resourceLocation == null) return;
        float red = (rgbaCombined >> 16 & 255) / 255f;
        float green = (rgbaCombined >> 8 & 255) / 255f;
        float blue = (rgbaCombined & 255) / 255f;
        float alpha = (rgbaCombined >> 24 & 255) / 255f;

        //tint
        GL11.glColor4f(red, green, blue, alpha);

        Minecraft.getMinecraft().renderEngine.bindTexture(resourceLocation);

        //the most valuable line of code here, anti-aliasing
        Minecraft.getMinecraft().renderEngine.getTexture(resourceLocation);


        //enable transparency
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        draw(x, y, 0, 0, width, height, width, height, alpha);

        GL11.glDisable(GL11.GL_BLEND);

    }

    public static void draw(double xPos, double yPos, float zLevel, float u, float v, int width, int height, float textureWidth, float textureHeight) {
    	float lvt_8_1_ = 1.0F / textureWidth;
        float lvt_9_1_ = 1.0F / textureHeight;
        Tessellator lvt_10_1_ = Tessellator.instance;
        lvt_10_1_.addVertexWithUV(xPos, yPos + height, zLevel, u * lvt_8_1_, (v + height) * lvt_9_1_);
        lvt_10_1_.addVertexWithUV(xPos + width, yPos + height, zLevel, (u + width) * lvt_8_1_, (v + height) * lvt_9_1_);
        lvt_10_1_.addVertexWithUV(xPos + width, yPos, zLevel, (u + width) * lvt_8_1_, v * lvt_9_1_);
        lvt_10_1_.addVertexWithUV(xPos, yPos, zLevel, u * lvt_8_1_, v * lvt_9_1_);
        lvt_10_1_.draw();
    }


    public static void drawRegularPolygon(double x, double y, int radius, int sides, Color color) {
        Tessellator tessellator = Tessellator.instance;
        double twicePI = Math.PI * 2;
        GL11.glColor4f(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);

        // Activer la transparence
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        tessellator.startDrawing(GL11.GL_TRIANGLE_FAN);
        tessellator.addVertex(x, y, 0);

        for (int i = 0; i <= sides; i++) {
            double angle = (twicePI * i / sides) + Math.toRadians(180);
            tessellator.addVertex(x + Math.sin(angle) * radius, y + Math.cos(angle) * radius, 0);
        }
        tessellator.draw();

        GL11.glDisable(GL11.GL_BLEND);
    }

}
