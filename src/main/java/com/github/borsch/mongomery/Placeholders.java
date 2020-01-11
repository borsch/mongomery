package com.github.borsch.mongomery;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Placeholders {
    ANY_OBJECT("\\$anyObject\\(\\)"),
    ANY_OBJECT_WITH_ARG("\\$anyObject\\(\\d{1,10}\\)"),
    ANY_STRING("\\$anyString\\(\\)"),
    ANY_STRING_WITH_ARG("\\$anyString\\(\\\\?/.+\\\\?/\\)");

    private final Pattern containPattern;
    private final Pattern equalPattern;

    Placeholders(final String pattern) {
        this.containPattern = Pattern.compile(pattern);
        this.equalPattern = Pattern.compile("^" + pattern + "$");
    }

    public boolean eq(final String s) {
        return this.equalPattern.matcher(s).matches();
    }

    public static Set<Pattern> getContainPatterns() {
        return Stream.of(values())
            .map(item -> item.containPattern)
            .collect(Collectors.toSet());
    }

    public static Set<Pattern> getEqualPatterns() {
        return Stream.of(values())
            .map(item -> item.equalPattern)
            .collect(Collectors.toSet());
    }
}