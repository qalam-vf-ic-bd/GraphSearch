package com.graphdb.connection;

import com.graphdb.utils.PropertiesLoader;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.driver.ser.GryoMessageSerializerV1d0;
import org.janusgraph.graphdb.tinkerpop.JanusGraphIoRegistry;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * Created by mishkat on 9/11/17.
 */
public class GraphDB implements Closeable {

    public class Key {
        public static final String LABEL = "label";
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String TYPE = "type";
    }

    public class Type {
        public static final String USER = "user";
        public static final String PLACE = "place";
        public static final String PHOTO = "photo";
        public static final String AUDIO = "audio";
        public static final String VIDEO = "video";
        public static final String INSTITUTION = "institution";
        public static final String ORGANIZATION = "organization";
    }

    public class Label {
        public static final String FRIEND = "friend";
        public static final String LIVE = "live";
        public static final String WORK = "work";
        public static final String STUDY = "study";
        public static final String VISIT = "visit";
        public static final String POST = "post";
    }

    private class Config {
        private static final String HOSTS = "graph.hosts";
        private static final String PORT = "graph.port";
        private static final String USERNAME = "graph.username";
        private static final String PASSWORD = "graph.password";
        private static final int DEFAULT_PORT = 8182;
    }

    private static final GraphDB instance;

    static {
        try {
            instance = new GraphDB();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final Cluster cluster;
    private final Client client;

    private GraphDB() throws Exception {
        Properties properties = PropertiesLoader.getInstance().getProperties();

        if (!properties.containsKey(Config.HOSTS)) {
            throw new Exception("Host not defined");
        }
        if (!properties.containsKey(Config.USERNAME)) {
            throw new Exception("Username not defined");
        }
        if (!properties.containsKey(Config.PASSWORD)) {
            throw new Exception("Password not defined");
        }

        GryoMessageSerializerV1d0 gryoMessageSerializerV1d0 = new GryoMessageSerializerV1d0();
        gryoMessageSerializerV1d0.configure(new HashMap<String, Object>() {{
            put(GryoMessageSerializerV1d0.TOKEN_IO_REGISTRIES, Collections.singletonList(JanusGraphIoRegistry.class.getName()));
        }}, null);

        cluster = Cluster
                .build()
                .addContactPoints(properties.getProperty(Config.HOSTS).split(","))
                .port(properties.containsKey(Config.PORT) ? Integer.parseInt(properties.getProperty(Config.PORT)) : Config.DEFAULT_PORT)
                .credentials(properties.getProperty(Config.USERNAME), properties.getProperty(Config.PASSWORD))
                .serializer(gryoMessageSerializerV1d0)
                .create();
        client = cluster.connect();
    }

    public static GraphDB getInstance() {
        return instance;
    }

    public static void main(String[] args) throws IOException {
        try (GraphDB graphDB = GraphDB.getInstance()) {
            String gremlin1 = "graph.addVertex('name', 'mishkat');";
            gremlin1 += "graph.tx().commit();";
            graphDB.execute(gremlin1);

            String gremlin2 = "g.V();";
            ResultSet resultSet = graphDB.execute(gremlin2);
            for (Result result : resultSet) {
                System.out.println(result);
            }
        }
    }

    public ResultSet execute(String gremlin) {
        return client.submit(gremlin);
    }

    public ResultSet execute(String gremlin, Map<String, Object> params) {
        return client.submit(gremlin, params);
    }

    public CompletableFuture executeAsync(String gremlin) {
        return client.submitAsync(gremlin);
    }

    public CompletableFuture executeAsync(String gremlin, Map<String, Object> params) {
        return client.submitAsync(gremlin, params);
    }

    @Override
    public void close() throws IOException {
        client.close();
        cluster.close();
    }
}
