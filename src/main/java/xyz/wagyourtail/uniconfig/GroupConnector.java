package xyz.wagyourtail.uniconfig;

public abstract class GroupConnector {

    public final Group item;

    public GroupConnector(Group item) {
        this.item = item;
    }

    public Group getItem() {
        return item;
    }
    public abstract Class<? extends GroupConnector> getConnectorClass();

}
