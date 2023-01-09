package xyz.wagyourtail.jsmacros.client.mixins.events;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.util.Session;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.ClientPlayerEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.wagyourtail.jsmacros.client.api.event.impl.world.EventDimensionChange;
import xyz.wagyourtail.jsmacros.client.api.event.impl.world.EventDisconnect;
import xyz.wagyourtail.jsmacros.client.api.event.impl.EventLaunchGame;
import xyz.wagyourtail.jsmacros.client.api.event.impl.inventory.EventOpenContainer;
import xyz.wagyourtail.jsmacros.client.api.event.impl.player.EventOpenScreen;
import xyz.wagyourtail.jsmacros.client.mixins.access.MixinDisconnectedScreen;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Shadow
    public Screen currentScreen;

    @Shadow public abstract void openScreen(Screen screen);

    @Shadow public ClientPlayerEntity player;

    @Shadow @Final
    private Session session;

    @Inject(at = @At("HEAD"), method="connect(Lnet/minecraft/client/world/ClientWorld;)V")
    public void onJoinWorld(ClientWorld world, CallbackInfo info) {
        if (world != null)
            new EventDimensionChange(world.getLevelProperties().getLevelName());
    }

    @Unique
    private Screen prevScreen;
    
    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;", opcode = Opcodes.PUTFIELD), method="openScreen")
    public void onOpenScreen(Screen screen, CallbackInfo info) {
        if (screen != currentScreen) {
            if (screen instanceof InventoryScreen && interactionManager.hasCreativeInventory()) {
                if (!(screen instanceof CreativeInventoryScreen)) {
                    prevScreen = currentScreen;
                }
            } else {
                prevScreen = currentScreen;
            }
            new EventOpenScreen(screen);
        }
    }

    @Inject(at = @At("TAIL"), method = "openScreen")
    public void afterOpenScreen(Screen screen, CallbackInfo info) {
        if (screen instanceof HandledScreen) {
            if (interactionManager.hasCreativeInventory() && !(screen instanceof CreativeInventoryScreen)) {
                return;
            }
            EventOpenContainer event = new EventOpenContainer(((HandledScreen<?>) screen));
            if (event.isCanceled()) {
                openScreen(prevScreen);
            }
        }
        prevScreen = null;
    }
    
    @Inject(at = @At(value = "INVOKE", target= "Lnet/minecraft/client/MinecraftClientGame;onLeaveGameSession()V"), method="disconnect(Lnet/minecraft/client/gui/screen/Screen;)V")
    public void onDisconnect(Screen s, CallbackInfo info) {
        if (s instanceof DisconnectedScreen) {
            new EventDisconnect(((MixinDisconnectedScreen) s).getReason());
        } else {
            new EventDisconnect(null);
        }
    }

    @Inject(at = @At("HEAD"), method = "run")
    private void onStart(CallbackInfo ci) {
        new EventLaunchGame(this.session.getUsername());
    }
    
}