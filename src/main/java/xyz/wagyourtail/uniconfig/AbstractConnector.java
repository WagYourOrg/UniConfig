package xyz.wagyourtail.uniconfig;

public abstract class AbstractConnector<T> {
    public final ConfigSetting<T, ?> item;

    public AbstractConnector(ConfigSetting<T, ?> item) {
        this.item = item;
    }

    public abstract Class<? extends AbstractConnector<T>> getConnectorClass();

}
