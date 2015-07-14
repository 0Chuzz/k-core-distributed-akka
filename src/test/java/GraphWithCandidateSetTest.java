import kcore.structures.GraphWithCandidateSet;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by chuzz on 5/31/15.
 */
public class GraphWithCandidateSetTest {
    @Test
    public void testTest() {

        GraphWithCandidateSet g1 = new GraphWithCandidateSet();
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
        GraphWithCandidateSet g2 = new GraphWithCandidateSet();
        g2.addEdge(4, 5);
        g2.addEdge(5, 6);
        g2.addRemoteEdge(5, 7);
        g2.addRemoteEdge(4, 1);
        g2.addRemoteEdge(5, 1);
        g2.addRemoteEdge(6, 1);
        g2.addRemoteEdge(4, 2);
        g2.addRemoteEdge(5, 2);
        g2.addRemoteEdge(6, 2);

        GraphWithCandidateSet g3 = new GraphWithCandidateSet();
        g3.addEdge(7, 8);
        g3.addEdge(8, 9);
        g3.addRemoteEdge(9, 1);
        g3.addRemoteEdge(8, 2);
        g3.addRemoteEdge(7, 5);

        GraphWithCandidateSet gm = new GraphWithCandidateSet();
        gm.merge(g1);
        assertTrue("test", gm.getNodes().size() == 4);
        gm.merge(g2);
        assertTrue("test", gm.getNodes().size() == 7);
        gm.merge(g3);
        assertTrue("test", gm.getNodes().size() == 10);


        assertTrue(g1.getNodes().size() == 4);
        assertTrue(g2.getNodes().size() == 3);
        assertTrue(g3.getNodes().size() == 3);


        g1.calculateCoreness();
        g2.calculateCoreness();
        gm = new GraphWithCandidateSet();
        gm.merge(g1);
        gm.merge(g2);
        GraphWithCandidateSet gm2 = new GraphWithCandidateSet(gm, 4);
        assertTrue("some candidate node", gm2.getCandidateSet().size() > 0);
        assertTrue("some to be updated", gm2.getPrunedSet().size() > 0);

        for (int node : gm2.getCandidateSet()) {
            assertTrue(gm2.getCoreness(node) == gm.getCoreness(4));
            for (int neigh : gm2.getNeighbours(node)) {
                assertTrue(gm2.isRemote(neigh) || gm2.getCoreness(neigh) >= gm2.getCoreness(node));
            }
        }


    }

    /*
    @Test
    public void testTest2() {

        GraphWithCandidateSet g1 = new GraphWithCandidateSet();
        g1.addEdge(0, 1);
        g1.addEdge(0, 2);
        g1.addEdge(0, 3);
        g1.addEdge(2, 3);
        g1.addRemoteEdge(1, 9);
        g1.addRemoteEdge(1, 4);
        g1.addRemoteEdge(1, 5);
        g1.addRemoteEdge(1, 6);
        g1.addRemoteEdge(2, 4);

        GraphWithCandidateSet g2 = new GraphWithCandidateSet();
        g2.addEdge(4, 5);
        g2.addEdge(5, 6);

        g2.addRemoteEdge(4, 1);
        g2.addRemoteEdge(5, 1);
        g2.addRemoteEdge(6, 1);
        g2.addRemoteEdge(4, 2);

        GraphWithCandidateSet g3 = new GraphWithCandidateSet();
        g3.addEdge(7, 8);
        g3.addEdge(8, 9);
        g3.addRemoteEdge(9, 1);

        g1.calculateCoreness();
        g2.calculateCoreness();
        g3.calculateCoreness();


        GraphWithCandidateSet gm = new GraphWithCandidateSet();
        gm.merge(new GraphWithCandidateSet(g1, 2));
        gm.merge(new GraphWithCandidateSet(g2, 4));
        assertTrue("some candidate node", gm.getCandidateSet().size() > 0);
        assertTrue("some to be updated", gm.getPrunedSet().size() > 0);

        for (int node : gm.getCandidateSet()) {
            assertTrue(gm.getCoreness(node) == gm.getCoreness(4));
            for (int neigh : gm.getNeighbours(node)) {
                assertTrue(gm.getCoreness(neigh) >= gm.getCoreness(node));
            }
        }


    }
   */
}
