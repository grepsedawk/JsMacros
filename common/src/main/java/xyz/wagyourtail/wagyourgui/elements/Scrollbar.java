package xyz.wagyourtail.wagyourgui.elements;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;

import java.util.function.Consumer;

public class Scrollbar extends ButtonWidget {
    protected double scrollPages = 1;
    protected double scrollAmount = 0;
    protected double scrollbarHeight;
    protected double scrollDistance;
    protected int color;
    protected int borderColor;
    protected int hilightColor;
    protected Consumer<Double> onChange;

    public Scrollbar(int x, int y, int width, int height, int color, int borderColor, int hilightColor, double scrollPages, Consumer<Double> onChange) {
        super(1, x, y, width, height, "");
        this.color = color;
        this.borderColor = borderColor;
        this.hilightColor = hilightColor;
        this.onChange = onChange;
        this.setScrollPages(scrollPages);
    }
    
    public Scrollbar setPos(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.scrollbarHeight = (height - 2) / (scrollPages + 1);
        double oldDistance = this.scrollDistance;
        this.scrollDistance = Math.max(height - 2 - this.scrollbarHeight, 1);
        this.scrollAmount = this.scrollAmount / oldDistance * scrollDistance;
        return this;
    }

    public void setScrollPages(double scrollPages) {
        this.scrollPages = Math.max(scrollPages - 1, 0);
        this.scrollbarHeight = (int) Math.ceil((height - 2) / Math.max(1, scrollPages));
        this.scrollDistance = Math.max(height - 2 - this.scrollbarHeight, 1);
        if (scrollPages < 1) {
            scrollAmount = 0;
            onChange();
            this.active = false;
            this.visible = false;
        } else {
            this.active = true;
            this.visible = true;
        }
    }
    
    public void scrollToPercent(double percent) {
        scrollAmount = scrollDistance * percent;
        onChange();
    }

    int prevY = 0;

    @Override
    public boolean isMouseOver(MinecraftClient mc, int mouseX, int mouseY) {
        if (this.active && super.isMouseOver(mc, mouseX, mouseY)) {
            prevY = mouseY;
            double mpos = mouseY - y - 1;
            if (mpos < scrollAmount) {
                scrollAmount = Math.max(mpos - (scrollbarHeight / 2), 0);
                onChange();
            }
            if (mpos > (scrollAmount + scrollbarHeight)) {
                scrollAmount = Math.min(mpos - (scrollbarHeight / 2), scrollDistance);
                onChange();
            }
            return true;
        }
        return false;
    }
    
    public void onChange() {
        if (onChange != null) onChange.accept(scrollPages * scrollAmount / scrollDistance);
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        scrollAmount += deltaY;
        if (scrollAmount > scrollDistance) scrollAmount = scrollDistance;
        if (scrollAmount < 0) scrollAmount = 0;
        onChange();
    }


    @Override
    public void method_891(MinecraftClient mc, int mouseX, int mouseY, float delta) {
        if (this.visible) {
            // mainpart
            fill(x + 1, (int) (y + 1 + scrollAmount), x + width - 1, (int) (y + 1 + scrollAmount + scrollbarHeight), hilightColor);

            // outline and back
            fill(x + 1, y + 1, x + width - 1, y + height - 1, color);
            fill(x, y, x + 1, y + height, borderColor);
            fill(x + width - 1, y, x + width, y + height, borderColor);
            fill(x + 1, y, x + width - 1, y + 1, borderColor);
            fill(x + 1, y + height - 1, x + width - 1, y + height, borderColor);
        }
    }
    
}
