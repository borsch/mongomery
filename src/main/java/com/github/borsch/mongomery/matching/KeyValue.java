package com.github.borsch.mongomery.matching;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
@Getter
public class KeyValue {

    private final String key;
    private final String value;

}
