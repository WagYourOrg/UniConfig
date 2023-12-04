package xyz.wagyourtail.uniconfig.connector.screen;

import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.uniconfig.Group;
import xyz.wagyourtail.uniconfig.GroupConnector;
import xyz.wagyourtail.uniconfig.Setting;

import java.util.function.Supplier;

public class ScreenGroupConnector extends GroupConnector {
    @Nullable
    private final Setting<?> attachedSetting;

    private final Supplier<Boolean> enabled;


    public ScreenGroupConnector(Group item, @Nullable Setting<?> attachedSetting, Supplier<Boolean> enabled) {
        super(item);
        if (attachedSetting != null) {
            if (!attachedSetting.group.children.contains(item)) {
                throw new IllegalArgumentException("attachedSetting must be in the same group as the group being attached to");
            }
            if (attachedSetting.getConnector(ScreenConnector.class) == null) {
                throw new IllegalArgumentException("attachedSetting must have a ScreenConnector");
            }
        }
        this.attachedSetting = attachedSetting;
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
    public Class<? extends GroupConnector> getConnectorClass() {
        return ScreenGroupConnector.class;
    }
}
