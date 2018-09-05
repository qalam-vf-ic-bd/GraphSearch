package com.graphdb.search;
/**
 * Created by mishkat on 7/22/17.
 */

import com.graphdb.connection.GraphDB;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import static com.graphdb.utils.Utils.resultSetToListOfVertex;

public class GraphSearchTest {

    private static GraphSearch graphSearch;
    private static GraphDB graphDB;
    private String nlQuery;
    private String query;

    @BeforeClass
    public static void init() {
        graphSearch = GraphSearch.getInstance();
        graphDB = GraphDB.getInstance();
        loadDataIfNotExist();
    }

    public static void loadDataIfNotExist() {
        if (graphDB.execute("g.V().hasNext()").one().getBoolean()) {
            return;
        }

        System.out.println("--------------------------------------------------");
        System.out.println("################# Loading data ###################");
        System.out.println("--------------------------------------------------");

        AtomicLong idGenerator = new AtomicLong(0);
        StringBuilder gremlin = new StringBuilder();
        // user
        gremlin.append(String.format("mishu = graph.addVertex('type', 'user', '%s', '%s', '%s', 'mishu');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("ashraful = graph.addVertex('type', 'user', '%s', '%s', '%s', 'ashraful');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("mishkat = graph.addVertex('type', 'user', '%s', '%s', '%s', 'mishkat');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("amlan = graph.addVertex('type', 'user', '%s', '%s', '%s', 'amlan');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("kaidul = graph.addVertex('type', 'user', '%s', '%s', '%s', 'kaidul');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("rakib = graph.addVertex('type', 'user', '%s', '%s', '%s', 'rakib');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));

        // place
        gremlin.append(String.format("dhaka = graph.addVertex('type', 'place', '%s', '%s', '%s', 'dhaka');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("chittagong = graph.addVertex('type', 'place', '%s', '%s', '%s', 'chittagong');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("khulna = graph.addVertex('type', 'place', '%s', '%s', '%s', 'khulna');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("rajshahi = graph.addVertex('type', 'place', '%s', '%s', '%s', 'rajshahi');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("barishal = graph.addVertex('type', 'place', '%s', '%s', '%s', 'barishal');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("sylhet = graph.addVertex('type', 'place', '%s', '%s', '%s', 'sylhet');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));

        // visit place
        gremlin.append(String.format("niketon = graph.addVertex('type', 'place', '%s', '%s', '%s', 'niketon, gulshan, dhaka, bangladesh');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("nabinagor = graph.addVertex('type', 'place', '%s', '%s', '%s', 'nabinagor, brahmanbaria,  bangladesh');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("dhanmondi = graph.addVertex('type', 'place', '%s', '%s', '%s', 'dhanmondi, dhaka, bangladesh');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("motijheel = graph.addVertex('type', 'place', '%s', '%s', '%s', 'motijheel, dhaka, bangladesh');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("jhautola = graph.addVertex('type', 'place', '%s', '%s', '%s', 'jhautola, chittagong, bangladesh');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("akhalia = graph.addVertex('type', 'place', '%s', '%s', '%s', 'akhalia, sylhet, bangladesh');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));

        // photo
        gremlin.append(String.format("pp_mishu = graph.addVertex('type', 'photo', '%s', '%s', '%s', 'pp_mishu');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("pp_ashraful = graph.addVertex('type', 'photo', '%s', '%s', '%s', 'pp_ashraful');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("pp_mishkat = graph.addVertex('type', 'photo', '%s', '%s', '%s', 'pp_mishkat');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("pp_amlan = graph.addVertex('type', 'photo', '%s', '%s', '%s', 'pp_amlan');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("pp_kaidul = graph.addVertex('type', 'photo', '%s', '%s', '%s', 'pp_kaidul');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("pp_rakib = graph.addVertex('type', 'photo', '%s', '%s', '%s', 'pp_rakib');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));

        // institution
        gremlin.append(String.format("buet = graph.addVertex('type', 'institution', '%s', '%s', '%s', 'bangladesh university of engineering and technology');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("kuet = graph.addVertex('type', 'institution', '%s', '%s', '%s', 'kuet');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("ruet = graph.addVertex('type', 'institution', '%s', '%s', '%s', 'ruet');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("cuet = graph.addVertex('type', 'institution', '%s', '%s', '%s', 'cuet');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("sust = graph.addVertex('type', 'institution', '%s', '%s', '%s', 'sust');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("du = graph.addVertex('type', 'institution', '%s', '%s', '%s', 'du');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));

        // organization
        gremlin.append(String.format("ipvision = graph.addVertex('type', 'organization', '%s', '%s', '%s', 'ipvision');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("pipilika = graph.addVertex('type', 'organization', '%s', '%s', '%s', 'pipilika');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("ipay = graph.addVertex('type', 'organization', '%s', '%s', '%s', 'ipay');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("veriflow = graph.addVertex('type', 'organization', '%s', '%s', '%s', 'veriflow');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("tiger_it = graph.addVertex('type', 'organization', '%s', '%s', '%s', 'tiger_it');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));
        gremlin.append(String.format("widespace = graph.addVertex('type', 'organization', '%s', '%s', '%s', 'widespace');", GraphDB.Key.ID, idGenerator.getAndIncrement(), GraphDB.Key.NAME));

        // friend
        gremlin.append("mishu.addEdge('friend', kaidul);");
        gremlin.append("kaidul.addEdge('friend', mishu);");
        gremlin.append("mishu.addEdge('friend', rakib);");
        gremlin.append("rakib.addEdge('friend', mishu);");

        gremlin.append("ashraful.addEdge('friend', mishkat);");
        gremlin.append("mishkat.addEdge('friend', ashraful);");
        gremlin.append("ashraful.addEdge('friend', kaidul);");
        gremlin.append("kaidul.addEdge('friend', ashraful);");
        gremlin.append("ashraful.addEdge('friend', rakib);");
        gremlin.append("rakib.addEdge('friend', ashraful);");

        gremlin.append("mishkat.addEdge('friend', amlan);");
        gremlin.append("amlan.addEdge('friend', mishkat);");
        gremlin.append("mishkat.addEdge('friend', kaidul);");
        gremlin.append("kaidul.addEdge('friend', mishkat);");
        gremlin.append("mishkat.addEdge('friend', rakib);");
        gremlin.append("rakib.addEdge('friend', mishkat);");

        gremlin.append("amlan.addEdge('friend', kaidul);");
        gremlin.append("kaidul.addEdge('friend', amlan);");
        gremlin.append("amlan.addEdge('friend', rakib);");
        gremlin.append("rakib.addEdge('friend', amlan);");

        gremlin.append("kaidul.addEdge('friend', rakib);");
        gremlin.append("rakib.addEdge('friend', kaidul);");

        // post
        gremlin.append("mishu.addEdge('post', pp_mishu);");
        gremlin.append("pp_mishu.addEdge('post', mishu);");
        gremlin.append("ashraful.addEdge('post', pp_ashraful);");
        gremlin.append("pp_ashraful.addEdge('post', ashraful);");
        gremlin.append("mishkat.addEdge('post', pp_mishkat);");
        gremlin.append("pp_mishkat.addEdge('post', mishkat);");
        gremlin.append("amlan.addEdge('post', pp_amlan);");
        gremlin.append("pp_amlan.addEdge('post', amlan);");
        gremlin.append("kaidul.addEdge('post', pp_kaidul);");
        gremlin.append("pp_kaidul.addEdge('post', kaidul);");
        gremlin.append("rakib.addEdge('post', pp_rakib);");
        gremlin.append("pp_rakib.addEdge('post', rakib);");

        // visit
        gremlin.append("mishu.addEdge('visit', dhaka);");
        gremlin.append("dhaka.addEdge('visit', mishu);");
        gremlin.append("mishu.addEdge('visit', rajshahi);");
        gremlin.append("rajshahi.addEdge('visit', mishu);");
        gremlin.append("mishu.addEdge('visit', barishal);");
        gremlin.append("barishal.addEdge('visit', mishu);");

        gremlin.append("ashraful.addEdge('visit', dhaka);");
        gremlin.append("dhaka.addEdge('visit', ashraful);");
        gremlin.append("ashraful.addEdge('visit', khulna);");
        gremlin.append("khulna.addEdge('visit', ashraful);");
        gremlin.append("ashraful.addEdge('visit', sylhet);");
        gremlin.append("sylhet.addEdge('visit', ashraful);");

        gremlin.append("mishkat.addEdge('visit', chittagong);");
        gremlin.append("chittagong.addEdge('visit', mishkat);");
        gremlin.append("mishkat.addEdge('visit', khulna);");
        gremlin.append("khulna.addEdge('visit', mishkat);");
        gremlin.append("mishkat.addEdge('visit', sylhet);");
        gremlin.append("sylhet.addEdge('visit', mishkat);");

        gremlin.append("amlan.addEdge('visit', chittagong);");
        gremlin.append("chittagong.addEdge('visit', amlan);");
        gremlin.append("amlan.addEdge('visit', rajshahi);");
        gremlin.append("rajshahi.addEdge('visit', amlan);");

        gremlin.append("kaidul.addEdge('visit', dhaka);");
        gremlin.append("dhaka.addEdge('visit', kaidul);");
        gremlin.append("kaidul.addEdge('visit', sylhet);");
        gremlin.append("sylhet.addEdge('visit', kaidul);");

        gremlin.append("rakib.addEdge('visit', khulna);");
        gremlin.append("khulna.addEdge('visit', rakib);");
        gremlin.append("rakib.addEdge('visit', barishal);");
        gremlin.append("barishal.addEdge('visit', rakib);");
        gremlin.append("rakib.addEdge('visit', sylhet);");
        gremlin.append("sylhet.addEdge('visit', rakib);");

        // live
        gremlin.append("ashraful.addEdge('live', nabinagor);");
        gremlin.append("nabinagor.addEdge('live', ashraful);");
        gremlin.append("mishu.addEdge('live', niketon);");
        gremlin.append("niketon.addEdge('live', mishu);");
        gremlin.append("mishkat.addEdge('live', dhanmondi);");
        gremlin.append("dhanmondi.addEdge('live', mishkat);");
        gremlin.append("amlan.addEdge('live', motijheel);");
        gremlin.append("motijheel.addEdge('live', amlan);");
        gremlin.append("rakib.addEdge('live', jhautola);");
        gremlin.append("jhautola.addEdge('live', rakib);");
        gremlin.append("kaidul.addEdge('live', akhalia);");
        gremlin.append("akhalia.addEdge('live', kaidul);");

        // study
        gremlin.append("mishu.addEdge('study', sust);");
        gremlin.append("sust.addEdge('study', mishu);");
        gremlin.append("mishu.addEdge('study', cuet);");
        gremlin.append("cuet.addEdge('study', mishu);");

        gremlin.append("ashraful.addEdge('study', sust);");
        gremlin.append("sust.addEdge('study', ashraful);");
        gremlin.append("ashraful.addEdge('study', ruet);");
        gremlin.append("ruet.addEdge('study', ashraful);");

        gremlin.append("mishkat.addEdge('study', buet);");
        gremlin.append("buet.addEdge('study', mishkat);");
        gremlin.append("mishkat.addEdge('study', buet);");
        gremlin.append("buet.addEdge('study', mishkat);");

        gremlin.append("amlan.addEdge('study', buet);");
        gremlin.append("buet.addEdge('study', amlan);");
        gremlin.append("amlan.addEdge('study', buet);");
        gremlin.append("buet.addEdge('study', amlan);");

        gremlin.append("kaidul.addEdge('study', du);");
        gremlin.append("du.addEdge('study', kaidul);");
        gremlin.append("kaidul.addEdge('study', kuet);");
        gremlin.append("kuet.addEdge('study', kaidul);");

        gremlin.append("rakib.addEdge('study', kuet);");
        gremlin.append("kuet.addEdge('study', rakib);");
        gremlin.append("rakib.addEdge('study', cuet);");
        gremlin.append("cuet.addEdge('study', rakib);");

        // work
        gremlin.append("mishu.addEdge('work', ipvision);");
        gremlin.append("ipvision.addEdge('work', mishu);");
        gremlin.append("mishu.addEdge('work', pipilika);");
        gremlin.append("pipilika.addEdge('work', mishu);");
        gremlin.append("mishu.addEdge('work', tiger_it);");
        gremlin.append("tiger_it.addEdge('work', mishu);");

        gremlin.append("ashraful.addEdge('work', ipvision);");
        gremlin.append("ipvision.addEdge('work', ashraful);");
        gremlin.append("ashraful.addEdge('work', veriflow);");
        gremlin.append("veriflow.addEdge('work', ashraful);");
        gremlin.append("ashraful.addEdge('work', widespace);");
        gremlin.append("widespace.addEdge('work', ashraful);");

        gremlin.append("mishkat.addEdge('work', ipvision);");
        gremlin.append("ipvision.addEdge('work', mishkat);");
        gremlin.append("mishkat.addEdge('work', ipay);");
        gremlin.append("ipay.addEdge('work', mishkat);");

        gremlin.append("amlan.addEdge('work', tiger_it);");
        gremlin.append("tiger_it.addEdge('work', amlan);");
        gremlin.append("amlan.addEdge('work', widespace);");
        gremlin.append("widespace.addEdge('work', amlan);");

        gremlin.append("kaidul.addEdge('work', pipilika);");
        gremlin.append("pipilika.addEdge('work', kaidul);");
        gremlin.append("kaidul.addEdge('work', veriflow);");
        gremlin.append("veriflow.addEdge('work', kaidul);");
        gremlin.append("kaidul.addEdge('work', tiger_it);");
        gremlin.append("tiger_it.addEdge('work', kaidul);");

        gremlin.append("rakib.addEdge('work', ipay);");
        gremlin.append("ipay.addEdge('work', rakib);");
        gremlin.append("rakib.addEdge('work', widespace);");
        gremlin.append("widespace.addEdge('work', rakib);");

        gremlin.append("graph.tx().commit();");

        graphDB.execute(gremlin.toString());

        ResultSet resultSet = graphDB.execute("g.V().values('name')");

        for (Result result : resultSet) {
            System.out.println(result);
        }
    }

    @AfterClass
    public static void close() throws IOException {
        graphSearch.close();
    }

    private boolean isSuccess(List<Vertex> list1, List<Vertex> list2) {
        return !list1.isEmpty() && !list2.isEmpty() && list1.size() == list2.size() && list1.containsAll(list2);
    }

    private boolean scriptException(String nlQuery) {
        try {
            graphSearch.search(0, nlQuery);
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Match Natural Language Query with a Gremlin Query
     *
     * @param nlQuery   Match Natural Language
     * @param glQueries Gremlin Query List
     * @return
     * @throws ScriptException
     */
    private boolean matchQuery(String nlQuery, String... glQueries) throws ScriptException {
        List<Vertex> nlResult = graphSearch.search(0, nlQuery);
        Set<Vertex> set = new HashSet<>();
        for (String glQuery : glQueries) {
            set.addAll(resultSetToListOfVertex(graphDB.execute(glQuery)));
        }
        return isSuccess(nlResult, new ArrayList<>(set));
    }

    @Test
    public void basicQueries() throws ScriptException {
        nlQuery = "people who posted pp_mishu";
        query = "g.V().has('type','user').where(out('post').has('name', textContains('pp_mishu')).has('type','photo')).dedup()";
        Assert.assertTrue("'" + nlQuery + "' failed to execute.",
                isSuccess(graphSearch.search(0, nlQuery), resultSetToListOfVertex(graphDB.execute(query))));

        nlQuery = "people who visited dhaka";
        query = "g.V().has('type','user').where(out('visit').has('name', textContains('dhaka')).has('type','place')).dedup()";
        Assert.assertTrue("'" + nlQuery + "' failed to execute.",
                isSuccess(graphSearch.search(0, nlQuery), resultSetToListOfVertex(graphDB.execute(query))));

        nlQuery = "people who studied at bangladesh university of engineering and technology";
        query = "g.V().has('type','user').where(out('study').has('name', textContains('bangladesh university of engineering and technology')).has('type','institution')).dedup()";
        Assert.assertTrue("'" + nlQuery + "' failed to execute.",
                isSuccess(graphSearch.search(0, nlQuery), resultSetToListOfVertex(graphDB.execute(query))));

        nlQuery = "people who work at ipvision";
        query = "g.V().has('type','user').where(out('work').has('name', textContains('ipvision')).has('type','organization')).dedup()";
        Assert.assertTrue("'" + nlQuery + "' failed to execute.",
                isSuccess(graphSearch.search(0, nlQuery), resultSetToListOfVertex(graphDB.execute(query))));
    }

    @Test
    public void basicQueriesBackward() throws ScriptException {
        nlQuery = "photos posted by mishu";
        query = "g.V().has('type','user').has('name', textContains('mishu')).out('post').dedup()";
        Assert.assertTrue("\"" + nlQuery + "' failed to execute.",
                isSuccess(graphSearch.search(0, nlQuery), resultSetToListOfVertex(graphDB.execute(query))));

        nlQuery = "places visited by mishu";
        query = "g.V().has('type','place').where(out('visit').has('name', textContains('mishu')).has('type','user')).dedup()";
        Assert.assertTrue("'" + nlQuery + "' failed to execute.",
                isSuccess(graphSearch.search(0, nlQuery), resultSetToListOfVertex(graphDB.execute(query))));

        nlQuery = "institutions studied by mishu";
        query = "g.V().has('type','institution').where(out('study').has('name', textContains('mishu')).has('type','user')).dedup()";
        Assert.assertTrue("'" + nlQuery + "' failed to execute.",
                isSuccess(graphSearch.search(0, nlQuery), resultSetToListOfVertex(graphDB.execute(query))));

        nlQuery = "organizations worked by mishu";
        query = "g.V().has('type','organization').where(out('work').has('name', textContains('mishu')).has('type','user')).dedup()";
        Assert.assertTrue("'" + nlQuery + "' failed to execute.",
                isSuccess(graphSearch.search(0, nlQuery), resultSetToListOfVertex(graphDB.execute(query))));
    }

    @Test
    public void queriesWithMyFriends() throws ScriptException {
        nlQuery = "my friends who visited dhaka";
        query = "g.V().has('type','user').has('id', '0').out('friend').has('type','user').where(out('visit').has('name', textContains('dhaka')).has('type','place')).dedup()";
        Assert.assertTrue("'" + nlQuery + "' failed to execute.",
                isSuccess(graphSearch.search(0, nlQuery), resultSetToListOfVertex(graphDB.execute(query))));
    }

    @Test
    public void queriesWithMyFriendsBackward() throws ScriptException {
        nlQuery = "places visited by my friends";
        query = "g.V().has('type','user').has('id', '0').out('friend').has('type','user').out('visit').has('type','place').dedup()";
        Assert.assertTrue("'" + nlQuery + "' failed to execute.",
                isSuccess(graphSearch.search(0, nlQuery), resultSetToListOfVertex(graphDB.execute(query))));
    }

    @Test
    public void queriesWithMyFriendsofFriends() throws ScriptException {
        nlQuery = "my friends of friends who visited dhaka";
        query = "g.V().has('type','user').has('id', '0').out('friend').out('friend').has('type','user').where(out('visit').has('name', textContains('dhaka')).has('type','place')).dedup()";
        Assert.assertTrue("'" + nlQuery + "' failed to execute.",
                isSuccess(graphSearch.search(0, nlQuery), resultSetToListOfVertex(graphDB.execute(query))));
    }

    @Test
    public void queriesWithMyFriendsofFriendsBackward() throws ScriptException {
        nlQuery = "places visited by my friends of friends";
        query = "g.V().has('type','user').has('id', '0').out('friend').out('friend').has('type','user').out('visit').has('type','place').dedup()";
        Assert.assertTrue("'" + nlQuery + "' failed to execute.",
                isSuccess(graphSearch.search(0, nlQuery), resultSetToListOfVertex(graphDB.execute(query))));
    }

    @Test
    public void ofIsRelation() throws ScriptException {
        nlQuery = "photos of mishu";
        query = "g.V().has('type','user').has('name', textContains('mishu')).out('post').has('type','photo').dedup()";
        Assert.assertTrue("'" + nlQuery + "' failed to execute.",
                isSuccess(graphSearch.search(0, nlQuery), resultSetToListOfVertex(graphDB.execute(query))));

        nlQuery = "photos of my friends";
        query = "g.V().has('type','user').has('id', '0').out('friend').has('type','user').out('post').has('type','photo').dedup()";
        Assert.assertTrue("'" + nlQuery + "' failed to execute.",
                isSuccess(graphSearch.search(0, nlQuery), resultSetToListOfVertex(graphDB.execute(query))));

        nlQuery = "photos of my friends of friends";
        query = "g.V().has('type','user').has('id', '0').out('friend').out('friend').has('type','user').out('post').has('type','photo').dedup()";
        Assert.assertTrue("'" + nlQuery + "' failed to execute.",
                isSuccess(graphSearch.search(0, nlQuery), resultSetToListOfVertex(graphDB.execute(query))));

        Assert.assertTrue("Photos Of and Me", matchQuery("photos of me", "g.V().has('id', '0').out('post')"));

        Assert.assertTrue("University of Me", matchQuery("university of me", "g.V().has('id', '0').out('study')"));

        Assert.assertTrue("Photos of My friend", matchQuery("photos of my friends", "g.V().has('id', '0').out('friend').out('post')"));

        Assert.assertTrue("Photos of Name", matchQuery("photos of ashraful", "g.V().has('type', 'user').has('name', 'ashraful').out('post')"));

        Assert.assertTrue("Photos of People", matchQuery("photos of people who visit dhaka", "g.V().has('type','user').where(out('visit').has('name', 'dhaka')).out('post')"));
    }

    @Test
    public void queriesWithConsecutiveNonKeyWord() throws ScriptException {// test 14
        nlQuery = "people who studied at bangladesh university of engineering and technology";
        query = "g.V().has('type','user').where(out('study').has('name', textContains('bangladesh university of engineering and technology')).has('type','institution')).dedup()";
        Assert.assertTrue("'" + nlQuery + "' failed to execute.",
                isSuccess(graphSearch.search(0, nlQuery), resultSetToListOfVertex(graphDB.execute(query))));
    }

    @Test
    public void queriesWithAnd() throws ScriptException {
        nlQuery = "people who posted pp_mishu and visited dhaka and studied at sust and worked at ipvision";
        query = "g.V().has('type','user').where(and(out('post').has('name', textContains('pp_mishu')),out('visit').has('name', textContains('dhaka')),out('study').has('name', textContains('sust')),out('work').has('name', textContains('ipvision')))).dedup()";
        Assert.assertTrue("'" + nlQuery + "' failed to execute.",
                isSuccess(graphSearch.search(0, nlQuery), resultSetToListOfVertex(graphDB.execute(query))));
    }

    public void splitQueryTest() throws Exception {
        Assert.assertTrue(matchQuery(
                "places visited by me and lived by my friends",
                "g.V().has('id', '0').out('visit').has('type', 'place')",
                "g.V().has('id', '0').out('friend').out('live').has('type', 'place')"
        ));

        Assert.assertTrue(matchQuery(
                "places visited by people who live in dhaka and study at sust and lived by my friends",
                "g.V().has('type','user').where(and(out('live').has('name', textContains('dhaka')), out('study').has('name', textContains('sust')))).out('visit').has('type', 'place')",
                "g.V().has('id', '0').out('friend').out('live').has('type', 'place')"
        ));

        Assert.assertTrue(matchQuery(
                "places visited by me and lived by people who study at sust and work at ipvision",
                "g.V().has('id', '0').out('visit').has('type', 'place')",
                "g.V().has('type', 'user').where(and(out('study').has('name', textContains('sust')), out('work').has('name', textContains('ipvision')))).out('live')"
        ));

        Assert.assertTrue(matchQuery(
                "places visited by ashraful and lived by my friends",
                "g.V().has('type', 'user').has('name', textContains('ashraful')).out('visit').has('type', 'place')",
                "g.V().has('id', '0').out('friend').out('live').has('type', 'place')"
        ));

        Assert.assertTrue(matchQuery(
                "university studied by me and studied by my friends",
                "g.V().has('id', '0').out('study').has('type','institution')",
                "g.V().has('id', '0').out('friend').out('study').has('type','institution')"
        ));
    }

    /**
     * We will support these query in Future
     *
     * @throws ScriptException, {@link InterruptedException}, {@link ExecutionException}
     */
    @Test
    public void futureQuery() throws ScriptException {
        //Todo
        Assert.assertFalse(matchQuery("Ashraful's Photo", "g.V().has('type','user').has('name', 'ashraful').out('post').has('type','photo')"));

        Assert.assertFalse(matchQuery(
                "people who studied at bangladesh university of engineering and technology and work at ipvision",
                "g.V().has('type','user').where(and(out('study').has('name', 'bangladesh university of engineering and technology'),out('work').has('name', 'ipvision')))"
                )
        );

        Assert.assertFalse(matchQuery(
                "universities where my friends studied",
                "g.V().has('id', '0').out('friend').out('study').has('type','institution')"
                )
        );

        Assert.assertTrue(scriptException("places where my friends visited"));
        Assert.assertTrue(scriptException("places where my friends visited who studied at sust"));

        try {
            splitQueryTest();
            Assert.fail();
        } catch (Exception e) {
            //Will suppress the error in future
        }
    }

    @Test
    public void pagingTest() throws ScriptException {
        query = "my friends of friends";
        List<Vertex> all = graphSearch.search(0, query);
        List<Vertex> paging = new ArrayList<>();

        int start = 0;
        int limit = 3;

        while (true) {
            List<Vertex> list = graphSearch.search(0, query, start, limit);
            if (list.isEmpty()) {
                break;
            }
            Assert.assertTrue("Paging not match", isSuccess(all.subList(start, start + limit), list));
            paging.addAll(list);
            start += limit;
        }

        Assert.assertTrue("Result not match", isSuccess(all, paging));
    }

    @Test
    public void namedTest() throws ScriptException {
        Assert.assertTrue(matchQuery("my friends named rakib", "g.V().has('id', '0').out('friend').has('name', textContains('rakib'))"));
        Assert.assertTrue(matchQuery("people named ashraful who live in nabinagor", "g.V().has('type', 'user').has('name', textContains('ashraful')).where(out('live').has('name', textContains('nabinagor')))"));
    }

    @Test
    public void test() throws ScriptException {
        //g.V().or(has("name", textContains("ashraful")).out("visit").has("type","place"), has("type","user").has("id", 0).out("live").has("type","place")).forEachRemaining(System.out::println);
        //graphSearch.search(0,"people who visit dhaka and live in bangladesh");
        //graphSearch.search(0, "places visited by ashraful and lived by me");
        //g.V().has("name", textContains("ashraful")),has("type","user").has("id", 0).out("live").out("visit").has("type","place").dedup()
        //graphSearch.search(0, "places my friend visit at and people lived in");
        //scriptException("places visited by me");
        //scriptException("people who visit dhaka");
        //scriptException("places visited by me and lived by my friends");
        //scriptException("places visited by people who live in dhaka and study at sust and lived by my friends");
        //scriptException("places visited by me and lived by my friends who study at sust and work at ipay");

//        Map<String, Object> params = new HashMap<>();
//        params.put("a", "name");
//        params.put("b", "qwertyqwertyqwertyqwertyqwertyqwertyqwerty");
//        params.put("c", "blablablablablablablablablablablablablabla");

        ResultSet resultSet = graphDB.execute("g.V().values(\"name\")");
//                graphDB.execute("graph.addVertex(a, b);graph.addVertex(a, c);", params);

        for (Result result : resultSet) {
            System.out.println(result.getString());
        }
    }
}
