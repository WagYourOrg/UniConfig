package xyz.wagyourtail.uniconfig.connector.screen.impl;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class ScrollWidget extends AbstractScrollWidget {
    private static final WidgetSprites BACKGROUND_SPRITES = new WidgetSprites(new ResourceLocation("widget/text_field"), new ResourceLocation("widget/text_field_highlighted"));
    private final Renderable content;
    private final Consumer<Double> onScroll;
    private int innerHeight;

    public ScrollWidget(int i, int j, int k, int l, Component arg, Consumer<Double> onScroll, Renderable content) {
        super(i, j, k, l, arg);
        this.content = content;
        this.onScroll = onScroll;
    }

    @Override
    protected void setScrollAmount(double scrollAmount) {
        super.setScrollAmount(scrollAmount);
        onScroll.accept(this.scrollAmount());
    }

    @Override
    public int getInnerHeight() {
        return innerHeight;
    }

    public void setInnerHeight(int innerHeight) {
        this.innerHeight = innerHeight;
    }

    @Override
    protected double scrollRate() {
        return 9;
    }

    @Override
    protected void renderBorder(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        ResourceLocation lv = BACKGROUND_SPRITES.get(this.isActive(), false);
        guiGraphics.blitSprite(lv, x, y, width, height);
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.pose().translate(0.0, this.scrollAmount(), 0.0);
        content.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
