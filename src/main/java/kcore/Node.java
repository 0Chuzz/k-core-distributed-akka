package kcore;

public class Node {


	public Node(int node, int partition) {
		this.node = node;
		this.partition = partition;
	}
int node;
int partition;

public int getNode() {
	return node;
}
public void setNode(int node) {
	this.node = node;
}
public int getPartition() {
	return partition;
}
public void setPartition(int partition) {
	this.partition = partition;
}

}