import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;

public class SeamCarver {
  private final Picture pic;

  // create a seam carver object based on the given picture
  public SeamCarver(Picture picture) {
    pic = new Picture(picture);
  }

  // current picture
  public Picture picture() {
    return pic;
  }

  // width of current picture
  public int width() {
    return pic.width();
  }

  // height of current picture
  public int height() {
    return pic.height();
  }

  // energy of pixel at column x and row y
  public double energy(int x, int y) {
    int[] left  = getRGB(x - 1, y);
    int[] right = getRGB(x + 1, y);
    int[] above = getRGB(x, y - 1);
    int[] below = getRGB(x, y + 1);
    int xGrad = 0;
    int yGrad = 0;

    for (int i = 0; i < left.length; i++) {
      xGrad += Math.pow(left[i]  - right[i], 2);
      yGrad += Math.pow(above[i] - below[i], 2);
    }
    return Math.sqrt(xGrad + yGrad);
  }

  // returns RGB values of passed (x,y) pixel as an array of doubles
  private int[] getRGB(int x, int y) {
    int rgb = pic.getRGB(x, y);
    int r = (rgb >> 16) & 0xFF;
    int g = (rgb >>  8) & 0xFF;
    int b =  rgb        & 0xFF;
    return new int[] { r, g, b };
  }

  // sequence of indices for horizontal seam
  public int[] findHorizontalSeam() {
    return new int[0];
  }

  // sequence of indices for vertical seam
  public int[] findVerticalSeam() {
    return new int[0];
  }

  // remove horizontal seam from current picture
  public void removeHorizontalSeam(int[] seam) {

  }

  // remove vertical seam from current picture
  public void removeVerticalSeam(int[] seam) {

  }

  // unit testing (optional)
  public static void main(String[] args) {
    Picture p = new Picture("test/4x6.png");
    SeamCarver sc = new SeamCarver(p);
    for (int i = 0; i < p.width(); i++)
      for (int j = 0; j < p.height(); j++) {
        for (int k = 0; k < 3; k++)
          StdOut.print(sc.getRGB(i, j)[k] + "\t");
        StdOut.println();
      }
  }
}
