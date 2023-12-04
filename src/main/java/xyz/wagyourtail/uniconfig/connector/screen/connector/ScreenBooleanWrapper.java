package xyz.wagyourtail.uniconfig.connector.screen.connector;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;
import xyz.wagyourtail.uniconfig.Setting;
import xyz.wagyourtail.uniconfig.connector.screen.ScreenConnector;

import java.util.Optional;
import java.util.function.Supplier;

public class ScreenBooleanWrapper extends ScreenConnector<Boolean> {


    public ScreenBooleanWrapper(Setting<Boolean> access, Supplier<Boolean> enabled) {
        super(access, enabled);
    }

    @Override
    public boolean isSubscreen() {
        return false;
    }

    @Override
    public Optional<AbstractWidget> constructElement() {
        return Optional.of(new Checkbox(0, 0, 20, 20, Component.empty(), access.getValue()) {

            @Override
            public void onPress() {
                super.onPress();
                access.setValue(this.selected());
            }
        });
    }

}
