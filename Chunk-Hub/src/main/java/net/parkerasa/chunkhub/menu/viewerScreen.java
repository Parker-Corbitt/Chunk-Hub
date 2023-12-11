package net.parkerasa.chunkhub.menu;

import java.awt.Image;
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
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                try {

                    if (imageWidths == null) {
                        imageWidths = new int[listOfFiles.length];
                    }
                    
                    if (imageHeights == null) {
                        imageHeights = new int[listOfFiles.length];
                    }

                    BufferedImage bufferedImage = ImageIO.read(file);
                    NativeImage nativeImage = null;

                    imageWidths[index] = nativeImage.getWidth();
                    imageHeights[index] = nativeImage.getHeight();

                    DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
                    
                    ResourceLocation resourceLocation = Minecraft.getInstance().getTextureManager().register("chunkhub",
                            dynamicTexture);
                    if (images == null) {
                        images = new ResourceLocation[listOfFiles.length];
                    }
                    images[index] = resourceLocation;
                    index++;
                    System.out.println("Loaded image: " + file.getName());
                } catch (IOException e) {
                    System.out.println("Failed to load image: " + file.getName());
                    e.printStackTrace();
                }
            }
        }
        index = 0;

    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        if (images[index] != null) {
            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            textureManager.getTexture(images[index]);
            textureManager.bindForSetup(images[index]);

            int imageWidth = imageWidths[index];
            int imageHeight = imageHeights[index];
            int imageX = this.width / 2 - imageWidth / 2;
            int imageY = this.height / 2 - imageHeight / 2;

            // Draw the image
            graphics.blit(images[index], imageX, imageY, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        }

        super.render(graphics, mouseX, mouseY, partialTick);

    }

    @Override
    public void onClose() {
        super.onClose();
    }

}
