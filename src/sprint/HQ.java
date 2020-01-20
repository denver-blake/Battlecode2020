package sprint;

import battlecode.common.*;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class HQ implements Robot {

    enum UnitType {
        MINER,
        BUILDER,
        SCOUT,

    }
    private class Unit {
        UnitType unitType;


        public Unit(UnitType unitType) {
            this.unitType = unitType;
        }


    }

    private RobotController rc;
    Queue<Unit> buildQueue;
    int currentBlockChainRound;
    boolean built;
    NetGun netGun;
    MapLocation localSoupCentroid;

    public HQ (RobotController rc) throws GameActionException {
        this.rc = rc;
        buildQueue = new LinkedList<Unit>();
        currentBlockChainRound = 1;
        int[] hqPosBlock = {utils.BLOCKCHAIN_TAG, rc.getLocation().x, rc.getLocation().y, 0, 0, 0, 0};
        rc.submitTransaction(hqPosBlock, 5);

        netGun = new NetGun(rc);


        localSoupCentroid = utils.weightedSoupCentroid(rc);

        buildQueue.add(new Unit(UnitType.BUILDER));
        for(int i=0;i<2;i++) {
            buildQueue.add(new Unit(UnitType.MINER));
        }

    }

    public void run() throws GameActionException {


        netGun.run();


        if(!buildQueue.isEmpty()) {
            switch (buildQueue.peek().unitType) {
                case MINER:
                    buildMiner();
                    break;
                case BUILDER:
                    buildBuilder();
                    break;
                default:
                    break;
            }
        }
        readBlockChain();
        //if (rc.canBuildRobot(RobotType.MINER,Direction.NORTH))  rc.buildRobot(RobotType.MINER,Direction.NORTH);

    }
    private void readBlockChain() throws GameActionException {
        for (Transaction transaction : rc.getBlock(currentBlockChainRound)) {
            int[] msg = transaction.getMessage();
            if (msg.length != 7) continue;
            if (msg[0] == utils.BLOCKCHAIN_TAG) {
                //Transaction tags to look for

            }
        }
        currentBlockChainRound++;
        if (Clock.getBytecodesLeft() > 500 && currentBlockChainRound < rc.getRoundNum()) {
            readBlockChain();
        }

    }

    private void buildMiner() throws GameActionException {
        if (rc.getTeamSoup() > 80 && utils.tryBuild(rc,RobotType.MINER)) {

            int[] msg = new int[] {utils.BLOCKCHAIN_TAG,rc.getRoundNum(),utils.MINER_TAG,localSoupCentroid.x,localSoupCentroid.y,0,0};

            if (rc.canSubmitTransaction(msg,10)) {
                rc.submitTransaction(msg,10);
            }
            buildQueue.remove();
        }
    }



    private void buildBuilder() throws GameActionException {
        if (rc.getTeamSoup() > 80 && utils.tryBuild(rc, RobotType.MINER)) {
            MapLocation school = findDesignSchoolLocation();
            int[] msg = {utils.BLOCKCHAIN_TAG, rc.getRoundNum(), utils.BUILDER_TAG, localSoupCentroid.x, localSoupCentroid.y, school.x, school.y};
            if (rc.canSubmitTransaction(msg, 10)) {
                rc.submitTransaction(msg, 10);
                System.out.println("SUBMITTED JOB TRANSACTION");
            }
            buildQueue.remove();
        }
    }


    private MapLocation findDesignSchoolLocation() throws GameActionException {
        MapLocation bestLocation = null;
        int bestValue = 0;
        for (Direction direction : Direction.cardinalDirections()) {
            MapLocation location = rc.getLocation().add(direction).add(direction);
            Direction dir = direction.opposite().rotateRight().rotateRight();
            if (!rc.onTheMap(location)) continue;
            int value = 0;
            for (int i = 0;i < 5;i++) {
                if (!rc.senseFlooding(location.add(dir)) && Math.abs(rc.senseElevation(location) - rc.senseElevation(location.add(dir))) <= 3) {
                    value++;
                }
            }
            if (value > bestValue) {
                bestLocation = location;
                bestValue = value;
            }

        }
        return bestLocation;
    }


}
