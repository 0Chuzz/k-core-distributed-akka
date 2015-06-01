import kcore.structures.GraphWithRemoteNodes;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by chuzz on 5/31/15.
 */
public class GraphWithRemoteNodesTest {

    GraphWithRemoteNodes g1;
    GraphWithRemoteNodes g2;
    GraphWithRemoteNodes g3;

    @Test
    public void testProperties() {
        init();
        assertTrue(g1.getRemoteEdges().containsKey(1));
        assertTrue(g1.getRemoteEdges().get(1).contains(4));
        assertTrue(g1.getRemoteEdges().get(1).contains(5));


    }

    @Test
    public void testMerge1() {
        init();

        GraphWithRemoteNodes gm = new GraphWithRemoteNodes();
        gm.merge(g1);
        gm.merge(g2);

        assertTrue(gm.getEdges().get(1).contains(4));
        assertTrue(gm.getEdges().get(1).contains(5));
        assertTrue(gm.getEdges().get(4).contains(1));

    }

    @Test
    public void testTest() {

        init();

        GraphWithRemoteNodes gm = new GraphWithRemoteNodes();
        gm.merge(g1);
        assertTrue("test", gm.getNodes().size() == 4);
        assertTrue("test", gm.isRemote(4));
        assertTrue(gm.getNeighbours(1).contains(4));
        assertTrue(!gm.contains(4));
        gm.merge(g2);
        assertTrue("test", gm.getNodes().size() == 7);
        assertTrue("test", !gm.isRemote(4));
        assertTrue(gm.getNeighbours(1).contains(4));
        assertTrue(gm.contains(4));
        gm.merge(g3);
        assertTrue("test", gm.getNodes().size() == 10);
        assertTrue("no more remote nodes", gm.getRemoteNodes().isEmpty());

    }

    private void init() {
        g1 = new GraphWithRemoteNodes();
        g1.addEdge(0, 1);
        g1.addEdge(0, 2);
        g1.addEdge(0, 3);
        g1.addEdge(2, 3);
        g1.addRemoteEdge(1, 9);
        g1.addRemoteEdge(1, 4);
        g1.addRemoteEdge(1, 5);
        g1.addRemoteEdge(1, 6);
        g1.addRemoteEdge(2, 4);
        g1.addRemoteEdge(2, 5);
        g1.addRemoteEdge(2, 6);
        g1.addRemoteEdge(2, 8);

        g2 = new GraphWithRemoteNodes();
        g2.addEdge(4, 5);
        g2.addEdge(5, 6);
        g2.addRemoteEdge(5, 7);
        g2.addRemoteEdge(4, 1);
        g2.addRemoteEdge(5, 1);
        g2.addRemoteEdge(6, 1);
        g2.addRemoteEdge(4, 2);
        g2.addRemoteEdge(5, 2);
        g2.addRemoteEdge(6, 2);

        g3 = new GraphWithRemoteNodes();
        g3.addEdge(7, 8);
        g3.addEdge(7, 9);
        g3.addRemoteEdge(9, 1);
        g3.addRemoteEdge(8, 2);
        g3.addRemoteEdge(7, 5);
    }
}
