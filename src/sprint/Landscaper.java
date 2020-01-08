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


    public Landscaper(RobotController rc) throws GameActionException {
        this.rc = rc;
        HQ = utils.hqPosition(rc);
        jobQueue = new LinkedList<>();
        System.out.println("ROBOT COUNT: " + rc.getRobotCount());
        if (rc.getRobotCount() > 6) {
            System.out.println("FORTIFY HQ JOB");
            jobQueue.add(new Job(Mode.FORTIFY_HQ,0,0));
        } else {
            System.out.println("PROTECT DEPOSIT JOB");
            jobQueue.add(new ProtectDepositJob(Mode.PROTECT_DEPOSIT,33,6));
        }

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

        System.out.println("BYTECODE: " + Clock.getBytecodeNum());

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
        private MapLocation refineryLocation;
        public ProtectDepositJob(Mode mode, int param1, int param2) {
            super(mode, param1, param2);
            deposit = new MapLocation(param1,param2);
            pathfinder = new utils.Bug2Pathfinder(rc,deposit);

            refineryLocation = null;
        }

        public void work() throws GameActionException {
            if (!rc.isReady()) return;
            if (refineryLocation != null) {
                System.out.println("Info: " + rc.senseElevation(rc.getLocation()) +
                        " " + rc.getDirtCarrying() + " " + refineryLocation.distanceSquaredTo(rc.getLocation()));

            }
            if (refineryLocation == null) {
                pathfinder.moveTowards();
                if (rc.getLocation().isWithinDistanceSquared(deposit,24)) {
                    for (RobotInfo robotInfo: rc.senseNearbyRobots(-1,rc.getTeam())) {
                        if (robotInfo.type == RobotType.REFINERY) {
                            refineryLocation = robotInfo.location;
                            System.out.println("FOUND REFINERY: " + refineryLocation);
//
                        }
                    }
                }
            } else if (refineryLocation.distanceSquaredTo(rc.getLocation()) < 25) {
                System.out.println("moving to flood wall building location");
                Direction moveDirection  = refineryLocation.directionTo(rc.getLocation());
                if (rc.canMove(moveDirection)) {
                    rc.move(moveDirection);
                } else if (rc.canMove(moveDirection.rotateRight())) {
                    rc.move(moveDirection.rotateRight());
                } else if (rc.canMove(moveDirection.rotateLeft())) {
                    rc.move(moveDirection.rotateLeft());
                }

            } else if (refineryLocation.distanceSquaredTo(rc.getLocation()) > 24 && refineryLocation.distanceSquaredTo(rc.getLocation()) < 33) {
                int waterLevel = utils.getWaterLevel(rc.getRoundNum());
                if (rc.getDirtCarrying() == 0 && rc.canDigDirt(refineryLocation.directionTo(rc.getLocation()))) {
                    System.out.println("Low on dirt, digging");
                    rc.digDirt(refineryLocation.directionTo(rc.getLocation()));
                } else if (waterLevel  + 2 >= rc.senseElevation(rc.getLocation())) {
                    System.out.println("Water level: " + waterLevel + "Increasing elevation of flood wall");
                    rc.depositDirt(Direction.CENTER);
                } else {
                    Direction dir = rc.getLocation().directionTo(refineryLocation);
                    System.out.println("Finish current flood wall, looking for next wall segment to build");
                    for (int i = 0;i < 7;i++) {
                        dir = dir.rotateLeft();
                        MapLocation tempLoc = rc.getLocation().add(dir);
                        if (refineryLocation.distanceSquaredTo(tempLoc) > 24 && refineryLocation.distanceSquaredTo(tempLoc) < 33) {
                            System.out.println("Next wall segment: " + tempLoc);
                            if (rc.senseElevation(tempLoc) < rc.senseElevation(rc.getLocation())) {
                                System.out.println("Water level: " + waterLevel + "Increasing elevation of flood wall");
                                rc.depositDirt(dir);
                            } else if (rc.senseElevation(tempLoc) > rc.senseElevation(rc.getLocation()) + 3){
                                System.out.println("Too high, lowering  elevation of flood wall");
                                rc.digDirt(dir);
                            } else {
                                rc.move(dir);
                            }
                            break;
                        }
                    }
                }
            }



        }

    }


    public void attack() {
        for (RobotInfo robotInfo : rc.senseNearbyRobots(-1,rc.getTeam().opponent())) {


        }
    }




}
