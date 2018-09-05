package com.graphdb.search;

import com.graphdb.utils.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by mishkat, ashraful on 7/12/17.
 */
public class SubQueryMapper {

    private static final SubQueryMapper instance;

    static {
        try {
            instance = new SubQueryMapper();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final HashMap<String, String> subQueryMap;

    public SubQueryMapper() throws IOException {
        subQueryMap = getSubQueryMap(Constants.SUB_QUERY_FILENAME);
    }

    public static SubQueryMapper getInstance() {
        return instance;
    }

    private HashMap<String, String> getSubQueryMap(String fileName) throws IOException {
        HashMap<String, String> subQueryMap = new HashMap<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(fileName)));
        String nextLine;
        while ((nextLine = bufferedReader.readLine()) != null) {
            String[] entry = nextLine.split("\\s*=\\s*", 2);
            subQueryMap.put(entry[0], entry[1]);
        }
        bufferedReader.close();
        return subQueryMap;
    }

    public String getSubQuery(Token token) {
        String name = token.getToken();
        return token.isKeyword() && subQueryMap.containsKey(name) ?
                subQueryMap.get(name)
                :
                subQueryMap.get(Constants.NAME).replace(Constants.NAME, name);
    }

    public boolean containsKey(String token) {
        return subQueryMap.containsKey(token);
    }

}
