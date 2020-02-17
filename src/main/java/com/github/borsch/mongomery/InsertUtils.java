package com.github.borsch.mongomery;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minidev.json.JSONObject;

class InsertUtils {

    private static final Map<Pattern, Function<Matcher, ?>> REPLACER_MAP = new HashMap<>();
    static {
        REPLACER_MAP.put(
            Pattern.compile("\\$insertLocalDateTime\\((\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})\\)"),
            matcher -> LocalDateTime.parse(matcher.group(1))
        );
    }

    static void replaceInsertPlaceholders(final JSONObject jsonObject) {
        for (final String key : jsonObject.keySet()) {
            final Object value = jsonObject.get(key);

            if (value instanceof String) {
                REPLACER_MAP.forEach(((pattern, converter) -> {
                    final Matcher matcher = pattern.matcher((String) value);
                    if (matcher.find()) {
                        jsonObject.put(key, converter.apply(matcher));
                    }
                }));
            }
        }
    }

}
