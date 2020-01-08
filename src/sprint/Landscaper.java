package sprint;

import battlecode.common.*;

import java.util.Queue;
import java.util.Random;

public class Landscaper implements Robot {

    enum Mode {
        SAVE_WORKER, //(param1, param2) = worker location
        DEFEND_FROM_FLOOD, //  location to protect
        FORTIFY_HQ,
        ATTACK, //(param1, param2) = enemy location

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

    }

    public void run() throws GameActionException {

//        if(!jobQueue.isEmpty()) {
//            switch (jobQueue.peek().mode) {
//                case SAVE_WORKER:
//                    break;
//                case DEFEND_FROM_FLOOD:
//
//                    break;
//                case FORTIFY_HQ:
//
//                    break;
//                case ATTACK:
//                    break;
//                default:
//                    break;
//            }
//        }
        if (pathfinder == null) {
            fortifyHQInit();
        } else {
            fortifyHQ();
        }
        System.out.println("BYTECODE: " + Clock.getBytecodeNum());

    }

    public void fortifyHQInit() {
        pathfinder = new utils.Bug2Pathfinder(rc,HQ);
    }
    public void fortifyHQ() throws GameActionException {
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

    public void attack() {
        for (RobotInfo robot : rc.senseNearbyRobots(-1,rc.getTeam().opponent())) {

        }
    }


}
