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

public class ScreenDoubleSliderSettingConnector extends ScreenSettingConnector<Double> {
    private final double min;
    private final double max;

    public ScreenDoubleSliderSettingConnector(Setting<Double> access, double min, double max, Supplier<Boolean> enabled) {
        super(access, enabled);
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean isSubscreen() {
        return false;
    }

    private double toRaw(double value) {
        return (value - min) / (max - min);
    }

    private double fromRaw(double sliderValue) {
        return sliderValue * (max - min) + min;
    }

    @Override
    public Optional<AbstractWidget> constructElement() {
        return Optional.of(new DoubleSlider());
    }

    @Override
    public SettingConnector<Double> copyTo(Setting<Double> item) {
        return new ScreenDoubleSliderSettingConnector(item, min, max, enabled);
    }

    public class DoubleSlider extends AbstractSliderButton {
        public DoubleSlider() {
            super(0, 0, 150, 20, item.getTextValue(), toRaw(item.getValue()));
            setTooltip(item.description() == null ? null : Tooltip.create(item.description()));
        }

        @Override
        protected void updateMessage() {
            this.setMessage(item.textValue.apply(fromRaw(value)));
        }

        @Override
        protected void applyValue() {
            // snap to closest double
    //                this.value = toRaw(fromRaw(value));
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            super.onRelease(mouseX, mouseY);
            item.setValue(fromRaw(value));
        }

    }
}
