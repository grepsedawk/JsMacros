package xyz.wagyourtail.jsmacros.client.api.classes.render;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import xyz.wagyourtail.jsmacros.client.JsMacros;
import xyz.wagyourtail.jsmacros.client.access.IScreenInternal;
import xyz.wagyourtail.jsmacros.client.api.classes.math.Pos3D;
import xyz.wagyourtail.jsmacros.core.Core;
import xyz.wagyourtail.jsmacros.core.MethodWrapper;
import xyz.wagyourtail.wagyourgui.BaseScreen;

/**
 * just go look at {@link IScreen IScreen}
 * since all the methods are done through a mixin...
 * 
 * @author Wagyourtail
 * 
 * @since 1.0.5
 * 
 * @see IScreen
 */
public class ScriptScreen extends BaseScreen {
    public boolean drawTitle;
    /**
     * @since 1.8.4
     * WARNING: this can break the game if you set it false and don't have a way to close the screen.
     */
    public boolean shouldCloseOnEsc = true;
    /**
     * @since 1.8.4
     */
    public boolean shouldPause = true;
    private final int bgStyle;
    private MethodWrapper<Pos3D, MatrixStack, Object, ?> onRender;
    
    public ScriptScreen(String title, boolean dirt) {
        super(Text.literal(title), null);
        this.bgStyle = dirt ? 0 : 1;
        this.drawTitle = true;
    }

    @Override
    protected void init() {
        BaseScreen prev = JsMacros.prevScreen;
        super.init();
        JsMacros.prevScreen = prev;
    }

    /**
     * @param parent parent screen to go to when this one exits.
     * @since 1.4.0
     */
    public void setParent(IScreen parent) {
        this.parent = (net.minecraft.client.gui.screen.Screen) parent;
    }

    /**
     * add custom stuff to the render function on the main thread.
     *
     * @param onRender pos3d elements are mousex, mousey, tickDelta
     * @since 1.4.0
     */
    public void setOnRender(MethodWrapper<Pos3D, MatrixStack, Object, ?> onRender) {
        this.onRender = onRender;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (matrices == null) return;
        if (bgStyle == 0) this.renderBackgroundTexture(0);
        else if (bgStyle == 1) this.renderBackground(matrices, 0);

        if (drawTitle) {
            drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        }

        super.render(matrices, mouseX, mouseY, delta);

        for (Element button : ImmutableList.copyOf(this.children())) {
            if (!(button instanceof Drawable)) continue;
            ((Drawable) button).render(matrices, mouseX, mouseY, delta);
        }

        ((IScreenInternal) this).jsmacros_render(matrices, mouseX, mouseY, delta);
        try {
            if (onRender != null) onRender.accept(new Pos3D(mouseX, mouseY, delta), matrices);
        } catch (Throwable e) {
            Core.getInstance().profile.logError(e);
            onRender = null;
        }
    }

    @Override
    public void close() {
        openParent();
    }

    @Override
    public boolean shouldPause() {
        return shouldPause;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return shouldCloseOnEsc && super.shouldCloseOnEsc();
    }
}
