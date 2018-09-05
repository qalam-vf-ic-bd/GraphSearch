package com.graphdb.index;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface Indexer {
    public void createUser(long userId, Map<String, Object> map);

    public void updateUser(long userId, Map<String, Object> map);

    public void deleteUser(long userId);

    public void addFriend(long userId, long friendId);

    public void removeFriend(long userId, long friendId);

    public void userVisit(long userId, String place);

    public void userPost(long userId, List<Map<String, Object>> mediaList);

    public void close() throws IOException;
}
