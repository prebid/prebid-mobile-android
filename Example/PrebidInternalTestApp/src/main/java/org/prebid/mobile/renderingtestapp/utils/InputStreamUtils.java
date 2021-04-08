package org.prebid.mobile.renderingtestapp.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InputStreamUtils {

    public static String convert(InputStream inputStream) {

        StringBuilder stringBuilder = new StringBuilder();
        String line = null;

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\r\n");
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load input stream");
        }

        return stringBuilder.toString();
    }
}
