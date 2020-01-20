package sprint;
import battlecode.common.*;

import java.util.*;

import battlecode.common.Direction;

public class utils {


    public static final int BLOCKCHAIN_TAG = (RobotPlayer.team == Team.A) ? -471247 : 471247;
    public static final int NEW_REFINERY_TAG = (RobotPlayer.team == Team.A) ? -24880: 24880;
    public static final int FINISHED_REFINERY_TAG = (RobotPlayer.team == Team.A) ? -578838 : 578838;
    public static final int LANDSCAPER_PROTECT_DEPOSIT_TAG = (RobotPlayer.team == Team.A) ? -234987 : 234987;
    public static final int LANDSCAPER_FORTIFY_CASTLE_TAG = (RobotPlayer.team == Team.A) ? -876323 : 876323;
    public static final int UNREACHABLE_DEPOSIT_TAG = (RobotPlayer.team == Team.A) ? -526723 : 526723;
    public static  final int UNDERWATER_DEPOSIT_TAG = (RobotPlayer.team == Team.A) ? -982794 : 982794;
    public static final int BUILDER_TAG = (RobotPlayer.team == Team.A) ? -113298 : 113298;
    public static final int SCOUT_TAG = (RobotPlayer.team == Team.A) ? -729234 : 729234;
    public static final int MINER_TAG = (RobotPlayer.team == Team.A) ? -398274 : 398274;
    public static final int SCOUT_DRONE_TAG = (RobotPlayer.team == Team.A) ? -432998 : 432998;
    public static final int TRANSPORT_MINERS_TAG = (RobotPlayer.team == Team.A) ? -554094 : 554094;
    public static final int START_FORTIFICATION = (RobotPlayer.team == Team.A) ? -909232 : 909232;
    public static final int GOT_RID_OF_WATER = (RobotPlayer.team == Team.A) ? -124322 : 124322;
    public static final int NO_REFINERY_TAG = (RobotPlayer.team == Team.A) ? -2222222 : 2222222;
    public static final int MINER_SCOUT_TAG = (RobotPlayer.team == Team.A) ? -923422 : 923422;
    public static final int FINISHED_FLOOD_WALL = (RobotPlayer.team == Team.A) ? -889921 : 889921;

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

    public static void moveTowardsLandscaper(RobotController rc,MapLocation destination) throws GameActionException {
        System.out.println("Moving towards: " + destination);
        rc.setIndicatorLine(rc.getLocation(),destination,255,255,0);
        if (!rc.isReady()) return;
        int waterLevel = utils.getWaterLevel(rc.getRoundNum()) + 2;
        if (rc.getDirtCarrying() > 0 && rc.senseElevation(rc.getLocation()) < waterLevel) {
            rc.depositDirt(Direction.CENTER);
            return;
        }
        Direction tempDir = rc.getLocation().directionTo(destination);

        Direction[] dirs = {tempDir,tempDir.rotateLeft(),tempDir.rotateRight()};
        for (int i = 0;i < dirs.length;i++) {
            Direction dir = dirs[i];
            if (rc.canMove(dir) && rc.senseElevation(rc.adjacentLocation(dir)) >= waterLevel) {
                rc.move(dir);
                return;
            } else if (rc.senseRobotAtLocation(rc.adjacentLocation(dir)) == null && Math.abs(rc.senseElevation(rc.getLocation()) - rc.senseElevation(rc.adjacentLocation(dir))) < 500) {
                if (rc.senseElevation(rc.getLocation()) > rc.senseElevation(rc.adjacentLocation(dir))) {
                    if (rc.getDirtCarrying() == 0) {
                        if (rc.senseElevation(rc.getLocation()) > waterLevel) {
                            rc.digDirt(Direction.CENTER);
                            return;
                        } else {
                            for (int j = 0; j < 7; j++) {
                                Direction temp = dir.rotateLeft();
                                if (rc.canDigDirt(temp)) {
                                    rc.digDirt(temp);
                                    return;
                                }
                            }
                        }
                    } else {
                        rc.depositDirt(dir);
                        return;
                    }
                } else {
                    if (rc.getDirtCarrying() == 0) {
                        if (rc.senseElevation(rc.adjacentLocation(dir)) > waterLevel) {
                            rc.digDirt(dir);
                        } else {
                            for (int j = 0; j < 7; j++) {
                                Direction temp = dir.rotateLeft();
                                if (rc.canDigDirt(temp)) {
                                    rc.digDirt(temp);
                                    return;
                                }
                            }
                        }
                    } else {
                        rc.depositDirt(Direction.CENTER);
                        return;
                    }

                }

            }
        }

        Direction dir = tempDir;
        for (int i = 0;i < 7;i++) {
            dir = dir.rotateLeft();
            if (rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
                rc.move(dir);
                return;
            }
        }
    }

    public static void moveAwayLandscaper(RobotController rc,MapLocation destination) throws GameActionException {
        System.out.println("Moving towards: " + destination);
        rc.setIndicatorLine(rc.getLocation(),destination,255,255,0);
        if (!rc.isReady()) return;
        int waterLevel = utils.getWaterLevel(rc.getRoundNum()) + 2;
        if (rc.getDirtCarrying() > 0 && rc.senseElevation(rc.getLocation()) < waterLevel) {
            rc.depositDirt(Direction.CENTER);
            return;
        }
        Direction tempDir = rc.getLocation().directionTo(destination).opposite();

        Direction[] dirs = {tempDir,tempDir.rotateLeft(),tempDir.rotateRight()};
        for (int i = 0;i < dirs.length;i++) {
            Direction dir = dirs[i];
            if (rc.canMove(dir) && rc.senseElevation(rc.adjacentLocation(dir)) >= waterLevel) {
                rc.move(dir);
                return;
            } else if (rc.senseRobotAtLocation(rc.adjacentLocation(dir)) == null && Math.abs(rc.senseElevation(rc.getLocation()) - rc.senseElevation(rc.adjacentLocation(dir))) < 500) {
                if (rc.senseElevation(rc.getLocation()) > rc.senseElevation(rc.adjacentLocation(dir))) {
                    if (rc.getDirtCarrying() == 0) {
                        if (rc.senseElevation(rc.getLocation()) > waterLevel) {
                            rc.digDirt(Direction.CENTER);
                            return;
                        } else {
                            for (int j = 0; j < 7; j++) {
                                Direction temp = dir.rotateLeft();
                                if (rc.canDigDirt(temp)) {
                                    rc.digDirt(temp);
                                    return;
                                }
                            }
                        }
                    } else {
                        rc.depositDirt(dir);
                        return;
                    }
                } else {
                    if (rc.getDirtCarrying() == 0) {
                        if (rc.senseElevation(rc.adjacentLocation(dir)) > waterLevel) {
                            rc.digDirt(dir);
                        } else {
                            for (int j = 0; j < 7; j++) {
                                Direction temp = dir.rotateLeft();
                                if (rc.canDigDirt(temp)) {
                                    rc.digDirt(temp);
                                    return;
                                }
                            }
                        }
                    } else {
                        rc.depositDirt(Direction.CENTER);
                        return;
                    }

                }

            }
        }

        Direction dir = tempDir;
        for (int i = 0;i < 7;i++) {
            dir = dir.rotateLeft();
            if (rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
                rc.move(dir);
                return;
            }
        }
    }

    public static void moveTowardsLandscaperNoWater(RobotController rc,MapLocation destination) throws GameActionException {
        System.out.println("Moving towards: " + destination);
        rc.setIndicatorLine(rc.getLocation(),destination,255,255,0);
        if (!rc.isReady()) return;

        Direction tempDir = rc.getLocation().directionTo(destination);

        Direction[] dirs = {tempDir,tempDir.rotateLeft(),tempDir.rotateRight()};
        for (int i = 0;i < dirs.length;i++) {
            Direction dir = dirs[i];
            if (rc.canMove(dir) && !rc.senseFlooding(rc.adjacentLocation(dir))) {
                rc.move(dir);
                return;
            } else if (rc.senseRobotAtLocation(rc.adjacentLocation(dir)) == null && Math.abs(rc.senseElevation(rc.getLocation()) - rc.senseElevation(rc.adjacentLocation(dir))) < 500) {
                if (rc.senseElevation(rc.getLocation()) > rc.senseElevation(rc.adjacentLocation(dir))) {
                    if (rc.getDirtCarrying() == 0) {
                        rc.digDirt(Direction.CENTER);
                        return;
                    } else {
                        rc.depositDirt(dir);
                        return;
                    }
                } else {
                    if (rc.getDirtCarrying() == 0) {
                        rc.digDirt(dir);
                        return;
                    } else {
                        rc.depositDirt(Direction.CENTER);
                        return;
                    }

                }

            }
        }

        Direction dir = tempDir;
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

    public static boolean canSeeHq(RobotController rc) {
        RobotInfo[] robots = rc.senseNearbyRobots();

        for(RobotInfo robot : robots) {
            if(robot.team == rc.getTeam() && robot.type == RobotType.HQ)
                return true;
        }

        return false;
    }

    public static int countLocalSoup(RobotController rc) throws GameActionException {
        int soup = 0;

        int rx = rc.getLocation().x, ry = rc.getLocation().y;

        for(int i=rx-6;i<=rx+6;i++) {
            for(int j=ry-6;j<=ry+6;j++) {
                if(rc.canSenseLocation(new MapLocation(i, j))) {
                    soup += rc.senseSoup(new MapLocation(i, j));
                }
            }
        }

        return soup;
    }

    public static int soupToMiners(int soup) {
        // 500 soup = 1 miner? linear for now
        return soup / 500;
    }

    public static MapLocation weightedSoupCentroid(RobotController rc) throws GameActionException {
        int totalSoup = 0;
        int totalX = 0, totalY = 0;
        int rx = rc.getLocation().x, ry = rc.getLocation().y;

        for(int i=rx-6;i<=rx+6;i++) {
            for(int j=ry-6;j<=ry+6;j++) {
                MapLocation tempLocation = new MapLocation(i, j);

                if(rc.canSenseLocation(tempLocation)) {
                    int soup = rc.senseSoup(tempLocation);

                    totalSoup += soup;
                    totalX += i * soup;
                    totalY += j * soup;
                }
            }
        }

        return new MapLocation(totalX / totalSoup, totalY / totalSoup);
    }

    public static MapLocation unweightedSoupCentroid(RobotController rc) throws GameActionException {
        int totalSoup = 0;
        int totalX = 0, totalY = 0;
        int rx = rc.getLocation().x, ry = rc.getLocation().y;

        for(int i=rx-6;i<=rx+6;i++) {
            for(int j=ry-6;j<=ry+6;j++) {
                MapLocation tempLocation = new MapLocation(i, j);

                if(rc.canSenseLocation(tempLocation) && rc.senseSoup(tempLocation) > 0) {
                    totalSoup += 1;
                    totalX += i;
                    totalY += j;
                }
            }
        }

        return new MapLocation(totalX / totalSoup, totalY / totalSoup);
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
            prevDirection = rc.getLocation().directionTo(destination);
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

    public static class Bug2PathfinderDrone {
        private RobotController rc;
        private MapLocation destination;
        private List<MapLocation> line;
        private Direction prevDirection;
        public Bug2PathfinderDrone(RobotController rc, MapLocation destination) {
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
                    if (rc.canMove(dir)) {
                        System.out.println(dir + " Actual");
                        prevDirection = dir;
                        rc.move(dir);
                        return;
                    }
                    for (int j = 0;j < 8;j++) {
                        dir = dir.rotateLeft();
                        if (rc.canMove(dir)) {
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

    public static boolean tryBuildDirectional(RobotController rc,RobotType robotType,Direction dir) throws GameActionException {
        Direction rightDir = dir;
        Direction leftDir = dir;
        if (rc.canBuildRobot(robotType,dir)) {
            rc.buildRobot(robotType,dir);
            return true;
        }
        for (int i = 0;i < 4;i++) {
            rightDir = rightDir.rotateRight();
            leftDir = leftDir.rotateLeft();
            if (rc.canBuildRobot(robotType,rightDir)) {
                rc.buildRobot(robotType,rightDir);
                return true;
            }
            if (rc.canBuildRobot(robotType,leftDir)) {
                rc.buildRobot(robotType,leftDir);
                return true;
            }
        }
        return false;
    }

    public static MapLocation centroid(MapLocation[] locations) {
        int x = 0;
        int y = 0;
        for (MapLocation location : locations) {
            x += location.x;
            y += location.y;
        }
        return new MapLocation(x / locations.length, y / locations.length);
    }


    public static boolean tryDigHighest(RobotController rc) throws GameActionException {
        Direction bestDir = Direction.CENTER;
        for (Direction dir: Direction.allDirections()) {
            if (rc.canDigDirt(dir)) {
                if (rc.senseElevation(rc.adjacentLocation(dir)) > rc.senseElevation(rc.adjacentLocation(bestDir))) {
                    bestDir = dir;
                }
            }
        }
        if (rc.canDigDirt(bestDir)) {
            rc.digDirt(bestDir);
            rc.setIndicatorLine(rc.getLocation(),rc.adjacentLocation(bestDir),0,255,0);
            return true;
        }
        return false;
    }

    public static boolean tryDigLowest(RobotController rc) throws GameActionException {
        Direction bestDir = Direction.CENTER;
        for (Direction dir: Direction.allDirections()) {
            if (rc.canDigDirt(dir)) {
                if (rc.senseElevation(rc.adjacentLocation(dir)) < rc.senseElevation(rc.adjacentLocation(bestDir))) {
                    bestDir = dir;
                }
            }
        }
        if (rc.canDigDirt(bestDir)) {
            rc.digDirt(bestDir);
            rc.setIndicatorLine(rc.getLocation(),rc.adjacentLocation(bestDir),0,255,0);
            return true;
        }
        return false;
    }

    public static boolean tryDepositLowest(RobotController rc) throws GameActionException {
        Direction bestDir = Direction.CENTER;
        for (Direction dir: Direction.allDirections()) {
            if (rc.canDigDirt(dir)) {
                if (rc.senseElevation(rc.adjacentLocation(dir)) < rc.senseElevation(rc.adjacentLocation(bestDir))) {
                    bestDir = dir;
                }
            }
        }
        if (rc.canDigDirt(bestDir)) {
            rc.digDirt(bestDir);
            return true;
        }
        return false;
    }


    public static boolean notAlliedBuilding(RobotController rc,MapLocation location) throws GameActionException {
        RobotInfo robot = rc.senseRobotAtLocation(location);
        return robot == null || robot.team == rc.getTeam().opponent() ||
                robot.type == RobotType.MINER || robot.type == RobotType.COW || robot.type == RobotType.DELIVERY_DRONE || robot.type == RobotType.LANDSCAPER;
    }











}
