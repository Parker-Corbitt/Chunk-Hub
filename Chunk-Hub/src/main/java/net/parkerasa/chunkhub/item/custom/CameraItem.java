package net.parkerasa.chunkhub.item.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.function.Consumer;
import net.minecraft.world.level.Level;
import com.mojang.blaze3d.pipeline.RenderTarget;
import java.io.File;
import net.parkerasa.chunkhub.menu.photoScreen;



public class CameraItem extends Item {
    public CameraItem(Properties cameraProperties) {
        super(cameraProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {

            Minecraft.getInstance().setScreen(new photoScreen(Component.literal("test")));
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }
}
