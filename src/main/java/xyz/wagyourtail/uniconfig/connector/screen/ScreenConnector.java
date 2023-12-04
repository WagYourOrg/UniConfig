package xyz.wagyourtail.uniconfig.connector.screen;

import xyz.wagyourtail.uniconfig.Group;
import xyz.wagyourtail.uniconfig.Setting;
import xyz.wagyourtail.uniconfig.connector.screen.connector.ScreenBooleanSettingConnector;
import xyz.wagyourtail.uniconfig.connector.screen.connector.ScreenDoubleSliderSettingConnector;
import xyz.wagyourtail.uniconfig.connector.screen.connector.ScreenEnumCycleSettingConnector;
import xyz.wagyourtail.uniconfig.connector.screen.connector.ScreenIntSliderSettingConnector;

import java.util.function.Supplier;

public final class ScreenConnector {

    private ScreenConnector() {
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
        setting.connector(new ScreenBooleanSettingConnector(setting, () -> true));
    }

    public static void bool(Setting<Boolean> setting, Supplier<Boolean> isEnabled) {
        setting.connector(new ScreenBooleanSettingConnector(setting, isEnabled));
    }

    public static void intSlider(Setting<Integer> setting, int min, int max) {
        setting.connector(new ScreenIntSliderSettingConnector(setting, min, max, () -> true));
    }

    public static void intSlider(Setting<Integer> setting, int min, int max, Supplier<Boolean> isEnabled) {
        setting.connector(new ScreenIntSliderSettingConnector(setting, min, max, isEnabled));
    }

    public static void doubleSlider(Setting<Double> setting, int min, int max) {
        setting.connector(new ScreenDoubleSliderSettingConnector(setting, min, max, () -> true));
    }

    public static void doubleSlider(Setting<Double> setting, int min, int max, Supplier<Boolean> isEnabled) {
        setting.connector(new ScreenDoubleSliderSettingConnector(setting, min, max, isEnabled));
    }

    public static <E extends Enum<E>> void enumCycle(Setting<E> setting) {
        setting.connector(new ScreenEnumCycleSettingConnector<>(setting, () -> true));
    }

    public static <E extends Enum<E>> void enumCycle(Setting<E> setting, Supplier<Boolean> isEnabled) {
        setting.connector(new ScreenEnumCycleSettingConnector<>(setting, isEnabled));
    }

}
