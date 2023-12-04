package xyz.wagyourtail.uniconfig;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.ServiceLoader;

public interface PlatformMethods {
    PlatformMethods INSTANCE = ServiceLoader.load(PlatformMethods.class).iterator().next();

    void sendFeedback(SharedSuggestionProvider suggestionProvider, Component component, boolean broadcast);

    void sendFailure(SharedSuggestionProvider suggestionProvider, Component component);

}
