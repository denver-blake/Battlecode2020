package sprint;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DesignSchool implements Robot {
    enum UnitType {
        FORTIFY_HQ,
        DISINTEGRATE,
        PROTECT_DEPOSIT // (param1,param2) = deposit location

    }
    
    private class Unit {
        UnitType unitType;
        int param1,param2;
        
        public Unit(UnitType unitType,int param1,int param2) {
            this.unitType = unitType;
            this.param1 = param1;
            this.param2 = param2;
        }
    }
    private RobotController rc;
    private LinkedList<Unit> buildQueue;
    private MapLocation hqLocation;
    private int currentBlockChainRound;
    private List<MapLocation> refineryLocations;
    private int built = 0;
    private boolean refineryBuilt;
    
    public DesignSchool(RobotController rc) throws GameActionException {
        this.rc = rc;
        buildQueue = new LinkedList<Unit>();
        hqLocation = utils.hqPosition(rc);
        refineryLocations = new ArrayList<MapLocation>();
        currentBlockChainRound = 1;
        buildQueue.add(new Unit(UnitType.PROTECT_DEPOSIT,hqLocation.x,hqLocation.y));
        buildQueue.add(new Unit(UnitType.PROTECT_DEPOSIT,hqLocation.x,hqLocation.y));
        refineryBuilt = false;


    }

    public void run() throws GameActionException {
//        if (built < 1 ) {
//
//            rc.buildRobot(RobotType.LANDSCAPER,Direction.NORTH);
//
//            built++;
//        }
        System.out.println("COOLDOWN: " + rc.getCooldownTurns() + "TEAM SOUP: " + rc.getTeamSoup());
        if(!buildQueue.isEmpty()) {
            switch (buildQueue.peek().unitType) {
                case FORTIFY_HQ:
                    buildFortifyHQ();
                    System.out.println("build fortify HQ unit");
                    break;

                case PROTECT_DEPOSIT:
                    buildProtectDeposit();
                    break;
                case DISINTEGRATE:
                    int[] msg = {utils.BLOCKCHAIN_TAG,rc.getRoundNum(),utils.START_FORTIFICATION,0,0,0,0};
                    if (rc.canSubmitTransaction(msg,10)) {
                        rc.submitTransaction(msg,10);
                        System.out.println("SUBMITTED JOB TRANSACTION");
                        rc.disintegrate();

                    }
                default:
                    break;
            }
        }
        readBlockChain();


    }

    private void readBlockChain() throws GameActionException {
        for (Transaction transaction : rc.getBlock(currentBlockChainRound)) {
            int[] msg = transaction.getMessage();
            if (msg.length != 7) continue;
            if (msg[0] == utils.BLOCKCHAIN_TAG) {
//                if (msg[1] == utils.NEW_REFINERY_TAG) {
//                    MapLocation loc = new MapLocation(msg[2],msg[3]);
//                    if (!refineryLocations.contains(loc)) {
//                        refineryLocations.add(loc);
//                        buildQueue.addFirst(new Unit(UnitType.PROTECT_DEPOSIT,loc.x,loc.y));
//                    }
//                }
                if (msg[2] == utils.GOT_RID_OF_WATER) {
                    if (msg[3] == hqLocation.x && msg[4] == hqLocation.y) {
                        createFortifyHQBuildQueue();
                    }
                }
            }
        }
        currentBlockChainRound++;
        if (Clock.getBytecodesLeft() > 500 && currentBlockChainRound < rc.getRoundNum()) {
            readBlockChain();
        }

    }

    private void createFortifyHQBuildQueue() throws GameActionException {
        Direction dir;
        Direction rightDir;
        Direction leftDir;
        MapLocation rightLoc;
        MapLocation leftLoc;

        dir = rc.getLocation().directionTo(hqLocation);
        leftLoc = hqLocation.add(dir.rotateLeft());
        rightLoc = hqLocation.add(dir.rotateRight());
        leftDir = dir;
        rightDir = dir;
        if (rc.onTheMap(leftLoc.add(leftDir))) {
            buildQueue.add(new Unit(UnitType.FORTIFY_HQ,leftLoc.add(leftDir).x,leftLoc.add(leftDir).y));
        }
        if (rc.onTheMap(rightLoc.add(rightDir))) {
            buildQueue.add(new Unit(UnitType.FORTIFY_HQ,rightLoc.add(rightDir).x,rightLoc.add(rightDir).y));
        }
        leftDir = leftDir.rotateLeft();
        rightDir = rightDir.rotateRight();
        if (rc.onTheMap(leftLoc.add(leftDir))) {
            buildQueue.add(new Unit(UnitType.FORTIFY_HQ,leftLoc.add(leftDir).x,leftLoc.add(leftDir).y));
        }
        if (rc.onTheMap(rightLoc.add(rightDir))) {
            buildQueue.add(new Unit(UnitType.FORTIFY_HQ,rightLoc.add(rightDir).x,rightLoc.add(rightDir).y));
        }
        leftDir = leftDir.rotateLeft();
        rightDir = rightDir.rotateRight();
        if (rc.onTheMap(leftLoc.add(leftDir))) {
            buildQueue.add(new Unit(UnitType.FORTIFY_HQ,leftLoc.add(leftDir).x,leftLoc.add(leftDir).y));
        }
        if (rc.onTheMap(rightLoc.add(rightDir))) {
            buildQueue.add(new Unit(UnitType.FORTIFY_HQ,rightLoc.add(rightDir).x,rightLoc.add(rightDir).y));
        }
//


        dir = rc.getLocation().directionTo(hqLocation);
        if (rc.onTheMap(hqLocation.add(dir))) {
            buildQueue.add(new Unit(UnitType.FORTIFY_HQ,hqLocation.add(dir).x,hqLocation.add(dir).y));
        }
        rightDir = dir.rotateRight();
        leftDir = dir.rotateLeft();
        if (rc.onTheMap(hqLocation.add(rightDir))) {
            buildQueue.add(new Unit(UnitType.FORTIFY_HQ,hqLocation.add(rightDir).x,hqLocation.add(rightDir).y));
        }
        if (rc.onTheMap(hqLocation.add(leftDir))) {
            buildQueue.add(new Unit(UnitType.FORTIFY_HQ,hqLocation.add(leftDir).x,hqLocation.add(leftDir).y));
        }
        rightDir = rightDir.rotateRight();
        leftDir = leftDir.rotateLeft();
        if (rc.onTheMap(hqLocation.add(rightDir))) {
            buildQueue.add(new Unit(UnitType.FORTIFY_HQ,hqLocation.add(rightDir).x,hqLocation.add(rightDir).y));
        }
        if (rc.onTheMap(hqLocation.add(leftDir))) {
            buildQueue.add(new Unit(UnitType.FORTIFY_HQ,hqLocation.add(leftDir).x,hqLocation.add(leftDir).y));
        }






        dir = rc.getLocation().directionTo(hqLocation);
        leftDir = dir.rotateLeft().rotateLeft();
        rightDir = dir.rotateRight().rotateRight();
        dir = dir.opposite();
        leftLoc = hqLocation.add(dir.rotateRight());
        rightLoc = hqLocation.add(dir.rotateLeft());
        if (rc.onTheMap(leftLoc.add(leftDir))) {
            buildQueue.add(new Unit(UnitType.FORTIFY_HQ,leftLoc.add(leftDir).x,leftLoc.add(leftDir).y));
        }
        if (rc.onTheMap(rightLoc.add(rightDir))) {
            buildQueue.add(new Unit(UnitType.FORTIFY_HQ,rightLoc.add(rightDir).x,rightLoc.add(rightDir).y));
        }
        leftDir = leftDir.rotateLeft();
        rightDir = rightDir.rotateRight();
        if (rc.onTheMap(leftLoc.add(leftDir))) {
            buildQueue.add(new Unit(UnitType.FORTIFY_HQ,leftLoc.add(leftDir).x,leftLoc.add(leftDir).y));
        }
        if (rc.onTheMap(rightLoc.add(rightDir))) {
            buildQueue.add(new Unit(UnitType.FORTIFY_HQ,rightLoc.add(rightDir).x,rightLoc.add(rightDir).y));
        }
        leftDir = leftDir.rotateLeft();
        rightDir = rightDir.rotateRight();
        if (rc.onTheMap(leftLoc.add(leftDir))) {
            buildQueue.add(new Unit(UnitType.FORTIFY_HQ,leftLoc.add(leftDir).x,leftLoc.add(leftDir).y));
        }
        if (rc.onTheMap(rightLoc.add(rightDir))) {
            buildQueue.add(new Unit(UnitType.FORTIFY_HQ,rightLoc.add(rightDir).x,rightLoc.add(rightDir).y));
        }




        dir = rc.getLocation().directionTo(hqLocation);

        rightDir = dir.rotateRight();
        leftDir = dir.rotateLeft();


        rightDir = rightDir.rotateRight();
        leftDir = leftDir.rotateLeft();

        rightDir = rightDir.rotateRight();
        leftDir = leftDir.rotateLeft();
        if (rc.onTheMap(hqLocation.add(rightDir))) {
            buildQueue.add(new Unit(UnitType.FORTIFY_HQ,hqLocation.add(rightDir).x,hqLocation.add(rightDir).y));
        }
        if (rc.onTheMap(hqLocation.add(leftDir))) {
            buildQueue.add(new Unit(UnitType.FORTIFY_HQ,hqLocation.add(leftDir).x,hqLocation.add(leftDir).y));
        }
        dir = dir.opposite();
        if (rc.onTheMap(hqLocation.add(dir))) {
            buildQueue.add(new Unit(UnitType.FORTIFY_HQ,hqLocation.add(dir).x,hqLocation.add(dir).y));
        }

        buildQueue.add(new Unit(UnitType.DISINTEGRATE,0,0));







    }

    private void buildFortifyHQ() throws GameActionException {
        MapLocation loc = new MapLocation(buildQueue.peek().param1,buildQueue.peek().param2);
        if (rc.getTeamSoup() > 160 && (refineryBuilt || rc.getTeamSoup() > 300) && utils.tryBuildDirectional(rc,RobotType.LANDSCAPER,rc.getLocation().directionTo(loc))) {
            System.out.println("Built fortify HQ Landscaper");
            int[] msg = {utils.BLOCKCHAIN_TAG,rc.getRoundNum(),utils.LANDSCAPER_FORTIFY_CASTLE_TAG,loc.x,loc.y,0,0};
            if (rc.canSubmitTransaction(msg,10)) {
                rc.submitTransaction(msg,10);
                System.out.println("SUBMITTED JOB TRANSACTION");
            }
            buildQueue.remove();
        }
    }

    private void buildProtectDeposit() throws GameActionException {
        if (rc.getTeamSoup() > 160 && utils.tryBuild(rc,RobotType.LANDSCAPER)) {
            System.out.println("Built Protect Deposit Landscaper at: " + (new MapLocation(buildQueue.peek().param1,buildQueue.peek().param2)) );
            int[] msg = {utils.BLOCKCHAIN_TAG,rc.getRoundNum(),utils.LANDSCAPER_PROTECT_BASE_TAG,buildQueue.peek().param1,buildQueue.peek().param2,0,0};
            if (rc.canSubmitTransaction(msg,10)) {
                rc.submitTransaction(msg,10);
                System.out.println("SUBMITTED JOB TRANSACTION");
            }
            buildQueue.remove();

        }
    }
}
