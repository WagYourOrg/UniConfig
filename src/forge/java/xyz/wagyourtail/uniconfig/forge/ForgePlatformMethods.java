package xyz.wagyourtail.uniconfig.forge;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.wagyourtail.uniconfig.PlatformMethods;

public class ForgePlatformMethods implements PlatformMethods {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForgePlatformMethods.class);

    @Override
    public void sendFeedback(SharedSuggestionProvider suggestionProvider, Component component, boolean broadcast) {
        if (suggestionProvider instanceof CommandSourceStack) {
            ((CommandSourceStack) suggestionProvider).sendSuccess(() -> component, broadcast);
        } else {
            LOGGER.warn("Unknown command builder type: {}", suggestionProvider.getClass().getName());
        }
    }

    @Override
    public void sendFailure(SharedSuggestionProvider suggestionProvider, Component component) {
        if (suggestionProvider instanceof CommandSourceStack) {
            ((CommandSourceStack) suggestionProvider).sendFailure(component);
        } else {
            LOGGER.warn("Unknown command builder type: {}", suggestionProvider.getClass().getName());
        }
    }
}
