package net.parkerasa.chunkhub.menu;

import java.awt.*;
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
    private final File folder = new File(System.getProperty("user.home") + File.separator
            + "AppData\\Roaming\\.minecraft\\screenshots");

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
            if (index < listOfFiles.length - 1) {
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
        // this.addRenderableWidget(loadButton);

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

        // Verifies that there are files in the folder
        if (listOfFiles.length != 0) {

            // Verifies that the image at the current index is loaded
            if (images[index] == null) {
                load_file();
            }

            TextureManager textureManager = Minecraft.getInstance().getTextureManager();

            try {
                // Binds the image to the texture manager
                textureManager.bindForSetup(images[index]);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error: Not Bind" + e);
            }

            textureManager.getTexture(images[index]).setFilter(true, true);

            // Sets the width and height of the image
            int x = this.width / 2 - imageWidths[index] / 2;
            int y = this.height / 2 - imageHeights[index] / 2;

            try {

                // Renders the image to the screen
                graphics.blit(images[index], x, y, 0, 0, imageWidths[index], imageHeights[index]);
                System.out.println("Blit");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error: Not Blit" + e);
            }
        }
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    //Resizes the image to fit the screen
    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {

        // Get the current width and height of the image
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = resizedImage.createGraphics();

        // Draw the image to the new dimensions
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();

        return resizedImage;
    }

    public void load_file() {

        if (listOfFiles.length == 0) {
            return;
        }

        listOfFiles = folder.listFiles();

        System.out.println("Load File");
        System.out.println(listOfFiles[index].getName());

        //Initialize the image
        BufferedImage image = null;
        File location = new File(folder + File.separator + listOfFiles[index].getName());

        try {
            // Read the image
            image = ImageIO.read(location);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: Not Read" + e);
        }

        if (image != null) {
            // Get the current game window size
            int windowWidth = Minecraft.getInstance().getWindow().getWidth();
            int windowHeight = Minecraft.getInstance().getWindow().getHeight();

            // Calculate the scaling factor
            double scaleFactor = Math.min(256.0 / windowWidth, 256.0 / windowHeight);

            // Calculate the new dimensions
            int newWidth = (int) (image.getWidth() * scaleFactor);
            int newHeight = (int) (image.getHeight() * scaleFactor);

            // Resize the image
            image = resizeImage(image, newWidth, newHeight);
        }

        int width = image.getWidth();
        int height = image.getHeight();

        imageWidths[index] = width;
        imageHeights[index] = height;

        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);

        for (int i = 0; i < pixels.length; i++) {
            int argb = pixels[i];
            pixels[i] = (argb & 0xFF00FF00) | ((argb & 0xFF0000) >> 16) | ((argb & 0xFF) << 16);
        }

        // Create a new ByteBuffer for the pixels
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * width * height);

        // Set the pixels of the ByteBuffer in RGBA format
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                byteBuffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
                byteBuffer.put((byte) ((pixel >> 8) & 0xFF)); // Green component
                byteBuffer.put((byte) (pixel & 0xFF)); // Blue component
                byteBuffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component. Only for RGBA
            }
        }

        // Reset the position of the ByteBuffer
        byteBuffer.flip();

        try {

            // Create a new NativeImage
            NativeImage imagetorender = new NativeImage(NativeImage.Format.RGBA, width, height, false);

            // Set the pixels of the NativeImage
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = pixels[y * width + x];
                    imagetorender.setPixelRGBA(x, y, pixel);
                }
            }

            // Create a new DynamicTexture from the NativeImage
            DynamicTexture texture = new DynamicTexture(imagetorender);

            // Create a new ResourceLocation from the DynamicTexture
            ResourceLocation resourceLocation = Minecraft.getInstance().getTextureManager()
                    .register("imagetorender", texture);

            // Set the ResourceLocation to the images array
            images[index] = resourceLocation;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
