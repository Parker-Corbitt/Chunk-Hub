package net.parkerasa.chunkhub.item.custom;

import net.minecraft.client.Screenshot;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CameraItem extends Item {
    public CameraItem(Properties cameraProperties)
    {
        super(cameraProperties);
    }

//    public void takeScreenshot(String directory, String format)
//    {
//        try
//        {
//            Robot robot = new Robot();
//            String fileName = directory + File.separator + "Screenshot." + format;
//
//
//            new File(directory).mkdirs();
//
//            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//            Rectangle captureRect = new Rectangle(screenSize);
//            BufferedImage screenFullImage = robot.createScreenCapture(captureRect);
//            ImageIO.write(screenFullImage, format, new File(fileName));
//        }
//        catch (Exception ex)
//        {
//            System.err.println(ex);
//        }
//    }

    @Override
    public InteractionResult useOn(UseOnContext snapshot)
    {
        if(!snapshot.getLevel().isClientSide)
        {
            Player player = snapshot.getPlayer();
            player.sendSystemMessage(Component.literal("attempt"));
            //takeScreenshot("C:\\Users\\gwend\\AppData\\Roaming\\.minecraft\\screenshots", "png");
            File x = new File("C:\\Users\\gwend\\AppData\\Roaming\\.minecraft");
            try {
                Screenshot a = new Screenshot(x, 1, 1, 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }

        return InteractionResult.SUCCESS;
    }
}
