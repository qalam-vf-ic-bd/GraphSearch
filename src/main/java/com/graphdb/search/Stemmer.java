package com.graphdb.search;

import com.graphdb.utils.Constants;
import org.tartarus.snowball.SnowballStemmer;

public class Stemmer {
    private final SnowballStemmer snowballStemmer;
    public static final Stemmer instance;

    static {
        try {
            instance = new Stemmer();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Stemmer() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        snowballStemmer = (SnowballStemmer) Class.forName(Constants.ENGLISH_STEMMER).newInstance();
    }

    public static Stemmer getInstance() {
        return instance;
    }

    public synchronized String stem(String token) {
        snowballStemmer.setCurrent(token);
        snowballStemmer.stem();
        return snowballStemmer.getCurrent();
    }
}
