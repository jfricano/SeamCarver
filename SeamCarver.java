import edu.princeton.cs.algs4.Picture;
// import edu.princeton.cs.algs4.StdOut;

public class SeamCarver {
  private Picture pic;
  // first two elements id the row and col of the vertex
  // last element is array of size 2 
  //  that IDs the row leading to the edge at row, col
  private double[][] pixEnergy;   // energy for pixel

  // create a seam carver object based on the given picture
  public SeamCarver(Picture picture) {
    pic = new Picture(picture);
    pixEnergy = new double[pic.width()][pic.height()];

    for (int i = 0; i < pixEnergy.length; i++) 
      for (int j = 0; j < pixEnergy[i].length; j++) 
        pixEnergy[i][j] = energy(i, j);
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
    if (x == 0 || x == pic.width()  - 1) return 1000;
    if (y == 0 || y == pic.height() - 1) return 1000;
    
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
    int[] seam;
    int[][][] edgeTo;
    double[][] accEnergy;
    int seamEnd = -1;
    double minE = Double.POSITIVE_INFINITY;

    edgeTo    = new int[pic.width()][pic.height()][];
    accEnergy = new double[pic.width()][pic.height()];
    for (int i = 0; i < accEnergy.length; i++) 
      for (int j = 0; j < accEnergy[i].length; j++) 
        accEnergy[i][j] = i == 0 ? energy(i, j) : Double.POSITIVE_INFINITY;

    // initialize the first col
    // relax edges in topological order
    // topological order is col-by-col, top to bottom
    for (int x = 0; x < pic.width() - 1; x++) {
      for (int y = 0; y < pic.height(); y++) {
                                  relax(x, y, x + 1, y,     accEnergy, edgeTo);
        if (y - 1 >= 0)           relax(x, y, x + 1, y - 1, accEnergy, edgeTo);
        if (y + 1 < pic.height()) relax(x, y, x + 1, y + 1, accEnergy, edgeTo);
      }
    }

    // find the shortest of all shortest paths
    // by reference to the accEnergy of the last element in row
    int lastX = pic.width() - 1;
    for (int y = 0; y < pic.height(); y++) {
      if (accEnergy[lastX][y] < minE) {
        minE = accEnergy[lastX][y];
        seamEnd = y;
      }
    }

    // set the last el of seam = seamEnd found above
    // and backtrack to build the seam array using edgeTo
    // edgeTo[destRow][destCol][0 for row]
    seam = new int[pic.width()];
    seam[pic.width() - 1] = seamEnd;
    for (int x = pic.width() - 1; x > 0; x--) {
      seam[x - 1] = edgeTo[x][seam[x]][1];
    }

    return seam;
  }

  // check if accumulated energy of the destination
  // is greater than the accum energy of source plus the pixE of destn
  // if so, then update the accE of dest'n
  private void relax(int xFrom, int yFrom, 
                     int xTo, int yTo, 
                     double[][] accEnergy,
                     int[][][] edgeTo) {
    if (accEnergy[xTo][yTo] > accEnergy[xFrom][yFrom] + pixEnergy[xTo][yTo]) {
      accEnergy[xTo][yTo] = accEnergy[xFrom][yFrom] + pixEnergy[xTo][yTo];
      edgeTo[xTo][yTo]    = new int[] { xFrom, yFrom };
    }
  }

  // sequence of indices for vertical seam
  public int[] findVerticalSeam() {
    // transpose the pic
    // run horizontalSeam on transposed pic
    // return resulting seam
    Picture transpose = new Picture(pic.height(), pic.width());
    for (int x = 0; x < transpose.width(); x++) {
      for (int y = 0; y < transpose.height(); y++) {
        transpose.setRGB(x, y, pic.getRGB(y, x));
      }
    }
    return new SeamCarver(transpose).findHorizontalSeam();    // maybe these can be static methods so no new sc isntance
  }

  // remove horizontal seam from current picture
  public void removeHorizontalSeam(int[] seam) {
    Picture cpy = new Picture(pic.width(), pic.height() - 1);
    for (int x = 0; x < pic.width(); x++) {
      for (int y = 0; y < pic.height(); y++) {
        if (seam[x] == y) continue;
        cpy.setRGB(x, y, pic.getRGB(x, y));
      }
    }
    pic = new Picture(cpy);
  }

  // remove vertical seam from current picture
  public void removeVerticalSeam(int[] seam) {
    Picture cpy = new Picture(pic.width(), pic.height() - 1);
    for (int y = 0; y < pic.height(); y++) {
      for (int x = 0; x < pic.width(); x++) {
        if (seam[y] == x) continue;
        cpy.setRGB(x, y, pic.getRGB(x, y));
      }
    }
    pic = new Picture(cpy);
  }

  // unit testing (optional)
  public static void main(String[] args) {
    // Picture p = new Picture("test/6x5.png");
    // SeamCarver sc = new SeamCarver(p);
    
    // // print energy, RGB breakdown to console
    // for (int j = 0; j < p.height(); j++) {
    //   for (int i = 0; i < p.width(); i++) {
    //     // StdOut.println("(" + i + ", " + j + ")\nE   = " + sc.energy(i, j));
    //     StdOut.print(i + ", " + j + ":\t");
    //     for (int k = 0; k < 3; k++)
    //       StdOut.print("RGB = " + sc.getRGB(i, j)[k] + "\t");
    //     StdOut.println();
    //   }
    // }
  }
}
