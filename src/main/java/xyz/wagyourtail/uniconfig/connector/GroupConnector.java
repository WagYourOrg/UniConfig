package xyz.wagyourtail.uniconfig.connector;

import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.uniconfig.Group;
import xyz.wagyourtail.uniconfig.Setting;

public abstract class GroupConnector {

    protected final Group item;
    @Nullable
    protected final Setting<?> attachedSetting;

    public GroupConnector(Group item, @Nullable Setting<?> attachedSetting) {
        this.item = item;
        this.attachedSetting = attachedSetting;
    }

    public Group getItem() {
        return item;
    }

    @Nullable
    public Setting<?> getAttachedSetting() {
        return attachedSetting;
    }

    public abstract GroupConnector copyTo(Group item, @Nullable Setting<?> attachedSetting);

    public abstract Class<? extends GroupConnector> getConnectorClass();

}
