package com.graphdb.search;

import com.graphdb.utils.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by ashraful on 7/16/17.
 */
public class Standardizer {

    private static final Standardizer instance = new Standardizer();
    private final Map<String, String> synonymMap;

    private Standardizer() {
        synonymMap = loadFileToHashMap(Constants.SYNONYM_FILENAME, Constants.SYNONYM_SEPARATOR);
    }

    public static Standardizer getInstance() {
        return instance;
    }

    public ArrayList<Token> standardize(List<Token> tokens) {
        ArrayList<Token> result = new ArrayList<>();
        StringBuilder key = new StringBuilder();
        Token value = null;

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            String matchValue = getMatchValue(key.length() == 0 ? token.getToken() : key.toString() + ' ' + token.getToken());

            if (matchValue != null) {
                if (key.length() > 0) {
                    key.append(' ');
                }
                key.append(token.getToken());
                value = Token.buildToken(matchValue);
            } else {
                if (value == null && key.length() == 0) {
                    result.add(token);
                } else {
                    result.add(value);
                    key = new StringBuilder();
                    value = null;
                    i--;
                }
            }
        }

        if (value != null && key.length() != 0) {
            result.add(value);
        }

        return result;
    }

    private String getMatchValue(String text) {
        for (Map.Entry<String, String> entry : synonymMap.entrySet()) {
            if (text.matches(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private Map<String, String> loadFileToHashMap(String fileName, String separatorRegex) {
        Map<String, String> map = new LinkedHashMap<>();

        try {
            HashSet<String> prefixSet = new HashSet<>();
            Scanner sc = new Scanner(new File(fileName));

            while (sc.hasNextLine()) {
                String[] entry = sc.nextLine().trim().toLowerCase().split(separatorRegex, 2);
                String[] keys = entry[0].split("\\s*,\\s*");
                String value = entry[1];

                //Build Regex Rule
                map.put(buildRegex(keys), value);

                //Build Prefix Set
                for (String key : keys) {
                    if (prefixSet.contains(key)) {
                        continue;
                    }

                    String[] words = key.split("\\s+");
                    StringBuilder prefixBuilder = new StringBuilder();

                    for (int i = 0; i < words.length - 1; i++) {
                        if (prefixBuilder.length() > 0) {
                            prefixBuilder.append(' ');
                        }
                        prefixBuilder.append(words[i]);
                        prefixSet.add(prefixBuilder.toString());
                    }
                }
            }

            sc.close();

            //Put all the prefix to map so that the standardize loop continue to match
            for (String prefix : prefixSet) {
                boolean match = false;
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    if (prefix.matches(entry.getKey())) {
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    map.put(prefix, prefix);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return map;
    }


    private String buildRegex(String... keys) {
        StringBuilder regexBuilder = new StringBuilder().append("^(?:").append(keys[0].replaceAll("\\s+", "\\\\s+"));
        for (int i = 1; i < keys.length; i++) {
            regexBuilder.append('|').append(keys[i].replaceAll("\\s+", "\\\\s+"));
        }
        regexBuilder.append(")$");
        return regexBuilder.toString();
    }
}
