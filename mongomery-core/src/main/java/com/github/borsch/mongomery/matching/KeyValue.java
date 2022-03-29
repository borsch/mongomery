package com.github.borsch.mongomery.matching;

class KeyValue {

    private final String key;
    private final String value;

    KeyValue(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
