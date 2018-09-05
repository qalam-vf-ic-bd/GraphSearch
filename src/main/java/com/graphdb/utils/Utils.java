package com.graphdb.utils;

import com.graphdb.index.IndexEvent;
import com.graphdb.search.Token;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.EventTranslatorThreeArg;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static String removeNonWord(String text) {
        StringBuilder result = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(text);
        while (tokenizer.hasMoreTokens()) {
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(tokenizer.nextToken().replaceAll("[^'_\\p{L}\\p{Nd}]+", ""));
        }
        return result.toString();
    }

    public static ArrayList<Token> combineConsecutiveNonKeyWord(ArrayList<Token> tokenList) {
        ArrayList<Token> result = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        for (Token token : tokenList) {
            if (token.isKeyword()) {
                if (text.length() > 0) {
                    result.add(Token.buildToken(text.toString(), Constants.NAME));
                    text = new StringBuilder();
                }
                result.add(token);
            } else {
                if (text.length() > 0) {
                    text.append(' ');
                }
                text.append(token.getToken());
            }
        }
        if (text.length() > 0) {
            result.add(Token.buildToken(text.toString(), Constants.NAME));
        }
        return result;
    }


    /***
     *  Indexing
     */

    public static final EventTranslatorThreeArg<IndexEvent, String, String, Map<String, Object>> DATA_TRANSLATOR = new EventTranslatorThreeArg<IndexEvent, String, String, Map<String, Object>>() {
        public void translateTo(IndexEvent updateEvent, long sequence, String id, String type, Map<String, Object> keyValues) {
            updateEvent.set(EventType.DATA, id, type, keyValues);
        }
    };

    public static final EventTranslatorOneArg<IndexEvent, EventType> EVENT_TRANSLATOR = new EventTranslatorOneArg<IndexEvent, EventType>() {
        public void translateTo(IndexEvent updateEvent, long sequence, EventType eventType) {
            updateEvent.set(eventType);
        }
    };

    public static void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(Constants.MAX_WAIT_MIN, TimeUnit.MINUTES)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(Constants.MAX_WAIT_MIN, TimeUnit.MINUTES))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    public static Map<String, Object> map(Object... keyValue) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 1; i < keyValue.length; i += 2) {
            map.put((String) keyValue[i - 1], keyValue[i]);
        }
        return map;
    }

    public static Map<String, Object> clone(Map<String, Object> map) {
        return new HashMap<>(map);
    }

    public static <E> HashSet<E> newHashSet(E... elements) {
        HashSet<E> set = new HashSet<>(elements.length);
        Collections.addAll(set, elements);
        return set;
    }

    public static List<Vertex> resultSetToListOfVertex(ResultSet resultSet) {
        List<Vertex> vertexList = new ArrayList<>();
        for (Result result : resultSet) {
            vertexList.add(result.getVertex());
        }
        return vertexList;
    }
}
