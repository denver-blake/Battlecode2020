package sprint;
import battlecode.common.*;

import java.util.*;

import battlecode.common.Direction;

public class utils {

    public static final int BLOCKCHAIN_TAG = -471247;

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

    public static void moveTowardsSimple(RobotController rc,MapLocation destination) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(destination);
        if (rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
            System.out.println("Next Location" + rc.getLocation().add(dir));
            rc.move(dir);
            return;
        }
        for (int i = 0;i < 7;i++) {
            dir = dir.rotateLeft();
            if (rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
                rc.move(dir);
                return;
            }
        }
    }

    public static MapLocation findNearbySoup(RobotController rc) throws GameActionException {
        List<MapLocation> queue = new LinkedList<>();
        queue.add(rc.getLocation());

        while(queue.size() > 0) {
            MapLocation current = queue.remove(0);

            for(Direction dir : Direction.allDirections()) {
                MapLocation newLocation = current.add(dir);

                if(!rc.canSenseLocation(newLocation)) continue;

                if(rc.senseSoup(newLocation) > 0) {
                    return newLocation;
                }

                queue.add(newLocation);
            }
        }

        return null;
    }

    public static MapLocation findHighGround(RobotController rc) throws GameActionException {
        List<MapLocation> queue = new LinkedList<>();
        queue.add(rc.getLocation());

        MapLocation highGround = rc.getLocation();

        while(queue.size() > 0) {
            MapLocation current = queue.remove(0);

            for(Direction dir : Direction.allDirections()) {
                MapLocation newLocation = current.add(dir);

                if(!rc.canSenseLocation(newLocation)) continue;

                if(rc.senseElevation(newLocation) >= rc.senseElevation(highGround)
                        && rc.senseRobotAtLocation(newLocation).type == null) {
                    highGround = newLocation;
                }

                queue.add(newLocation);
            }
        }

        return highGround;
    }

    public static MapLocation searchForRefinery(RobotController rc) throws GameActionException {
        List<MapLocation> queue = new LinkedList<>();
        queue.add(rc.getLocation());

        while(queue.size() > 0) {
            MapLocation current = queue.remove(0);

            for(Direction dir : Direction.allDirections()) {
                MapLocation newLocation = current.add(dir);

                if(!rc.canSenseLocation(newLocation)) continue;

                if(rc.senseRobotAtLocation(newLocation).type == RobotType.REFINERY) {
                    return newLocation;
                }

                queue.add(newLocation);
            }
        }

        return null;
    }

    public static MapLocation newRefineryLocation(RobotController rc) throws GameActionException {
        List<MapLocation> queue = new LinkedList<>();
        queue.add(rc.getLocation());

        while(queue.size() > 0) {
            MapLocation current = queue.remove(0);

            for(Direction dir : Direction.allDirections()) {
                MapLocation newLocation = current.add(dir);

                if(!rc.canSenseLocation(newLocation)) continue;

                if(rc.senseSoup(newLocation) == 0 && !rc.senseFlooding(newLocation)) {
                    return newLocation;
                }

                queue.add(newLocation);
            }
        }

        return null;
    }

    public static class Bug2Pathfinder {
        private RobotController rc;
        private MapLocation destination;
        private List<MapLocation> line;
        private Direction prevDirection;
        public Bug2Pathfinder(RobotController rc,MapLocation destination) {
            this.rc = rc;
            this.destination = destination;
            line = findLine(rc,rc.getLocation(),destination);
        }

        public void moveTowards() throws GameActionException {
            if (rc.getCooldownTurns() >= 1) return;
            Direction dir;

            for (int i = 0;i < line.size()-1;i++) {

                if (line.get(i).equals(rc.getLocation())) {
                    dir = rc.getLocation().directionTo(line.get(i+1));
                    System.out.println(dir + " Towards goal");
                    if (rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
                        System.out.println(dir + " Actual");
                        prevDirection = dir;
                        rc.move(dir);
                        return;
                    }
                    for (int j = 0;j < 8;j++) {
                        dir = dir.rotateLeft();
                        if (rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
                            prevDirection = dir;
                            System.out.println(dir + " Actual");
                            rc.move(dir);
                            return;
                        }
                    }
                }
            }
            dir = prevDirection.opposite().rotateLeft();
            for (int j = 0;j < 8;j++) {
                dir = dir.rotateLeft();
                if (rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
                    prevDirection = dir;
                    rc.move(dir);
                    return;
                }
            }

        }
    }



    public static List<MapLocation> findLine(RobotController rc, MapLocation initial,MapLocation end) {
        List<MapLocation> line = new ArrayList<MapLocation>();
        int x0 = initial.x;
        int y0 = initial.y;
        int x1 = end.x;
        int y1 = end.y;

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx-dy;
        int e2;

        while (true)
        {
            line.add(new MapLocation(x0,y0));

            if (x0 == x1 && y0 == y1)
                break;

            e2 = 2 * err;
            if (e2 > -dy)
            {
                err = err - dy;
                x0 = x0 + sx;
            }

            if (e2 < dx)
            {
                err = err + dx;
                y0 = y0 + sy;
            }
        }
        System.out.println(line);
        return line;
    }

    public static MapLocation hqPosition(RobotController rc) throws GameActionException {
        Transaction[] block = rc.getBlock(0);

        for(int i=0; i<block.length; i++) {
            if(block[i].getMessage()[0] == utils.BLOCKCHAIN_TAG) {
                return new MapLocation(block[i].getMessage()[1], block[i].getMessage()[2]);
            }
        }

        //hq failed to send its coords
        return new MapLocation(0, 0);
    }
}
