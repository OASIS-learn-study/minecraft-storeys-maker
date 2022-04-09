/*
 * ch.vorburger.minecraft.storeys
 *
 * Copyright (C) 2016 - 2018 Michael Vorburger.ch <mike@vorburger.ch>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.vorburger.minecraft.storeys.model.parser;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.ScoreText;
import org.spongepowered.api.text.SelectorText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.text.serializer.SafeTextSerializer;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.text.translation.locale.Locales;

/**
 * Copied from Sponge to test Text.
 */
public class TestPlainTextSerializer implements SafeTextSerializer {

    public static void inject() throws ReflectiveOperationException {
        setCatalogElement(TextSerializers.class, "PLAIN", new TestPlainTextSerializer());
    }

    private static void setCatalogElement(Class<?> catalog, String name, Object value) throws NoSuchFieldException, IllegalAccessException {
        setStaticFinalField(catalog.getDeclaredField(name), value);
    }

    private static void setStaticFinalField(Field field, Object value) throws NoSuchFieldException, IllegalAccessException {
        removeFinal(field);
        field.set(null, value);
    }

    private static void removeFinal(Field field) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }

    @Override public String getId() {
        return "sponge:plain";
    }

    @Override public String getName() {
        return "Plain Text";
    }

    @Override public Text deserialize(String input) {
        return Text.of(input);
    }

    @Override public String serialize(Text text) {
        final StringBuilder ret = new StringBuilder();
        for (Text child : text.withChildren()) {
            if (child instanceof LiteralText) {
                ret.append(((LiteralText) child).getContent());
            } else if (child instanceof TranslatableText) {
                ret.append(((TranslatableText) child).getTranslation().get(Locales.DEFAULT,
                        convertArgs(((TranslatableText) child).getArguments())));
            } else if (child instanceof ScoreText) {
                ret.append(((ScoreText) child).getScore().getScore());
            } else if (child instanceof SelectorText) {
                ret.append(((SelectorText) child).getSelector().toPlain());
            }
        }
        return ret.toString();
    }

    private Object[] convertArgs(List<Object> args) {
        Object[] ret = new Object[args.size()];
        for (int i = 0; i < ret.length; ++i) {
            Object current = args.get(i);
            if (current instanceof Text) {
                current = serialize((Text) current);
            }
            ret[i] = current;
        }
        return ret;
    }

}