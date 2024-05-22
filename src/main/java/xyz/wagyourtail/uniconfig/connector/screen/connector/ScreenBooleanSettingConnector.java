package xyz.wagyourtail.uniconfig.connector.screen.connector;

import net.minecraft.client.Minecraft;
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
        Checkbox box = Checkbox.builder(Component.empty(), Minecraft.getInstance().font)
            .pos(0, 0)
            .onValueChange((a, b) -> {
                item.setValue(b);
            })
            .tooltip(item.description() == null ? null : Tooltip.create(item.description()))
            .build();

        box.setWidth(20);
        box.setHeight(20);

        return Optional.of(box);
    }

    @Override
    public SettingConnector<Boolean> copyTo(Setting<Boolean> item) {
        return new ScreenBooleanSettingConnector(item, enabled);
    }

}
