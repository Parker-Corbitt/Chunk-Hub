package net.parkerasa.chunkhub.menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.parkerasa.chunkhub.menu.photoScreen;
import net.minecraft.client.Minecraft;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Screenshot;
import net.minecraft.world.entity.player.Player;
import java.util.Base64;
import org.apache.http.Consts;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import java.util.concurrent.CompletableFuture;
import java.util.Arrays;

public class photoScreen extends Screen {

    private final Minecraft minecraft;
    private String editText;
    private String[] tags;
    JsonObject body = new JsonObject();

    public photoScreen(Component title) {
        super(title);
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    protected void init() {
        super.init();

        // File name Edit Box
        int nameBoxWidth = 285;
        int nameBoxHeight = 20;
        int boxX = (this.width - nameBoxWidth) / 2; // center the box horizontally
        int boxY = (this.height - nameBoxHeight) / 2 - 15; // move the box up vertically
        EditBox editBox = new EditBox(font, boxX, boxY, nameBoxWidth, nameBoxHeight, title);
        Component prompt = Component.literal("Enter photo name (only letters/numbers/underscores)");
        editBox.setHint(prompt);

        // Tags Edit Box
        int tagsBoxWidth = 285;
        int tagsBoxHeight = 20;
        int tagsBoxX = (this.width - tagsBoxWidth) / 2; // center the box horizontally
        int tagsBoxY = (this.height - tagsBoxHeight + 10) / 2; // center the box vertically
        EditBox tagsBox = new EditBox(font, tagsBoxX, tagsBoxY, tagsBoxWidth, tagsBoxHeight, title);
        Component tagsHint = Component.literal("Enter max 3 tags (comma separated)");
        tagsBox.setHint(tagsHint);

        // Button basics
        int buttonWidth = 85;
        int buttonHeight = 20;
        int buttonY = (this.height - 20) / 2 + 30; // below the edit box
        int lowerButtonY = (this.height - 20) / 2 + 60; // below the edit box

        // Individual Buttons
        int buttonXRight = boxX + nameBoxWidth - buttonWidth; // align the right side of the button with the right side
        int buttonXLeft = boxX; // align the left side of the button with the left side of the box
        int buttonXMiddle = (this.width - buttonWidth) / 2; // center the button horizontally
        
        Component message = Component.literal("Take photo");
        Component cancelMessage = Component.literal("Cancel");
        Component viewPhotosMessage = Component.literal("View Photos");

        // get the filename from the editbox
        Button.OnPress onPress = (button) -> {

            editText = editBox.getValue();
            editText = editText.replaceAll("[^a-zA-Z0-9_]", "");

            // Get the directory
            File directory = new File(
                    System.getProperty("user.home") + File.separator + "AppData\\Roaming\\.minecraft\\screenshots");

            // Get all files in the directory
            File[] files = directory.listFiles();

            System.out.println("Files in directory: " + files.length);

            // Check if editText is the same as any filename in the directory
            for (File file : files) {
                if (file.isFile()) {
                    String filename = file.getName();
                    int pos = filename.lastIndexOf(".");
                    if (pos > 0) {
                        filename = filename.substring(0, pos);
                    }
                    
                    if (filename.equals(editText) || editText == "") {
                        this.onClose();
                        Player player = minecraft.player;
                        player.sendSystemMessage(
                                Component.literal("File already exists or is empty/invalid. Please enter a different name."));
                        return; // exit the method if a file with the same name exists
                    }
                }
            }

            tags = tagsBox.getValue().split(",");
            for (int i = 0; i < tags.length; i++) {
                tags[i] = tags[i].replaceAll("[^a-zA-Z0-9_]", "");
            }

            take_picture();

            this.onClose();
        };

        Button.OnPress cancelPress = (button) -> {
            this.onClose();
        };

        Button.OnPress queryPress = (button) -> {
            this.minecraft.setScreen(new queryScreen(Component.literal("test")));
        };
    
        Button.OnPress viewPress = (button) -> {
            this.minecraft.setScreen(new viewerScreen(Component.literal("test")));
        };

        Button button = new Button.Builder(message, onPress)
                .pos(buttonXRight, buttonY) // set position
                .size(buttonWidth, buttonHeight) // set size
                .build();

        Button cancel = new Button.Builder(cancelMessage, cancelPress)
                .pos(buttonXMiddle, lowerButtonY) // set position
                .size(buttonWidth, buttonHeight) // set size
                .build();

        Button query = new Button.Builder(Component.literal("Get other pics"), queryPress)
                .pos(buttonXMiddle, buttonY) // set position
                .size(buttonWidth, buttonHeight) // set size
                .build();

        Button viewPhotos = new Button.Builder(viewPhotosMessage, viewPress)
                .pos(buttonXLeft, buttonY) // set position
                .size(buttonWidth, buttonHeight) // set size
                .build();

        this.addRenderableWidget(editBox);
        this.addRenderableWidget(tagsBox);
        this.addRenderableWidget(viewPhotos);
        this.addRenderableWidget(query);
        this.addRenderableWidget(button);
        this.addRenderableWidget(cancel);

    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        super.render(graphics, mouseX, mouseY, partialTick);

    }

    @Override
    public void onClose() {

        super.onClose();
    }

    @Override
    public void removed() {

        super.removed();

    }

    public void take_picture() {

        Player player = minecraft.player;
        minecraft.options.hideGui = true;

        CompletableFuture.runAsync(() -> {
            try {
                // Wait for the screen to be completely removed
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            File x = new File(System.getProperty("user.home") + File.separator + "AppData\\Roaming\\.minecraft");
            Minecraft minecraft = Minecraft.getInstance();

            RenderTarget renderTarget = minecraft.getMainRenderTarget();
            Consumer<Component> consumer = (component) -> player.sendSystemMessage(component);
            Screenshot.grab(x, renderTarget, consumer);

            try {
                // Add a delay to give the screenshot operation some time to complete
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Error sleeping");
            }

            minecraft.options.hideGui = false;

            File ScreenshotDir = new File(
                    System.getProperty("user.home") + File.separator + "AppData\\Roaming\\.minecraft\\screenshots");

            File[] files = ScreenshotDir.listFiles();
            if (files == null || files.length == 0) {
                System.out.println("No screenshots found");
                return;
            }
            Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

            File mostRecentScreenshot = files[0];
            String newName = editText;

            Path sourcePath = Paths.get(mostRecentScreenshot.getAbsolutePath());
            try {
                Files.move(sourcePath, sourcePath.resolveSibling(newName + ".png"));
                mostRecentScreenshot = new File(
                        System.getProperty("user.home") + File.separator + "AppData\\Roaming\\.minecraft\\screenshots\\"
                                + newName + ".png");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error renaming file");
            }

            byte[] pngData = null;
            try {
                pngData = Files.readAllBytes(Paths.get(mostRecentScreenshot.getAbsolutePath()));
            } catch (Exception e) {
                System.out.println("Error reading file");
                e.printStackTrace();
            }

            String pngBytes = Base64.getEncoder().encodeToString(pngData);

            if (tags.length > 3) {
                tags = Arrays.copyOfRange(tags, 0, 2);
            }

            body.addProperty("username", minecraft.getUser().getName());
            body.addProperty("filename", editText);
            body.addProperty("tags", Arrays.toString(tags));
            body.addProperty("image", pngBytes);

            try {
                sendPutRequest(body);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error sending put request");
            }

            String tagsString = Arrays.toString(tags);
            player.sendSystemMessage(Component.literal(tagsString));
        });
    }

    public void sendPutRequest(JsonObject body) throws Exception {

        final StringEntity entity = new StringEntity(body.toString(), Consts.UTF_8);
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        try {
            HttpPut request = new HttpPut("http://24.210.19.44:8080/photos");
            request.addHeader("content-type", "application/json");
            request.setEntity(entity);

            if (httpClient.execute(request).getStatusLine().getStatusCode() == 200) {
                Player player = minecraft.player;
                player.sendSystemMessage(Component.literal("Photo uploaded successfully!"));
            } else {
                Player player = minecraft.player;
                player.sendSystemMessage(Component.literal("Photo upload failed."));
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            httpClient.close();
        }
    }
}
