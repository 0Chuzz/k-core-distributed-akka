import kcore.structures.Graph;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by Stefano on 30/05/2015.
 */

public class GraphTest {
    Graph g1;
    Graph g2;
    Graph g3;

    @Test
    public void testTest() {

        init();

        Graph gm = new Graph();
        gm.merge(g1);
        assertTrue("test", gm.getNodes().size() == 4);
        gm.merge(g2);
        assertTrue("test", gm.getNodes().size() == 7);
        gm.merge(g3);
        assertTrue("test", gm.getNodes().size() == 10);

    }

    private void init() {
        g1 = new Graph();
        g1.addEdge(0, 1);
        g1.addEdge(0, 2);
        g1.addEdge(0, 3);
        g1.addEdge(2, 3);
        g2 = new Graph();
        g2.addEdge(4, 5);
        g2.addEdge(5, 6);
        g3 = new Graph();
        g3.addEdge(7, 8);
        g3.addEdge(8, 9);
    }

}
