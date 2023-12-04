package xyz.wagyourtail.uniconfig.connector.screen.connector;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import xyz.wagyourtail.uniconfig.Setting;
import xyz.wagyourtail.uniconfig.connector.screen.ScreenConnector;

import java.util.Optional;
import java.util.function.Supplier;

public class ScreenDoubleSliderWrapper extends ScreenConnector<Double> {
    private final double min;
    private final double max;

    public ScreenDoubleSliderWrapper(Setting<Double> access, int min, int max,  Supplier<Boolean> enabled) {
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
        return Optional.of(new AbstractSliderButton(0, 0, 150, 20, access.getTextValue(), toRaw(access.getValue())) {

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

        });
    }
}
