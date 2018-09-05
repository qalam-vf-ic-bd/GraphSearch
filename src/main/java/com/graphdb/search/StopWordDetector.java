package com.graphdb.search;

import com.graphdb.utils.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by mishkat, ashraful on 7/15/17.
 */
public class StopWordDetector {
    private static final StopWordDetector instance;

    static {
        try {
            instance = new StopWordDetector();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final HashSet<String> stopWordSet;

    private StopWordDetector() throws IOException {
        stopWordSet = getStopWordSet(Constants.STOP_WORD_FILENAME);
    }

    public static StopWordDetector getInstance() {
        return instance;
    }

    private HashSet<String> getStopWordSet(String fileName) throws IOException {
        HashSet<String> stopWordSet = new HashSet<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(fileName)));
        String nextLine;
        while ((nextLine = bufferedReader.readLine()) != null) {
            stopWordSet.add(nextLine);
        }
        return stopWordSet;
    }

    public boolean isStopWord(String token) {
        return stopWordSet.contains(token);
    }

    public boolean containsValue(String token) {
        return stopWordSet.contains(token);
    }

    public ArrayList<Token> removeStopWords(ArrayList<Token> tokenList) {
        ArrayList<Token> resultList = new ArrayList<>();
        for (Token token : tokenList) {
            if (!token.isKeyword() || !isStopWord(token.getToken())) {
                resultList.add(token);
            }
        }
        return resultList;
    }
}
