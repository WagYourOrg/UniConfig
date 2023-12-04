package xyz.wagyourtail.uniconfig.connector.screen.connector;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import xyz.wagyourtail.uniconfig.Setting;
import xyz.wagyourtail.uniconfig.connector.screen.ScreenConnector;

import java.util.Optional;
import java.util.function.Supplier;

public class ScreenIntSliderWrapper extends ScreenConnector<Integer> {
    private final int min;
    private final int max;

    public ScreenIntSliderWrapper(Setting<Integer> access, int min, int max, Supplier<Boolean> enabled) {
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

    public int fromRaw(double sliderValue) {
        return (int) (sliderValue * (max - min) + min);
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
                // snap to closest int
                this.value = toRaw(fromRaw(value));
            }

            @Override
            public void onRelease(double mouseX, double mouseY) {
                super.onRelease(mouseX, mouseY);
                access.setValue(fromRaw(value));
            }

        });
    }
}
