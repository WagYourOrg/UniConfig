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

public class Utils {

    public static MutableComponent translatable(@Translatable String key, Object... args) {
        return ensureHasFallback(Component.translatable(key, args));
    }


    // Gaming32 said I could use it
    public static MutableComponent ensureHasFallback(MutableComponent component) {
        if (component.getContents() instanceof TranslatableContents && ((TranslatableContents) component.getContents()).getFallback() == null) {
            TranslatableContents translatable = (TranslatableContents) component.getContents();
            final String fallbackText = Language.getInstance().getOrDefault(translatable.getKey(), null);

            Object[] args = translatable.getArgs();
            if (args.length > 0) {
                args = args.clone();
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof MutableComponent) {
                        MutableComponent subComponent = (MutableComponent) args[i];
                        args[i] = ensureHasFallback(subComponent);
                    }
                }
            }

            Style style = component.getStyle();
            if (style.getHoverEvent() != null) {
                if (style.getHoverEvent().getAction() == HoverEvent.Action.SHOW_TEXT) {
                    final Component hoverText = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_TEXT);
                    if (hoverText instanceof MutableComponent) {
                        MutableComponent mutableComponent = (MutableComponent) hoverText;
                        style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, mutableComponent));
                    }
                } else if (style.getHoverEvent().getAction() == HoverEvent.Action.SHOW_ENTITY) {
                    final HoverEvent.EntityTooltipInfo info = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_ENTITY);
                    assert info != null;
                    if (info.name instanceof MutableComponent) {
                        MutableComponent mutableComponent = (MutableComponent) info.name;
                        style = style.withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_ENTITY,
                                new HoverEvent.EntityTooltipInfo(info.type, info.id, ensureHasFallback(mutableComponent))
                        ));
                    }
                }
            }

            List<Component> siblings = component.getSiblings();
            if (!siblings.isEmpty()) {
                siblings = new ArrayList<>(siblings);
                for (int i = 0; i < siblings.size(); i++) {
                    if (siblings.get(i) instanceof MutableComponent) {
                        MutableComponent subComponent = (MutableComponent) siblings.get(i);
                        siblings.set(i, ensureHasFallback(subComponent));
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
