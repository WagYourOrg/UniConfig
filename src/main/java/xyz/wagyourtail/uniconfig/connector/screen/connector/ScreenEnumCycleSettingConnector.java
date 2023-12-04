package xyz.wagyourtail.uniconfig.connector.screen.connector;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import xyz.wagyourtail.uniconfig.Setting;
import xyz.wagyourtail.uniconfig.connector.SettingConnector;
import xyz.wagyourtail.uniconfig.connector.screen.ScreenSettingConnector;

import java.util.Optional;
import java.util.function.Supplier;

public class ScreenEnumCycleSettingConnector<E extends Enum<E>> extends ScreenSettingConnector<E> {


    public ScreenEnumCycleSettingConnector(Setting<E> access, Supplier<Boolean> enabled) {
        super(access, enabled);
    }

    @Override
    public boolean isSubscreen() {
        return false;
    }

    @Override
    public Optional<AbstractWidget> constructElement() {
        return Optional.of(new CycleButton());
    }

    @Override
    public SettingConnector<E> copyTo(Setting<E> item) {
        return new ScreenEnumCycleSettingConnector<>(item, enabled);
    }

    private class CycleButton extends AbstractButton {
        public CycleButton() {
            super(0, 0, 150, 20, item.getTextValue());
            setTooltip(item.description() == null ? null : Tooltip.create(item.description()));
        }

        @Override
        public void onPress() {
            // get next enum
            E[] values = item.getValue().getDeclaringClass().getEnumConstants();
            int index = item.getValue().ordinal();
            index++;
            if (index >= values.length) index = 0;
            item.setValue(values[index]);
            this.setMessage(item.getTextValue());
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }
    }
}
