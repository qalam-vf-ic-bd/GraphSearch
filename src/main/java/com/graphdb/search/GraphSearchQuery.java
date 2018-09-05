package com.graphdb.search;

import com.graphdb.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tartarus.snowball.SnowballStemmer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.graphdb.utils.Constants.END_TOKEN;
import static com.graphdb.utils.Constants.START_TOKEN;
import static com.graphdb.utils.Utils.combineConsecutiveNonKeyWord;

/**
 * Created by mishkat, ashraful on 7/12/17.
 */
public class GraphSearchQuery {
    private final Logger LOGGER = LoggerFactory.getLogger(GraphSearchQuery.class);
    private final SubQueryMapper subQueryMapper;
    private final StopWordDetector stopWordDetector;
    private final Stemmer stemmer;
    private final Standardizer standardizer;
    private final NamedEntityRecognizer namedEntityRecognizer;
    private static final GraphSearchQuery instance;

    static {
        try {
            instance = new GraphSearchQuery();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private GraphSearchQuery() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        subQueryMapper = SubQueryMapper.getInstance();
        stopWordDetector = StopWordDetector.getInstance();
        stemmer = Stemmer.getInstance();
        standardizer = Standardizer.getInstance();
        namedEntityRecognizer = NamedEntityRecognizer.getInstance();
    }

    public static GraphSearchQuery getInstance() {
        return instance;
    }

    private ArrayList<Token> preProcess(ArrayList<Token> tokenList) {
        tokenList = stemNlQuery(tokenList);
        tokenList = toLowerCase(tokenList);
        tokenList = stopWordDetector.removeStopWords(tokenList);
        tokenList = standardizer.standardize(tokenList);
        tokenList = verifyAndUnStemNonKeyWords(tokenList);
        tokenList = combineConsecutiveNonKeyWord(tokenList);
        tokenList = appendStartEnd(tokenList);
        return tokenList;
    }

    private ArrayList<Token> appendStartEnd(ArrayList<Token> tokenList) {
        tokenList.add(0, START_TOKEN);
        tokenList.add(END_TOKEN);
        return tokenList;
    }

    private ArrayList<Token> verifyAndUnStemNonKeyWords(ArrayList<Token> tokenList) {
        for (Token token : tokenList) {
            if (token.isKeyword() && !subQueryMapper.containsKey(token.getToken())) {
                token.setType(Constants.NAME);
                if (token.getOriginal() != null) {
                    token.updateToken(token.getOriginal());
                }
            }
        }
        return tokenList;
    }

    private ArrayList<Token> toLowerCase(ArrayList<Token> tokenList) {
        for (Token token : tokenList) {
            token.setToken(token.getToken().toLowerCase());
            if (token.getOriginal() != null) {
                token.setOriginal(token.getOriginal().toLowerCase());
            }
        }
        return tokenList;
    }

    private ArrayList<Token> stemNlQuery(ArrayList<Token> tokenList) {
        for (Token token : tokenList) {
            if (token.isKeyword()) {
                token.updateToken(stemmer.stem(token.getToken()));
            }
        }
        return tokenList;
    }

    public String getQuery(long sessionUserId, String nlQuery) {
        ArrayList<Token> tokenList = namedEntityRecognizer.tokenize(nlQuery);
        tokenList = preProcess(tokenList);
        return tokenListToGremlinQuery(sessionUserId, tokenList);
    }

    public String tokenListToGremlinQuery(long sessionUserId, List<Token> tokenList) {
        String query = "%s";

        for (Token token : tokenList) {
            query = String.format(query, subQueryMapper.getSubQuery(token).replace(Constants.ME, String.valueOf(sessionUserId)));
            LOGGER.info("Preparing Query : {}{}", Constants.QUERY_PREFIX, query);
        }

        return Constants.QUERY_PREFIX + query + Constants.QUERY_SUFFIX;
    }
}
