package Pi;

import java.io.Serializable;

public class PiResult implements Serializable {
  public final int n;
  public final double pi;

  PiResult(int n, Double pi) {
    this.n = n;
    this.pi = pi;
  }
}