package com.github.borsch.mongomery;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

public class Utils {
    private Utils() {
    }

    public static String[] splitByLast$(String s) {
        final int $ = s.lastIndexOf("$");
        return new String[]{s.substring(0, $), s.substring($)};
    }

    public static JSONObject readJsonFile(String filePath) {
        final InputStream fileStream = Utils.class.getResourceAsStream(filePath);

        if (fileStream == null) {
            throw new RuntimeException(new FileNotFoundException(filePath));
        }

        return JSONValue.parse(new InputStreamReader(fileStream, StandardCharsets.UTF_8), JSONObject.class);
    }
}
