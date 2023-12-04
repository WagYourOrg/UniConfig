package xyz.wagyourtail.uniconfig.connector.screen.impl;

import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.uniconfig.Group;
import xyz.wagyourtail.uniconfig.connector.screen.ScreenConnector;
import xyz.wagyourtail.uniconfig.connector.screen.ScreenGroupConnector;

public abstract class AbstractElementContainerScreen extends Screen {
    @Nullable
    private final Screen parent;
    private final Group group;

    protected AbstractElementContainerScreen(@Nullable Screen parent, Group group) {
        super(group.name());
        this.parent = parent;
        this.group = group;
    }

    @Override
    protected void init() {
        super.init();
        parseGroup();
        parseSettings();
    }

    private void parseGroup() {
        for (Group group : group.children) {
            ScreenGroupConnector connector = group.getConnector(ScreenGroupConnector.class);
            if (connector == null) {
                connector = new ScreenGroupConnector(group, null, () -> true);
            }
            addGroup(connector);
        }
    }

    private void parseSettings() {
        group.configItems.values().forEach(e -> {
            ScreenConnector<?> connector = e.getConnector(ScreenConnector.class);
            if (connector != null) {
                addSetting(connector);
            }
        });
    }

    public abstract void addGroup(ScreenGroupConnector widget);

    public abstract void addSetting(ScreenConnector<?> connector);

    public abstract void openSubGroupScreen(Group group);

    @Override
    public void onClose() {
        assert minecraft != null;
        minecraft.setScreen(parent);
    }
}
