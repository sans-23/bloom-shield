package com.bloomshield.filter;

import java.util.ArrayList;

public class BloomFilter {
    public int size = 10000000;
    ArrayList<Integer> slots;

    BloomFilter(int size){
        this.size = size;
        slots = new ArrayList<>(size);
    }
}
