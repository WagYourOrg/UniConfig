package xyz.wagyourtail.uniconfig.connector.screen.impl;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.EqualSpacingLayout;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.uniconfig.Group;
import xyz.wagyourtail.uniconfig.UniConfig;
import xyz.wagyourtail.uniconfig.connector.screen.ScreenSettingConnector;
import xyz.wagyourtail.uniconfig.connector.screen.ScreenGroupConnector;

public abstract class AbstractElementContainerScreen extends Screen {
    @Nullable
    private final Screen parent;
    private final Group group;

    private final Group groupCopy;

    @SuppressWarnings("NotNullFieldNotInitialized")
    protected LinearLayout bottomButtons;

    protected AbstractElementContainerScreen(@Nullable Screen parent, Group group, boolean allowCancel) {
        super(group.name());
        this.parent = parent;
        this.group = group;
        if (allowCancel) {
            UniConfig config = new UniConfig(group.name);
            this.groupCopy = config;
            group.copyTo(config);
        } else {
            this.groupCopy = group;
        }
    }

    @Override
    protected void init() {
        super.init();
        bottomButtons = LinearLayout.vertical();
        EqualSpacingLayout line1 = bottomButtons.addChild(new EqualSpacingLayout(308, 20, EqualSpacingLayout.Orientation.HORIZONTAL));
        line1.addChild(this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), btn -> {
            save();
            onClose();
        }).width(100).build()));
        if (this.groupCopy != group) {
            line1.addChild(this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), btn -> onClose()).width(100).build()));
        }
        bottomButtons.arrangeElements();
        FrameLayout.centerInRectangle(bottomButtons, 0, this.height - 40, this.width, 40);
        parseGroup();
        parseSettings();
    }

    protected boolean allowCancel() {
        return this.groupCopy != this.group;
    }

    private void parseGroup() {
        for (Group group : groupCopy.children) {
            ScreenGroupConnector connector = group.getConnector(ScreenGroupConnector.class);
            if (connector == null) {
                connector = new ScreenGroupConnector(group, null, () -> true);
            }
            addGroup(connector);
        }
    }

    private void parseSettings() {
        groupCopy.configItems.values().forEach(e -> {
            ScreenSettingConnector<?> connector = e.getConnector(ScreenSettingConnector.class);
            if (connector != null) {
                addSetting(connector);
            }
        });
    }

    public abstract void addGroup(ScreenGroupConnector widget);

    public abstract void addSetting(ScreenSettingConnector<?> connector);

    public abstract void openSubGroupScreen(Group group);

    @Override
    public void onClose() {
        assert minecraft != null;
        minecraft.setScreen(parent);
    }

    public void save() {
        if (groupCopy != group) {
            group.readFrom(groupCopy);
        }
    }
}
