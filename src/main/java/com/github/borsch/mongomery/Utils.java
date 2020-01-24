package com.github.borsch.mongomery;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

public class Utils {
    private Utils() {
    }

    public static String[] splitByLast$(final String s) {
        final int $ = s.lastIndexOf("$");
        return new String[] { s.substring(0, $), s.substring($) };
    }

    public static JSONObject readJsonFile(final String filePath) {
        return JSONValue.parse(readFile(filePath), JSONObject.class);
    }

    public static String readFile(final String filePath) {
        final URL url = Utils.class.getResource(filePath);

        if (url == null) {
            throw new RuntimeException(new FileNotFoundException(filePath));
        }

        try {
            return String.join("\n", Files.readAllLines(Paths.get(url.toURI())));
        } catch (final IOException | URISyntaxException ex) {
            throw new RuntimeException("Can't read file under path: " + filePath, ex);
        }
    }
}
