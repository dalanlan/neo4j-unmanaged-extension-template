package com.neo4j.example.extension;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.neo4j.graphdb.*;
import org.neo4j.server.database.CypherExecutor;
import org.neo4j.test.TestGraphDatabaseFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class MyServiceTest {

    private GraphDatabaseService graphDb;
    private MyService service;
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final RelationshipType KNOWS = DynamicRelationshipType.withName("KNOWS");
    @Mock
    private CypherExecutor cypherExecutor;

    @Before
    public void setUp() {
        graphDb = new TestGraphDatabaseFactory().newImpermanentDatabase();
        populateDb(graphDb);
        service = new MyService();
    }

    @After
    public void tearDown() throws Exception {
        graphDb.shutdown();

    }

    @Test
    public void shouldRespondToHelloWorld() {
        assertEquals("Hello World!", service.helloWorld());
    }

    @Test
    public void shouldQueryDbForFriendsWithCypher() throws IOException {
        Response response = service.getFriendsCypher("B", graphDb);
        List list = objectMapper.readValue((String) response.getEntity(), List.class);
        assertEquals(new HashSet<>(Arrays.asList("A", "C")), new HashSet<>(list));
    }

    @Test
    public void shouldQueryDbForFriendsWithJava() throws IOException {
        Response response = service.getFriendsJava("B", graphDb);
        List list = objectMapper.readValue((String) response.getEntity(), List.class);
        assertEquals(new HashSet<>(Arrays.asList("A", "C")), new HashSet<>(list));
    }

    private void populateDb(GraphDatabaseService db) {
        try(Transaction tx = db.beginTx())
        {
            Node personA = createPerson(db, "A");
            Node personB = createPerson(db, "B");
            Node personC = createPerson(db, "C");
            Node personD = createPerson(db, "D");
            personA.createRelationshipTo(personB, KNOWS);
            personB.createRelationshipTo(personC, KNOWS);
            personC.createRelationshipTo(personD, KNOWS);
            tx.success();
        }
    }

    private Node createPerson(GraphDatabaseService db, String name) {
        Node node = db.createNode(MyService.Labels.Person);
        node.setProperty("name", name);
        return node;
    }

}
