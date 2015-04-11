package kcore;


public class EstimateTable {

    /**
     * Estimated values; contains both those under control of this server and
     * those received from other servers. Its size is equal to nNodes
     */
    private int[] estimates;

    private int[] rounds;

    private int index;

    private int min;

    public EstimateTable(int nNodes, int nServers, int index) {
        estimates = new int[nNodes];
        rounds = new int[nServers];
        for (int i = 0; i < nNodes; i++) {
            estimates[i] = Integer.MAX_VALUE;
        }
        this.index = index;

    }

    public synchronized boolean set(int node, int ksh) {
        if (ksh < estimates[node]) {
            //if (index == 0) System.err.println(estimates[node] + " " + ksh);
            estimates[node] = ksh;
            return true;
        }
        return false;
    }


    public int get(int node) {
        return estimates[node];
    }

    public synchronized boolean addServer(int s) {
        rounds[s]++;
        int newmin = Integer.MAX_VALUE;
        for (int i = 0; i < rounds.length; i++) {
            if (i != index && rounds[i] < newmin) {
                newmin = rounds[i];
            }
        }
        if (newmin > min) {
            min++;
            return true;
        } else {
            return false;
        }
    }


}
