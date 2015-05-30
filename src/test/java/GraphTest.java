import kcore.structures.Graph;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by Stefano on 30/05/2015.
 */
public class GraphTest {

    @Test
    public void testTest() {

        Graph g1 = new Graph();
        g1.addEdge(0, 1);
        g1.addEdge(0, 2);
        g1.addEdge(0, 3);
        g1.addEdge(2, 3);
        Graph g2 = new Graph();
        g2.addEdge(4, 5);
        g2.addEdge(5, 6);
        Graph g3 = new Graph();
        g3.addEdge(7, 8);
        g3.addEdge(8, 9);

        Graph gm = new Graph();
        gm.merge(g1);
        assertTrue("test", gm.getNodes().size() == 4);
        gm.merge(g2);
        assertTrue("test", gm.getNodes().size() == 7);
        gm.merge(g3);
        assertTrue("test", gm.getNodes().size() == 10);

    }

}
