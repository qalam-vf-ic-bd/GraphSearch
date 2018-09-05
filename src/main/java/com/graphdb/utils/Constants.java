package com.graphdb.utils;

import com.graphdb.search.Token;

/**
 * Created by mishkat, ashraful on 7/12/17.
 */
public class Constants {
    /**
     * Graph Search Constants
     */
    public static final String SUB_QUERY_FILENAME = "subquery.txt";
    public static final String STOP_WORD_FILENAME = "stopword.txt";
    public static final String SYNONYM_FILENAME = "synonym.txt";
    public static final String SYNONYM_SEPARATOR = "\\s*=>\\s*";
    public static final String ME = "[me]";
    public static final String START = "[start]";
    public static final String NAME = "[name]";
    public static final String END = "[end]";
    public static final String AND = "and";
    public static final String QUERY_PREFIX = "g.V()";
    public static final String QUERY_SUFFIX = ".dedup()";
    public static final String NER_CLASSIFIER = "models/english.all.3class.caseless.distsim.crf.ser.gz";
    public static final String KEYWORD_MARK = "O";
    public static final String ENGLISH_STEMMER = "org.tartarus.snowball.ext.englishStemmer";
    public static final String RULE_FILEPATH = "rule.txt";
    public static final String EQUAL_DELIMITER = "\\s*=\\s*";
    public static final String COMMA_DELIMITER = "\\s*(?:,|$)\\s*";
    public static final String PREFIX_DELIMITER = "\\s+";
    public static final String NAME_NODE_REGEX = "^\\[[a-z_]+_name\\]$";
    public static final int MAX_LEVEL = 3;

    public static final Token START_TOKEN = Token.buildToken(Constants.START);
    public static final Token END_TOKEN = Token.buildToken(Constants.END);


    /***
     *  Indexing Constants
     */
    public static final int INDEXER_MAX_THREAD = 1; //In our Case It Must be Single Thread
    public static final int RING_BUFFER_SIZE = 128;
    public static final int MAX_WAIT_MIN = 2;

    public static final String OP_TYPE = "op_type";

    public static final String CREATE_VERTEX = "create_vertex";
    public static final String UPDATE_VERTEX = "update_vertex";
    public static final String DELETE_VERTEX = "delete_vertex";
    public static final String CREATE_RELATION = "create_relation";
    public static final String UPDATE_RELATION = "update_relation";
    public static final String DELETE_RELATION = "delete_relation";
    public static final String CREATE_VERTEX_IF_NOT_EXIST = "create_vertex_if_not_exist";
    public static final String CREATE_RELATION_IF_NOT_EXIST = "create_relation_if_not_exist";

    public static final String CONFIG_FILE = "StorageConfig.properties";
}
