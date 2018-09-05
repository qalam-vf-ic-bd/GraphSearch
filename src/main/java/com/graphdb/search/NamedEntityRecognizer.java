package com.graphdb.search;

import com.graphdb.utils.Constants;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NamedEntityRecognizer {

    private static final NamedEntityRecognizer instance;

    static {
        try {
            instance = new NamedEntityRecognizer();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final AbstractSequenceClassifier<CoreLabel> classifier;

    private NamedEntityRecognizer() throws IOException, ClassNotFoundException {
        classifier = CRFClassifier.getClassifier(Constants.NER_CLASSIFIER);
    }

    public static NamedEntityRecognizer getInstance() {
        return instance;
    }

    public ArrayList<Token> tokenize(String nlQuery) {
        ArrayList<Token> result = new ArrayList<>();
        List<List<CoreLabel>> classifiedList = classifier.classify(nlQuery);
        for (List<CoreLabel> tmp1 : classifiedList) {
            for (CoreLabel tmp2 : tmp1) {
                String key = tmp2.get(CoreAnnotations.ValueAnnotation.class);
                String type = tmp2.get(CoreAnnotations.AnswerAnnotation.class);
                if (type.equals(Constants.KEYWORD_MARK)) {
                    result.add(Token.buildToken(key));
                } else {
                    Token top = result.size() == 0 ? null : result.get(result.size() - 1);
                    if (top == null || top.isKeyword() || !top.getType().equals(type)) {
                        result.add(Token.buildToken(key, type));
                    } else {
                        top.setToken(top.getToken() + " " + key);
                    }
                }
            }
        }
        return result;
    }

}
