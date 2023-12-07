package net.parkerasa.chunkhub.menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.parkerasa.chunkhub.menu.photoScreen;
import net.minecraft.client.Minecraft;

public class photoScreen extends Screen {

    private final Minecraft minecraft;

    public photoScreen(Component title) {
        super(title);
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    protected void init() {
        super.init();

        int boxWidth = 200;
        int boxHeight = 20;
        int boxX = (this.width - boxWidth) / 2; // center the box horizontally
        int boxY = (this.height - boxHeight) / 2; // center the box vertically

        this.addRenderableWidget(new EditBox(font, boxX, boxY, boxWidth, boxHeight, title));

        int buttonWidth = 100;
        int buttonHeight = 20;
        int buttonY = boxY + boxHeight + 10; // 10 pixels below the EditBox

        // Cancel button on the left
        
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Background is typically rendered first
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        // Render things here before widgets (background textures)

        // Then the widgets if this is a direct child of the Screen
        super.render(graphics, mouseX, mouseY, partialTick);

        // Render things after widgets (tooltips)
    }

    @Override
    public void onClose() {
        // Stop any handlers here

        // Call last in case it interferes with the override
        super.onClose();
    }

    @Override
    public void removed() {
        // Reset initial states here

        // Call last in case it interferes with the override
        super.removed();
    }

}