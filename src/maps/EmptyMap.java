package maps;

import battlecode.world.MapBuilder;

import java.io.IOException;

/**
 * Generate a map.
 */
public class EmptyMap {

    // change this!!!
    // this needs to be the same as the name of the file
    // it also cannot be the same as the name of an existing engine map
    public static final String mapName = "EmptyMap";

    // don't change this!!
    public static final String outputDirectory = "maps/";

    private static int width;
    private static int height;

    /**
     * @param args unused
     */
    public static void main(String[] args) {
        try {
            makeSimple();
        } catch (IOException e) {
            System.out.println(e);
        }
        System.out.println("Generated a map!");
    }

    public static void makeSimple() throws IOException {
        width = 50;
        height = 50;
        MapBuilder mapBuilder = new MapBuilder(mapName, width, height, 148);

        mapBuilder.setSymmetry(MapBuilder.MapSymmetry.horizontal);
        mapBuilder.addSymmetricHQ(18, 6);








        mapBuilder.saveMap(outputDirectory);

    }

    public static void addRectangleSoup(MapBuilder mapBuilder, int xl, int yb, int xr, int yt, int v) {
        for (int i = xl; i < xr+1; i++) {
            for (int j = yb; j < yt+1; j++) {
                mapBuilder.setSymmetricSoup(i, j, v);
            }
        }
    }
}
