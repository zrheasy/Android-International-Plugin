package com.zrh.international.business;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.cache.MapCache;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ExcelReader {
    public List<Map<Integer,String>> read(File file, String sheetName) {
        return EasyExcel.read(file).readCache(new MapCache()).sheet(sheetName).doReadSync();
    }
}
