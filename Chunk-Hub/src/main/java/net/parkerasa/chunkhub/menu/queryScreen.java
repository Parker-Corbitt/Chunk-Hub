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
import net.minecraft.world.entity.player.Player;

public class queryScreen extends Screen {

    private final Minecraft minecraft;
    private String username;
    private String[] tags;
    private int get_param;

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

        int tagsBoxWidth = 285;
        int tagsBoxHeight = 20;
        int tagsBoxX = (this.width - tagsBoxWidth) / 2; // center the box horizontally
        int tagsBoxY = (this.height - tagsBoxHeight + 10) / 2; // center the box vertically
        EditBox tagsBox = new EditBox(font, tagsBoxX, tagsBoxY, tagsBoxWidth, tagsBoxHeight, title);
        Component tagsHint = Component.literal("Enter 3 tags (comma separated) desired in photo");
        tagsBox.setHint(tagsHint);

        int buttonWidth = 85;
        int buttonHeight = 20;
        int buttonY = (this.height - 20) / 2 + 30; // below the edit box

        // Individual Buttons
        int buttonXRight = boxX + nameBoxWidth - buttonWidth; // align the right side of the button with the right side
        int buttonXLeft = boxX; // align the left side of the button with the left side of the box
        int buttonXMiddle = (this.width - buttonWidth) / 2; // center the button horizontally

        Component cancelMessage = Component.literal("Cancel");
        Component backMessage = Component.literal("Back");
        Component queryMessage = Component.literal("Get Photos");

        Button.OnPress cancelPress = (button) -> {
            this.onClose();
        };

        Button.OnPress backPress = (button) -> {
            this.minecraft.setScreen(new photoScreen(Component.literal("test")));
        };

        Button.OnPress queryPress = (button) -> {
            username = editBox.getValue();
            tags = tagsBox.getValue().split(",");
            for (int i = 0; i < tags.length; i++) {
                tags[i] = tags[i].replaceAll("[^a-zA-Z0-9_]", "");
            }

            if (username.length() > 0 && tags.length >= 1 && tags[0] != "") {
                get_param = 2;
            } else if (username.length() > 0 && tags[0] == "") {
                get_param = 0;
            } else if (tags[0] != "") {
                get_param = 1;
            } else {
                Player player = Minecraft.getInstance().player;
                player.sendSystemMessage(
                                Component.literal("Both field are empty. Please fill out at least one field"));
                this.onClose();
            }

            try {
                System.out.println(get_param);
                sendGetRequest();
            } catch (Exception e) {
                System.out.println("Error: " + e);
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
        this.addRenderableWidget(tagsBox);
        this.addRenderableWidget(cancel);
        this.addRenderableWidget(back_button);
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

    }

    public void sendGetRequest() throws Exception {

        // Define the URL
        URL url = new URL("http://");
        if (get_param == 0) {
            url = new URL("http://24.210.19.44:8080/photo-user?username=");
            url = new URL(url.toString() + username);
        } else if (get_param == 1) {
            url = new URL("http://24.210.19.44:8080/photo-tags?tag=");
            url = new URL(url.toString() + tags[0]);
            for (int i = 1; i < tags.length; i++) {
                url = new URL(url.toString() + "," + tags[i]);
            }
        } else if (get_param == 2) {
            url = new URL("http://24.210.19.44:8080/photo-tags-user?username=");
            url = new URL(url.toString() + username + "&tag=");
            url = new URL(url.toString() + tags[0]);
            for (int i = 1; i < tags.length; i++) {
                url = new URL(url.toString() + "," + tags[i]);
            }
        }

        // Open a connection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // Check for successful response code or throw error
        int responseCode = conn.getResponseCode();
        System.out.println("Response Code : " + responseCode);

        // Define the zip file path
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

        // Delete the zip file
        File zipFile = new File(zipFilePath);
        zipFile.delete();

        // Close the connection
        conn.disconnect();
    }
}
