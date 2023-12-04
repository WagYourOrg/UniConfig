package xyz.wagyourtail.uniconfig.connector.screen.connector;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import org.jetbrains.annotations.ApiStatus;
import xyz.wagyourtail.uniconfig.Setting;
import xyz.wagyourtail.uniconfig.connector.SettingConnector;
import xyz.wagyourtail.uniconfig.connector.screen.ScreenSettingConnector;

import java.util.Optional;
import java.util.function.Supplier;

public class ScreenIntSliderSettingConnector extends ScreenSettingConnector<Integer> {
    private final int min;
    private final int max;

    public ScreenIntSliderSettingConnector(Setting<Integer> access, int min, int max, Supplier<Boolean> enabled) {
        super(access, enabled);
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean isSubscreen() {
        return false;
    }

    private double toRaw(int value) {
        return (value - min) / (double) (max - min);
    }

    private int fromRaw(double sliderValue) {
        return (int) (sliderValue * (max - min) + min);
    }

    @Override
    public Optional<AbstractWidget> constructElement() {
        return Optional.of(new IntSlider());
    }

    @Override
    public SettingConnector<Integer> copyTo(Setting<Integer> item) {
        return new ScreenIntSliderSettingConnector(item, min, max, enabled);
    }

    public class IntSlider extends AbstractSliderButton {

        public IntSlider() {
            super(0, 0, 150, 20, item.getTextValue(), toRaw(item.getValue()));
            setTooltip(item.description() == null ? null : Tooltip.create(item.description()));
        }

        @Override
        protected void updateMessage() {
            this.setMessage(item.textValue.apply(fromRaw(value)));
        }

        @Override
        protected void applyValue() {
            // snap to closest int
            this.value = toRaw(fromRaw(value));
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            super.onRelease(mouseX, mouseY);
            item.setValue(fromRaw(value));
        }

    }
}
