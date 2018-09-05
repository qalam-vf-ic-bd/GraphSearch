package com.graphdb.autocomplete;

import com.graphdb.autocomplete.FSMNode.Type;
import com.graphdb.search.Token;
import com.graphdb.utils.Constants;
import com.graphdb.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static com.graphdb.utils.Utils.combineConsecutiveNonKeyWord;

/**
 * Created by mishkat on 7/29/17.
 */
public class Suggester {

    private final Map<String, Set<String>> ruleMap;
    private final Map<String, Set<String>> synonymMap;
    private final TreeMap<String, FSMNode> nodeMap;
    private final FSMNode start;

    private static final Suggester instance;

    static {
        try {
            instance = new Suggester();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Suggester() throws IOException {
        ruleMap = new HashMap<>();
        synonymMap = new HashMap<>();
        nodeMap = new TreeMap<>();
        readRules();
        start = buildGraph();
    }

    public static Suggester getInstance() {
        return instance;
    }

    private SortedMap<String, FSMNode> prefixList(TreeMap<String, FSMNode> map, String prefix) {
        return map.subMap(prefix, prefix + Character.MAX_VALUE);
    }

    private SortedMap<String, FSMNode> nameNodeList(TreeMap<String, FSMNode> child, String word) {
        SortedMap<String, FSMNode> result = new TreeMap<>();
        for (Map.Entry<String, FSMNode> entry : child.entrySet()) {
            if (entry.getValue().getType() == Type.NAME_NODE) {
                result.put(
                        entry.getKey(),
                        new FSMNode(
                                word,
                                word,
                                Type.CUSTOM_NODE,
                                entry.getValue().getChildren()
                        )
                );
            }
        }
        return result;
    }

    private void readRules() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(Constants.RULE_FILEPATH)));
        synonymMap.put(Constants.END, Collections.singleton(Constants.END));

        String nextLine;
        while ((nextLine = bufferedReader.readLine()) != null) {
            String[] key_value = nextLine.replaceAll("\\s+", " ").split(Constants.EQUAL_DELIMITER);

            String[] keys = key_value[0].split(Constants.COMMA_DELIMITER);
            String[] values = key_value[1].split(Constants.COMMA_DELIMITER);

            synonymMap.put(keys[0], Utils.newHashSet(keys));
            ruleMap.put(keys[0], Utils.newHashSet(values));
        }
    }

    private FSMNode buildGraph() {
        for (HashMap.Entry<String, Set<String>> entry : synonymMap.entrySet()) {
            String key = entry.getKey();
            for (String value : entry.getValue()) {
                nodeMap.put(value, new FSMNode(key, value, key.matches(Constants.NAME_NODE_REGEX) ? Type.NAME_NODE : Type.NORMAL_NODE));
            }
        }

        nodeMap.put(Constants.END, new FSMNode(Constants.END, Type.END_STEP));

        for (HashMap.Entry<String, Set<String>> entry : ruleMap.entrySet()) {
            String key = entry.getKey();
            for (String value : entry.getValue()) {
                for (String x : synonymMap.get(key)) {
                    for (String y : synonymMap.get(value)) {
                        nodeMap.get(x).getChildren().put(y, nodeMap.get(y));
                    }
                }
            }
        }

        return nodeMap.get(Constants.START);
    }

    private Set<String> suggest(FSMNode current, ArrayList<Token> words, int index, boolean isExpanding, int level) {
        Set<String> results = new TreeSet<>();
        TreeMap<String, FSMNode> child = current.getChildren();

        //Todo
        if (current.getType() == Type.NAME_NODE) {
            return null;
        }

        if (child.isEmpty() || level == 0) {
            return isExpanding ? results : null;
        }

        if (index < words.size()) {
            SortedMap<String, FSMNode> nextNodes;

            if (words.get(index).isKeyword()) {
                nextNodes = prefixList(child, words.get(index).getToken());
            } else {
                nextNodes = nameNodeList(child, words.get(index).getToken());
            }

            if (nextNodes.isEmpty()) {
                for (FSMNode node : child.values()) {
                    Set<String> list = suggest(node, words, index, false, level - 1);

                    if (list != null) {
                        for (String next : list) {
                            results.add(node.getValue() + " " + next);
                        }
                    }
                }
            } else {
                for (FSMNode node : nextNodes.values()) {
                    Set<String> list = suggest(node, words, index + 1, words.size() == index + 1, level - 1);
                    if (list != null) {
                        String value = node.getType() == Type.END_STEP ? "" : node.getAlternative();
                        if (list.isEmpty()) {
                            results.add(value);
                        } else {
                            for (String next : list) {
                                results.add(value + " " + next);
                            }
                        }
                    }
                }
            }
        } else if (isExpanding) {
            for (FSMNode node : child.values()) {
                Set<String> list = suggest(node, words, index, true, level - 1);
                if (list != null) {
                    String value = node.getType() == Type.END_STEP ? "" : node.getValue();
                    if (list.isEmpty()) {
                        results.add(value);
                    } else {
                        for (String next : list) {
                            results.add(value + " " + next);
                        }
                    }
                }
            }
        }

        return results;
    }

    public Set<String> suggest(String prefix) {
        prefix = Utils.removeNonWord(prefix);
        String[] words = prefix.toLowerCase().split(Constants.PREFIX_DELIMITER);
        ArrayList<Token> tokens = new ArrayList<>(words.length);
        for (String word : words) {
            tokens.add(Token.buildToken(word, prefixList(nodeMap, word).isEmpty() ? Constants.NAME : Constants.KEYWORD_MARK));
        }
        tokens = combineConsecutiveNonKeyWord(tokens);
        return suggest(start, tokens, 0, false, words.length + Constants.MAX_LEVEL);
    }

    public static void main(String[] args) {
        Suggester suggester = Suggester.getInstance();
        for (String suggestion : suggester.suggest("people ash")) {
            System.out.println(suggestion);
        }
    }
}