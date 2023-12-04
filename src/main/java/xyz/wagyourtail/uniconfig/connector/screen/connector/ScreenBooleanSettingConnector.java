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
        return Optional.of(new Checkbox());
    }

    @Override
    public SettingConnector<Boolean> copyTo(Setting<Boolean> item) {
        return new ScreenBooleanSettingConnector(item, enabled);
    }

    private class Checkbox extends net.minecraft.client.gui.components.Checkbox {
        public Checkbox() {
            super(0, 0, 20, 20, Component.empty(), item.getValue());
            setTooltip(item.description() == null ? null : Tooltip.create(item.description()));
        }

        @Override
        public void onPress() {
            super.onPress();
            item.setValue(this.selected());
        }
    }
}
