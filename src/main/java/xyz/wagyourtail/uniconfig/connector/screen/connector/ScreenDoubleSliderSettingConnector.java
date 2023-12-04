package xyz.wagyourtail.uniconfig.connector.screen.connector;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
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

    public double fromRaw(double sliderValue) {
        return sliderValue * (max - min) + min;
    }

    @Override
    public Optional<AbstractWidget> constructElement() {
        AbstractSliderButton asb = new AbstractSliderButton(0, 0, 150, 20, access.getTextValue(), toRaw(access.getValue())) {

            @Override
            protected void updateMessage() {
                this.setMessage(access.textValue.apply(fromRaw(value)));
            }

            @Override
            protected void applyValue() {
                // snap to closest double
//                this.value = toRaw(fromRaw(value));
            }

            @Override
            public void onRelease(double mouseX, double mouseY) {
                super.onRelease(mouseX, mouseY);
                access.setValue(fromRaw(value));
            }

        };
        asb.setTooltip(item.description() == null ? null : Tooltip.create(item.description()));
        return Optional.of(asb);
    }

    @Override
    public SettingConnector<Double> copyTo(Setting<Double> item) {
        return new ScreenDoubleSliderSettingConnector(item, min, max, enabled);
    }
}
