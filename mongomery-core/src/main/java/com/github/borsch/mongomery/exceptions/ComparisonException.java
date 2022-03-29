package com.github.borsch.mongomery.exceptions;

public class ComparisonException extends RuntimeException {

    public ComparisonException(final String template, final Object... templateArgs) {
        super(String.format(template, templateArgs));
    }

}
