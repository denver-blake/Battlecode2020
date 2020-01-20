package sprint;

import battlecode.common.*;

import java.util.LinkedList;
import java.util.Queue;

public class Drone implements Robot  {

    enum Mode {
        SCOUT,
        TRANSPORT
    }
    private class Job {
        Mode mode;
        public Job(Mode mode) {
            this.mode = mode;
        }

        public void work() throws  GameActionException { }
    }

    RobotController rc;
    Queue<Job> jobQueue;
    MapLocation HQLocation;
    int currentBlockChainRound;
    int roundBuilt;
    utils.Bug2PathfinderDrone pathfinder;

    public Drone(RobotController rc) throws GameActionException {
        this.rc = rc;
        HQLocation = utils.hqPosition(rc);
        jobQueue = new LinkedList<>();
        currentBlockChainRound = rc.getRoundNum()-1;
        roundBuilt = rc.getRoundNum()-1;
        jobQueue.add(new ScoutJob(Mode.SCOUT));
        System.out.println("Round Built: " + roundBuilt);
        pathfinder = new utils.Bug2PathfinderDrone(rc, new MapLocation(rc.getMapWidth()/2,rc.getMapHeight()/2));
    }

    public void run() throws GameActionException {
        if(!jobQueue.isEmpty()) {
            jobQueue.peek().work();
        } else {
            defaultJob();
        }
        readBlockChain();

        System.out.println("BYTECODE: " + Clock.getBytecodeNum());

    }

    private void defaultJob() throws GameActionException {
        if (rc.getLocation().distanceSquaredTo(HQLocation) <= 18) {
            pathfinder.moveTowards();

        }
    }

    private void readBlockChain() throws GameActionException {
        for (Transaction transaction : rc.getBlock(currentBlockChainRound)) {
            int[] msg = transaction.getMessage();
            if (msg.length != 7) continue;
            for (int i = 0;i < msg.length;i++) {
                System.out.print(msg[i] + ", ");
            }
            System.out.println("msg ");
            if (msg[0] == utils.BLOCKCHAIN_TAG) {
                if (msg[1] == roundBuilt) {
                    //Transaction tags to look for
                }
                if (msg[2] == utils.NEED_DRONE_TRANSPORT) {
                    MapLocation start = new MapLocation(msg[3],msg[4]);
                    MapLocation end = new MapLocation(msg[5],msg[6]);
                    jobQueue.add(new TransportJob(Mode.TRANSPORT,start,end,msg[1]));

                }
            }
        }
        currentBlockChainRound++;
        if (Clock.getBytecodesLeft() > 500 && currentBlockChainRound < rc.getRoundNum()) {
            readBlockChain();
        }
    }

    public class ScoutJob extends Job {

        MapLocation[] locationsToSearch;
        int locationIndex = 0;
        int timer = 0;
        utils.Bug2PathfinderDrone pathfinder;
        public ScoutJob(Mode mode) {
            super(mode);
            calculateLocationsToSearch();
            pathfinder = new utils.Bug2PathfinderDrone(rc,locationsToSearch[0]);
            System.out.println("Pathing to " + locationsToSearch[0]);

        }
        public void calculateLocationsToSearch() {

            locationsToSearch = new MapLocation[(int) Math.ceil(rc.getMapWidth() / 9.0 * 2)];
            for (int i = 0;i  < locationsToSearch.length / 2;i++) {
                if (i % 2 == 0) {
                    locationsToSearch[i*2] = new MapLocation(4 + i * 9,4);
                    locationsToSearch[i*2+ 1] = new MapLocation(4 + i * 9,rc.getMapHeight() - 5);
                } else {
                    locationsToSearch[i*2+1] = new MapLocation(4 + i * 9,4);
                    locationsToSearch[i*2] = new MapLocation(4 + i * 9,rc.getMapHeight() - 5);
                }
            }
        }

        //
        public void emptyDeposit() throws GameActionException {
            MapLocation minerLocation = null;
            for (RobotInfo robot: rc.senseNearbyRobots(-1,rc.getTeam())) {
                if (robot.type == RobotType.MINER) {
                    minerLocation = robot.location;
                    break;
                }
            }
            if (minerLocation == null)
            for (int i = -4;i <= 4;i++) {
                for (int j = -4;j <= 4;j++) {
                    MapLocation loc = new MapLocation(i,j);
                    if (rc.canSenseLocation(loc) && rc.senseSoup(loc) > 0) {
                        
                    }
                }
            }

        }


        public void work() throws GameActionException {
            System.out.println("Pathing to " + locationsToSearch[locationIndex]);
            rc.setIndicatorLine(rc.getLocation(),locationsToSearch[locationIndex],255,0,0);
            if (locationIndex == locationsToSearch.length) jobQueue.remove();
            if (pathfinder == null) {
                pathfinder = new utils.Bug2PathfinderDrone(rc,locationsToSearch[locationIndex]);

            }
            if (timer == Math.max(rc.getMapHeight(),rc.getMapWidth()) * 2) {
                locationIndex++;
                pathfinder = null;
                timer = 0;
            } else if (rc.getLocation().isWithinDistanceSquared(locationsToSearch[locationIndex],4)) {
                locationIndex++;
                pathfinder = null;
                timer = 0;
            } else {
                if (rc.isReady())  timer++;
                pathfinder.moveTowards();

            }



        }



    }

    public class TransportJob extends Job {

        MapLocation startLocation;
        MapLocation endLocation;
        utils.Bug2PathfinderDrone pathfinder;
        int robotID;
        public TransportJob(Mode mode,MapLocation startLocation,MapLocation endLocation,int robotID) {
            super(mode);
            this.startLocation = startLocation;
            this.endLocation = endLocation;
            this.robotID = robotID;
            pathfinder = new utils.Bug2PathfinderDrone(rc,startLocation);

        }

        public void work() throws GameActionException {
            if (!rc.isReady()) return;

            RobotInfo robot = rc.senseRobot(robotID);

            if ( robot != null) {
                if (robot.location.isAdjacentTo(rc.getLocation())) {
                    rc.pickUpUnit(robotID);
                    pathfinder = new utils.Bug2PathfinderDrone(rc,endLocation);
                } else {
                    utils.moveTowardsSimple(rc, robot.location);
                }
            } else if (rc.isCurrentlyHoldingUnit()) {
                if (rc.getLocation().distanceSquaredTo(endLocation) == 0
                ) {
                    for (Direction dir: Direction.allDirections()) {
                        if (rc.canMove(dir)) {
                            rc.move(dir);
                        }
                    }
                }
                if (rc.getLocation().distanceSquaredTo(endLocation) <= 2 && utils.tryDropAlly(rc,rc.getLocation().directionTo(endLocation))) {
                    jobQueue.remove();
                }
                pathfinder.moveTowards();
            } else if (!rc.isCurrentlyHoldingUnit()) {
                pathfinder.moveTowards();
            }
        }
    }

}
