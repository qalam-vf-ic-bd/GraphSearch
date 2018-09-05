package com.graphdb.autocomplete;

import java.util.TreeMap;

/**
 * Created by mishkat on 7/29/17.
 */
public class FSMNode {
    private String value;
    private String alternative;
    private Type type;
    private TreeMap<String, FSMNode> children;

    enum Type {
        NORMAL_NODE,
        CUSTOM_NODE,
        END_STEP,
        NAME_NODE
    }

    public FSMNode(String value) {
        this(value, Type.NORMAL_NODE);
    }

    public FSMNode(String value, Type type) {
        this (value, value, type);
    }

    public FSMNode(String value, String alternative, Type type) {
        this (value, alternative, type, new TreeMap<>());
    }

    public FSMNode(String value, String alternative, Type type, TreeMap<String, FSMNode> children) {
        this.value = value;
        this.alternative = alternative;
        this.type = type;
        this.children = children;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAlternative() {
        return alternative;
    }

    public void setAlternative(String alternative) {
        this.alternative = alternative;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public TreeMap<String, FSMNode> getChildren() {
        return children;
    }

    public void setChildren(TreeMap<String, FSMNode> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "FSMNode{" +
                "value='" + value + '\'' +
                ", alternative='" + alternative + '\'' +
                ", type=" + type +
                '}';
    }
}
