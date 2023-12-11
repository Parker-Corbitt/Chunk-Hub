package net.parkerasa.chunkhub.menu;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class viewerScreen extends Screen {

    private ResourceLocation[] images;
    private int index = 0;
    private int[] imageWidths;
    private int[] imageHeights;
    private File[] listOfFiles;

    public viewerScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 85;
        int buttonHeight = 20;
        int buttonXLeft = this.width / 2 - buttonWidth - 100;
        int buttonXMiddle = this.width / 2 - buttonWidth / 2;
        int buttonXRight = this.width / 2 + 100;

        Button.OnPress prevPress = (button) -> {
            if (index > 0) {
                index--;
            }
        };

        Button.OnPress nextPress = (button) -> {
            if (index < 30) {
                index++;
            }
        };

        Button.OnPress backPress = (button) -> {
            this.minecraft.setScreen(new photoScreen(Component.literal("test")));
        };

        Button backButton = new Button.Builder(Component.literal("Back"), backPress)
                .pos(buttonXMiddle, this.height - 30)
                .size(buttonWidth, buttonHeight)
                .build();

        Button prevButton = new Button.Builder(Component.literal("Prev"), prevPress)
                .pos(buttonXLeft, this.height - 30)
                .size(buttonWidth, buttonHeight)
                .build();

        Button nextButton = new Button.Builder(Component.literal("Next"), nextPress)
                .pos(buttonXRight, this.height - 30)
                .size(buttonWidth, buttonHeight)
                .build();

        this.addRenderableWidget(backButton);
        this.addRenderableWidget(prevButton);
        this.addRenderableWidget(nextButton);

        File folder = new File(System.getProperty("user.home") + File.separator
                + "AppData\\Roaming\\.minecraft\\screenshots");
        listOfFiles = folder.listFiles();

        if (images == null) {
            images = new ResourceLocation[listOfFiles.length];
        }

        if (imageWidths == null) {
            imageWidths = new int[listOfFiles.length];
        }

        if (imageHeights == null) {
            imageHeights = new int[listOfFiles.length];
        }
        
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        super.render(graphics, mouseX, mouseY, partialTick);

        if (images[index] == null) {
            load_file();
        }

        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        textureManager.bindForSetup(images[index]);
        textureManager.getTexture(images[index]).setFilter(false, false);

        int width = imageWidths[index];
        int height = imageHeights[index];

        int x = this.width / 2 - width / 2;
        int y = this.height / 2 - height / 2;

        graphics.blit(images[index], y, mouseX, mouseY, partialTick, width, height, x, y);

    }

    @Override
    public void onClose() {
        super.onClose();
    }

    public void load_file() {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(System.getProperty("user.home") + File.separator
                    + "AppData\\Roaming\\.minecraft\\screenshots\\" + listOfFiles[index].getName()));
        } catch (IOException e) {
            System.out.println("Error: Couldn't load image" + e);
            e.printStackTrace();
        }

        if(image != null) {
            int width = image.getWidth();
            int height = image.getHeight();
    
            imageWidths[index] = width;
            imageHeights[index] = height;
        }
        else
        {   
            IOException e = new IOException();
            e.printStackTrace();
        }
        

        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        for (int i = 0; i < pixels.length; i++) {
            int argb = pixels[i];
            pixels[i] = (argb & 0xFF00FF00) | ((argb & 0xFF0000) >> 16) | ((argb & 0xFF) << 16);
        }

        // for (int y = height - 1; y >= 0; y--) {
        // for (int x = 0; x < width; x++) {

        // int pixel = pixels[y * width + x];

        // byteBuffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
        // byteBuffer.put((byte) ((pixel >> 8) & 0xFF)); // Green component
        // byteBuffer.put((byte) (pixel & 0xFF)); // Blue component
        // byteBuffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component. Only for
        // RGBA
        // }
        // }

        // byteBuffer.flip(); //FOR THE LOVE OF GOD DO NOT FORGET THIS

        DynamicTexture dynamicTexture = new DynamicTexture(width, height, false);
        dynamicTexture.getPixels().drawPixels();
        dynamicTexture.upload();

        // Register the texture
        ResourceLocation textureLocation = Minecraft.getInstance().getTextureManager().register("dynamic",
                dynamicTexture);

        images[index] = textureLocation;

    }

}
