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
package ch.vorburger.minecraft.storeys.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtility {

    private final File zipFilePath;

    public ZipUtility(File zipFilePath) throws IOException {
        if (zipFilePath == null) {
            throw new IllegalArgumentException("The source path cannot be null");
        }
        if (!zipFilePath.isFile()) {
            throw new IllegalArgumentException("The passed path does not denote a valid file");
        }
        this.zipFilePath = zipFilePath;
    }

    public void addOrReplaceEntry(String entry, InputStream entryData) throws IOException {
        File temporaryFile = File.createTempFile(zipFilePath.getName(), null);
        temporaryFile.delete();
        Files.copy(zipFilePath.toPath(), temporaryFile.toPath());

        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFilePath));
        addOrReplaceEntry(temporaryFile, entry, entryData, zipOutputStream);
        zipOutputStream.close();
        temporaryFile.delete();
    }

    private static void addOrReplaceEntry(File source, String entry, InputStream entryData, ZipOutputStream zipOutputStream) throws IOException {
        writeZipEntry(zipOutputStream, entry, entryData);

        if (source.length() != 0) {
            ZipFile zipSourceFile = new ZipFile(source);

            Enumeration<? extends ZipEntry> zipEntries = zipSourceFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                String entryName = zipEntry.getName();
                if (!entry.equalsIgnoreCase(entryName)) {
                    try {
                        writeZipEntry(zipOutputStream, entryName, zipSourceFile.getInputStream(zipEntry));
                    } catch (Exception e) {
                        logException(e);
                    }
                }
            }
            zipSourceFile.close();
        }
    }

    private static void writeZipEntry(ZipOutputStream zipOutputStream, String entryName, InputStream entryData) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        byte[] buf = new byte[1024];
        zipOutputStream.putNextEntry(entry);
        int len;
        while ((len = entryData.read(buf)) > 0) {
            zipOutputStream.write(buf, 0, len);
        }
        zipOutputStream.closeEntry();

    }

    private static void logException(Exception e) {
        e.printStackTrace();
    }

}