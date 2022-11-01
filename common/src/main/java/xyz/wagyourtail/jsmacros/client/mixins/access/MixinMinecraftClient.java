package xyz.wagyourtail.jsmacros.client.mixins.access;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.wagyourtail.jsmacros.client.access.IMinecraftClient;
import xyz.wagyourtail.jsmacros.client.api.classes.render.Draw2D;
import xyz.wagyourtail.jsmacros.client.api.library.impl.FHud;
import xyz.wagyourtail.jsmacros.client.api.classes.render.IDraw2D;
import xyz.wagyourtail.jsmacros.client.api.classes.render.IScreen;
import xyz.wagyourtail.jsmacros.core.Core;

import java.util.function.Consumer;

@Mixin(MinecraftClient.class)
abstract
class MixinMinecraftClient implements IMinecraftClient {

    @Shadow protected abstract void doUse();

    @Shadow protected abstract void doAttack();

    @Shadow public Screen currentScreen;

    @Inject(at = @At("TAIL"), method = "method_2923")
    public void onResolutionChanged(CallbackInfo info) {

        synchronized (FHud.overlays) {
            for (IDraw2D<Draw2D> h : FHud.overlays) {
                try {
                    ((Draw2D) h).init();
                } catch (Throwable ignored) {}
            }
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;removed()V"), method="openScreen")
    public void onCloseScreen(Screen screen, CallbackInfo ci) {
        Consumer<IScreen> onClose = ((IScreen)currentScreen).getOnClose();
        try {
            if (onClose != null) onClose.accept((IScreen) currentScreen);
        } catch (Throwable e) {
            Core.getInstance().profile.logError(e);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;removed()V", shift = At.Shift.AFTER), method = "openScreen")
    public void onGuiClose(Screen screen, CallbackInfo ci) {
        if (currentScreen != null && ((IScreen) currentScreen).getOnClose() != null) {
            try {
                ((IScreen) currentScreen).getOnClose().accept((IScreen) currentScreen);
            } catch (Throwable e) {
                Core.getInstance().profile.logError(e);
            }
        }
    }

    @Override
    public void jsmacros_doItemUse() {
        doUse();
    }

    @Override
    public void jsmacros_doAttack() {
        doAttack();
    }


}
