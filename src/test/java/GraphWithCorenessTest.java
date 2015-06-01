import kcore.structures.GraphWithCoreness;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by chuzz on 5/31/15.
 */
public class GraphWithCorenessTest {

    @Test
    public void testTest() {

        GraphWithCoreness g1 = new GraphWithCoreness();
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
        GraphWithCoreness g2 = new GraphWithCoreness();
        g2.addEdge(4, 5);
        g2.addEdge(5, 6);
        g2.addRemoteEdge(5, 7);
        g2.addRemoteEdge(4, 1);
        g2.addRemoteEdge(5, 1);
        g2.addRemoteEdge(6, 1);
        g2.addRemoteEdge(4, 2);
        g2.addRemoteEdge(5, 2);
        g2.addRemoteEdge(6, 2);
        GraphWithCoreness g3 = new GraphWithCoreness();
        g3.addEdge(7, 8);
        g3.addEdge(8, 9);
        g3.addRemoteEdge(9, 1);
        g3.addRemoteEdge(8, 2);
        g3.addRemoteEdge(7, 5);

        GraphWithCoreness gm = new GraphWithCoreness();
        gm.merge(g1);
        assertTrue("test", gm.getNodes().size() == 4);
        gm.merge(g2);
        assertTrue("test", gm.getNodes().size() == 7);
        gm.merge(g3);
        assertTrue("test", gm.getNodes().size() == 10);

        g1.calculateCoreness();
        assertTrue(g1.getCoreness(0) == 2);
        assertTrue(g1.getCoreness(1) == 1);

        gm.calculateCoreness();
        assertTrue("final coreness of node 0", gm.getCoreness(0) == 2);
        assertTrue("final coreness of node 2: " + gm.getCoreness(2), gm.getCoreness(2) == 3);
    }
}
