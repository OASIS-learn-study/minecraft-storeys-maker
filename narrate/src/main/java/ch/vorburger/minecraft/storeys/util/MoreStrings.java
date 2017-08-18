package ch.vorburger.minecraft.storeys.util;

public final class MoreStrings {

    private MoreStrings() { }

    public static String normalizeCRLF(String text) {
        return text.replace("\r\n", "\n");
    }

}
