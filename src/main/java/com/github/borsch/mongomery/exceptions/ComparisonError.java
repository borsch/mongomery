package com.github.borsch.mongomery.exceptions;

public class ComparisonError extends AssertionError {

    public ComparisonError(final String template, final Object... templateArgs) {
        super(String.format(template, templateArgs));
    }

}
