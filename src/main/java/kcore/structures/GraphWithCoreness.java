package kcore.structures;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by chuzz on 4/29/15.
 */
public class GraphWithCoreness extends GraphWithRemoteNodes {
    protected HashMap<Integer, Integer> corenessTable;

    public GraphWithCoreness() {
        super();
        corenessTable = new HashMap<Integer, Integer>();
    }

    public GraphWithCoreness(Graph g) {
        this.nodes = g.nodes;
        this.edges = g.edges;
        this.remoteEdges = new HashMap<Integer, HashSet<Integer>>();
        this.remoteNodes = new HashSet<Integer>();
        corenessTable = new HashMap<Integer, Integer>();
        calculateCoreness();
    }


    public void calculateCoreness() {
        int[] cornessArray;

        try {
            File tempFile = File.createTempFile("prefix", "suffix");
            DataOutputStream out1 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));

            int size1 = -1; //number of nodes of graph 1
            for (int node : nodes) {
                node++;
                if (size1 < node) size1 = node;
            }
            out1.writeInt(size1);
            for (int i = 0; i < size1; i++) {
                out1.writeInt(i);
                int degree = edges.containsKey(i) ? this.edges.get(i).size() : 0;
                out1.writeInt(degree);
                if (degree > 0) for (int j : this.edges.get(i)) {
                    out1.writeInt(j);
                }
            }
            out1.close();

            cornessArray = kShellCoreness(new RandomAccessFile(tempFile, "r"));
            for (int n : this.nodes) {
                corenessTable.put(n, cornessArray[n]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int[] kShellCoreness(RandomAccessFile f) throws Exception {
        int n = f.readInt();
        int[] index = new int[n];
        long[] positions = new long[n];

        int md = 0;
        for (int i = 0; i < n; i++) {
            int check = f.readInt();
            if (check != i) System.out.println("problems");
            positions[i] = f.getFilePointer();
            index[i] = f.readInt();

            f.skipBytes(4 * index[i]);
            if (index[i] > md)
                md = index[i];
        }
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

        f.close();
        return index;

    }


    public HashMap<Integer, Integer> getcorenessTable() {
        return corenessTable;
    }


    public int getCoreness(int node) {
        return corenessTable.get(node);
    }

    public void updateCorenessFrom(GraphWithCoreness graph) {
        for (int node : nodes) {
            if (graph.contains(node) || graph.isRemote(node))
                corenessTable.put(node, graph.getCoreness(node));
        }
    }

    public void updateCorenessFrom(HashSet<Integer> toBeUpdated) {
        for (int node : toBeUpdated) {
            if (corenessTable.containsKey(node)) {
                corenessTable.put(node, corenessTable.get(node) + 1);
            }
        }
    }

    public void merge(GraphWithCoreness g) {
        super.merge(g);
        corenessTable.putAll(g.corenessTable);
    }

    public void addRemoteEdge(int localNode, int remoteNode, int remoteCoreness) {
        super.addRemoteEdge(localNode, remoteNode);
        corenessTable.put(remoteNode, remoteCoreness);
    }
}
