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

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Resources.getResource;

import com.google.common.io.Resources;
import java.io.IOException;

public class ClassLoaderResourceStoryRepository implements StoryRepository {

    @Override public String getStoryScript(String storyName) throws IOException {
        return Resources.toString(getResource(ClassLoaderResourceStoryRepository.class, "/" + storyName + ".story"), UTF_8);
    }

}
