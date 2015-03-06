package kcore;

import java.util.Hashtable;
import java.util.Vector;

public class Edge {
int node1; 
int node2;
int partition1;
int partition2;
int coreness1;
int coreness2;
int defaultNode;
int defaultPartition;
Vector reachableNodes;
Hashtable neighborsOfreachableNodes;
Hashtable CorenessOfNeighborsOfreachableNodes;
public Edge(int a, int b, int p1, int p2, int c1, int c2)
{
	node1=a;
	node2=b;
	partition1=p1;
	partition2=p2;
	coreness1=c1;
	coreness2=c2;
	defaultNode=a;
	reachableNodes=new Vector();
	neighborsOfreachableNodes= new Hashtable<Integer,IntArray>();
	CorenessOfNeighborsOfreachableNodes= new Hashtable<Integer,Integer>();

}
public Edge(int a, int b, int p1, int p2)
{
	node1=a;
	node2=b;
	partition1=p1;
	partition2=p2;
}
public Edge(int a, int b)
{
	node1=a;
	node2=b;
}
public int getNode1() {
	return node1;
}
public void setNode1(int node1) {
	this.node1 = node1;
}
public int getNode2() {
	return node2;
}
public void setNode2(int node2) {
	this.node2 = node2;
}
public int getPartition1() {
	return partition1;
}
public void setPartition1(int partition1) {
	this.partition1 = partition1;
}
public int getPartition2() {
	return partition2;
}
public void setPartition2(int partition2) {
	this.partition2 = partition2;
}
public int getCoreness1() {
	return coreness1;
}
public void setCoreness1(int coreness1) {
	this.coreness1 = coreness1;
}
public int getCoreness2() {
	return coreness2;
}
public void setCoreness2(int coreness2) {
	this.coreness2 = coreness2;
}
public int getDefaultNode() {
	return defaultNode;
}
public void setDefaultNode(int defaultNode) {
	this.defaultNode = defaultNode;
}
public Vector getReachableNodes() {
	return reachableNodes;
}
public void setReachableNodes(Vector reachableNodes) {
	this.reachableNodes = reachableNodes;
}
public Hashtable getNeighborsOfreachableNodes() {
	return neighborsOfreachableNodes;
}
public void setNeighborsOfreachableNodes(Hashtable h) {
	this.neighborsOfreachableNodes = h;
}
public Hashtable getCorenessOfNeighborsOfreachableNodes() {
	return CorenessOfNeighborsOfreachableNodes;
}
public void setCorenessOfNeighborsOfreachableNodes(
		Hashtable corenessOfNeighborsOfreachableNodes) {
	CorenessOfNeighborsOfreachableNodes = corenessOfNeighborsOfreachableNodes;
}
public int getDefaultPartition() {
	return defaultPartition;
}
public void setDefaultPartition(int defaultPartition) {
	this.defaultPartition = defaultPartition;
}
}
