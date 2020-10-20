import edu.princeton.cs.algs4.Picture;

public class SeamCarver {
  private int[][] pixColors;
  private int width, height;

  /**
   * Creates a SeamCarver object based on given picture
   * 
   * @param picture picture to be carved
   */
  public SeamCarver(Picture picture) {
    if (picture == null) 
      throw new IllegalArgumentException("constructor called with null arg");

    width = picture.width();
    height = picture.height();
    pixColors = new int[width][height];
    for (int x = 0; x < width; x++)
      for (int y = 0; y < height; y++) 
        pixColors[x][y] = picture.getRGB(x, y);
  }

  /**
   * current picture
   * 
   * @return current picture
   */
  public Picture picture() {
    Picture pic = new Picture(width, height);
    for (int x = 0; x < width; x++) 
      for (int y = 0; y < height; y++) 
        pic.setRGB(x, y, pixColors[x][y]);

    return pic;
  }

  // width of current picture
  public int width() {
    return width;
  }

  // height of current picture
  public int height() {
    return height;
  }

  // TO DO REFACTOR TO ELIMINATE NEED FOR PIXENERGY FIELD
  // energy of pixel at column x and row y
  public double energy(int x, int y) {
    if (x < 0 || x >= width || y < 0 || y >= height) 
      throw new IllegalArgumentException("energy() argument out of range");

    if (x == 0 || x == width - 1)  return 1000;
    if (y == 0 || y == height - 1) return 1000;

    int[] left  = this.getRGB(x - 1, y);
    int[] right = this.getRGB(x + 1, y);
    int[] above = this.getRGB(x, y - 1);
    int[] below = this.getRGB(x, y + 1);
    int xGrad = 0;
    int yGrad = 0;

    for (int i = 0; i < left.length; i++) {
      xGrad += Math.pow(left[i] - right[i], 2);
      yGrad += Math.pow(above[i] - below[i], 2);
    }
    return Math.sqrt(xGrad + yGrad);
  }

  // returns RGB values of passed (x,y) pixel as an array of doubles
  private int[] getRGB(int x, int y) {
    if (x < 0 || x >= width || y < 0 || y >= height) 
      throw new IllegalArgumentException("energy() argument out of range");

    int rgb = pixColors[x][y];
    int r = (rgb >> 16) & 0xFF;
    int g = (rgb >> 8) & 0xFF;
    int b = rgb & 0xFF;
    return new int[] { r, g, b };
  }

  // public int[] findHorizontalSeam() {
  // return findHorizontalSeam(pixColors);
  // }

  // sequence of indices for horizontal seam
  public int[] findHorizontalSeam() {
    int[] seam;
    int[][][] edgeTo;
    double[][] accEnergy;
    int seamEnd = -1;
    double minE = Double.POSITIVE_INFINITY;

    accEnergy = new double[width][height];
    for (int x = 0; x < width; x++)
      for (int y = 0; y < height; y++)
        accEnergy[x][y] = x == 0 ? energy(x, y) : Double.POSITIVE_INFINITY;

    // initialize the first col
    // relax edges in topological order
    // topological order is col-by-col, top to bottom
    edgeTo = new int[width][height][];
    for (int x = 0; x < width - 1; x++) {
      for (int y = 0; y < height; y++) {
                            relax(x, y, x + 1, y, accEnergy, edgeTo);
        if (y - 1 >= 0)     relax(x, y, x + 1, y - 1, accEnergy, edgeTo);
        if (y + 1 < height) relax(x, y, x + 1, y + 1, accEnergy, edgeTo);
      }
    }

    // find the shortest of all shortest paths
    // by reference to the accEnergy of the last element in row
    int lastX = width - 1;
    for (int y = 0; y < height; y++) {
      if (accEnergy[lastX][y] < minE) {
        minE = accEnergy[lastX][y];
        seamEnd = y;
      }
    }

    // set the last el of seam = seamEnd found above
    // and backtrack to build the seam array using edgeTo
    // edgeTo[destRow][destCol][0 for row]
    seam = new int[width];
    seam[width - 1] = seamEnd;
    for (int x = width - 1; x > 0; x--)
      seam[x - 1] = edgeTo[x][seam[x]][1];

    return seam;
  }

  // check if accumulated energy of the destination
  // is greater than the accum energy of source plus the pixE of destn
  // if so, then update the accE of dest'n
  private void relax(int xFrom, int yFrom, int xTo, int yTo, 
                     double[][] accEnergy, int[][][] edgeTo) {
    double energyTo = energy(xTo, yTo);
    if (accEnergy[xTo][yTo] > accEnergy[xFrom][yFrom] + energyTo) {
      accEnergy[xTo][yTo] = accEnergy[xFrom][yFrom] + energyTo;
      edgeTo[xTo][yTo] = new int[] { xFrom, yFrom };
    }
  }

  // sequence of indices for vertical seam
  public int[] findVerticalSeam() {
    int[] seam;
    int[][][] edgeTo;
    double[][] accEnergy;
    int seamEnd = -1;
    double minE = Double.POSITIVE_INFINITY;

    accEnergy = new double[width][height];
    for (int x = 0; x < width; x++)
      for (int y = 0; y < height; y++)
        accEnergy[x][y] = y == 0 ? energy(x, y) : Double.POSITIVE_INFINITY;

    // initialize the first col
    // relax edges in topological order
    // topological order is row by row, left to right
    edgeTo = new int[width][height][];
    for (int y = 0; y < height - 1; y++) {
      for (int x = 0; x < width; x++) {
                           relax(x, y, x, y + 1, accEnergy, edgeTo);
        if (x - 1 >= 0)    relax(x, y, x - 1, y + 1, accEnergy, edgeTo);
        if (x + 1 < width) relax(x, y, x + 1, y + 1, accEnergy, edgeTo);
      }
    }

    // find the shortest of all shortest paths
    // by reference to the accEnergy of the last element in row
    int lastY = height - 1;
    for (int x = 0; x < width; x++) {
      if (accEnergy[x][lastY] < minE) {
        minE = accEnergy[x][lastY];
        seamEnd = x;
      }
    }

    // set the last el of seam = seamEnd found above
    // and backtrack to build the seam array using edgeTo
    // edgeTo[destRow][destCol][0 for row]
    seam = new int[height];
    seam[height - 1] = seamEnd;
    for (int y = height - 1; y > 0; y--)
      seam[y - 1] = edgeTo[seam[y]][y][0];

    return seam;
  }

  // remove horizontal seam from current picture
  public void removeHorizontalSeam(int[] seam) {
    if (height <= 1)
      throw new IllegalArgumentException("width of picture <= 1");
    if (seam == null) 
      throw new IllegalArgumentException("method called with null arg");
    if (seam.length != width)
      throw new IllegalArgumentException("length of arg does not match width of picture");
    for (int i = 0; i < seam.length - 1; i++) 
      if (Math.abs(seam[i] - seam[i + 1]) > 1)
        throw new IllegalArgumentException("dist betw successive seam elements greater than 1");

    int[][] rmSeam = new int[width][--height];
    for (int x = 0; x < width; x++) 
      for (int y = 0; y < height; y++) {
        if (seam[x] < 0 || seam[x] > height)
          throw new IllegalArgumentException("invalid seam element");
        rmSeam[x][y] = y >= seam[x] ? pixColors[x][y + 1] : pixColors[x][y];
      }

    pixColors = rmSeam;
  }

  // remove vertical seam from current picture
  public void removeVerticalSeam(int[] seam) {
    if (width <= 1)
      throw new IllegalArgumentException("width of picture <= 1");
    if (seam == null) 
      throw new IllegalArgumentException("method called with null arg");
    if (seam.length != height)
      throw new IllegalArgumentException("length of arg does not match width of picture");
    for (int i = 0; i < seam.length - 1; i++) 
      if (Math.abs(seam[i] - seam[i + 1]) > 1)
        throw new IllegalArgumentException("dist betw successive seam elements greater than 1");
    
    int[][] rmSeam = new int[--width][height];
    for (int y = 0; y < height; y++)
      for (int x = 0; x < width; x++) {
        if (seam[y] < 0 || seam[y] > width) 
          throw new IllegalArgumentException("seam element out of range");
        rmSeam[x][y] = x >= seam[y] ? pixColors[x + 1][y] : pixColors[x][y];
      }

    pixColors = rmSeam;
  }

  // unit testing (optional)
  public static void main(String[] args) {
    // unit testing done with external clients
  }
}
