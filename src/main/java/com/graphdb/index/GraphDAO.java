package com.graphdb.index;

import com.google.gson.Gson;
import com.graphdb.connection.GraphDB;
import com.graphdb.utils.Constants;
import com.graphdb.utils.Utils;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.graphdb.utils.Constants.*;

/**
 * Created by mishkat on 8/24/17.
 */
public class GraphDAO implements Closeable {
    private final GraphDB graphDB;
    private final Logger LOGGER = LoggerFactory.getLogger(GraphDAO.class);
    private final Gson gson;

    private static final Set<String> SPACIAL_KEY = new HashSet<String>() {{
        add(Constants.OP_TYPE);
        add(GraphDB.Key.ID);
        add(GraphDB.Key.TYPE);
        add(GraphDB.Key.LABEL);
    }};

    public GraphDAO() {
        this.graphDB = GraphDB.getInstance();
        this.gson = new Gson();
    }

    public boolean crud(String id, String type, Map<String, Object> keyValues) {
        LOGGER.info("ID : {}, Type : {}, Map : {}", id, type, keyValues);
        if (keyValues.containsKey(OP_TYPE)) {
            String opType = (String) keyValues.get(OP_TYPE);
            try {
                switch (opType) {
                    case CREATE_VERTEX:
                        return createVertex(id, type, keyValues);
                    case UPDATE_VERTEX:
                        return updateVertex(id, type, keyValues);
                    case DELETE_VERTEX:
                        return deleteVertex(id, type);
                    case CREATE_RELATION:
                        return createRelation(id, type, keyValues);
                    case UPDATE_RELATION:
                        return updateRelation(id, type, keyValues);
                    case DELETE_RELATION:
                        return deleteRelation(id, type, keyValues);
                    case CREATE_VERTEX_IF_NOT_EXIST:
                        return createVertexIfNotExist(id, type, keyValues);
                    case CREATE_RELATION_IF_NOT_EXIST:
                        return createRelationIfNotExist(id, type, keyValues);
                }
            } catch (Exception e) {
                LOGGER.error(opType, e);
                return false;
            }
        }
        return false;
    }

    private boolean createRelationIfNotExist(String id, String type, Map<String, Object> keyValues) {
        Map<String, Object> params = new HashMap<>();
        params.put("param1", GraphDB.Key.ID);
        params.put("param2", id);
        params.put("param3", GraphDB.Key.TYPE);
        params.put("param4", type);
        params.put("param5", keyValues.get(GraphDB.Key.LABEL));
        params.put("param6", keyValues.get(GraphDB.Key.ID));
        params.put("param7", keyValues.get(GraphDB.Key.TYPE));

        String gremlin1 = "g.V().has(param1, param2).has(param3, param4).outE(param5).where(__.inV().has(param1, param6).has(param3, param7)).hasNext();";
        boolean r1Exist = graphDB.execute(gremlin1, params).one().getBoolean();
        String gremlin2 = "g.V().has(param1, param2).has(param3, param4).inE(param5).where(__.outV().has(param1, param6).has(param3, param7)).hasNext();";
        boolean r2Exist = graphDB.execute(gremlin2, params).one().getBoolean();
        return (r1Exist && r2Exist) || createRelation(id, type, keyValues);
    }

    private boolean createVertexIfNotExist(String id, String type, Map<String, Object> keyValues) {
        Map<String, Object> params = new HashMap<>();
        params.put("param1", GraphDB.Key.ID);
        params.put("param2", id);
        params.put("param3", GraphDB.Key.TYPE);
        params.put("param4", type);

        String gremlin = "g.V().has(param1, param2).has(param3, param4).hasNext();";
        boolean exist = graphDB.execute(gremlin, params).one().getBoolean();
        return exist || createVertex(id, type, keyValues);
    }

    /**
     * @param id
     * @param type
     * @param keyValues
     */
    private boolean createVertex(String id, String type, Map<String, Object> keyValues) { //ok
        Map<String, Object> params = new HashMap<>();
        params.put("param1", GraphDB.Key.ID);
        params.put("param2", id);
        params.put("param3", GraphDB.Key.TYPE);
        params.put("param4", type);

        StringBuilder gremlin = new StringBuilder("vertex = graph.addVertex();");
        gremlin.append("vertex.property(param1, param2);");
        gremlin.append("vertex.property(param3, param4);");

        int i = 5;
        for (Map.Entry<String, Object> entry : keyValues.entrySet()) {
            if (!SPACIAL_KEY.contains(entry.getKey())) {
                String paramX = "param" + i++;
                String paramY = "param" + i++;
                params.put(paramX, entry.getKey());
                params.put(paramY, entry.getValue());
                gremlin.append("vertex.property(").append(paramX).append(", ").append(paramY).append(");");
            }
        }
        graphDB.execute(gremlin.toString(), params);

        return true;
    }

    /**
     * @param id
     * @param type
     * @param keyValues
     */
    private boolean updateVertex(String id, String type, Map<String, Object> keyValues) { //ok
        Map<String, Object> params = new HashMap<>();
        params.put("param1", GraphDB.Key.ID);
        params.put("param2", id);
        params.put("param3", GraphDB.Key.TYPE);
        params.put("param4", type);

        String gremlin1 = "g.V().has(param1, param2).has(param3, param4).hasNext();";
        Boolean boolean1 = graphDB.execute(gremlin1, params).one().getBoolean();
        if (boolean1) {
            StringBuilder gremlin2 = new StringBuilder("vertex = g.V().has(param1, param2).has(param3, param4).next();");

            int i = 5;
            for (Map.Entry<String, Object> entry : keyValues.entrySet()) {
                if (!SPACIAL_KEY.contains(entry.getKey())) {
                    String paramX = "param" + i++;
                    String paramY = "param" + i++;
                    params.put(paramX, entry.getKey());
                    params.put(paramY, entry.getValue());
                    gremlin2.append("vertex.property(").append(paramX).append(", ").append(paramY).append(");");
                }
            }
            graphDB.execute(gremlin2.toString(), params);
            return true;
        } else {
            return createVertex(id, type, keyValues);
        }
    }

    /**
     * @param id
     * @param type
     */
    private boolean deleteVertex(String id, String type) { //ok
        Map<String, Object> params = new HashMap<>();
        params.put("param1", GraphDB.Key.ID);
        params.put("param2", id);
        params.put("param3", GraphDB.Key.TYPE);
        params.put("param4", type);
        String gremlin = "g.V().has(param1, param2).has(param3, param4).next().remove();";
        graphDB.execute(gremlin, params);
        return true;
    }

    /**
     * @param id
     * @param type
     * @param keyValues
     */
    private boolean createRelation(String id, String type, Map<String, Object> keyValues) { //ok
        Map<String, Object> params = new HashMap<>();
        params.put("param1", GraphDB.Key.ID);
        params.put("param2", id);
        params.put("param3", GraphDB.Key.TYPE);
        params.put("param4", type);
        params.put("param5", keyValues.get(GraphDB.Key.ID));
        params.put("param6", keyValues.get(GraphDB.Key.TYPE));
        params.put("param7", keyValues.get(GraphDB.Key.LABEL));
        StringBuilder gremlin = new StringBuilder("vertex1 = g.V().has(param1, param2).has(param3, param4).next();");
        gremlin.append("vertex2 = g.V().has(param1, param5).has(param3, param6).next();");
        gremlin.append("edge1 = vertex1.addEdge(param7, vertex2);");
        gremlin.append("edge2 = vertex2.addEdge(param7, vertex1);");

        int i = 8;
        for (Map.Entry<String, Object> entry : keyValues.entrySet()) {
            if (!SPACIAL_KEY.contains(entry.getKey())) {
                String paramX = "param" + i++;
                String paramY = "param" + i++;
                params.put(paramX, entry.getKey());
                params.put(paramY, entry.getValue());
                gremlin.append("edge1.property(").append(paramX).append(", ").append(paramY).append(");");
                gremlin.append("edge2.property(").append(paramX).append(", ").append(paramY).append(");");
            }
        }
        graphDB.execute(gremlin.toString(), params);

        return true;
    }

    /**
     * @param id
     * @param type
     * @param keyValues
     */
    private boolean updateRelation(String id, String type, Map<String, Object> keyValues) { //ok
        Map<String, Object> params = new HashMap<>();
        params.put("param1", GraphDB.Key.ID);
        params.put("param2", id);
        params.put("param3", GraphDB.Key.TYPE);
        params.put("param4", type);
        params.put("param5", keyValues.get(GraphDB.Key.LABEL));
        params.put("param6", keyValues.get(GraphDB.Key.ID));
        params.put("param7", keyValues.get(GraphDB.Key.TYPE));
        String gremlin1 = "g.V().has(param1, param2).has(param3, param4).outE(param5).where(__.inV().has(param1, param6).has(param3, param7)).hasNext();";
        Boolean boolean1 = graphDB.execute(gremlin1, params).one().getBoolean();
        String gremlin2 = "g.V().has(param1, param2).has(param3, param4).inE(param5).where(__.outV().has(param1, param6).has(param3, param7)).hasNext();";
        Boolean boolean2 = graphDB.execute(gremlin2, params).one().getBoolean();

        if (boolean1 && boolean2) {
            String gremlin3 = "edge1 = g.V().has(param1, param2).has(param3, param4).outE(param5).where(__.inV().has(param1, param6).has(param3, param7)).next();";
            String gremlin4 = "edge2 = g.V().has(param1, param2).has(param3, param4).inE(param5).where(__.outV().has(param1, param6).has(param3, param7)).next();";

            StringBuilder gremlin5 = new StringBuilder(gremlin3 + gremlin4);

            int i = 8;
            for (Map.Entry<String, Object> entry : keyValues.entrySet()) {
                if (!SPACIAL_KEY.contains(entry.getKey())) {
                    String paramX = "param" + i++;
                    String paramY = "param" + i++;
                    params.put(paramX, entry.getKey());
                    params.put(paramY, entry.getValue());
                    gremlin5.append("edge1.property(").append(paramX).append(", ").append(paramY).append(");");
                    gremlin5.append("edge2.property(").append(paramX).append(", ").append(paramY).append(");");
                }
            }
            graphDB.execute(gremlin5.toString(), params);
            return true;
        } else {
            return createRelation(id, type, keyValues);
        }
    }

    /**
     * @param id
     * @param type
     * @param keyValues
     */
    private boolean deleteRelation(String id, String type, Map<String, Object> keyValues) { //ok
        Map<String, Object> params = new HashMap<>();
        params.put("param1", GraphDB.Key.ID);
        params.put("param2", id);
        params.put("param3", GraphDB.Key.TYPE);
        params.put("param4", type);
        params.put("param5", keyValues.get(GraphDB.Key.LABEL));
        params.put("param6", keyValues.get(GraphDB.Key.ID));
        params.put("param7", keyValues.get(GraphDB.Key.TYPE));
        String gremlin1 = "g.V().has(param1, param2).has(param3, param4).outE(param5).where(__.inV().has(param1, param6).has(param3, param7)).next().remove();";
        graphDB.execute(gremlin1, params);
        String gremlin2 = "g.V().has(param1, param2).has(param3, param4).inE(param5).where(__.outV().has(param1, param6).has(param3, param7)).next().remove();";
        graphDB.execute(gremlin2, params);
        return true;
    }

    public void errorLog(IndexEvent indexEvent) {
        LOGGER.error(gson.toJson(indexEvent));
    }


    @Override
    public void close() throws IOException {
        graphDB.close();
    }


    public static void main(String[] args) throws ScriptException, IOException, InterruptedException {
        ResultSet resultSet;

        try (GraphDAO graphDAO = new GraphDAO()) {
            graphDAO.crud("123456789", GraphDB.Type.USER, Utils.map(OP_TYPE, CREATE_VERTEX, GraphDB.Key.NAME, "mishu"));
            graphDAO.crud("987654321", GraphDB.Type.USER, Utils.map(OP_TYPE, CREATE_VERTEX, GraphDB.Key.NAME, "mishkat"));
            resultSet = graphDAO.graphDB.execute("g.V()");
            for (Result result : resultSet) System.out.println(result);
            System.out.println("_________________________________________________________________________________________");


            graphDAO.crud("123456789", GraphDB.Type.USER, Utils.map(OP_TYPE, UPDATE_VERTEX, "name", "m!shu"));
            resultSet = graphDAO.graphDB.execute("g.V().values(\"name\")");
            for (Result result : resultSet) System.out.println(result);
            System.out.println("_________________________________________________________________________________________");


            graphDAO.crud("123456789", GraphDB.Type.USER, Utils.map(OP_TYPE, CREATE_RELATION, GraphDB.Key.ID, "987654321", GraphDB.Key.TYPE, GraphDB.Type.USER, GraphDB.Key.LABEL, GraphDB.Label.FRIEND, "property_key", "property_value"));
            resultSet = graphDAO.graphDB.execute("g.E()");
            for (Result result : resultSet) System.out.println(result);
            System.out.println("_________________________________________________________________________________________");


            graphDAO.crud("123456789", GraphDB.Type.USER, Utils.map(OP_TYPE, UPDATE_RELATION, GraphDB.Key.ID, "987654321", GraphDB.Key.TYPE, GraphDB.Type.USER, GraphDB.Key.LABEL, GraphDB.Label.FRIEND, "property_key", "alt_value"));
            resultSet = graphDAO.graphDB.execute("g.E().values(\"property_key\")");
            for (Result result : resultSet) System.out.println(result);
            System.out.println("_________________________________________________________________________________________");


            graphDAO.crud("123456789", GraphDB.Type.USER, Utils.map(OP_TYPE, DELETE_RELATION, GraphDB.Key.ID, "987654321", GraphDB.Key.TYPE, GraphDB.Type.USER, GraphDB.Key.LABEL, GraphDB.Label.FRIEND));
            resultSet = graphDAO.graphDB.execute("g.E()");
            for (Result result : resultSet) System.out.println(result);
            System.out.println("_________________________________________________________________________________________");


            graphDAO.crud("123456789", GraphDB.Type.USER, Utils.map(OP_TYPE, DELETE_VERTEX));
            resultSet = graphDAO.graphDB.execute("g.V()");
            for (Result result : resultSet) System.out.println(result);
            System.out.println("_________________________________________________________________________________________");
        }
    }
}
