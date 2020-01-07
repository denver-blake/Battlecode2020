package sprint;

import battlecode.common.Direction;

public class Utils {

    public static Direction intToDirection(int x) {
        switch(x) {
            case 0: return Direction.NORTH;
            case 1: return Direction.NORTHEAST;
            case 2: return Direction.EAST;
            case 3: return Direction.SOUTHEAST;
            case 4: return Direction.SOUTH;
            case 5: return Direction.SOUTHWEST;
            case 6: return Direction.WEST;
            case 7: return Direction.NORTHWEST;
        }

        return Direction.NORTH;
    }

}
