package kcore;

public final class IntArray {

    private int[] A;
    private int size;

    public IntArray(int capacity) {
        size = 0;
        A = new int[capacity];
    }

    public static IntArray fusion(IntArray i1, IntArray i2) {
        if (i1 == null) {
            //System.out.println(" Niil i1");
            return i2;
        } else if (i2 == null) {
            //System.out.println(" Niil i2");

            return i1;
        } else {
            for (int i = 0; i < i1.size(); i++) {
                if (!i2.contains(i1.get(i))) {
                    i2.append(i1.get(i));
                } else {

                }
                //System.out.println(" Neighbor of Node "+(int)part.reachableNodes.elementAt(j) +" is " + i1.get(i));
            }
            return i2;
        }

    }

    public int[] getA() {
        int[] ret = new int[size];
        for (int i = 0; i < A.length && i < size; i++) {
            ret[i] = A[i];
        }
        return ret;
    }

    public Object clone() {
        IntArray temp = new IntArray(size);
        temp.size = size;
        for (int i = 0; i < size; i++) {
            temp.A[i] = A[i];
        }
        return temp;
    }

    public boolean contains(int v) {
        for (int i = 0; i < size; i++) {
            if (A[i] == v) {
                return true;
            }
        }
        return false;
    }

    public void append(int v) {
        if (size == A.length) {
            A = ArrayUtil.resizeArray(A, A.length, A.length * 2);
        }
        A[size] = v;
        size++;
    }

    public int get(int i) {
        return A[i];
    }

    public int size() {
        return size;
    }

    public void reset() {
        size = 0;
    }


}
