package sprint;

import battlecode.common.*;

import java.util.LinkedList;
import java.util.Queue;

public class HQ implements Robot {

    enum UnitType {
        MINER,
        BUILDER,
        SCOUT,

    }
    private class Unit {
        UnitType unitType;

        int[] transaction;

        public Unit(UnitType unitType, int[] transaction) {
            this.unitType = unitType;
            this.transaction = transaction;
        }

        public Unit(UnitType unitType) {
            this.unitType = unitType;
            this.transaction = null;
        }
    }

    private RobotController rc;
    Queue<Unit> buildQueue;
    int currentBlockChainRound;


    public HQ (RobotController rc) throws GameActionException {
        this.rc = rc;
        buildQueue = new LinkedList<Unit>();
        currentBlockChainRound = 1;
        int[] hqPosBlock = {utils.BLOCKCHAIN_TAG, rc.getLocation().x, rc.getLocation().y, 0, 0, 0, 0};
        rc.submitTransaction(hqPosBlock, 5);

        int localSoupMiners = utils.soupToMiners(utils.countLocalSoup(rc));
        MapLocation localSoupCentroid = utils.weightedSoupCentroid(rc);

        for(int i=0;i<localSoupMiners;i++) {
            MapLocation nextLocation = localSoupCentroid;
            for(int j = 0; j < i / 8; j++) {
                nextLocation = nextLocation.add(utils.intToDirection(i % 8));
            }
            int[] transaction = {utils.MINER_TAG, utils.NO_REFINERY_TAG, nextLocation.x, nextLocation.y, 0, 0, 0};
            buildQueue.add(new Unit(UnitType.MINER, transaction));
        }

        for(int i=0;i<8;i++) {
            Direction dir = utils.intToDirection(i);

            int[] transaction = {utils.MINER_TAG, utils.MINER_SCOUT_TAG, dir.dx, dir.dy, 0, 0, 0};
            buildQueue.add(new Unit(UnitType.MINER, transaction));
        }
    }


    public void run() throws GameActionException {
        if(!buildQueue.isEmpty()) {
            switch (buildQueue.peek().unitType) {
                case SCOUT:
                    buildScout();
                    break;
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
        if (rc.getTeamSoup() > 100 && utils.tryBuild(rc,RobotType.MINER)) {
            int[] msg;
            if(buildQueue.peek().transaction == null)
                msg = new int[] {utils.BLOCKCHAIN_TAG,rc.getRoundNum(),utils.MINER_TAG,0,0,0,0};
            else
                msg = buildQueue.peek().transaction;

            if (rc.canSubmitTransaction(msg,10)) {
                rc.submitTransaction(msg,10);
            }
            buildQueue.remove();
        }
    }

    private void buildScout() throws GameActionException {
        if (rc.getTeamSoup() > 100 && utils.tryBuild(rc,RobotType.MINER)) {
            int[] msg = {utils.BLOCKCHAIN_TAG,rc.getRoundNum(),utils.SCOUT_TAG,0,0,0,0};
            if (rc.canSubmitTransaction(msg,10)) {
                rc.submitTransaction(msg,10);
                System.out.println("SUBMITTED JOB TRANSACTION");
            }
            buildQueue.remove();
        }
    }

    private void buildBuilder() throws GameActionException {
        if (rc.getTeamSoup() > 100 && utils.tryBuild(rc,RobotType.MINER)) {
            int[] msg = {utils.BLOCKCHAIN_TAG,rc.getRoundNum(),utils.BUILDER_TAG,0,0,0,0};
            if (rc.canSubmitTransaction(msg,10)) {
                rc.submitTransaction(msg,10);
                System.out.println("SUBMITTED JOB TRANSACTION");
            }
            buildQueue.remove();
        }

    }

}
