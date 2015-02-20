package kcore;

import java.io.Serializable;

/**
 * Created by Stefano on 04/01/2015.
 */
public class PiWork implements Serializable {
    public final  int n;
    public final int nrofEls;
    PiWork(int a, int b){
        n = a;
        nrofEls = b;
    }

}
