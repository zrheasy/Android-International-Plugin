package com.zrh.international.utils;

import com.intellij.ide.util.PropertiesComponent;

public class PropertiesUtils {

    public static void save(String key, String value) {
        PropertiesComponent.getInstance().setValue(key, value);
    }

    public static String get(String key) {
        return get(key, "");
    }

    public static String get(String key, String defaultString) {
        return PropertiesComponent.getInstance().getValue(key, defaultString);
    }

}
