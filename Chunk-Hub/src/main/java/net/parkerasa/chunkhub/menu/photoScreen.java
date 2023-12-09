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

    public photoScreen(Component title) {
        super(title);
        this.minecraft = Minecraft.getInstance();
    }

    public void sendPutRequest(JsonObject body, byte[] pngData) throws Exception {
    URL url = new URL("http://24.210.19.44:8080/photos");
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

    // Set the request method to PUT
    conn.setRequestMethod("PUT");

    // Set the request property
    // conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
    conn.setDoOutput(true);

    try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {

        wr.writeBytes(body.toString() + "\r\n");

        wr.flush();
    }
    catch (Exception e) {
        e.printStackTrace();
    }
    finally {
        conn.disconnect();
    }

    int responseCode = conn.getResponseCode();
    System.out.println("Response Code : " + responseCode);
}

    @Override
    protected void init() {
        super.init();

        int boxWidth = 200;
        int boxHeight = 20;
        int boxX = (this.width - boxWidth) / 2; // center the box horizontally
        int boxY = (this.height - boxHeight) / 2; // center the box vertically
        EditBox editBox = new EditBox(font, boxX, boxY, boxWidth, boxHeight, title);

        int buttonX = (this.width - 150) / 2; // center the button horizontally
        int buttonY = (this.height - 20) / 2 + 30; // below the edit box
        Component message = Component.literal("Take Screenshot and save as entered file");

        // get the filename from the editbox
        Button.OnPress onPress = (button) -> {
            editText = editBox.getValue();
            this.onClose();
        };

        Button button = new Button.Builder(message, onPress)
                .pos(buttonX, buttonY) // set position
                .size(150, 20) // set size
                .build();

        this.addRenderableWidget(editBox);
        this.addRenderableWidget(button);

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

        minecraft.options.hideGui = true;
        CompletableFuture.runAsync(() -> {
            try {
                // Wait for the screen to be completely removed
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            File x = new File(System.getProperty("user.home") + File.separator + "AppData\\Roaming\\.minecraft");
            Player player = minecraft.player;
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

            JsonObject body = new JsonObject();
            body.addProperty("username", minecraft.getUser().getName());
            body.addProperty("filename", editText);
            body.addProperty("tags", "");
            body.addProperty("image", pngBytes);
        
            try {
                sendPutRequest(body, pngData);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }
}
