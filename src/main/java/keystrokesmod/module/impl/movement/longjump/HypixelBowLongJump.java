package keystrokesmod.module.impl.movement.longjump;

import keystrokesmod.Raven;
import keystrokesmod.event.*;
import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.impl.movement.LongJump;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.impl.player.blink.NormalBlink;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class HypixelBowLongJump extends SubMode<LongJump> {
    private final SliderSetting speed;
    private final ButtonSetting autoDisable;

    private State state = State.SELF_DAMAGE;
    private final NormalBlink blink = new NormalBlink("Blink", this);

    public HypixelBowLongJump(String name, @NotNull LongJump parent) {
        super(name, parent);
        this.registerSetting(speed = new SliderSetting("Speed", 1, 0, 1.5, 0.1));
        this.registerSetting(autoDisable = new ButtonSetting("Auto disable", true));
    }

    @Override
    public void onEnable() throws Throwable {
        MoveUtil.stop();
        state = State.SELF_DAMAGE;
    }

    @Override
    public void onDisable() throws Throwable {
        blink.disable();
    }

    @SubscribeEvent
    public void onMoveInput(MoveInputEvent event) {
        if (state == State.SELF_DAMAGE || state == State.SELF_DAMAGE_POST) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity && (state == State.JUMP)) {
            S12PacketEntityVelocity packet = (S12PacketEntityVelocity) event.getPacket();
            if (packet.getEntityID() != mc.thePlayer.getEntityId()) return;

            state = State.APPLY;
        }
    }

    @SubscribeEvent
    public void onRotation(RotationEvent event) {
        if (state == State.SELF_DAMAGE || state == State.SELF_DAMAGE_POST)
            event.setPitch(-90);
    }

    @SubscribeEvent
    public void onPrePlayerInput(PrePlayerInputEvent event) {
        int slot = getBow();
        if (slot == -1) {
            Notifications.sendNotification(Notifications.NotificationTypes.INFO, "Could not find Bow");
            parent.disable();
        }
        switch (state) {
            case SELF_DAMAGE:
                if (SlotHandler.getCurrentSlot() == slot) {
                    PacketUtils.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(SlotHandler.getHeldItem()));
                    Raven.getExecutor().schedule(() -> {
                        PacketUtils.sendPacketNoEvent(new C07PacketPlayerDigging(
                                C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                                BlockPos.ORIGIN, EnumFacing.UP
                        ));
                        Raven.getExecutor().schedule(() -> state = State.JUMP, 300, TimeUnit.MILLISECONDS);
                    }, 150, TimeUnit.MILLISECONDS);
                    state = State.SELF_DAMAGE_POST;
                }
                SlotHandler.setCurrentSlot(slot);
                break;
            case SELF_DAMAGE_POST:
                SlotHandler.setCurrentSlot(slot);
                break;
            case JUMP:
                if (!Utils.jumpDown() && mc.thePlayer.onGround) {
                    blink.enable();
                    MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance() - Math.random() / 100f);
                    mc.thePlayer.jump();
                }
                break;
            case APPLY:
                blink.disable();
                state = State.BOOST;
                break;
            case BOOST:
                if (speed.getInput() > 0)
                    MoveUtil.strafe(speed.getInput());
                state = State.NONE;
                break;
            case NONE:
                if (autoDisable.isToggled())
                    parent.disable();
        }
    }

    private int getBow() {
        int a = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack getStackInSlot = mc.thePlayer.inventory.getStackInSlot(i);
            if (getStackInSlot != null && getStackInSlot.getItem() instanceof ItemBow) {
                a = i;
                break;
            }
        }
        return a;
    }

    enum State {
        SELF_DAMAGE,
        SELF_DAMAGE_POST,
        JUMP,
        APPLY,
        BOOST,
        NONE
    }
}
