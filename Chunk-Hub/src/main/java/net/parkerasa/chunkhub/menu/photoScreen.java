package net.parkerasa.chunkhub.menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.parkerasa.chunkhub.menu.photoScreen;
import net.minecraft.client.Minecraft;
import java.io.File;
import java.util.function.Consumer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Screenshot;
import net.minecraft.world.entity.player.Player;
import java.util.concurrent.CompletableFuture;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class photoScreen extends Screen {

    private final Minecraft minecraft;
    private String editText;

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

            File ScreenshotDir = new File(System.getProperty("user.home") + File.separator + "AppData\\Roaming\\.minecraft\\screenshots");
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

        });
    }
}

// package net.minecraft.client.gui.components;

// import java.util.function.Supplier;
// import javax.annotation.Nullable;
// import net.minecraft.client.gui.narration.NarrationElementOutput;
// import net.minecraft.network.chat.Component;
// import net.minecraft.network.chat.MutableComponent;
// import net.minecraftforge.api.distmarker.Dist;
// import net.minecraftforge.api.distmarker.OnlyIn;

// @OnlyIn(Dist.CLIENT)
// public class Button extends AbstractButton {
// public static final int SMALL_WIDTH = 120;
// public static final int DEFAULT_WIDTH = 150;
// public static final int DEFAULT_HEIGHT = 20;
// protected static final Button.CreateNarration DEFAULT_NARRATION = (p_253298_)
// -> {
// return p_253298_.get();
// };
// protected final Button.OnPress onPress;
// protected final Button.CreateNarration createNarration;

// public static Button.Builder builder(Component p_254439_, Button.OnPress
// p_254567_) {
// return new Button.Builder(p_254439_, p_254567_);
// }

// protected Button(int p_259075_, int p_259271_, int p_260232_, int p_260028_,
// Component p_259351_, Button.OnPress p_260152_, Button.CreateNarration
// p_259552_) {
// super(p_259075_, p_259271_, p_260232_, p_260028_, p_259351_);
// this.onPress = p_260152_;
// this.createNarration = p_259552_;
// }

// protected Button(Builder builder) {
// this(builder.x, builder.y, builder.width, builder.height, builder.message,
// builder.onPress, builder.createNarration);
// setTooltip(builder.tooltip); // Forge: Make use of the Builder tooltip
// }

// public void onPress() {
// this.onPress.onPress(this);
// }

// protected MutableComponent createNarrationMessage() {
// return this.createNarration.createNarrationMessage(() -> {
// return super.createNarrationMessage();
// });
// }

// public void updateWidgetNarration(NarrationElementOutput p_259196_) {
// this.defaultButtonNarrationText(p_259196_);
// }

// @OnlyIn(Dist.CLIENT)
// public static class Builder {
// private final Component message;
// private final Button.OnPress onPress;
// @Nullable
// private Tooltip tooltip;
// private int x;
// private int y;
// private int width = 150;
// private int height = 20;
// private Button.CreateNarration createNarration = Button.DEFAULT_NARRATION;

// public Builder(Component p_254097_, Button.OnPress p_253761_) {
// this.message = p_254097_;
// this.onPress = p_253761_;
// }

// public Button.Builder pos(int p_254538_, int p_254216_) {
// this.x = p_254538_;
// this.y = p_254216_;
// return this;
// }

// public Button.Builder width(int p_254259_) {
// this.width = p_254259_;
// return this;
// }

// public Button.Builder size(int p_253727_, int p_254457_) {
// this.width = p_253727_;
// this.height = p_254457_;
// return this;
// }

// public Button.Builder bounds(int p_254166_, int p_253872_, int p_254522_, int
// p_253985_) {
// return this.pos(p_254166_, p_253872_).size(p_254522_, p_253985_);
// }

// public Button.Builder tooltip(@Nullable Tooltip p_259609_) {
// this.tooltip = p_259609_;
// return this;
// }

// public Button.Builder createNarration(Button.CreateNarration p_253638_) {
// this.createNarration = p_253638_;
// return this;
// }

// public Button build() {
// return build(Button::new);
// }

// public Button build(java.util.function.Function<Builder, Button> builder) {
// return builder.apply(this);
// }
// }

// @OnlyIn(Dist.CLIENT)
// public interface CreateNarration {
// MutableComponent createNarrationMessage(Supplier<MutableComponent>
// p_253695_);
// }

// @OnlyIn(Dist.CLIENT)
// public interface OnPress {
// void onPress(Button p_93751_);
// }
// }
