package com.github.borsch.mongomery.matching;

import static com.github.borsch.mongomery.matching.MatchingUtil.splitByLast$;
import static com.github.borsch.mongomery.matching.Placeholders.ANY_DATE;
import static com.github.borsch.mongomery.matching.Placeholders.ANY_LONG_VALUE;
import static com.github.borsch.mongomery.matching.Placeholders.ANY_OBJECT;
import static com.github.borsch.mongomery.matching.Placeholders.ANY_OBJECT_WITH_ARG;
import static com.github.borsch.mongomery.matching.Placeholders.ANY_STRING;
import static com.github.borsch.mongomery.matching.Placeholders.ANY_STRING_WITH_ARG;
import static com.github.borsch.mongomery.matching.Placeholders.EQ_LOCAL_DATE_TIME_VALUE;
import static com.github.borsch.mongomery.matching.Placeholders.EQ_LOCAL_DATE_VALUE;
import static com.github.borsch.mongomery.matching.Placeholders.EQ_LONG_VALUE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minidev.json.JSONObject;

class PatternMatchUtils {

    private static final String NUMBER_LONG_KEY = "$numberLong";
    private static final String DATE_KEY = "$date";

    static JSONObject applyPropsAndGetResultObj(final JSONObject object, final Set<String> props) {
        JSONObject clone = (JSONObject) object.clone();

        for (final String prop : props) {
            final String[] properties = prop.split("\\.");
            final LinkedList<JSONObject> trace = new LinkedList<>();
            trace.add(clone);

            for (int i = 0; i < properties.length; i++) {
                if (i == properties.length - 1) {
                    final KeyValue $s = splitByLast$(properties[i]);
                    final Object o = clone.get($s.getKey());

                    if (o == null) {
                        return null;
                    } else if (isAnyNoArgPattern($s.getValue(), o)) {
                        clone = createMergedObj(trace, properties);
                    } else if (ANY_OBJECT_WITH_ARG.eq($s.getValue()) && o instanceof JSONObject) {
                        final Matcher matcher = ANY_OBJECT_WITH_ARG.getEqualPattern().matcher($s.getValue());
                        if (matcher.find()) {
                            final int numOfObjs = Integer.parseInt(matcher.group(1));
                            if (((JSONObject) o).size() == numOfObjs) {
                                clone = createMergedObj(trace, properties);
                            }
                        }
                    } else if (ANY_STRING_WITH_ARG.eq($s.getValue()) && o instanceof String) {
                        final Matcher matcher = ANY_STRING_WITH_ARG.getEqualPattern().matcher($s.getValue());
                        if (matcher.find()) {
                            final String regex = matcher.group(1);
                            if (((String) o).matches(regex)) {
                                clone = createMergedObj(trace, properties);
                            }
                        }
                    } else if (EQ_LOCAL_DATE_TIME_VALUE.eq($s.getValue()) && isJsonObject(o) && hasOnlyProperties((JSONObject) o, DATE_KEY)) {
                        final Matcher matcher = EQ_LOCAL_DATE_TIME_VALUE.getEqualPattern().matcher($s.getValue());
                        if (matcher.find()) {
                            final long expectedMillis = parseLocalDateTime($s.getKey(), matcher.group(1));
                            final long actualMillis = (Long) ((JSONObject) o).get(DATE_KEY);

                            if (expectedMillis == actualMillis) {
                                clone = createMergedObj(trace, properties);
                            }
                        }
                    } else if (EQ_LOCAL_DATE_VALUE.eq($s.getValue()) && isJsonObject(o) && hasOnlyProperties((JSONObject) o, DATE_KEY)) {
                        final Matcher matcher = EQ_LOCAL_DATE_VALUE.getEqualPattern().matcher($s.getValue());
                        if (matcher.find()) {
                            final long expectedMillis = parseLocalDate($s.getKey(), matcher.group(1));
                            final long actualMillis = (Long) ((JSONObject) o).get(DATE_KEY);

                            if (expectedMillis == actualMillis) {
                                clone = createMergedObj(trace, properties);
                            }
                        }
                    } else if (isLongValue(EQ_LONG_VALUE, $s.getValue(), o)) {
                        final Matcher matcher = EQ_LONG_VALUE.getEqualPattern().matcher($s.getValue());
                        if (matcher.find()) {
                            final long actualLong = Long.parseLong(((JSONObject) o).get(NUMBER_LONG_KEY).toString());
                            final long expectedLong = Long.parseLong(matcher.group(1));

                            if (actualLong == expectedLong) {
                                clone = createMergedObj(trace, properties);
                            }
                        }
                    } else  {
                        return null;
                    }
                } else {
                    trace.add((JSONObject) clone.get(properties[i]));
                    clone = (JSONObject) clone.get(properties[i]);
                }
            }

        }

        return clone;
    }

    private static JSONObject createMergedObj(final LinkedList<JSONObject> trace, final String[] properties) {
        final KeyValue $s = splitByLast$(properties[properties.length - 1]);
        final JSONObject object = trace.removeLast();
        object.put($s.getKey(), $s.getValue());

        JSONObject temp = object;
        for (int i = properties.length - 2; i >= 0; i--) {
            final JSONObject upperNode = trace.removeLast();
            upperNode.put(properties[i], temp);
            temp = upperNode;
        }

        return temp;
    }

    static Set<String> findPatternPropertiesPaths(final JSONObject object) {
        int contains = countStringContainsPattern(object.toString());

        if (contains == 0) {
            return new HashSet<>();
        } else {
            final Set<String> properties = new HashSet<>();

            for (final Map.Entry<String, Object> node : object.entrySet()) {
                final Set<String> props = checkForNode(node, new HashSet<>());
                if (!props.isEmpty()) {
                    properties.addAll(props);
                    if (contains == properties.size()) {
                        //All was found
                        return properties;
                    }
                }
            }
            return properties;
        }
    }

    private static Set<String> checkForNode(final Map.Entry<String, Object> node, final Set<String> props) {
        final Object value = node.getValue();

        if (value == null || countStringContainsPattern(value.toString()) == 0) {
            return props;
        } else {
            final Set<String> newProps = addNodeForProps(props, node.getKey());

            if (value instanceof String) {
                if (checkForEqualsPattern((String) value)) {
                    final Set<String> hashSet = new HashSet<>();
                    for (final String p : newProps) {
                        hashSet.add(p + value);
                    }
                    return hashSet;
                } else {
                    if (countStringContainsPattern((String) value) > 0) {
                        throw new IllegalArgumentException("String with placeholder shouldn't contain any other characters (even whitespaces)! Your string was: \""
                                + value + "\"");
                    }
                }
            } else if (value instanceof ArrayList) {
                throw new UnsupportedOperationException("You can't use match patterns in arrays for now!");
            } else {
                final JSONObject childNodes = (JSONObject) value;
                final Set<String> theNewestProps = new HashSet<>();

                for (final Map.Entry<String, Object> childNode : childNodes.entrySet()) {
                    final Set<String> p = checkForNode(childNode, newProps);

                    if (!newProps.equals(p)) {
                        theNewestProps.addAll(p);
                    }
                }

                return theNewestProps;
            }
        }

        throw new IllegalArgumentException();
    }


    private static Set<String> addNodeForProps(final Set<String> props, final String nodeKey) {
        final Set<String> newProps = new HashSet<>();

        if (props.isEmpty()) {
            newProps.add(nodeKey);
        } else {
            for (final String p : props) {
                newProps.add(p + "." + nodeKey);
            }
        }

        return newProps;
    }

    static boolean checkForEqualsPattern(final String value) {
        return Placeholders.getEqualPatterns().stream()
            .anyMatch(pattern -> pattern.matcher(value).find());
    }

    static int countStringContainsPattern(final String string) {
        int count = 0;

        for (final Pattern pattern : Placeholders.getContainPatterns()) {
            final Matcher matcher = pattern.matcher(string);

            while (matcher.find()) {
                count++;
            }
        }

        return count;
    }

    private static long parseLocalDateTime(final String property, final String localDateTimeString) {
        try {
            final LocalDateTime localDateTime = LocalDateTime.parse(localDateTimeString);
            return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
        } catch (final DateTimeParseException ex) {
            throw new IllegalArgumentException(String.format("Can't parse value of placeholder %s to LocalDateTime. Field %s", localDateTimeString, property), ex);
        }
    }

    private static long parseLocalDate(final String property, final String localDateString) {
        try {
            final LocalDate localDate = LocalDate.parse(localDateString);
            return localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        } catch (final DateTimeParseException ex) {
            throw new IllegalArgumentException(String.format("Can't parse value of placeholder %s to LocalDate. Field %s", localDateString, property), ex);
        }
    }

    /**
     * check if object is equal to any non strict match pattern
     * @param $pattern - pattern to be checked
     * @param o - object under check
     * @return {@code true} if {@param $pattern} belongs to any matcher, {@code false} otherwise
     */
    private static boolean isAnyNoArgPattern(final String $pattern, final Object o) {
        return (ANY_STRING.eq($pattern) && o instanceof String) ||
            (ANY_OBJECT.eq($pattern) && isJsonObject(o)) ||
            isLongValue(ANY_LONG_VALUE, $pattern, o) ||
            isDateValue($pattern, o);
    }

    private static boolean isLongValue(final Placeholders longValueMatcher, final String $pattern, final Object object) {
        if (longValueMatcher.eq($pattern) && isJsonObject(object)) {
            final JSONObject jsonObject = (JSONObject) object;

            return hasOnlyProperties(jsonObject, NUMBER_LONG_KEY);
        }

        return false;
    }

    private static boolean isDateValue(final String $pattern, final Object object) {
        if (ANY_DATE.eq($pattern) && isJsonObject(object)) {
            final JSONObject jsonObject = (JSONObject) object;

            return hasOnlyProperties(jsonObject, DATE_KEY);
        }

        return false;
    }

    private static boolean hasOnlyProperties(final JSONObject object, final String... properties) {
        if (object.size() == properties.length) {
            return Arrays.stream(properties)
                .anyMatch(object::containsKey);
        }
        return false;
    }

    private static boolean isJsonObject(final Object o) {
        return o instanceof JSONObject;
    }
}
