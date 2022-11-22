package xyz.wagyourtail.jsmacros.client.mixins.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.wagyourtail.jsmacros.client.api.classes.inventory.Inventory;
import xyz.wagyourtail.jsmacros.client.api.event.impl.player.EventAttackBlock;
import xyz.wagyourtail.jsmacros.client.api.event.impl.player.EventAttackEntity;
import xyz.wagyourtail.jsmacros.client.api.event.impl.player.EventInteractBlock;
import xyz.wagyourtail.jsmacros.client.api.event.impl.player.EventInteractEntity;
import xyz.wagyourtail.jsmacros.client.api.event.impl.inventory.EventInventoryChange;
import xyz.wagyourtail.jsmacros.client.api.helpers.inventory.ItemStackHelper;
import xyz.wagyourtail.jsmacros.client.api.helpers.world.BlockDataHelper;

import java.util.List;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {

    @Shadow @Final private Minecraft client;

    @Inject(at = @At("RETURN"), method = "onRightClick")
    public void onInteractBlock(EntityPlayerSP p_onRightClick_1_, WorldClient p_onRightClick_2_, ItemStack p_onRightClick_3_, BlockPos p_onRightClick_4_, EnumFacing p_onRightClick_5_, Vec3 p_onRightClick_6_, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            new EventInteractBlock(
                false,
                cir.getReturnValue().toString(),
                new BlockDataHelper(p_onRightClick_2_.getBlockState(p_onRightClick_4_), p_onRightClick_2_.getBlockEntity(p_onRightClick_4_), p_onRightClick_4_),
                p_onRightClick_5_.getId()
            );
        }
    }

    @Inject(at = @At("RETURN"), method = "attackBlock")
    public void onAttackBlock(BlockPos p_attackBlock_1_, EnumFacing p_attackBlock_2_, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            assert client.world != null;
            new EventAttackBlock(
                new BlockDataHelper(client.world.getBlockState(p_attackBlock_1_), client.world.getBlockEntity(p_attackBlock_1_), p_attackBlock_1_),
                p_attackBlock_2_.getId()
            );
        }
    }

    @Inject(at = @At("RETURN"), method = "attackEntity")
    public void onAttackEntity(EntityPlayer p_attackEntity_1_, Entity p_attackEntity_2_, CallbackInfo ci) {
        new EventAttackEntity(p_attackEntity_2_);
    }

    @Inject(at = @At("RETURN"), method = "interactEntity")
    public void onInteractEntity(EntityPlayer p_interactEntity_1_, Entity p_interactEntity_2_, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            new EventInteractEntity(false, cir.getReturnValue().toString(), p_interactEntity_2_);
        }
    }

    @Inject(method = "clickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;onSlotClick(IILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onClickSlot(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci, ScreenHandler screenHandler, DefaultedList<Slot> defaultedList, int size, List<ItemStack> list) {
        Int2ObjectMap<ItemStackHelper> oldItems = new Int2ObjectOpenHashMap<>();
        Int2ObjectMap<ItemStackHelper> newItems = new Int2ObjectOpenHashMap<>();

        for (int idx = 0; idx < size; idx++) {
            ItemStack oldStack = list.get(idx);
            ItemStack newStack = defaultedList.get(idx).getStack();
            if (!ItemStack.areEqual(oldStack, newStack)) {
                oldItems.put(idx, new ItemStackHelper(oldStack));
                newItems.put(idx, new ItemStackHelper(newStack));
            }
        }
        new EventInventoryChange(Inventory.create(), oldItems.keySet().toIntArray(), oldItems, newItems);
    }
    


    @Inject(at = @At("RETURN"), method = "method_12236")
    public void onInteractEntity(PlayerEntity playerEntity, Entity entity, HitResult hitResult, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (cir.getReturnValue() != ActionResult.FAIL) {
            new EventInteractEntity(hand != Hand.MAIN_HAND, cir.getReturnValue().name(), entity);
        }
    }
}
