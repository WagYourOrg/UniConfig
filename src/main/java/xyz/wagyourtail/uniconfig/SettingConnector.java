package xyz.wagyourtail.uniconfig;
public abstract class SettingConnector<T> {

    public final Setting<T> item;

    public SettingConnector(Setting<T> item) {
        this.item = item;
    }

    public abstract Class<? extends SettingConnector<?>> getConnectorClass();

}
