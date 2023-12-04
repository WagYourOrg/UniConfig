package xyz.wagyourtail.uniconfig.connector.screen.connector;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
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

    public int fromRaw(double sliderValue) {
        return (int) (sliderValue * (max - min) + min);
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
                // snap to closest int
                this.value = toRaw(fromRaw(value));
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
    public SettingConnector<Integer> copyTo(Setting<Integer> item) {
        return new ScreenIntSliderSettingConnector(item, min, max, enabled);
    }
}
