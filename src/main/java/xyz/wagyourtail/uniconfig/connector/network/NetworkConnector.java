package xyz.wagyourtail.uniconfig.connector.network;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.uniconfig.Setting;
import xyz.wagyourtail.uniconfig.UniConfig;

public class NetworkConnector {



    private class NetworkConfig extends UniConfig {

        public NetworkConfig(@NotNull String name) {
            super(name);
        }

        @Override
        public void save(@Nullable Setting<?> item) {
            if (item == null) {

            } else {

            }
        }
    }

}
