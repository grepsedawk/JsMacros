package xyz.wagyourtail.jsmacros.client.mixins.events;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.wagyourtail.jsmacros.client.access.ISignEditScreen;
import xyz.wagyourtail.jsmacros.client.api.classes.inventory.Inventory;
import xyz.wagyourtail.jsmacros.client.api.classes.PlayerInput;
import xyz.wagyourtail.jsmacros.client.api.event.impl.inventory.EventDropSlot;
import xyz.wagyourtail.jsmacros.client.api.event.impl.inventory.EventInventoryChange;
import xyz.wagyourtail.jsmacros.client.api.event.impl.player.EventAirChange;
import xyz.wagyourtail.jsmacros.client.api.event.impl.player.EventEXPChange;
import xyz.wagyourtail.jsmacros.client.api.event.impl.player.EventRiding;
import xyz.wagyourtail.jsmacros.client.api.event.impl.player.EventSignEdit;
import xyz.wagyourtail.jsmacros.client.api.helpers.inventory.ItemStackHelper;
import xyz.wagyourtail.jsmacros.client.movement.MovementQueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(EntityPlayerSP.class)
abstract class MixinClientPlayerEntity extends AbstractClientPlayer {
    @Shadow
    protected Minecraft client;

    @Shadow
    @Final
    public NetHandlerPlayClient networkHandler;


    // IGNORE
    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow
    public MovementInput input;

    @Shadow public abstract boolean isSneaking();

    @Override
    public void setAir(int air) {
        if (air % 20 == 0) new EventAirChange(air);
        super.setAir(air);
    }

    @Inject(at = @At("HEAD"), method="setExperience")
    public void onSetExperience(float progress, int total, int level, CallbackInfo info) {
        new EventEXPChange(progress, total, level, this.experienceProgress, this.totalExperience, this.experienceLevel);
    }

    @Inject(at = @At("HEAD"), method = "openEditSignScreen", cancellable = true)
    public void onOpenEditSignScreen(TileEntitySign sign, CallbackInfo info) {
        List<String> lines = new ArrayList<>(Arrays.asList("", "", "", ""));
        final EventSignEdit event = new EventSignEdit(lines, sign.getPos().getX(), sign.getPos().getY(), sign.getPos().getZ());
        lines = event.signText;
        if (event.closeScreen) {
            for (int i = 0; i < 4; ++i) {
                sign.field_145915_a[i] = new ChatComponentText(lines.get(i));
            }
            sign.markDirty();
            networkHandler.sendPacket(new C12PacketUpdateSign(sign.getPos(), lines.stream().map(ChatComponentText::new).toArray(
                IChatComponent[]::new)));
            info.cancel();
            return;
        }
        //this part to not info.cancel is here for more compatibility with other mods.
        boolean cancel = false;
        for (String line : lines) {
            if (!line.equals("")) {
                cancel = true;
                break;
            }
        } //else
        if (cancel) {
            final GuiEditSign signScreen = new GuiEditSign(sign);
            client.openScreen(signScreen);
            for (int i = 0; i < 4; ++i) {
                ((ISignEditScreen) signScreen).jsmacros_setLine(i, lines.get(i));
            }
            info.cancel();
        }
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/MovementInput;func_78898_a()V", shift = At.Shift.AFTER))
    public void overwriteInputs(CallbackInfo ci) {
        PlayerInput moveInput = MovementQueue.tick(client.player);
        if (moveInput == null) {
            return;
        }
        this.input.movementForward = moveInput.movementForward;
        this.input.movementSideways = moveInput.movementSideways;
        this.input.jumping = moveInput.jumping;
        this.input.sneaking = moveInput.sneaking;
        KeyBinding.setKeyPressed(this.client.options.keySprint.getCode(), moveInput.sprinting);
        this.yaw = moveInput.yaw;
        this.pitch = moveInput.pitch;
    }


    public MixinClientPlayerEntity(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    public void onDropSelected(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        int selectedHotbarIndex = inventory.selectedSlot;
        EventDropSlot event = new EventDropSlot(null, 36 + selectedHotbarIndex, entireStack);
        if (event.isCanceled()) {
            cir.setReturnValue(false);
        }
        int[] slots = new int[]{getInventory().selectedSlot + 36};
        ItemStackHelper[] oldItems = new ItemStackHelper[]{new ItemStackHelper(getInventory().getMainHandStack())};
        ItemStackHelper[] newItems = new ItemStackHelper[]{new ItemStackHelper(entireStack ? Items.AIR.getDefaultStack() : getInventory().getMainHandStack())};
        new EventInventoryChange(Inventory.create(), slots, oldItems, newItems);
    }
}