package xyz.wagyourtail.uniconfig.connector.screen.impl;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.uniconfig.Group;
import xyz.wagyourtail.uniconfig.Setting;
import xyz.wagyourtail.uniconfig.connector.screen.ScreenSettingConnector;
import xyz.wagyourtail.uniconfig.connector.screen.ScreenGroupConnector;

import java.util.*;

public class ScrollScreen extends AbstractElementContainerScreen {
    private static final int SPACING = 30;
    private ScrollWidget scrollWidget;

    @Nullable
    private Separator separator;

    private final Map<ScreenGroupConnector, AbstractWidget> groups = new LinkedHashMap<>();
    private final Map<ScreenSettingConnector<?>, AbstractWidget> attachedGroups = new HashMap<>();

    private final Map<ScreenSettingConnector<?>, Pair<Label, AbstractWidget>> settings = new LinkedHashMap<>();


    public ScrollScreen(@Nullable Screen parent, Group group, boolean allowCancel) {
        super(parent, group, allowCancel);
    }

    @Override
    protected void init() {
        groups.clear();
        attachedGroups.clear();
        settings.clear();
        super.init();
        scrollWidget = new ScrollWidget(8, 20, this.width - 24, this.height - 80, Component.empty(), this::onScroll, (guiGraphics, mouseX, mouseY, partialTick) -> {
            super.render(guiGraphics, mouseX, mouseY, partialTick);
        });
        this.addWidget(scrollWidget);
        if (groups.keySet().stream().anyMatch(e -> e.getAttachedSetting() == null)) {
            separator = new Separator(0, 5, 375, 2, 0xFF6F6F6F);
            this.addRenderableOnly(separator);
        } else {
            separator = null;
        }
        onScroll(0);
    }

    public void onScroll(double current) {
        // place on screen
        int labelPosX = this.width / 2;
        int widgetPosX = this.width / 2 + 25;
        // place buttons on page
        int i = 0;
        boolean j = false;
        for (Map.Entry<ScreenGroupConnector, AbstractWidget> entry : groups.entrySet()) {
            if (entry.getKey().getAttachedSetting() != null) continue;
            if (j) {
                entry.getValue().setPosition(this.width / 2 + 5, (int) (20 + SPACING / 2 + i++ * SPACING - current));
                j = false;
            } else {
                entry.getValue().setPosition(this.width / 2 - 155, (int) (20 + SPACING / 2 + i * SPACING - current));
                j = true;
            }
            // set active based on location
        }
        if (j) i++;
        // place separator
        if (separator != null) {
            separator.setPosition(this.width / 2 - separator.width / 2, (int) (20 + SPACING / 2 + i++ * SPACING - current));
        }
        // place settings on page
        for (Map.Entry<ScreenSettingConnector<?>, Pair<Label, AbstractWidget>> entry : settings.entrySet()) {
            entry.getValue().first().setPosition(labelPosX - font.width(entry.getValue().first().text), (int) (20 + SPACING / 2 + i * SPACING - current) + entry.getValue().second().getHeight() / 2 - font.lineHeight / 2);
            entry.getValue().second().setPosition(widgetPosX + (150 - entry.getValue().second().getWidth()), (int) (20 + SPACING / 2 + i++ * SPACING - current));
            AbstractWidget group = attachedGroups.get(entry.getKey());
            if (group != null) {
                group.setPosition(widgetPosX - 40, (int) (20 + SPACING / 2 + i * SPACING - current));
            }
        }
        scrollWidget.setInnerHeight(i * SPACING + SPACING / 2);
    }

    @Override
    public void tick() {
        super.tick();
        for (Map.Entry<ScreenGroupConnector, AbstractWidget> entry : groups.entrySet()) {
            // active only if on screen and enabled
            entry.getValue().active = entry.getKey().isEnabled() && entry.getValue().getY() + entry.getValue().getHeight() > 20 && entry.getValue().getY() < this.height - 60;
        }
        for (Map.Entry<ScreenSettingConnector<?>, Pair<Label, AbstractWidget>> entry : settings.entrySet()) {
            // active only if on screen and enabled
            entry.getValue().second().active = entry.getKey().enabled.get() && entry.getValue().second().getY() + entry.getValue().second().getHeight() > 20 && entry.getValue().second().getY() < this.height - 60;
        }
    }

    @Override
    public void addGroup(ScreenGroupConnector group) {
        Setting<?> attachedSetting = group.getAttachedSetting();
        if (attachedSetting != null) {
            // construct as cog icon
            Button b = Button.builder(Component.literal("⚙"), (button) -> this.openSubGroupScreen(group.getItem())).size(20, 20).tooltip(group.getItem().description() == null ? null : Tooltip.create(group.getItem().description())).build();
            this.addRenderableWidget(b);
            groups.put(group, b);
            attachedGroups.put(attachedSetting.getConnector(ScreenSettingConnector.class), b);
        } else {
            // construct as text
            Button b = Button.builder(group.getItem().name(), (button) -> this.openSubGroupScreen(group.getItem())).size(150, 20).tooltip(group.getItem().description() == null ? null : Tooltip.create(group.getItem().description())).build();
            this.addRenderableWidget(b);
            groups.put(group, b);
        }
    }

    @Override
    public void addSetting(ScreenSettingConnector<?> setting) {
        Label l = new Label(this.font, setting.item.name(), 0, 0, 0xFFFFFF);
        this.addRenderableOnly(l);
        if (setting.isSubscreen()) {
            Button b = Button.builder(Component.translatable("uniconfig.screen.subscreen"), (button) -> {
                assert this.minecraft != null;
                this.minecraft.setScreen(setting.constructSubscreen(Optional.of(this)).orElseThrow(() -> new RuntimeException("failed to construct subscreen for " + setting.item.nameKey())));
            }).size(20, 20).tooltip(setting.item.description() == null ? null : Tooltip.create(setting.item.description())).build();
            this.addRenderableWidget(b);
            settings.put(setting, Pair.of(l, b));
        } else {
            AbstractWidget w = setting.constructElement().orElseThrow(() -> new RuntimeException("failed to construct element for " + setting.item.nameKey()));
            this.addRenderableWidget(w);
            settings.put(setting, Pair.of(l, w));
        }
    }

    @Override
    public void openSubGroupScreen(Group group) {
        assert this.minecraft != null;
        this.minecraft.setScreen(new ScrollScreen(this, group, this.allowCancel()));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return scrollWidget.mouseScrolled(mouseX, mouseY, scrollX, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 5, 0xFFFFFF);
        this.scrollWidget.render(guiGraphics, mouseX, mouseY, partialTick);
        bottomButtons.visitWidgets(e -> e.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    private static class Separator implements Renderable {

        private int x;
        private int y;
        private int width;
        private int height;
        private final int color;

        public Separator(int x, int y, int width, int height, int color) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
        }

        public void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void setSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.fill(x, y, x + width, y + height, color);
        }
    }

    private static class Label implements Renderable {
        private final Font font;
        private final Component text;
        private int x;
        private int y;
        private final int color;

        public Label(Font font, Component text, int x, int y, int color) {
            this.font = font;
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
        }

        public void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.drawString(font, text, x, y, color);
        }
    }

}
