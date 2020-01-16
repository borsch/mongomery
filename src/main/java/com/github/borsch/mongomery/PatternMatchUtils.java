package com.github.borsch.mongomery;

import static com.github.borsch.mongomery.Placeholders.ANY_DATE;
import static com.github.borsch.mongomery.Placeholders.ANY_LONG_VALUE;
import static com.github.borsch.mongomery.Placeholders.ANY_OBJECT;
import static com.github.borsch.mongomery.Placeholders.ANY_OBJECT_WITH_ARG;
import static com.github.borsch.mongomery.Placeholders.ANY_STRING;
import static com.github.borsch.mongomery.Placeholders.ANY_STRING_WITH_ARG;
import static com.github.borsch.mongomery.Placeholders.EQ_LONG_VALUE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minidev.json.JSONObject;

public class PatternMatchUtils {

    private static final String NUMBER_LONG_KEY = "$numberLong";
    private static final String DATE_KEY = "$date";

    private PatternMatchUtils() {
    }

    public static JSONObject applyPropsAndGetResultObj(final JSONObject object, final Set<String> props) {
        JSONObject clone = (JSONObject) object.clone();

        for (final String prop : props) {
            final String[] properties = prop.split("\\.");
            LinkedList<JSONObject> trace = new LinkedList<>();
            trace.add(clone);

            for (int i = 0; i < properties.length; i++) {
                if (i == properties.length - 1) {
                    final String[] $s = Utils.splitByLast$(properties[i]);
                    final Object o = clone.get($s[0]);

                    if (o == null) {
                        return null;
                    } else if (isAnyNoArgPattern($s[1], o)) {
                        clone = createMergedObj(trace, properties, clone.getAsString($s[0]));
                    } else if (ANY_OBJECT_WITH_ARG.eq($s[1]) && o instanceof JSONObject) {
                        int numOfObjs = Integer.parseInt($s[1].substring(11, $s[1].lastIndexOf(')')));
                        if (((JSONObject) o).size() == numOfObjs) {
                            clone = createMergedObj(trace, properties, clone.getAsString($s[0]));
                        }
                    } else if (ANY_STRING_WITH_ARG.eq($s[1]) && o instanceof String) {
                        String regex = $s[1].substring(12, $s[1].lastIndexOf('/'));
                        if (((String) o).matches(regex)) {
                            clone = createMergedObj(trace, properties, clone.getAsString($s[0]));
                        }
                    } else if (isLongValue(EQ_LONG_VALUE, $s[1], o)) {
                        final long actualLong = Long.parseLong(((JSONObject) o).get(NUMBER_LONG_KEY).toString());
                        final long expectedLong = Long.parseLong($s[1].substring(13, $s[1].indexOf(')')));

                        if (actualLong == expectedLong) {
                            clone = createMergedObj(trace, properties, clone.getAsString($s[0]));
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

    private static JSONObject createMergedObj(final LinkedList<JSONObject> trace, final String[] properties, final Object old) {
        final String[] $s = Utils.splitByLast$(properties[properties.length - 1]);
        final JSONObject object = trace.removeLast();
        object.put($s[0], $s[1]);

        JSONObject temp = object;
        for (int i = properties.length - 2; i >= 0; i--) {
            final JSONObject upperNode = trace.removeLast();
            upperNode.put(properties[i], temp);
            temp = upperNode;
        }

        return temp;
    }

    public static Set<String> findPatternPropertiesPaths(final JSONObject object) {
        int contains = countStringContainsPattern(object.toString());

        if (contains == 0) {
            return null;
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

    public static boolean checkForEqualsPattern(final String value) {
        return Placeholders.getEqualPatterns().stream()
            .anyMatch(pattern -> pattern.matcher(value).find());
    }

    public static int countStringContainsPattern(final String string) {
        int count = 0;

        for (final Pattern pattern : Placeholders.getContainPatterns()) {
            final Matcher matcher = pattern.matcher(string);

            while (matcher.find()) {
                count++;
            }
        }

        return count;
    }

    /**
     * check if object is equal to any non strict match pattern
     * @param $pattern - pattern to be checked
     * @param o - object under check
     * @return {@code true} if {@param $pattern} belongs to any matcher, {@code false} otherwise
     */
    private static boolean isAnyNoArgPattern(final String $pattern, final Object o) {
        return (ANY_STRING.eq($pattern) && o instanceof String) ||
            (ANY_OBJECT.eq($pattern) && o instanceof JSONObject) ||
            isLongValue(ANY_LONG_VALUE, $pattern, o) ||
            isDateValue($pattern, o);
    }

    private static boolean isLongValue(final Placeholders longValueMatcher, final String $pattern, final Object object) {
        if (longValueMatcher.eq($pattern) && object instanceof JSONObject) {
            final JSONObject jsonObject = (JSONObject) object;

            return jsonObject.size() == 1 && jsonObject.containsKey(NUMBER_LONG_KEY);
        }

        return false;
    }

    private static boolean isDateValue(final String $pattern, final Object object) {
        if (ANY_DATE.eq($pattern) && object instanceof JSONObject) {
            final JSONObject jsonObject = (JSONObject) object;

            return jsonObject.size() == 1 && jsonObject.containsKey(DATE_KEY);
        }

        return false;
    }
}
