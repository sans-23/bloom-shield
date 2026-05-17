package com.bloomshield.filter;

public interface Filter {
    void add(String key);
    boolean mightContain(String key);
    String getName();
    int[] getHashes(String key);
}
