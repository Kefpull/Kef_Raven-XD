package keystrokesmod.utility;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import keystrokesmod.clickgui.ClickGui;
import keystrokesmod.mixins.impl.render.RenderManagerAccessor;
import keystrokesmod.module.impl.render.Freecam;
import keystrokesmod.module.impl.render.HUD;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.Theme;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.font.IFont;
import keystrokesmod.utility.render.shader.GaussianFilter;
import keystrokesmod.utility.render.shader.impl.ShaderScissor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static keystrokesmod.Raven.mc;
import static org.lwjgl.opengl.GL11.*;
public class GroundUtils {

    public static boolean basicGroundCheck(@NotNull WorldClient level, @NotNull BlockPos groundPos) {
        if (!level.getBlockState(groundPos).getBlock().isAir(level, groundPos) || !level.getBlockState(groundPos.down()).getBlock().isAir(level, groundPos.down()))
            return false;

        short count = 0;
        final List<BlockPos> blocks = new ArrayList<>();
        blocks.add(groundPos.east());
        blocks.add(groundPos.east().north());
        blocks.add(groundPos.west());
        blocks.add(groundPos.west().south());
        blocks.add(groundPos.north());
        blocks.add(groundPos.north().west());
        blocks.add(groundPos.south());
        blocks.add(groundPos.south().east());

        for (BlockPos blockPos : blocks) {
            if (level.getBlockState(blockPos).getBlock().isAir(level, blockPos)) {
                count++;
            }
        }

        return count >= 8;
    }






}
