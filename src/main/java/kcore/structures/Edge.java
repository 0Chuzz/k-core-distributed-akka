package kcore.structures;

/**
 * Specialized edge class, with equality and hashing
 */
class Edge {
    public int node1, node2;

    public Edge(int part1, int part2) {
        node1 = part1;
        node2 = part2;
    }

    @Override
    public boolean equals(Object o) {
        Edge obj = (Edge) o;
        if (obj == null) return super.equals(o);
        return obj.node1 == node1 && obj.node2 == node2;
    }

    @Override
    public int hashCode() {
        return (node1 << 16) + node2;
    }
}
