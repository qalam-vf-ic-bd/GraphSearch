package com.graphdb.index;

import java.util.List;
import java.util.Map;

public class EmptyIndexer implements Indexer {

    private static final EmptyIndexer instance = new EmptyIndexer();

    public static EmptyIndexer getInstance() {
        return instance;
    }

    @Override
    public void createUser(long userId, Map<String, Object> map) {

    }

    @Override
    public void updateUser(long userId, Map<String, Object> map) {

    }

    @Override
    public void deleteUser(long userId) {

    }

    @Override
    public void addFriend(long userId, long friendId) {

    }

    @Override
    public void removeFriend(long userId, long friendId) {

    }

    @Override
    public void userVisit(long userId, String place) {

    }

    @Override
    public void userPost(long userId, List<Map<String, Object>> mediaList) {

    }

    @Override
    public void close() {

    }
}
