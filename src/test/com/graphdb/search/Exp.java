package com.graphdb.search;

import com.graphdb.connection.GraphDB;
import com.graphdb.utils.Utils;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.script.ScriptException;

public class Exp {

    private static GraphSearch graphSearch;
    private static GraphDB graphDB;

    @BeforeClass
    public static void init() {
        graphSearch = GraphSearch.getInstance();
        graphDB = GraphDB.getInstance();
    }

    @Test
    public void test() throws ScriptException {
        ResultSet resultSet = graphDB.execute("g.V()");
        for (Vertex vertex : Utils.resultSetToListOfVertex(resultSet)) {
            System.out.println(vertex + " : " + vertex.property("name"));
            graphDB.execute(String.format("g.V(%d).outE()", (long) vertex.id())).stream().forEach(result -> System.out.println(result.getEdge()));
        }
    }
}
