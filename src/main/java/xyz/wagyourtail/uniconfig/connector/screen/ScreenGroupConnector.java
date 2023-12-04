package xyz.wagyourtail.uniconfig.connector.screen;

import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.uniconfig.Group;
import xyz.wagyourtail.uniconfig.connector.GroupConnector;
import xyz.wagyourtail.uniconfig.Setting;

import java.util.function.Supplier;

public class ScreenGroupConnector extends GroupConnector {

    private final Supplier<Boolean> enabled;


    public ScreenGroupConnector(Group item, @Nullable Setting<?> attachedSetting, Supplier<Boolean> enabled) {
        super(item, attachedSetting);
        if (attachedSetting != null) {
            if (!attachedSetting.group.children.contains(item)) {
                throw new IllegalArgumentException("attachedSetting must be in the same group as the group being attached to");
            }
            if (attachedSetting.getConnector(ScreenSettingConnector.class) == null) {
                throw new IllegalArgumentException("attachedSetting must have a ScreenConnector");
            }
        }
        this.enabled = enabled;
    }

    @Nullable
    public Setting<?> getAttachedSetting() {
        return attachedSetting;
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    @Override
    public GroupConnector copyTo(Group item, @Nullable Setting<?> attachedSetting) {
        return new ScreenGroupConnector(item, attachedSetting, enabled);
    }

    @Override
    public Class<? extends GroupConnector> getConnectorClass() {
        return ScreenGroupConnector.class;
    }
}
