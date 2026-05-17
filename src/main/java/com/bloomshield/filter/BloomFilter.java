package com.bloomshield.filter;

public class BloomFilter {
    
    private final boolean[] bitArray;
    private final int bitSize;
    private final int hashCount;
    private int insertedCount;

    BloomFilter(int bitSize, int hashCount){
        this.bitSize = bitSize;
        this.bitArray = new boolean[bitSize];
        this.insertedCount = 0;
        this.hashCount = hashCount;
    }

    public void add(String key){
        int[] hashes = getHashes(key);
        for(int hash: hashes) {
            bitArray[hash] = true;
        }
        insertedCount++;
    }

    public boolean mightContain(String key){
        int[] hashes = getHashes(key);
        for(int hash: hashes){
            if(!bitArray[hash]) return false;
        }
        return true;
    }

    int[] getHashes(String Key){
        int[] pos = new int[hashCount];
        // hashing logic here
        return pos;
    }
}
