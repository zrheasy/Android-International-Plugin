package com.zrh.international.business;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class Translator {

    private static Translator mInstance = null;

    private Configure configure;
    private TranslateCallback callback;
    private final ExecutorService mExecutorService = Executors.newFixedThreadPool(4, r -> new Thread(r, "Translator"));

    private final XmlWriter xmlWriter = new XmlWriter();
    private final ExcelReader excelReader = new ExcelReader();


    private Translator() {
    }

    public static synchronized Translator getInstance() {
        if (mInstance == null) {
            mInstance = new Translator();
        }
        return mInstance;
    }

    public void translate(Configure configure, TranslateCallback callback) {
        this.configure = configure;
        this.callback = callback;

        mExecutorService.submit(() -> {
            try {
                readExcelAndWriteXml();
                notifySuccess();
            } catch (Exception e) {
                notifyError(e);
            }
        });
    }

    public void translateSync(Configure configure) throws Exception {
        this.configure = configure;
        readExcelAndWriteXml();
    }

    private void readExcelAndWriteXml() throws Exception {
        File excelFile = configure.excelFile;
        String sheetName = configure.sheetName;
        List<Map<Integer, String>> list = excelReader.read(excelFile, sheetName);
        writeLanguageToXml(list);
    }

    private void writeLanguageToXml(List<Map<Integer, String>> list) throws Exception {
        for (Pair<Map<String, String>, File> data : parseLanguageData(list)) {
            xmlWriter.write(data.fst, data.snd);
        }
    }

    private void notifyError(Exception e) {
        if (callback != null) {
            callback.onError(e);
        }
    }

    private void notifySuccess() {
        if (callback != null) {
            callback.onSuccess();
        }
    }

    private List<Pair<Map<String, String>, File>> parseLanguageData(List<Map<Integer, String>> list) {
        List<Pair<Map<String, String>, File>> result = new ArrayList<>();

        Map<String, Integer> textIndexMap = configure.textIndexMap;
        int defaultIndex = -1;
        if (configure.useDefault) {
            Integer index = textIndexMap.get(configure.defaultLanguage);
            if (index != null) defaultIndex = index;
        }
        for (String language : textIndexMap.keySet()) {
            File xmlFile = getXmlFile(language);
            int languageIndex = textIndexMap.get(language);
            Map<String, String> languageData = getLanguageData(languageIndex, defaultIndex, list);
            result.add(new Pair<>(languageData, xmlFile));
        }

        return result;
    }

    private File getXmlFile(String language) {
        File dir = configure.moduleDir;
        String xmlPath = "/src/main/res/values/strings.xml";
        if (!language.equals(configure.defaultLanguage)) {
            xmlPath = "/src/main/res/values-" + language + "/strings.xml";
        }
        return new File(dir, xmlPath);
    }

    private Map<String, String> getLanguageData(int languageIndex, int defaultIndex, List<Map<Integer, String>> list) {
        Map<String, String> map = new HashMap<>();
        for (Map<Integer, String> excelData : list) {
            String tagName = excelData.get(configure.tagNameIndex);
            // 过滤tag
            if (isNotEmpty(tagName) && filterTag(tagName)) {
                String str = excelData.get(languageIndex);
                if (isNotEmpty(str) && defaultIndex != -1) {
                    str = excelData.get(defaultIndex);
                }
                if (isNotEmpty(str)) {
                    String value = str
                            .replace("&", "&amp;")
                            .replace("\n", "\\n")
                            .replace("'", "\\'")
                            .replace("\"", "\\\"");
                    map.put(tagName, value);
                }
            }
        }
        return map;
    }

    private boolean isNotEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    private boolean filterTag(String content) {
        if (configure.matchRegex == null || configure.matchRegex.isEmpty()) {
            return true;
        }

        try {
            boolean isMatch = Pattern.matches(configure.matchRegex, content);
            return isMatch;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 如果发生异常则不过滤
        return true;
    }

    public interface TranslateCallback {
        void onSuccess();

        void onError(Throwable error);
    }

    public static class Configure {
        private final File excelFile;
        private final String sheetName;
        private final File moduleDir;
        private final int tagNameIndex;
        private final String defaultLanguage;
        private final Map<String, Integer> textIndexMap;

        private String matchRegex;
        private boolean useDefault;

        public Configure(File excelFile, String sheetName, File moduleDir, int tagNameIndex, String defaultLanguage, Map<String, Integer> textIndexMap) {
            this.excelFile = excelFile;
            this.sheetName = sheetName;
            this.moduleDir = moduleDir;
            this.tagNameIndex = tagNameIndex;
            this.defaultLanguage = defaultLanguage;
            this.textIndexMap = textIndexMap;
        }

        public void setMatchRegex(String matchRegex) {
            this.matchRegex = matchRegex;
        }

        public void setUseDefault(boolean useDefault) {
            this.useDefault = useDefault;
        }
    }

    private static class Pair<A, B> {
        private final A fst;
        private final B snd;

        public Pair(A fst, B snd) {
            this.fst = fst;
            this.snd = snd;
        }
    }
}
