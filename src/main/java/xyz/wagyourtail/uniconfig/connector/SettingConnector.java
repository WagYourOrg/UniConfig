package xyz.wagyourtail.uniconfig.connector;

import xyz.wagyourtail.uniconfig.Setting;

public abstract class SettingConnector<T> {

    protected final Setting<T> item;

    public SettingConnector(Setting<T> item) {
        this.item = item;
    }

    public Setting<T> getItem() {
        return item;
    }
    public abstract SettingConnector<T> copyTo(Setting<T> item);
    public abstract Class<? extends SettingConnector<?>> getConnectorClass();

}
