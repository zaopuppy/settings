package com.zero.util.test;

import com.zero.util.storage.MemoryStorage;
import com.zero.util.storage.SharedPereferenceStorage;

/**
 * Created by zhaoyi on 7/23/16.
 */
public class TestSettings {

    private SharedPereferenceStorage mSpStorage;

    private MemoryStorage mMemStorage;

    // visitor-url
    // max-page-num
    public String getVisitorUrl() {
        return mSpStorage.getString("visitor-url", "");
    }

    public boolean setVisitorUrl(String url) {
        return mSpStorage.put("visitor-url", url);
    }

    public int getMaxPageNum() {
        return mMemStorage.getInt("max-page-num", 100);
    }

    public boolean setMaxPageNum(int maxPageNum) {
        return mMemStorage.put("max-page-num", maxPageNum);
    }
}
