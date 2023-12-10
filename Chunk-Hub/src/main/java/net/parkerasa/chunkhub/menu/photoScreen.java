package net.parkerasa.chunkhub.menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.parkerasa.chunkhub.menu.photoScreen;
import net.minecraft.client.Minecraft;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Screenshot;
import net.minecraft.world.entity.player.Player;
import java.util.concurrent.CompletableFuture;
import java.util.Arrays;
import java.net.HttpURLConnection;
import java.net.URL;

public class photoScreen extends Screen {

    private final Minecraft minecraft;
    private String editText;
    private String[] tags;
    private String pngstuff;

    public photoScreen(Component title) {
        super(title);
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    protected void init() {
        super.init();

        //File name Edit Box
        int nameBoxWidth = 285;
        int nameBoxHeight = 20;
        int boxX = (this.width - nameBoxWidth) / 2; // center the box horizontally
        int boxY = (this.height - nameBoxHeight) / 2 - 15; // move the box up vertically
        EditBox editBox = new EditBox(font, boxX, boxY, nameBoxWidth, nameBoxHeight, title);
        Component prompt = Component.literal("Enter photo name (only letters/numbers/underscores)");
        editBox.setHint(prompt);

        //Tags Edit Box
        int tagsBoxWidth = 285;
        int tagsBoxHeight = 20;
        int tagsBoxX = (this.width - tagsBoxWidth) / 2; // center the box horizontally
        int tagsBoxY = (this.height - tagsBoxHeight + 10) / 2; // center the box vertically
        EditBox tagsBox = new EditBox(font, tagsBoxX, tagsBoxY, tagsBoxWidth, tagsBoxHeight, title);
        Component tagsHint = Component.literal("Enter tags (comma separated)");
        tagsBox.setHint(tagsHint);

        //Button basics
        int buttonWidth = 100;
        int buttonHeight = 20;
        int buttonY = (this.height - 20) / 2 + 30; // below the edit box

        //Individual Buttons
        int buttonXRight = boxX + nameBoxWidth - buttonWidth; // align the right side of the button with the right side
        int buttonXLeft = boxX; // align the left side of the button with the left side of the box
        int buttonXMiddle = (this.width - buttonWidth) / 2; // center the button horizontally

        Component message = Component.literal("Take photo");
        Component cancelMessage = Component.literal("Cancel");


        // get the filename from the editbox
        Button.OnPress onPress = (button) -> {
            editText = editBox.getValue();
            editText = editText.replaceAll("[^a-zA-Z0-9_]", "");

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

        Button button = new Button.Builder(message, onPress)
                .pos(buttonXRight, buttonY) // set position
                .size(buttonWidth, buttonHeight) // set size
                .build();

        Button cancel = new Button.Builder(cancelMessage, cancelPress)
                .pos(buttonXLeft, buttonY) // set position
                .size(buttonWidth, buttonHeight) // set size
                .build();

        Button query = new Button.Builder(Component.literal("Get other pics"), queryPress)
                .pos(buttonXMiddle, buttonY) // set position
                .size(buttonWidth, buttonHeight) // set size
                .build();

        this.addRenderableWidget(editBox);
        this.addRenderableWidget(tagsBox);
        this.addRenderableWidget(button);
        this.addRenderableWidget(cancel);
        this.addRenderableWidget(query);

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
        // Player player = minecraft.player;
        // player.sendSystemMessage(Component.literal(player.getName().getString()));

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

            // Step 2: Get the new name from the editText
            String newName = editText;

            // Step 3: Rename the screenshot
            Path sourcePath = Paths.get(mostRecentScreenshot.getAbsolutePath());
            try {
                Files.move(sourcePath, sourcePath.resolveSibling(newName + ".png"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            byte[] pngData = null;
            try {
                pngData = Files.readAllBytes(Paths.get(mostRecentScreenshot.getAbsolutePath()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            String pngBytes = new String(pngData);
            if (tags.length > 3) {
                tags = Arrays.copyOfRange(tags, 0, 2);
            }

            pngstuff = pngBytes;

            JsonObject body = new JsonObject();
            body.addProperty("username", minecraft.getUser().getName());
            body.addProperty("filename", editText);
            body.addProperty("tags", Arrays.toString(tags));
            body.addProperty("image", pngBytes);

            try {
                sendPutRequest(body);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        String tagsString = Arrays.toString(tags);
        player.sendSystemMessage(Component.literal(tagsString));

    }

    public void sendPutRequest(JsonObject body) throws Exception {
        URL url = new URL("http://24.210.19.44:8080/photos");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("PUT");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {

            wr.writeBytes(body.toString() + "\r\n");

            wr.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }

        int responseCode = conn.getResponseCode();
        System.out.println("Response Code : " + responseCode);
        System.out.println(pngstuff);
    }
}
