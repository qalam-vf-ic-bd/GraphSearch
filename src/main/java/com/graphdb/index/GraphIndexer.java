package com.graphdb.index;

import com.graphdb.connection.GraphDB;
import com.graphdb.utils.PropertiesLoader;
import com.graphdb.utils.Utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class GraphIndexer implements Closeable {

    private class Config {
        private static final String ENABLE = "graph.enable";
        private static final String DEFAULT_ENABLE = "true";
    }

    private final Indexer indexer;

    public GraphIndexer() {
        Properties properties = PropertiesLoader.getInstance().getProperties();
        indexer = Boolean.parseBoolean(properties.getProperty(Config.ENABLE, Config.DEFAULT_ENABLE)) ? AsyncIndexer.getInstance() : EmptyIndexer.getInstance();
    }

    public void createUser(long userId, Map<String, Object> map) {
        indexer.createUser(userId, map);
    }

    public void updateUser(long userId, Map<String, Object> map) {
        indexer.createUser(userId, map);
    }

    public void deleteUser(long userId) {
        indexer.deleteUser(userId);
    }

    public void addFriend(long userId, long friendId) {
        indexer.addFriend(userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        indexer.removeFriend(userId, friendId);
    }

    public void userVisit(long userId, String place) {
        indexer.userVisit(userId, place);
    }

    public void userPost(long userId, List<Map<String, Object>> mediaList) {
        indexer.userPost(userId, mediaList);
    }

    @Override
    public void close() throws IOException {
        indexer.close();
    }

    public static void main(String[] args) throws IOException {
        try (GraphIndexer indexer = new GraphIndexer()) {
            for (int i = 1; i <= 100; i++) {
                indexer.createUser(i, Utils.map(GraphDB.Key.NAME, "ashraful " + i, GraphDB.Label.LIVE, "Dhaka"));
                System.out.println("User Created");
                indexer.updateUser(i, Utils.map(GraphDB.Key.NAME, "ashraful islam " + i));
                System.out.println("User Updated");
            }

            for (int i = 1; i < 100; i += 2) {
                indexer.addFriend(i, i + 1);
                System.out.println("Friendship Created");
            }

            for (int i = 1; i < 50; i += 2) {
                indexer.removeFriend(i, i + 1);
                System.out.println("Friendship deleted");
            }

            for (int i = 60; i <= 70; i++) {
                indexer.deleteUser(i);
                System.out.println("User Deleted");
            }

            for (int i = 1; i <= 50; i++) {
                indexer.userVisit(i, (i & 1) == 0 ? "Nabinagor, B-Baria" : "Dhaka");
            }

            for (int i = 1; i <= 50; i++) {
                indexer.userPost(
                        i,
                        Arrays.asList(
                                Utils.map(GraphDB.Key.ID, String.format("photo#%d", i), GraphDB.Key.TYPE, GraphDB.Type.PHOTO, GraphDB.Key.NAME, String.format("Photo %d", i)),
                                Utils.map(GraphDB.Key.ID, String.format("audio#%d", i), GraphDB.Key.TYPE, GraphDB.Type.AUDIO, GraphDB.Key.NAME, String.format("Audio %d", i)),
                                Utils.map(GraphDB.Key.ID, String.format("video#%d", i), GraphDB.Key.TYPE, GraphDB.Type.VIDEO, GraphDB.Key.NAME, String.format("Video %d", i))
                        )
                );
            }
        }
    }

}
