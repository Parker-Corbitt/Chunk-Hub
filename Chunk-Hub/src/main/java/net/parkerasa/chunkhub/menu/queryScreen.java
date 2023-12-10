package net.parkerasa.chunkhub.menu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.Screen;

public class queryScreen extends Screen {

    private final Minecraft minecraft;
    private String username;

    public queryScreen(Component title) {
        super(title);
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    protected void init() {
        super.init();

        int nameBoxWidth = 285;
        int nameBoxHeight = 20;
        int boxX = (this.width - nameBoxWidth) / 2; // center the box horizontally
        int boxY = (this.height - nameBoxHeight) / 2 - 15; // move the box up vertically
        EditBox editBox = new EditBox(font, boxX, boxY, nameBoxWidth, nameBoxHeight, title);
        Component prompt = Component.literal("Enter username of photo owner");
        editBox.setHint(prompt);

        int buttonWidth = 100;
        int buttonHeight = 20;
        int buttonY = (this.height - 20) / 2 + 30; // below the edit box

        int buttonXRight = boxX + nameBoxWidth - buttonWidth; // align the right side of the button with the right side
        int buttonXLeft = boxX; // align the left side of the button with the left side of the box
        int buttonXMiddle = (this.width - buttonWidth) / 2;

        Component cancelMessage = Component.literal("Cancel");
        Component backMessage = Component.literal("Back");
        Component queryMessage = Component.literal("Query");

        Button.OnPress cancelPress = (button) -> {
            this.onClose();
        };

        Button.OnPress backPress = (button) -> {
            this.minecraft.setScreen(new photoScreen(Component.literal("test")));
        };

        Button.OnPress queryPress = (button) -> {
            username = editBox.getValue();
            try {
                sendGetRequest();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        Button cancel = new Button.Builder(cancelMessage, cancelPress)
                .pos(buttonXLeft, buttonY) // set position
                .size(buttonWidth, buttonHeight) // set size
                .build();

        Button back_button = new Button.Builder(backMessage, backPress)
                .pos(buttonXMiddle, buttonY) // set position
                .size(buttonWidth, buttonHeight) // set size
                .build();

        Button query = new Button.Builder(queryMessage, queryPress)
                .pos(buttonXRight, buttonY) // set position
                .size(buttonWidth, buttonHeight) // set size
                .build();

        this.addRenderableWidget(editBox);
        this.addRenderableWidget(back_button);
        this.addRenderableWidget(query);
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
        // Player player = minecraft.player;
        // player.sendSystemMessage(Component.literal(player.getName().getString()));

    }

    public void take_picture() {

    }

    public void sendGetRequest() throws Exception {
        URL url = new URL("http://24.210.19.44:8080/photo-zip?username=");
        url = new URL(url.toString() + username);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        System.out.println("Response Code : " + responseCode);
        // Define the path where you want to save the zip file
        String zipFilePath = System.getProperty("user.home") + File.separator
                + "AppData\\Roaming\\.minecraft\\screenshots\\file.zip";

        // Download the zip file
        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, Paths.get(zipFilePath), StandardCopyOption.REPLACE_EXISTING);
        }

        // Define the directory where you want to unpack the zip file
        String unzipDir = System.getProperty("user.home") + File.separator
                + "AppData\\Roaming\\.minecraft\\screenshots";

        // Unpack the zip file
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File file = new File(unzipDir, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    file.getParentFile().mkdirs();
                    try (OutputStream out = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            out.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }
}
