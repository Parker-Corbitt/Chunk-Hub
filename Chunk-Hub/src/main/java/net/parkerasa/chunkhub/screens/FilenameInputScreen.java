package net.parkerasa.chunkhub.screens;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Button;


public class FilenameInputScreen extends Screen {
    private EditBox filenameInput;

    public FilenameInputScreen(Component input) {
        super(input);
    }

    @Override
    protected void init() {
        this.filenameInput = new EditBox(this.font, this.width / 2 - 100, this.height / 2, 200, 20,
                Component.nullToEmpty(this.getFilename()));
        this.filenameInput.setMaxLength(32);
        this.filenameInput.setResponder((input) -> {
            this.getFilename();
        });
        this.addRenderableWidget(this.filenameInput);
        this.setInitialFocus(this.filenameInput);

        // Add a button
        Button doneButton = Button.builder(Component.nullToEmpty("Done"), (button) -> {
            // Close the screen when the button is pressed
            this.minecraft.setScreen((Screen)null);
        })
        .pos(this.width / 2 - 50, this.height / 2 + 30)
        .size(100, 20)
        .build();

        this.addRenderableWidget(doneButton);
    }

    public String getFilename() {
        return this.filenameInput.getValue();
    }
}