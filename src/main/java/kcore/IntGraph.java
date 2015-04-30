package kcore;

import kcore.structures.Graph;

import java.util.Hashtable;
import java.util.Vector;

public class IntGraph extends Graph {

    public Vector<Integer> frontierNodes;
//private int startFrom; 
public int[] CorenessTable;
    public Vector<Integer> reachableNodes;
    //A hashtable containing the coreness of all nodes of the current partition and the frontier nodes
    public Hashtable<Integer, Integer> distNeighbCoreness;
    //A hashtable containing the identifier of all frontier nodes
    public Hashtable<Integer, Integer> distNeighbPartition;
    private int size;
    private IntArray[] neighbors;
    private int[] neighborsPart;
    private Vector<Integer> visitedNodes;
    private int partition;


    public IntGraph(int size, int part) {
        this.size = size;
        //this.startFrom = startFrom;

        neighbors = new IntArray[size];

        neighborsPart = new int[size];
        CorenessTable = new int[size];
        visitedNodes = new Vector<Integer>();
        reachableNodes = new Vector<Integer>();
        frontierNodes = new Vector<Integer>();
        partition = part;
        distNeighbCoreness = new Hashtable<Integer, Integer>();
        distNeighbPartition = new Hashtable<Integer, Integer>();
    }

    public void intialize() {
        visitedNodes.removeAllElements();
        reachableNodes.removeAllElements();
    }

    public int size() {
        return size;
    }


    //candidate nodes
    public Vector toBeUpdatedNodes(Edge e) {
        if (e == null) {
            return null;
        }
        //get the partition of the distant node
        int part = e.partition2;
        Vector v1, v2;
        //get the coreness of the distant node
        int corenessNode2 = e.coreness2;
        if (CorenessTable[e.node1] < CorenessTable[e.node2]) {
            //System.out.println("Candidate nodes are nodes of the induced core subgraph from node "+e.node1);
            inducedCoreSubgraph(e.node1);

		/*
        for(int i=0;i<reachableNodes.size();i++)
		{
			System.out.println(reachableNodes.elementAt(i)+" is a candidate node");
		}
		*/
            //Looking for the set of nodes that need to be updated
            Vector toBeUpdated = pruneCandidateNodes(reachableNodes);

            return toBeUpdated;

        } else if (CorenessTable[e.node1] > CorenessTable[e.node2]) {
            //System.out.println("candidate nodes are nodes of the induced core subgraph from node "+e.node2);
            //System.out.println("Remote call to partition "+part+" in order to look for candidate nodes");
            inducedCoreSubgraph(e.node2);

		/*
		for(int i=0;i<reachableNodes.size();i++)
		{
			System.out.println(reachableNodes.elementAt(i)+" is a candidate node");
		}
		*/
            //Looking for the set of nodes that need to be updated
            Vector toBeUpdated = pruneCandidateNodes(reachableNodes);

            return toBeUpdated;
        } else {
            //System.out.println("candidate nodes are union of the induced core subgraph from node "+e.node1+" the induced core subgraph from node "+e.node2);
            //System.out.println("Local call to partition "+e.partition1+" in order to look for candidate nodes");

            //System.out.println("Remote call to partition "+part+" in order to look for candidate nodes");

            inducedCoreSubgraph(e.node1);
            inducedCoreSubgraph(e.node2);

		/*
		for(int i=0;i<reachableNodes.size();i++)
		{
			System.out.println(reachableNodes.elementAt(i)+" is a candidate node");
		}
		*/

            //Looking for the set of nodes that need to be updated
            Vector toBeUpdated = pruneCandidateNodes(reachableNodes);

            return toBeUpdated;
        }

    }

    //set the initial coreness of nodes in this partition
    public void initialcoreness(int[] tab) {
        for (int i = 0; i < size; i++) {
            //System.out.println("Coreness of node "+i+" updated");
            //CorenessTable[i]=tab[i];
            //if(tab[i]!=0)
            //{
            distNeighbCoreness.put(i, tab[i]);
            CorenessTable[i] = tab[i];
            //}
        }
    }


    //update the coreness of candidate nodes in this partition
    public Vector<Integer> pruneCandidateNodes(Vector<Integer> cand) {
        int flag = 0;
        if (cand == null) {
            return null;
        }
        int count = 0;
/*
	for(int i=0;i<cand.size();i++)
	{
		System.out.println(cand.elementAt(i)+" is a candidate node");
	}
	*/

        for (int i = 0; i < cand.size(); i++) {
            count = 0;
            //System.out.println("Node "+(int)cand.elementAt(i));
            IntArray ng = neighbors(cand.elementAt(i));
            for (int j = 0; j < ng.size(); j++) {
                //System.out.println("Verify neighbor "+ng.get(j)+"("+CorenessTable[ng.get(j)]+") from "+ng.size() +" neighbors "+CorenessTable[(int)cand.elementAt(i)]);
                if (cand.contains(ng.get(j))) {
                    //System.out.println("node "+ng.get(j)+" in the candidate set ");

                    count++;
                } else if ((CorenessTable[ng.get(j)] > CorenessTable[cand.elementAt(i)])) {
                    //System.out.println("Coreness of node "+ng.get(j)+" ("+CorenessTable[ng.get(j)]+") is greater than "+ "the coreness of node "+i+" ("+CorenessTable[i]);

                    count++;
                }
            }
            if (count <= CorenessTable[cand.elementAt(i)]) {
                //System.out.println("Node "+cand.elementAt(i)+" will not be updated");
                cand.removeElementAt(i);

                flag = 1;
                //CorenessTable[(int)cand.elementAt(i)]++;
                //System.out.println("Coreness of node "+(int)cand.elementAt(i)+" updated ="+CorenessTable[i]);


            }
        }
        if (flag == 1) {
            pruneCandidateNodes(cand);
        }
        return cand;
    }


    //update the coreness of candidate nodes in this partition
    public Vector<Integer> pruneCandidateNodesAkka(Vector<Integer> cand, Hashtable<Integer, IntArray> h, Hashtable<Integer, Integer> hc) {
        //System.out.println(" Pruning step: ");

        int flag = 0;
        //System.out.println("test");
        if (cand == null) {
            return null;
        }
        int count = 0;

	/*
	for(int i=0;i<cand.size();i++)
	{
		System.out.println(cand.elementAt(i)+" is a candidate node");
	}
	*/

        for (int i = 0; i < cand.size(); i++) {
            System.out.println(" ***** Candidate node: " + cand.elementAt(i));
            count = 0;
            //System.out.println("Node "+(int)cand.elementAt(i));
            //IntArray ng = neighbors((int)cand.elementAt(i));
            IntArray ng = h.get(cand.elementAt(i));
            //System.out.println("Number of neighbors of Node: "+(int)cand.elementAt(i)+" is "+ng.size());

            for (int j = 0; j < ng.size(); j++) {
                //System.out.println("Verify neighbor "+ng.get(j)+"("+distNeighbCoreness.get(ng.get(j))+") from "+ng.size() +" neighbors ");
                System.out.println("[hc] Verify neighbor " + ng.get(j) + "(" + hc.get(ng.get(j)) + ") from " + ng.size() + " neighbors ");
                //System.out.println("Partition of neighbor "+ng.get(j)+" is "+neighborsPart[ng.get(j)]);
                if (cand.contains(ng.get(j))) {
                    System.out.println("count++ => Node " + ng.get(j) + " in the candidate set  ");
                    count++;
                }
                //else if ((distNeighbCoreness.get(ng.get(j))>distNeighbCoreness.get((int)cand.elementAt(i))))

                else if (hc.get(ng.get(j)) > hc.get(cand.elementAt(i))) {
                    //System.out.println("Coreness of node "+ng.get(j)+" ("+distNeighbCoreness.get(ng.get(j))+") is greater than "+ "the coreness of node "+i+" ("+distNeighbCoreness.get(i));
                    System.out.println("count++ => Coreness of node " + ng.get(j) + " (" + hc.get(ng.get(j)) + ") is greater than " + "the coreness of node " + j + " (" + hc.get(cand.elementAt(i)));
                    count++;
                }
                //if(count<=distNeighbCoreness.get((int)cand.elementAt(i))){
                //if(hc.get((int)cand.elementAt(i))==null){System.out.println("Null hc de "+(int)cand.elementAt(i));}

            }

            if (count <= hc.get(cand.elementAt(i))) {

                System.out.println("count is less than " + hc.get(cand.elementAt(i)) + " => Node " + cand.elementAt(i) + " will not be updated");
                cand.removeElementAt(i);

                flag = 1;
                //CorenessTable[(int)cand.elementAt(i)]++;
                //System.out.println("Coreness of node "+(int)cand.elementAt(i)+" updated ="+CorenessTable[i]);
            }
        }
        if (flag == 1) {
            pruneCandidateNodesAkka(cand, h, hc);
        }
        return cand;
    }

    public void AfficherpruneCandidateNodesAkka(Vector<Integer> cand, Hashtable h, Hashtable hc) {

        for (int i = 0; i < cand.size(); i++) {
            System.out.println(" [Test] ***** Candidate node: " + cand.elementAt(i));

            IntArray ng = (IntArray) h.get(cand.elementAt(i));
            //System.out.println("Number of neighbors of Node: "+(int)cand.elementAt(i)+" is "+ng.size());

            for (int j = 0; j < ng.size(); j++) {
                //System.out.println("Verify neighbor "+ng.get(j)+"("+distNeighbCoreness.get(ng.get(j))+") from "+ng.size() +" neighbors ");
                System.out.println("[Test] Node " + ng.get(j) + "is a neighbor with coreness =" + hc.get(ng.get(j)) + " from " + ng.size() + " neighbors ");
            }
        }
    }

    //update the coreness of candidate nodes in this partition
    public Vector<Integer> pruneCandidateNodesAkka(Vector<Integer> cand) {
        int flag = 0;
        if (cand == null) {
            return null;
        }
        int count = 0;

	/*
	for(int i=0;i<cand.size();i++)
	{
		System.out.println(cand.elementAt(i)+" is a candidate node");
	}
	*/

        for (int i = 0; i < cand.size(); i++) {
            //System.out.println(" ***** Candidate node: "+cand.elementAt(i));
            count = 0;
            //System.out.println("Node "+(int)cand.elementAt(i));
            IntArray ng = neighbors(cand.elementAt(i));
            //IntArray ng = (IntArray) h.get(cand.elementAt(i));
            //System.out.println("Number of neighbors of Node: "+(int)cand.elementAt(i)+" is "+ng.size());

            for (int j = 0; j < ng.size(); j++) {
                //System.out.println("Verify neighbor "+ng.get(j)+"("+distNeighbCoreness.get(ng.get(j))+") from "+ng.size() +" neighbors "+distNeighbCoreness.get(cand.elementAt(i)));
                //System.out.println("Verify neighbor "+ng.get(j)+"("+(int)hc.get(ng.get(j))+") from "+ng.size() +" neighbors ");
                //System.out.println("Partition of neighbor "+ng.get(j)+" is "+neighborsPart[ng.get(j)]);
                if (cand.contains(ng.get(j))) {
                    //System.out.println("Node "+ng.get(j)+" is in the candidate set ");

                    count++;
                } else if ((distNeighbCoreness.get(ng.get(j)) > distNeighbCoreness.get(cand.elementAt(i))))
                //else if ((int)hc.get(ng.get(j))>(int)hc.get((int)cand.elementAt(i)))
                {
                    //System.out.println("The coreness of node "+ng.get(j)+" ("+distNeighbCoreness.get(ng.get(j))+") is greater than "+ "the coreness of node "+(int)cand.elementAt(i)+" ("+distNeighbCoreness.get((int)cand.elementAt(i)));
                    //System.out.println("Coreness of node "+ng.get(j)+" ("+(int)hc.get(ng.get(j))+") is greater than "+ "the coreness of node "+i+" ("+(int)hc.get(i));

                    count++;
                }
            }
            if (count <= distNeighbCoreness.get(cand.elementAt(i))) {
                //System.out.println("Node "+cand.elementAt(i)+" will not be updated");
                cand.removeElementAt(i);

                flag = 1;
                //CorenessTable[(int)cand.elementAt(i)]++;
                //System.out.println("Coreness of node "+(int)cand.elementAt(i)+" updated ="+CorenessTable[i]);
            }
        }
        if (flag == 1) {
            pruneCandidateNodesAkka(cand);
        }
        return cand;
    }


    public void updateCoreness(Vector<Integer> v) {
        for (int i = 0; i < v.size(); i++) {
            CorenessTable[v.elementAt(i)]++;
            int core = distNeighbCoreness.get(v.elementAt(i));
            distNeighbCoreness.put(v.elementAt(i), core + 1);
            System.out.println("Coreness of node " + v.elementAt(i) + " updated =>" + distNeighbCoreness.get(v.elementAt(i)));
        }
    }

    public void updateCorenessAkka(Vector<Integer> v, Hashtable<Integer, Integer> h, Hashtable<Integer, Integer> hc) {
        for (int i = 0; i < v.size(); i++) {
            CorenessTable[v.elementAt(i)]++;
            //int core=distNeighbCoreness.get((int)v.elementAt(i));
            int core = hc.get(v.elementAt(i));
            distNeighbCoreness.put(v.elementAt(i), core + 1);
            System.out.println("Coreness of node " + v.elementAt(i) + " updated =>" + distNeighbCoreness.get(v.elementAt(i)));
        }
    }

    //determine the induced core subgraph from a node
    public void inducedCoreSubgraph(int node) {
        int coreness = CorenessTable[node];
        IntArray inducedCS = notVisitedNeighbors(node);

        visitedNodes.add(node);
        if (!reachableNodes.contains(node))
            reachableNodes.addElement(node);
        if (inducedCS.size() == 0) {
            //System.out.println("null");
            return;
        } else {
            for (int i = 0; i < inducedCS.size(); i++) {
                //System.out.println("node "+inducedCS.get(i)+" is neighbor of node"+ node);
                visitedNodes.addElement(inducedCS.get(i));
                if (CorenessTable[inducedCS.get(i)] == coreness) {
                    //System.out.println("node "+inducedCS.get(i)+" is part of the induced core subgraph");
                    if (!reachableNodes.contains(inducedCS.get(i)))
                        reachableNodes.addElement(inducedCS.get(i));

                    inducedCoreSubgraph(inducedCS.get(i));
                }

            }
        }

    }

    public void inducedCoreSubgraphAkka(int node) {
        //intialize();
        //System.out.println("Coreness of node "+1+" = "+distNeighbCoreness.get(1));
        int coreness = distNeighbCoreness.get(node);
        IntArray inducedCS = notVisitedNeighbors(node);

        visitedNodes.add(node);
        if (!reachableNodes.contains(node)) {
            reachableNodes.addElement(node);
            //System.out.println("node "+node+" is part of the induced core subgraph");
            //System.out.println("adding "+node);
        }
        if (inducedCS.size() == 0) {
            //System.out.println("null");
            return;
        } else {
            for (int i = 0; i < inducedCS.size(); i++) {
                //System.out.println("node "+inducedCS.get(i)+" is neighbor of node"+ node);
                visitedNodes.addElement(inducedCS.get(i));
                if (distNeighbCoreness.get(inducedCS.get(i)) == coreness) {
                    //System.out.println("node "+inducedCS.get(i)+" is part of the induced core subgraph");
                    if (!reachableNodes.contains(inducedCS.get(i)))
                        reachableNodes.addElement(inducedCS.get(i));
                    inducedCoreSubgraphAkka(inducedCS.get(i));
                }

            }
        }

    }


    public IntArray neighbors(int i) {
        if (neighbors[i] == null) {
            //System.out.println("neighbor"+i+"does not exist");
            neighbors[i] = new IntArray(4);
        }
        return neighbors[i];
    }

    public IntArray neighborsAkka(int i) {

        if (neighbors[i] == null) {
            return null;
        }
        return neighbors[i];
    }

    public IntArray notVisitedNeighbors(int i) {
        IntArray nvn = new IntArray(4);
        if (neighbors[i] == null) {
            //System.out.println("neighbor"+i+"does not exist");
            neighbors[i] = new IntArray(4);
        }
        IntArray ng = neighbors[i];
        for (int j = 0; j < ng.size(); j++) {
            //System.out.println("Node "+ng.get(j)+" is a neighbor");
            if (!visitedNodes.contains(ng.get(j))) {
                nvn.append(ng.get(j));

            }

        }

        return nvn;
    }


    public void addEdge(int i, int j) {
        if (neighbors[i] == null)
            neighbors[i] = new IntArray(4);
        if (!neighbors[i].contains(j))
            neighbors[i].append(j);
    }

    public void addEdgeAkka(int i, int j, int p1, int p2, int c1, int c2) {
        if (neighbors[i] == null)
            neighbors[i] = new IntArray(4);
        if (!neighbors[i].contains(j)) {
            neighbors[i].append(j);
            //neighborsPart[i]=p1;
            //neighborsPart[j]=p2;
            //distNeighbCoreness.put(i, c1);
            //distNeighbCoreness.put(j, c2);

        }
    }

    public String toString() {
        for (int i = 0; i < size; i++) {
            IntArray ia = neighbors(i);
            for (int j = 0; j < ia.size(); j++)
                System.out.println("(" + i + "," + ia.get(j) + ")");
        }
        return "";
    }

}
