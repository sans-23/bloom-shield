package com.bloomshield.filter;

public class CountingBloomFilter implements Filter {
    
    private final int[] counterArray;
    private final int bitSize;
    private final int hashCount;
    private int insertedCount;
    private final String name = "cbf";

    public CountingBloomFilter(int bitSize, int hashCount){
        this.bitSize = bitSize;
        this.counterArray = new int[bitSize];
        this.insertedCount = 0;
        this.hashCount = hashCount;
    }

    public void add(String key){
        int[] hashes = getHashes(key);
        for(int hash: hashes) {
            counterArray[hash]++;
        }
        insertedCount++;
    }

    public boolean mightContain(String key){
        int[] hashes = getHashes(key);
        for(int hash: hashes){
            if(counterArray[hash] == 0) return false;
        }
        return true;
    }

    public int[] getHashes(String key) {
        int[] pos = new int[hashCount];
        for (int i = 0; i < hashCount; i++) {
            // using built in hashCode() of String and bitwise AND to ensure positive index
            String toHash = key + i;
            int hash = toHash.hashCode();
            pos[i] = (hash & 0x7fffffff) % bitSize;
        }
        return pos;
    }

    public String getName() {
        return name;
    }
}
