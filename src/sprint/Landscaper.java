package sprint;

import battlecode.common.*;

import java.util.*;

public class Landscaper implements Robot {

    enum Mode {
        FORTIFY_HQ,
        DEFEND_FROM_FLOOD, //  location to protect
        SAVE_WORKER, //(param1, param2) = worker location
        ATTACK, //(param1, param2) = enemy location
        PROTECT_DEPOSIT // (param1,param2) = deposit location

    }
    private class Job {
        Mode mode;
        int param1, param2;

        public Job(Mode mode, int param1, int param2) {
            this.mode = mode;
            this.param1 = param1;
            this.param2 = param2;

        }
    }
    private Queue<Job> jobQueue;
    private RobotController rc;
    private MapLocation HQ;
    private Job currentJob;
    private utils.Bug2Pathfinder pathfinder;
    private int currentBlockChainRound;
    private int roundBuilt;


    public Landscaper(RobotController rc) throws GameActionException {
        this.rc = rc;
        HQ = utils.hqPosition(rc);
        jobQueue = new LinkedList<>();
        currentBlockChainRound = rc.getRoundNum()-1;

        roundBuilt = rc.getRoundNum()-1;
        System.out.println("Round Built: " + roundBuilt);

        pathfinder = null;


    }

    public void run() throws GameActionException {


        if(!jobQueue.isEmpty()) {
            switch (jobQueue.peek().mode) {
                case FORTIFY_HQ:
                    fortifyHQ();
                    System.out.println("fortify");
                    break;
                case DEFEND_FROM_FLOOD:
                    break;
                case SAVE_WORKER:
                    break;
                case ATTACK:
                    break;
                case PROTECT_DEPOSIT:
                    ((ProtectDepositJob) jobQueue.peek()).work();
                    break;
                default:
                    break;
            }
        }
        readBlockChain();

        System.out.println("BYTECODE: " + Clock.getBytecodeNum());

    }
    private void readBlockChain() throws GameActionException {
        //System.out.println("READING BLOCK AT ROUND " + currentBlockChainRound);
        for (Transaction transaction : rc.getBlock(currentBlockChainRound)) {
            int[] msg = transaction.getMessage();
            for (int i = 0;i < msg.length;i++) {
                System.out.print(msg[i] + ", ");
            }
            System.out.println("msg ");
            if (msg[0] == utils.BLOCKCHAIN_TAG) {
                if (msg[1] == roundBuilt) {
                    if (msg[2] == utils.LANDSCAPER_FORTIFY_CASTLE_TAG) {
                        System.out.println("Recieved fortify HQ Job");
                        jobQueue.add(new Job(Mode.FORTIFY_HQ,0,0));
                    } else if (msg[2] == utils.LANDSCAPER_PROTECT_DEPOSIT_TAG) {
                        System.out.println("Recieved Protect Deposit Job at:" + (new MapLocation(msg[3],msg[4])));
                        jobQueue.add(new ProtectDepositJob(Mode.PROTECT_DEPOSIT,msg[3],msg[4]));
                    }
                }
            }
        }
        currentBlockChainRound++;
        if (Clock.getBytecodesLeft() > 500 && currentBlockChainRound < rc.getRoundNum()) {
            readBlockChain();
        }

    }

    public void fortifyHQInit() {
        pathfinder = new utils.Bug2Pathfinder(rc,HQ);
    }
    public void fortifyHQ() throws GameActionException {
        if (pathfinder == null) {
            fortifyHQInit();
        }
        if (!rc.isReady()) return;
        if (!atFortifyHQLocation()) {
            System.out.println("MOVING");
            pathfinder.moveTowards();
        } else if (rc.getDirtCarrying() < 1) {
            Direction dir = rc.getLocation().directionTo(HQ).opposite();
            rc.digDirt(dir);


        } else {
            Direction dir = rc.getLocation().directionTo(HQ);
            rc.depositDirt(dir);
        }
    }
    public boolean atFortifyHQLocation() {
        switch(rc.getLocation().distanceSquaredTo(HQ)) {
            case 9:
            case 10:
            case 13:
            case 18:
                return true;
        }
        return false;

    }

    public class ProtectDepositJob extends Job{

        private utils.Bug2Pathfinder pathfinder;
        private MapLocation deposit;
        private List<MapLocation> wallLocations;
        private MapLocation locationToProtect;
        public ProtectDepositJob(Mode mode, int param1, int param2) {
            super(mode, param1, param2);
            deposit = new MapLocation(param1,param2);
            pathfinder = new utils.Bug2Pathfinder(rc,deposit);

            locationToProtect = new MapLocation(param1,param2);
        }

        public void work() throws GameActionException {
            if (!rc.isReady()) return;
            if (locationToProtect != null) {
                System.out.println("Info: " + rc.senseElevation(rc.getLocation()) +
                        " " + rc.getDirtCarrying() + " " + locationToProtect.distanceSquaredTo(rc.getLocation()));

            }

             if (atProtectDistance(rc.getLocation())) {
                int waterLevel = utils.getWaterLevel(rc.getRoundNum());
                if (rc.getDirtCarrying() == 0 && rc.canDigDirt(locationToProtect.directionTo(rc.getLocation()))) {
                    System.out.println("Low on dirt, digging");
                    rc.digDirt(locationToProtect.directionTo(rc.getLocation()));
                } else if (waterLevel  + 2 >= rc.senseElevation(rc.getLocation())) {
                    System.out.println("Water level: " + waterLevel + "Increasing elevation of flood wall");
                    rc.depositDirt(Direction.CENTER);
                } else {
                    Direction dir = rc.getLocation().directionTo(locationToProtect);
                    System.out.println("Finish current flood wall, looking for next wall segment to build");
                    for (int i = 0;i < 7;i++) {
                        dir = dir.rotateLeft();
                        MapLocation tempLoc = rc.getLocation().add(dir);
                        if (atProtectDistance(tempLoc)) {
                            System.out.println("Next wall segment: " + tempLoc);
                            if (rc.senseElevation(tempLoc) < rc.senseElevation(rc.getLocation())) {
                                System.out.println("Water level: " + waterLevel + "Increasing elevation of flood wall");
                                rc.depositDirt(dir);
                                break;
                            } else if (rc.senseElevation(tempLoc) > rc.senseElevation(rc.getLocation()) + 3){
                                System.out.println("Too high, lowering  elevation of flood wall");
                                rc.digDirt(dir);
                                break;
                            }
                        }
                    }
                    dir = rc.getLocation().directionTo(locationToProtect);

                    for (int i = 0;i < 7;i++) {
                        dir = dir.rotateLeft();
                        MapLocation tempLoc = rc.getLocation().add(dir);
                        if (atProtectDistance(tempLoc)) {

                            if (rc.canMove(dir)) {
                                System.out.println("Moving to next segment");
                                rc.move(dir);
                                break;
                            }

                        }
                    }
                }
            } else {
                 pathfinder.moveTowards();
             }



        }
        private boolean atProtectDistance(MapLocation loc) {
            switch(locationToProtect.distanceSquaredTo(loc)) {
                case 9:
                case 10:
                case 13:
                case 18:
                    return true;
            }
            return false;
        }

    }


    public void attack() {
        for (RobotInfo robotInfo : rc.senseNearbyRobots(-1,rc.getTeam().opponent())) {


        }
    }




}
