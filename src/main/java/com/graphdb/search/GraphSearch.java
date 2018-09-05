package com.graphdb.search;

import com.graphdb.connection.GraphDB;
import com.graphdb.utils.Utils;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static com.graphdb.utils.Utils.resultSetToListOfVertex;

/**
 * Created by ashraful on 7/16/17.
 */
public class GraphSearch implements Closeable {
    private static final GraphSearch instance;

    static {
        try {
            instance = new GraphSearch();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final Logger LOGGER = LoggerFactory.getLogger(GraphSearch.class);
    private final GraphSearchQuery graphSearchQuery;
    private final GraphDB graphDB;

    private GraphSearch() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        graphSearchQuery = GraphSearchQuery.getInstance();
        graphDB = GraphDB.getInstance();
    }

    public static GraphSearch getInstance() {
        return instance;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        try (GraphSearch search = new GraphSearch(); Scanner scanner = new Scanner(System.in)) {
            System.out.print("UserID : ");
            long sessionUserID = Long.parseLong(scanner.nextLine());

            System.out.print("Search : ");
            String query;

            do {
                query = scanner.nextLine();
                try {
                    for (Vertex vertex : search.search(sessionUserID, query)) {
                        System.out.println(vertex.value("id") + " : " + vertex.value("name"));
                    }
                } catch (ScriptException e) {
                    e.printStackTrace();
                }

                System.out.print("Search : ");
            } while (!query.equalsIgnoreCase("exit") && scanner.hasNextLine());
        }
    }

    public List<Vertex> search(long sessionUserId, String nlQuery, int start, int limit) throws ScriptException {
        nlQuery = Utils.removeNonWord(nlQuery);
        if (nlQuery.isEmpty()) return Collections.emptyList();

        LOGGER.info("userid : {} query : {} start : {} limit : {}", sessionUserId, nlQuery, start, limit);
        return resultSetToListOfVertex(graphDB.execute(String.format("%s.range(%d, %d)", graphSearchQuery.getQuery(sessionUserId, nlQuery), start, start + limit)));
    }

    public List<Vertex> search(long sessionUserId, String nlQuery) throws ScriptException {
        nlQuery = Utils.removeNonWord(nlQuery);
        if (nlQuery.isEmpty()) return Collections.emptyList();

        LOGGER.info("userid : {} query : {}", sessionUserId, nlQuery);
        return resultSetToListOfVertex(graphDB.execute(graphSearchQuery.getQuery(sessionUserId, nlQuery)));
    }


    @Override
    public void close() throws IOException {
        graphDB.close();
    }

}
