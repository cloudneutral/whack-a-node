package io.cockroachdb.wan.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PatternUtils {
    private static final Pattern LOCALITY_PATTERN
            = Pattern.compile("([^=,]+)=([^\0]+?)(?=,[^,]+=|$)", Pattern.CASE_INSENSITIVE);

    private PatternUtils() {
    }

    public static Map<String, String> parseLocality(String locality) {
        final Matcher matcher = LOCALITY_PATTERN.matcher(locality);

        Map<String, String> map = new LinkedHashMap<>();

        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                map.put(matcher.group(1), matcher.group(2));
            }
        }

        return map;
    }
}
