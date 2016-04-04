package de.fabmax.lightgl.util;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Max on 24.11.2014.
 */
public class CharMap {

    public static final CharMap ASCII_MAP;

    static {
        ASCII_MAP = new CharMap();
        for (char c = 32; c < 128; c++) {
            ASCII_MAP.addChar(c);
        }
    }

    private final HashSet<Character> mChars = new HashSet<>();
    private final HashMap<Character, Integer> mIndexMap = new HashMap<>();

    private int mHash = 0;

    public CharMap() {
        // default constructor is empty
    }

    public CharMap(String chars) {
        addChars(chars);
    }

    public CharMap(char[] chars) {
        addChars(chars);
    }

    public void addChar(char c) {
        if (mChars.add(c)) {
            mIndexMap.put(c, mIndexMap.size());
            // Note: by doing this CharMaps with same content but different order will have
            // different hashes although a Set should be order-independent
            mHash = mHash * 31 + c;
        }
    }

    public void addChars(char[] chars) {
        for (char c : chars) {
            addChar(c);
        }
    }

    public void addChars(String s) {
        for (int i = 0; i < s.length(); i++) {
            addChar(s.charAt(i));
        }
    }

    public HashMap<Character, Integer> getIndexMap() {
        return mIndexMap;
    }

    @Override
    public int hashCode() {
        return mHash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o.getClass() != getClass()) {
            return false;
        }
        return o.hashCode() == hashCode();
    }
}
