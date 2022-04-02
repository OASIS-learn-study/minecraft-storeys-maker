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
package ch.vorburger.minecraft.storeys.testutils;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ResourceList.ResourceFilter;
import java.util.Map.Entry;

/**
 * See
 * https://github.com/classgraph/classgraph/wiki/Code-examples#find-all-duplicate-class-definitions-in-the-classpath-or-module-path,
 * inspired by
 * https://github.com/opendaylight/infrautils/blob/ae405c37df3d62f78d3c482212534875eaba776d/testutils/src/main/java/org/opendaylight/infrautils/testutils/ClasspathHellDuplicatesChecker.java
 */
public class ClasspathHellDuplicatesChecker {

    // Using this via "./gradlew test" and in an IDE such as Eclipse will not have the same classpath; so try both!! ;-(

    private static final ResourceFilter EXCLUSIONS = resource -> !resource.getPath().endsWith("module-info.class")
            && !resource.getURI().toString().contains(".gradle/wrapper");

    public static void check() {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, ResourceList> dup : new ClassGraph().scan().getAllResources().classFilesOnly().filter(EXCLUSIONS)
                .findDuplicatePaths()) {
            sb.append(dup.getKey() + "\n");
            try (ResourceList value = dup.getValue()) {
                for (Resource res : value) {
                    sb.append(" -> " + res.getURI() + "\n");
                }
            }
        }
        if (sb.length() > 0) {
            throw new AssertionError(sb.toString());
        }
    }
}
