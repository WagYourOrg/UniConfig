package xyz.wagyourtail.uniconfig.connector.screen;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import xyz.wagyourtail.uniconfig.SettingConnector;
import xyz.wagyourtail.uniconfig.Setting;

import java.util.Optional;
import java.util.function.Supplier;

public abstract class ScreenConnector<T> extends SettingConnector<T> {

    protected final Setting<T> access;
    public Supplier<Boolean> enabled;

    public ScreenConnector(Setting<T> access, Supplier<Boolean> enabled) {
        super(access);
        this.access = access;
        this.enabled = enabled;
    }

    public abstract boolean isSubscreen();

    // optionals so that it doesn't die on server-side
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public Optional<Screen> constructSubscreen(Optional<Screen> parent) {
        return Optional.empty();
    }

    public Optional<AbstractWidget> constructElement() {
        return Optional.empty();
    }

    @Override
    public Class<? extends SettingConnector<?>> getConnectorClass() {
        return (Class) ScreenConnector.class;
    }
}
