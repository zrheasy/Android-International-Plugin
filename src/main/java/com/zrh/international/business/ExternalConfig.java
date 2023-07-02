package com.zrh.international.business;

import java.util.Map;

public class ExternalConfig {

    // 正则匹配需要翻译的tag
    private String matchRegex;
    // 当文案为空时是否使用默认语言文案
    private boolean useDefault;

    // 标签名称列数（必填）
    private int tagNameIndex = 0;
    // 默认语言（必填）
    private String defaultLanguage = "en";
    // 语言文案对应的列数映射（必填）
    private Map<String, Integer> textIndexMap;

    public int getTagNameIndex() {
        return tagNameIndex;
    }

    public void setTagNameIndex(int tagNameIndex) {
        this.tagNameIndex = tagNameIndex;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public Map<String, Integer> getTextIndexMap() {
        return textIndexMap;
    }

    public void setTextIndexMap(Map<String, Integer> textIndexMap) {
        this.textIndexMap = textIndexMap;
    }

    public String getMatchRegex() {
        return matchRegex;
    }

    public void setMatchRegex(String matchRegex) {
        this.matchRegex = matchRegex;
    }

    public boolean isUseDefault() {
        return useDefault;
    }

    public void setUseDefault(boolean useDefault) {
        this.useDefault = useDefault;
    }
}
