/*
 * Ultralight Java - Java wrapper for the Ultralight web engine
 * Copyright (C) 2020 - 2021 LabyMedia and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package examplemod.client.ultralight;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_ENABLE_BIT;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_TRANSFORM_BIT;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ROW_LENGTH;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glPopAttrib;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushAttrib;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameterf;
import static org.lwjgl.opengl.GL11.glTexSubImage2D;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL11.glVertex2i;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;

import com.labymedia.ultralight.UltralightPlatform;
import com.labymedia.ultralight.UltralightRenderer;
import com.labymedia.ultralight.UltralightView;
import com.labymedia.ultralight.bitmap.UltralightBitmap;
import com.labymedia.ultralight.bitmap.UltralightBitmapSurface;
import com.labymedia.ultralight.input.UltralightKeyEvent;
import com.labymedia.ultralight.input.UltralightKeyEventType;
import com.labymedia.ultralight.input.UltralightMouseEvent;
import com.labymedia.ultralight.input.UltralightMouseEventButton;
import com.labymedia.ultralight.input.UltralightMouseEventType;
import com.labymedia.ultralight.javascript.JavascriptContextLock;
import com.labymedia.ultralight.math.IntRect;

import examplemod.client.ultralight.opengl.js.JavaScriptBridge;
import examplemod.client.ultralight.opengl.listener.ExampleLoadListener;
import examplemod.client.ultralight.opengl.util.UltralightKeyMapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

/**
 * Class used for controlling the WebGUI rendered on top of the OpenGL GUI.
 */
public class ViewController {
    private final UltralightPlatform platform;
    private final UltralightRenderer renderer;
    private final UltralightView view;

    private final ExampleLoadListener loadListener;
    private final JavaScriptBridge bridge;

    private int glTexture;
    private long lastJavascriptGarbageCollections;

    /**
     * Constructs a new {@link ViewController} and retrieves the platform.
     */
    public ViewController(UltralightRenderer renderer, UltralightView view) {
        this.platform = UltralightPlatform.instance();

        this.renderer = renderer;


        this.view = view;
        this.bridge = new JavaScriptBridge(view);
        this.loadListener = new ExampleLoadListener(view);
        this.view.setLoadListener(loadListener);

        this.glTexture = -1;
        this.lastJavascriptGarbageCollections = 0;
    }


    /**
     * Loads the specified URL into this controller.
     *
     * @param url The URL to load
     */
    public void loadURL(String url) {
        this.view.loadURL(url);
    }

    public JavaScriptBridge getJSBridge() {
        return bridge;
    }

    /**
     * Updates and renders the renderer
     */
    public void update() {
        this.renderer.update();
        this.renderer.render();

        if(lastJavascriptGarbageCollections == 0) {
            lastJavascriptGarbageCollections = System.currentTimeMillis();
        } else if(System.currentTimeMillis() - lastJavascriptGarbageCollections > 1000) {
            System.out.println("Garbage collecting Javascript...");
            try(JavascriptContextLock lock = this.view.lockJavascriptContext()) {
                lock.getContext().garbageCollect();
            }
            lastJavascriptGarbageCollections = System.currentTimeMillis();
        }
    }

    /**
     * Resizes the web view.
     *
     * @param width  The new view width
     * @param height The new view height
     */
    public void resize(int width, int height) {
        this.view.resize(width, height);
    }

    /**
     * Render the current image using OpenGL
     */
    public void render() {
        if(glTexture == -1) {
            createGLTexture();
        }

        UltralightBitmapSurface surface = (UltralightBitmapSurface) this.view.surface();
        UltralightBitmap bitmap = surface.bitmap();

        int width = (int) view.width();
        int height = (int) view.height();

        // Prepare OpenGL for 2D textures and bind our texture
        glEnable(GL_TEXTURE_2D);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.glTexture);


        IntRect dirtyBounds = surface.dirtyBounds();

        if(dirtyBounds.isValid()) {
            ByteBuffer imageData = bitmap.lockPixels();
            glPixelStorei(GL_UNPACK_ROW_LENGTH, (int) bitmap.rowBytes() / 4);
            if(dirtyBounds.width() == width && dirtyBounds.height() == height) {
                // Update full image
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, imageData);
                glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
            } else {
                // Update partial image
                int x = dirtyBounds.x();
                int y = dirtyBounds.y();
                int dirtyWidth = dirtyBounds.width();
                int dirtyHeight = dirtyBounds.height();
                int startOffset = (int) ((y * bitmap.rowBytes()) + x * 4);

                glTexSubImage2D(
                        GL_TEXTURE_2D,
                        0,
                        x, y, dirtyWidth, dirtyHeight,
                        GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV,
                        (ByteBuffer) imageData.position(startOffset));
            }
            glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);

            bitmap.unlockPixels();
            surface.clearDirtyBounds();
        }

        // Set up the OpenGL state for rendering of a fullscreen quad
        glPushAttrib(GL_ENABLE_BIT | GL_COLOR_BUFFER_BIT | GL_TRANSFORM_BIT);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, this.view.width(), this.view.height(), 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();

        // Disable lighting and scissoring, they could mess up th renderer
        glLoadIdentity();
        glDisable(GL_LIGHTING);
        glDisable(GL_SCISSOR_TEST);
        glEnable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Make sure we draw with a neutral color
        // (so we don't mess with the color channels of the image)
        glColor4f(1, 1, 1, 1f);

        glBegin(GL_QUADS);

        // Lower left corner, 0/0 on the screen space, and 0/0 of the image UV
        glTexCoord2f(0, 0);
        glVertex2f(0, 0);

        // Upper left corner
        glTexCoord2f(0, 1);
        glVertex2i(0, height);

        // Upper right corner
        glTexCoord2f(1, 1);
        glVertex2i(width, height);

        // Lower right corner
        glTexCoord2f(1, 0);
        glVertex2i(width, 0);

        glEnd();

        glBindTexture(GL_TEXTURE_2D, 0);

        // Restore OpenGL state
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);

        glDisable(GL_TEXTURE_2D);
        glPopAttrib();

    }
    public UltralightView getView() {
        return view;
    }


    /**
     * Sets up the OpenGL texture for rendering
     */
    private void createGLTexture() {
        glEnable(GL_TEXTURE_2D);
        this.glTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.glTexture);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);
        glDisable(GL_TEXTURE_2D);
    }

    public void onMouseClick(int x, int y, int mouseButton, boolean buttonDown) {
        UltralightMouseEvent event = new UltralightMouseEvent();
        UltralightMouseEventButton button;
        switch (mouseButton) {
            case 0:
                button = UltralightMouseEventButton.LEFT;
                break;
            case 1:
                button = UltralightMouseEventButton.RIGHT;
                break;
            case 3:
            default:
                button = UltralightMouseEventButton.MIDDLE;
                break;

        }
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft(), x, y);
        event.button(button);
        event.x(x * scaledResolution.getScaleFactor());
        event.y(y * scaledResolution.getScaleFactor());
        event.type(buttonDown ? UltralightMouseEventType.DOWN : UltralightMouseEventType.UP);

        view.fireMouseEvent(event);
    }

    public void onMouseMove(int x, int y) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft(), y, y);
        UltralightMouseEvent event = new UltralightMouseEvent();
        event.x(x * scaledResolution.getScaleFactor());
        event.y(y * scaledResolution.getScaleFactor());
        event.type(UltralightMouseEventType.MOVED);
        view.fireMouseEvent(event);
    }
    public void onKeyDown(char c, int key) {
        UltralightKeyEvent event = new UltralightKeyEvent();
        event.virtualKeyCode(UltralightKeyMapper.getKey(key));
        event.unmodifiedText(String.valueOf(c));

        UltralightKeyMapper.KeyType keyType = UltralightKeyMapper.getKeyType(key);

        if (keyType == UltralightKeyMapper.KeyType.ACTION) {
            event.type(UltralightKeyEventType.RAW_DOWN);

        } else if (keyType == UltralightKeyMapper.KeyType.CHAR) {
            event.type(UltralightKeyEventType.CHAR);
        }


        event.text(String.valueOf(c));
        event.keyIdentifier(UltralightKeyEvent.getKeyIdentifierFromVirtualKeyCode(UltralightKeyMapper.getKey(key)));

        view.fireKeyEvent(event);
    }

}
