package xyz.wagyourtail.uniconfig.util;

import com.demonwav.mcdev.annotations.Translatable;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.ArrayList;
import java.util.List;

public class TranslationUtils {

    public static MutableComponent translatable(@Translatable String key, Object... args) {
        return wrapTranslatable(Component.translatable(key, args));
    }

    public static MutableComponent wrapTranslatable(MutableComponent component) {
        if (component.getContents() instanceof TranslatableContents translatable && translatable.getFallback() == null) {
            final String fallbackText = Language.getInstance().getOrDefault(translatable.getKey(), null);

            Object[] args = translatable.getArgs();
            if (args.length > 0) {
                args = args.clone();
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof MutableComponent subComponent) {
                        args[i] = wrapTranslatable(subComponent);
                    }
                }
            }

            Style style = component.getStyle();
            if (style.getHoverEvent() != null) {
                if (style.getHoverEvent().getAction() == HoverEvent.Action.SHOW_TEXT) {
                    final Component hoverText = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_TEXT);
                    if (hoverText instanceof MutableComponent mutableComponent) {
                        style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, mutableComponent));
                    }
                } else if (style.getHoverEvent().getAction() == HoverEvent.Action.SHOW_ENTITY) {
                    final HoverEvent.EntityTooltipInfo info = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_ENTITY);
                    assert info != null;
                    if (info.name.orElse(null) instanceof MutableComponent mutableComponent) {
                        style = style.withHoverEvent(new HoverEvent(
                            HoverEvent.Action.SHOW_ENTITY,
                            new HoverEvent.EntityTooltipInfo(info.type, info.id, wrapTranslatable(mutableComponent))
                        ));
                    }
                }
            }

            List<Component> siblings = component.getSiblings();
            if (!siblings.isEmpty()) {
                siblings = new ArrayList<>(siblings);
                for (int i = 0; i < siblings.size(); i++) {
                    if (siblings.get(i) instanceof MutableComponent subComponent) {
                        siblings.set(i, wrapTranslatable(subComponent));
                    }
                }
            }

            final MutableComponent result = Component.translatableWithFallback(
                translatable.getKey(), fallbackText, args
            ).setStyle(style);
            result.getSiblings().addAll(siblings);
            return result;
        }
        return component;
    }

}
