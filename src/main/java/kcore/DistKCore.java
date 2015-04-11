package kcore;

import java.io.*;


public class DistKCore {

    public static IntGraph graphConstruction(String file) throws IOException {
        int size = 100;
        //int startFrom;
        String line;

        BufferedReader reader = new BufferedReader(new FileReader(file));
        //line=reader.readLine();
        //size=Integer.parseInt(line);
        //line=reader.readLine();
        //startFrom=Integer.parseInt(line);
        IntGraph gr = new IntGraph(size, 1);
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\\s");
            //System.out.println("Edge ("+parts[0]+","+parts[1]+")");
            gr.addEdge(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            gr.addEdge(Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
        }

        reader.close();

        return gr;
    }

    public static void graphFileConstruction(IntGraph graph, String filename) throws Exception {

        //file containing graph1
        DataOutputStream out1 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));

        int size1 = graph.size(); //number of nodes of graph 1
        //System.out.println(size1);
        out1.writeInt(size1);
        for (int i = 0; i < size1; i++) {
            out1.writeInt(i);
            int degree = graph.neighbors(i).size();
            out1.writeInt(degree);
            for (int j = 0; j < degree; j++) {
                out1.writeInt(graph.neighbors(i).get(j));
            }
        }
        out1.close();


    }

    public static IntGraph computekCoreOnPartition(String originalFile, String graphFile) throws Exception {
        //transform the original dataset file to a graph object IntGraph
        IntGraph part1 = DistKCore.graphConstruction(originalFile);

        //transform the graph object IntGraph to a binary file
        DistKCore.graphFileConstruction(part1, graphFile);


        //step 1: execution of the standard k-core decomposition algorithm in parallel
        //System.out.println("****** Step 1: Computing the coreness in local partitions ******");
        KShellFS ks = new KShellFS();
        int[] corenessTable = ks.execute(graphFile);


        System.out.println("Step 1: Done!");
        return part1;
    }

    public static void main(String[] args) {
        try {
            computekCoreOnPartition("graphfile", "test.dat");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
