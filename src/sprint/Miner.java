package sprint;

import battlecode.common.*;

import java.util.LinkedList;

public class Miner implements Robot {

    enum Mode {
        BUILDER, //(param1, param2) = refinery location
        SCOUT_DEPOSIT, //param1 specifies direction to scout
        MINER, //(param1, param2) = deposit centroid
        BUILD_SCHOOL, //(param1, param2) = desired location
        BUILD_CENTER, //(param1, param2) = desired location
        BUILD_DEFENSIVE_GUN, //(param1, param2) = building location
        BUILD_VAPORATOR //(param1, param2) = desired location
    }

    private class Job {
        Mode mode;

        public Job(Mode mode) {
            this.mode = mode;
        }

        public void work() throws GameActionException{

        }

    }


    private LinkedList<Job> jobQueue;
    private RobotController rc;
    private MapLocation hqLocation;
    private int roundBuilt;
    private boolean built = false;
    private int currentBlockChainRound;
    private MapLocation refineryLocation;
    private MapLocation HQ;
    private Boolean HQAccessible;

    public Miner(RobotController rc) throws GameActionException {
        this.rc = rc;
        roundBuilt = rc.getRoundNum();
        jobQueue = new LinkedList<>();
        hqLocation = utils.hqPosition(rc);
        currentBlockChainRound = 1;
        roundBuilt = rc.getRoundNum()-1;
        HQ = utils.hqPosition(rc);
        HQAccessible = true;

    }

    public void run() throws GameActionException {
        if (rc.getRoundNum() > 250 && rc.getLocation().distanceSquaredTo(hqLocation) < 20) rc.disintegrate();
        if (utils.runFromDrone(rc)) {
            return;
        }



//        turn++;
        //System.out.println("running");
        if(!jobQueue.isEmpty()) {
            jobQueue.peek().work();
        }
        readBlockChain();


    }

    private void readBlockChain() throws GameActionException {
        //System.out.println("READING BLOCK AT ROUND " + currentBlockChainRound);
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
                    if (msg[2] == utils.MINER_TAG) {
                        jobQueue.add(new MinerJob(Mode.MINER,new MapLocation(msg[3],msg[4])));
                    }
                    if (msg[2] == utils.BUILDER_TAG) {
                        MinerJob minerJob = new MinerJob(Mode.MINER,new MapLocation(msg[3],msg[4]));
                        jobQueue.add(new BuilderJob(Mode.BUILDER,minerJob,new MapLocation(msg[5],msg[6]),new MapLocation(msg[3],msg[4])));
                    }

                }
                if (msg[2] == utils.NEW_REFINERY_TAG) {
                    refineryLocation = new MapLocation(msg[3],msg[4]);
                }
                if (msg[2] == utils.GOT_RID_OF_WATER) {
                    if (msg[3] == hqLocation.x && msg[4] == hqLocation.y) {
                        HQAccessible = false;
                    }
                }
            }
        }
        currentBlockChainRound++;
        if (Clock.getBytecodesLeft() > 500 && currentBlockChainRound < rc.getRoundNum()) {
            readBlockChain();
        }

    }


    public class MinerJob extends Job{

        MapLocation soupDeposit;
        utils.Bug2Pathfinder pathfinder;
        utils.Bug2Pathfinder explorer;
        MapLocation exploreLocation;
        int exploreTimer = 0;
        utils.Bug2Pathfinder refineryPathfinder;


        public MinerJob(Mode mode,MapLocation initialSoupDeposit) {
            super(mode);
            this.soupDeposit = initialSoupDeposit;
            pathfinder = new utils.Bug2Pathfinder(rc,soupDeposit);
            exploreLocation = new MapLocation((int) (Math.random() * rc.getMapWidth()),(int) (Math.random() * rc.getMapHeight()));
            explorer = new utils.Bug2Pathfinder(rc,exploreLocation);
        }


        public void work() throws GameActionException {
            if (!rc.isReady()) return;
            updateSoupDeposit();
            if (rc.getSoupCarrying() < 100) {
                refineryPathfinder = null;
                if (utils.tryMineSoup(rc)) {
                    System.out.println("Mining");
                } else {
                    MapLocation[] soupLocations = rc.senseNearbySoup();
                    if (soupLocations.length > 0) {
                        utils.moveTowardsSimple(rc,soupLocations[0]);
                        rc.setIndicatorLine(rc.getLocation(),soupDeposit,255,0,0);

                    } else if (soupDeposit != null) {
                        System.out.println("Moving towards deposit");

                        pathfinder.moveTowards();
                        rc.setIndicatorLine(rc.getLocation(),soupDeposit,150,75,0);
                    } else {
                        explore();
                    }
                }
            } else {
                if (utils.tryDepositSoup(rc)) {
                    System.out.println("Deposit Soup");
                }
                else if (refineryPathfinder == null) {
                    if (refineryLocation != null) {
                        refineryPathfinder = new utils.Bug2Pathfinder(rc,refineryLocation);
                    } else if (HQAccessible){
                        refineryPathfinder = new utils.Bug2Pathfinder(rc,HQ);
                    }
                }

                if (refineryPathfinder != null) {
                    refineryPathfinder.moveTowards();
                    rc.setIndicatorLine(rc.getLocation(),refineryLocation == null ? HQ:refineryLocation,0,0,255);
                } else if (soupDeposit != null) {
                    pathfinder.moveTowards();
                    rc.setIndicatorLine(rc.getLocation(),soupDeposit,150,75,0);

                } else {
                    explore();
                }
            }

        }

        private void updateSoupDeposit() throws GameActionException {
            MapLocation[] soupLocations = rc.senseNearbySoup();
            if (soupDeposit == null && soupLocations.length > 0) {
                soupDeposit = utils.weightedSoupCentroid(rc);

            } else if (soupDeposit != null && rc.getLocation().distanceSquaredTo(soupDeposit) <= 15 && soupLocations.length == 0) {
                soupDeposit = null;
            }
        }

        private void explore() throws GameActionException {
            if (exploreTimer > 50 || rc.getLocation().distanceSquaredTo(exploreLocation) < 20) {
                exploreLocation = new MapLocation((int) (Math.random() * rc.getMapWidth()),(int) (Math.random() * rc.getMapHeight()));
                explorer = new utils.Bug2Pathfinder(rc,exploreLocation);
                exploreTimer = 0;
            }
            explorer.moveTowards();
            rc.setIndicatorLine(rc.getLocation(),exploreLocation,0,255,0);
            exploreTimer++;
        }


    }

    class Building {
        RobotType robotType;
        MapLocation location;
        public Building(RobotType robotType,MapLocation location) {
            this.robotType = robotType;
            this.location = location;

        }

    }


    public  class BuilderJob extends Job{



        LinkedList<Building> buildQueue;
        LinkedList<Building> built;
        MinerJob minerJob;
        utils.Bug2Pathfinder pathfinder;
        utils.Bug2Pathfinder refineryPathfinder;
        MapLocation refineryLocation;
        public BuilderJob(Mode mode,MinerJob minerJob,MapLocation schoolLocation,MapLocation initialDeposit) {
            super(mode);
            this.minerJob = minerJob;
            buildQueue = new LinkedList<Building>();
            built = new LinkedList<Building>();
            buildQueue.add(new Building(RobotType.DESIGN_SCHOOL,schoolLocation));
            refineryLocation = initialDeposit;


        }

        public void work() throws GameActionException {
            if (!rc.isReady()) return;
            if (built.size() == 0 && rc.getTeamSoup() >= 160) {
                build();
            } else if (built.size() == 1 && rc.getTeamSoup() >= 210) {
                buildRefinery();
            } else {
                minerJob.work();
            }
        }

        private void buildRefinery() throws GameActionException {
            if (refineryPathfinder == null) {
                refineryPathfinder = new utils.Bug2Pathfinder(rc,refineryLocation);
            }
            if (rc.getLocation().distanceSquaredTo(HQ) > 25 && refineryLocation.distanceSquaredTo(rc.getLocation()) < 25) {
                for (Direction dir: Direction.allDirections()) {
                    if (rc.canBuildRobot(RobotType.REFINERY,dir)) {
                        rc.buildRobot(RobotType.REFINERY,dir);
                        built.add(new Building(RobotType.REFINERY,rc.adjacentLocation(dir)));
                        int[] msg = new int[] {utils.BLOCKCHAIN_TAG,rc.getRoundNum(),utils.NEW_REFINERY_TAG,rc.adjacentLocation(dir).x,rc.adjacentLocation(dir).y,0,0};
                        rc.submitTransaction(msg,10);
                        refineryLocation = null;
                        refineryPathfinder = null;
                    }
                }
                if (rc.isReady()) {
                    for (Direction dir: Direction.allDirections()) {
                        if (rc.canMove(dir)) {
                            rc.move(dir);
                        }
                    }
                }
            } else if (refineryLocation.distanceSquaredTo(rc.getLocation()) >= 25) {
                refineryPathfinder.moveTowards();
            } else {
                Direction direction = HQ.directionTo(rc.getLocation());
                if (rc.canMove(direction) && !rc.senseFlooding(rc.adjacentLocation(direction))) {
                    rc.move(direction);
                }
                else if (rc.canMove(direction.rotateLeft()) && !rc.senseFlooding(rc.adjacentLocation(direction.rotateLeft()))) {
                    rc.move(direction.rotateLeft());
                }
                else if (rc.canMove(direction.rotateRight()) && !rc.senseFlooding(rc.adjacentLocation(direction.rotateRight()))) {
                    rc.move(direction.rotateRight());
                }
            }

        }

        private void build() throws GameActionException {
            if (pathfinder == null) {
                pathfinder = new utils.Bug2Pathfinder(rc,buildQueue.peek().location);
            }
            if (rc.getLocation().distanceSquaredTo(buildQueue.peek().location) == 0) {
                for (Direction dir: Direction.allDirections()) {
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                    }
                }
            }
            if (rc.getLocation().distanceSquaredTo(buildQueue.peek().location) > 2) {
                pathfinder.moveTowards();
            } else if (rc.canBuildRobot(buildQueue.peek().robotType,rc.getLocation().directionTo(buildQueue.peek().location))) {
                rc.buildRobot(buildQueue.peek().robotType,rc.getLocation().directionTo(buildQueue.peek().location));
                built.add(buildQueue.remove());
                pathfinder = null;
            }
        }


    }

}