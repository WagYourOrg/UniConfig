package xyz.wagyourtail.uniconfig.connector.screen.connector;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import xyz.wagyourtail.uniconfig.Setting;
import xyz.wagyourtail.uniconfig.connector.SettingConnector;
import xyz.wagyourtail.uniconfig.connector.screen.ScreenSettingConnector;

import java.util.Optional;
import java.util.function.Supplier;

public class ScreenBooleanSettingConnector extends ScreenSettingConnector<Boolean> {


    public ScreenBooleanSettingConnector(Setting<Boolean> access, Supplier<Boolean> enabled) {
        super(access, enabled);
    }

    @Override
    public boolean isSubscreen() {
        return false;
    }

    @Override
    public Optional<AbstractWidget> constructElement() {
        Checkbox cb = new Checkbox(0, 0, 20, 20, Component.empty(), access.getValue()) {
            @Override
            public void onPress() {
                super.onPress();
                access.setValue(this.selected());
            }
        };
        cb.setTooltip(item.description() == null ? null : Tooltip.create(item.description()));
        return Optional.of(cb);
    }

    @Override
    public SettingConnector<Boolean> copyTo(Setting<Boolean> item) {
        return new ScreenBooleanSettingConnector(item, enabled);
    }

}
