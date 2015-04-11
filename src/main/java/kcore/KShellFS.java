package kcore;

import java.io.RandomAccessFile;

/**
 * This class computes the error between the correct index as computed by a
 * centralized algorithm and the current estimated index computed by our
 * decentralized protocol.
 *
 * @author Alberto Montresor
 * @version $Revision$
 */
public class KShellFS {


// --------------------------------------------------------------------------
// Variables
// --------------------------------------------------------------------------


    /**
     * Correct indexes of all nodes as computed by the centralized algorithm
     */
    private int[] index;

    /**
     * Starting position of the
     */
    private long[] positions;

// --------------------------------------------------------------------------
// Constructor
// --------------------------------------------------------------------------

    public KShellFS() {
    }

// --------------------------------------------------------------------------
// Methods
// --------------------------------------------------------------------------

    public static void main(String[] args)
            throws Exception {
        KShellFS ks = new KShellFS();
        ks.execute("test.dat");
    }

    /**
     * This function computes the correct index through the <i>O(m+n)</i>
     * algorithm of Batagelj and Zaversnik. <br>
     * V. Batagelj and M. Zaversnik,
     * "An O(m) algorithm for cores decomposition of networks", CoRR, vol.
     * cs.DS/0310049, 2003. [Online]. Available:
     * http://arxiv.org/abs/cs.DS/0310049
     */
    public int[] execute(String file) throws Exception {
        long time = System.nanoTime();

        RandomAccessFile f = new RandomAccessFile(file, "r");
        int n = f.readInt();

        //System.out.println("Size: " + n);
        index = new int[n];
        positions = new long[n];

        int md = 0;
        for (int i = 0; i < n; i++) {
            int check = f.readInt();
            if (check != i) System.out.println("problems");
            positions[i] = f.getFilePointer();
            index[i] = f.readInt();
            //System.out.println(i + " " + positions[i] + " " + index[i] + " " + (positions[i] + 4*(index[i]+1)));
            //f.seek(positions[i] + 4*(index[i]+1)); // TODO use constant
            f.skipBytes(4 * index[i]);
//		for (int j=0; j < index[i]; j++) {
//			f.readInt();
//		}
            if (index[i] > md)
                md = index[i];
        }
        //System.out.println("Max degree " + md);
        int[] bin = new int[md + 1];
        for (int i = 0; i < n; i++) {
            bin[index[i]]++;
        }
        int start = 0;
        for (int d = 0; d <= md; d++) {
            int temp = bin[d];
            bin[d] = start;
            start += temp;
        }
        int[] pos = new int[n];
        int[] vert = new int[n];
        for (int i = 0; i < n; i++) {
            pos[i] = bin[index[i]];
            vert[pos[i]] = i;
            bin[index[i]]++;
        }
        for (int d = md; d > 0; d--) {
            bin[d] = bin[d - 1];
        }
        bin[0] = 0;
        for (int i = 0; i < n; i++) {
            int v = vert[i];
            f.seek(positions[v]);
            int degree = f.readInt();
            for (int j = 0; j < degree; j++) {
                int u = f.readInt();
                if (index[u] > index[v]) {
                    int du = index[u];
                    int pu = pos[u];
                    int pw = bin[du];
                    int w = vert[pw];
                    if (u != w) {
                        pos[u] = pw;
                        vert[pu] = w;
                        pos[w] = pu;
                        vert[pw] = u;
                    }
                    bin[du]++;
                    index[u]--;
                }
            }
        }

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            //System.out.println("index " + i+ " = "+index[i]);

            if (index[i] < min)
                min = index[i];
            if (index[i] > max)
                max = index[i];
        }

        //System.out.println("min " + min);
        // System.out.println("max " + max);

        //System.out.println("TIME " + n + " " + (System.nanoTime() - time));
        f.close();
        return index;
    }

}