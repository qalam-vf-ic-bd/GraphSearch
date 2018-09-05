package com.graphdb.index;

import com.graphdb.connection.GraphDB;
import com.graphdb.utils.Constants;
import com.graphdb.utils.Utils;
import com.lmax.disruptor.dsl.Disruptor;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;

public class AsyncIndexer implements Indexer, Closeable {

    private static final AsyncIndexer instance = new AsyncIndexer();
    private final Disruptor<IndexEvent> disruptor;
    private final IndexEventProducer eventProducer;
    private final IndexEventHandler eventHandler;


    private static final Map<String, String> PROPERTY_KEY_VERTEX = new HashMap<String, String>() {{
        put(GraphDB.Label.WORK, GraphDB.Type.ORGANIZATION);
        put(GraphDB.Label.STUDY, GraphDB.Type.INSTITUTION);
        put(GraphDB.Label.LIVE, GraphDB.Type.PLACE);
    }};

    private AsyncIndexer() {
        disruptor = new Disruptor<>(IndexEventFactory.create(), Constants.RING_BUFFER_SIZE, Executors.newCachedThreadPool());
        eventHandler = new IndexEventHandler();
        disruptor.handleEventsWith(eventHandler);
        eventProducer = new IndexEventProducer(disruptor.getRingBuffer(), Constants.INDEXER_MAX_THREAD);
        start();
    }

    private void start() {
        disruptor.start();
    }

    void crud(String id, String type, Map<String, Object> keyValue) {
        eventProducer.sendDataAsync(id, type, keyValue);
    }

    public static AsyncIndexer getInstance() {
        return instance;
    }

    private void crudUser(long userId, String opType, Map<String, Object> keyValue) {
        String userIdString = String.valueOf(userId);
        Map<String, Object> map = Utils.clone(keyValue);
        Map<String, String> propertyMap = new HashMap<>();

        for (Map.Entry<String, String> propertyKey : PROPERTY_KEY_VERTEX.entrySet()) {
            if (map.containsKey(propertyKey.getKey())) {
                String name = (String) map.remove(propertyKey.getKey());

                //Create Vertex
                crud(
                        name.trim().toLowerCase(), propertyKey.getValue(),
                        Utils.map(Constants.OP_TYPE, Constants.CREATE_VERTEX_IF_NOT_EXIST, GraphDB.Key.NAME, name)
                );

                propertyMap.put(propertyKey.getKey(), name);
            }
        }

        map.put(Constants.OP_TYPE, opType);
        crud(userIdString, GraphDB.Type.USER, map);

        for (Map.Entry<String, String> entry : propertyMap.entrySet()) {
            //Create Relation
            crud(
                    userIdString, GraphDB.Type.USER,
                    Utils.map(Constants.OP_TYPE, Constants.CREATE_RELATION_IF_NOT_EXIST, GraphDB.Key.ID, entry.getValue().trim().toLowerCase(), GraphDB.Key.TYPE, PROPERTY_KEY_VERTEX.get(entry.getKey()), GraphDB.Key.LABEL, entry.getKey())
            );
        }
    }

    public void createUser(long userId, Map<String, Object> map) {
        crudUser(userId, Constants.CREATE_VERTEX, Utils.clone(map));
    }

    public void updateUser(long userId, Map<String, Object> map) {
        crudUser(userId, Constants.UPDATE_VERTEX, Utils.clone(map));
    }

    public void deleteUser(long userId) {
        crudUser(userId, Constants.DELETE_VERTEX, Collections.emptyMap());
    }

    public void addFriend(long userId, long friendId) {
        crud(
                String.valueOf(userId),
                GraphDB.Type.USER,
                Utils.map(Constants.OP_TYPE, Constants.CREATE_RELATION, GraphDB.Key.LABEL, GraphDB.Label.FRIEND, GraphDB.Key.ID, String.valueOf(friendId), GraphDB.Key.TYPE, GraphDB.Type.USER)
        );
    }

    public void removeFriend(long userId, long friendId) {
        crud(
                String.valueOf(userId),
                GraphDB.Type.USER,
                Utils.map(Constants.OP_TYPE, Constants.DELETE_RELATION, GraphDB.Key.LABEL, GraphDB.Label.FRIEND, GraphDB.Key.ID, String.valueOf(friendId), GraphDB.Key.TYPE, GraphDB.Type.USER)
        );
    }

    public void userVisit(long userId, String place) {
        String placeId = place.trim().toLowerCase();

        crud(
                placeId, GraphDB.Type.PLACE,
                Utils.map(Constants.OP_TYPE, Constants.CREATE_VERTEX_IF_NOT_EXIST, GraphDB.Key.NAME, place)
        );

        crud(
                String.valueOf(userId), GraphDB.Type.USER,
                Utils.map(Constants.OP_TYPE, Constants.CREATE_RELATION_IF_NOT_EXIST, GraphDB.Key.LABEL, GraphDB.Label.VISIT, GraphDB.Key.ID, placeId, GraphDB.Key.TYPE, GraphDB.Type.PLACE)
        );
    }

    public void userPost(long userId, List<Map<String, Object>> mediaList) {
        for (Map<String, Object> media : mediaList) {
            String id = (String) media.get(GraphDB.Key.ID);
            String type = (String) media.get(GraphDB.Key.TYPE);

            crud(
                    id,
                    type,
                    Utils.map(Constants.OP_TYPE, Constants.CREATE_VERTEX, GraphDB.Key.NAME, media.get(GraphDB.Key.NAME))
            );

            crud(
                    String.valueOf(userId),
                    GraphDB.Type.USER,
                    Utils.map(Constants.OP_TYPE, Constants.CREATE_RELATION, GraphDB.Key.LABEL, GraphDB.Label.POST, GraphDB.Key.ID, id, GraphDB.Key.TYPE, type)
            );
        }
    }

    public void close() throws IOException {
        //Stop Event Producer Service
        eventProducer.stopService();

        //Shutdown Disruptor
        disruptor.shutdown();

        //Close Event Handler
        eventHandler.close();
    }

    public static void main(String[] args) throws Exception {
        try (AsyncIndexer asyncIndexer = AsyncIndexer.getInstance()) {
            for (int i = 1; i <= 25; i++) {
                asyncIndexer.crud(String.valueOf(i), UUID.randomUUID().toString(), Collections.emptyMap());
                Thread.sleep(100);
            }
        }
    }
}
