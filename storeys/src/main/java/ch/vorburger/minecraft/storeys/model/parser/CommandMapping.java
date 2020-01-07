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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Provider;

import ch.vorburger.minecraft.storeys.model.*;
import com.google.inject.Inject;

public class CommandMapping {
    private List<Mapping> mappings = new ArrayList<>();

    @Inject
    public CommandMapping(
            Provider<CommandAction> commandActionProvider,
            Provider<NarrateAction> narrateProvider,
            Provider<TitleAction> titleActionProvider,
            Provider<AwaitAction> awaitActionProvider,
            Provider<DynamicAction> dynamicActionProvider,
            Provider<LocationAction> locationActionProvider,
            Provider<MessageAction> messageActionProvider
    ) {
        mappings.add(new Mapping(Pattern.compile("(?s)^=\\s(.*==[^\\n]*)"), titleActionProvider::get));
        mappings.add(new Mapping(Pattern.compile("(?s)^=\\s([^\\n]*)"), titleActionProvider::get));
        mappings.add(new Mapping(Pattern.compile("(?s)^(@.+?)\\n\\n"), narrateProvider::get));
        mappings.add(new Mapping(Pattern.compile("^%await\\s([^\\n]*)"), awaitActionProvider::get));
        mappings.add(new Mapping(Pattern.compile("^/([a-zA-Z]*\\s.*)"), commandActionProvider::get));
        mappings.add(new Mapping(Pattern.compile("(?s)^\\s+(.+?}\\n)"), dynamicActionProvider::get));
        mappings.add(new Mapping(Pattern.compile("^%in\\s([^\\n]*)"), locationActionProvider::get));
        mappings.add(new Mapping(Pattern.compile("^\\s*//(.*)"), NopAction::new));
        mappings.add(new Mapping(Pattern.compile("^([^\\n]*)"), messageActionProvider::get));
    }

    public List<Mapping> getMappings() {
        return mappings;
    }

    public static class Mapping {
        private final Pattern regex;
        private final Provider<Action<?>> actionProvider;

        Mapping(Pattern regex,  Provider<Action<?>> actionProvider) {
            this.regex = regex;
            this.actionProvider = actionProvider;
        }

        Pattern getRegex() {
            return regex;
        }

        Provider<Action<?>> getActionProvider() {
            return actionProvider;
        }
    }

}
