package com.zrh.international.business;

import java.util.Arrays;
import java.util.List;

public enum Language {
    zh("zh"), zhTw("zh-rTW"), en("en"), jp("ja"), ko("ko-rKR"), fr("fr"), de("de");

    private final String name;

    public String getName() {
        return name;
    }

    Language(String name) {
        this.name = name;
    }

    public static List<Language> getAll(){
        return Arrays.asList(Language.values());
    }
}