package xyz.wagyourtail.uniconfig.connector.screen;

import xyz.wagyourtail.uniconfig.Group;
import xyz.wagyourtail.uniconfig.Setting;
import xyz.wagyourtail.uniconfig.connector.screen.connector.ScreenBooleanWrapper;
import xyz.wagyourtail.uniconfig.connector.screen.connector.ScreenDoubleSliderWrapper;
import xyz.wagyourtail.uniconfig.connector.screen.connector.ScreenIntSliderWrapper;

import java.util.function.Supplier;

public final class Screen {

    private Screen() {
    }

    /* group connectors */

    public static void group(Group group) {
        group.connector(new ScreenGroupConnector(group, null, () -> true));
    }

    public static void group(Group group, Supplier<Boolean> isEnabled) {
        group.connector(new ScreenGroupConnector(group, null, isEnabled));
    }

    public static void group(Group group, Setting<?> setting) {
        group.connector(new ScreenGroupConnector(group, setting, () -> true));
    }

    public static void group(Group group, Setting<?> setting, Supplier<Boolean> isEnabled) {
        group.connector(new ScreenGroupConnector(group, setting, isEnabled));
    }

    /* setting connectors */

    public static void bool(Setting<Boolean> setting) {
        setting.connector(new ScreenBooleanWrapper(setting, () -> true));
    }

    public static void bool(Setting<Boolean> setting, Supplier<Boolean> isEnabled) {
        setting.connector(new ScreenBooleanWrapper(setting, isEnabled));
    }

    public static void intSlider(Setting<Integer> setting, int min, int max) {
        setting.connector(new ScreenIntSliderWrapper(setting, min, max, () -> true));
    }

    public static void intSlider(Setting<Integer> setting, int min, int max, Supplier<Boolean> isEnabled) {
        setting.connector(new ScreenIntSliderWrapper(setting, min, max, isEnabled));
    }

    public static void doubleSlider(Setting<Double> setting, int min, int max) {
        setting.connector(new ScreenDoubleSliderWrapper(setting, min, max, () -> true));
    }

    public static void doubleSlider(Setting<Double> setting, int min, int max, Supplier<Boolean> isEnabled) {
        setting.connector(new ScreenDoubleSliderWrapper(setting, min, max, isEnabled));
    }



}
