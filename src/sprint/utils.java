package sprint;
import battlecode.common.*;

import java.util.*;

import battlecode.common.Direction;

public class utils {

    public static final int BLOCKCHAIN_TAG = -471247;
    public static final int NEW_REFINERY_TAG = -24880;
    public static final int FINISHED_REFINERY_TAG = 578838;
    public static final int LANDSCAPER_PROTECT_DEPOSIT_TAG = 234987;
    public static final int LANDSCAPER_FORTIFY_CASTLE_TAG = -876323;


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
        int robotX = rc.getLocation().x;
        int robotY = rc.getLocation().y;

        MapLocation closestPossible = null;

        for(int i=robotX-6;i<=robotX+6;i++) {
            for(int j=robotY-6;j<=robotY+6;j++) {
                MapLocation loc = new MapLocation(i, j);

                if(rc.canSenseLocation(loc) && rc.onTheMap(loc) &&
                        rc.senseSoup(loc) != 0 &&
                        (closestPossible == null || rc.getLocation().distanceSquaredTo(loc) < rc.getLocation().distanceSquaredTo(closestPossible))) {
                    closestPossible = loc;
                }
            }
        }

        return closestPossible;
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
                        && rc.senseRobotAtLocation(newLocation) != null ) {
                    highGround = newLocation;
                }

                queue.add(newLocation);
            }
        }

        return highGround;
    }

    public static MapLocation searchForRefinery(RobotController rc) throws GameActionException {
        int robotX = rc.getLocation().x;
        int robotY = rc.getLocation().y;

        for(int i=robotX-6;i<=robotX+6;i++) {
            for(int j=robotY-6;j<=robotY+6;j++) {
                MapLocation loc = new MapLocation(i, j);

                if(rc.canSenseLocation(loc) && rc.onTheMap(loc) &&
                        rc.senseRobotAtLocation(loc) != null &&
                        rc.senseRobotAtLocation(loc).type == RobotType.REFINERY) {
                    return loc;
                }
            }
        }

        return null;
    }

    public static MapLocation newRefineryLocation(RobotController rc) throws GameActionException {
        int robotX = rc.getLocation().x;
        int robotY = rc.getLocation().y;

        MapLocation closestPossible = null;

        for(int i=0;i<=robotX+6;i++) {
            for(int j=0;j<=robotY+6;j++) {
                MapLocation loc = new MapLocation(i, j);

                if(rc.canSenseLocation(loc) && rc.onTheMap(loc) &&
                        rc.senseSoup(loc) == 0 &&
                        !rc.senseFlooding(loc) &&
                        (rc.getLocation().distanceSquaredTo(loc) < rc.getLocation().distanceSquaredTo(closestPossible)) || closestPossible == null) {
                    closestPossible = loc;
                }
            }
        }

        return closestPossible;
    }

    public static int countNearbyMiners(RobotController rc) {
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();

        int nearbyMiners = 0;
        for(RobotInfo info : nearbyRobots) {
            if(info.team == rc.getTeam() && info.type == RobotType.MINER) {
                nearbyMiners++;
            }
        }

        return nearbyMiners;
    }

    public static MapLocation lastRefineryLocation(RobotController rc) throws GameActionException {

         Set<Integer> usedRefineries = new HashSet<>();

         for(int turn = rc.getRoundNum() - 1; turn >= 1; turn--) {
            Transaction[] block = rc.getBlock(turn);

            for(Transaction trans : block) {
                if(trans.getMessage()[0] == BLOCKCHAIN_TAG) {
                    if(trans.getMessage()[1] == FINISHED_REFINERY_TAG) {
                        usedRefineries.add(trans.getMessage()[2] + trans.getMessage()[3]);
                    }

                    if(trans.getMessage()[1] == NEW_REFINERY_TAG
                            && !usedRefineries.contains(trans.getMessage()[2] + trans.getMessage()[3])) {
                        return new MapLocation(trans.getMessage()[2], trans.getMessage()[3]);
                    }
                }
            }
        }

        return null;
    }

    public static boolean shouldCopSoup(RobotController rc) throws GameActionException {
        Set<Integer> usedRefineries = new HashSet<>();

        for(int turn = rc.getRoundNum() - 1; turn >= 1; turn--) {
            Transaction[] block = rc.getBlock(turn);

            for (Transaction trans : block) {
                if (trans.getMessage()[0] == BLOCKCHAIN_TAG) {
                    if (trans.getMessage()[1] == FINISHED_REFINERY_TAG) {
                        usedRefineries.add(trans.getMessage()[2] + trans.getMessage()[3]);
                    }
                }
            }
        }

        RobotInfo[] robots = rc.senseNearbyRobots();

        for(RobotInfo robot : robots) {
            if(robot.team == rc.getTeam() && robot.type == RobotType.REFINERY && usedRefineries.contains(robot.location.x + robot.location.y)) {
                return false;
            }
        }

        return true;
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
        Transaction[] block = rc.getBlock(1);

        for(int i=0; i<block.length; i++) {
            if(block[i].getMessage()[0] == utils.BLOCKCHAIN_TAG) {
                return new MapLocation(block[i].getMessage()[1], block[i].getMessage()[2]);
            }
        }

        //hq failed to send its coords
        return new MapLocation(0, 0);
    }

    public static boolean onTheMap(RobotController rc,MapLocation location) {
        return location.x > -1 && location.y > -1 && location.x < rc.getMapWidth() && location.y < rc.getMapHeight();
    }

    public static int getWaterLevel(int round) {
        return (int) (Math.pow(Math.E,0.0028* round - 1.38 * Math.sin(0.00157 * round - 1.73) + 1.38 * Math.sin(-1.73)) - 1);
    }

    public static boolean tryBuild(RobotController rc,RobotType robotType) throws GameActionException {
        for (Direction direction : Direction.allDirections()) {
            if (rc.canBuildRobot(robotType,direction)) {
                rc.buildRobot(robotType,direction);
                return true;
            }
        }
        return false;
    }

}
