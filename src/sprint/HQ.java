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
        public Unit(UnitType unitType) {
            this.unitType = unitType;
        }
    }

    private RobotController rc;
    Queue<Unit> buildQueue;
    int currentBlockChainRound;
    boolean built;
    NetGun netGun;


    public HQ (RobotController rc) throws GameActionException {
        this.rc = rc;
        buildQueue = new LinkedList<Unit>();
        currentBlockChainRound = 1;
        int[] hqPosBlock = {utils.BLOCKCHAIN_TAG, rc.getLocation().x, rc.getLocation().y, 0, 0, 0, 0};
        rc.submitTransaction(hqPosBlock, 5);
        netGun = new NetGun(rc);
    }


    public void run() throws GameActionException {
        netGun.run();
//        if(!buildQueue.isEmpty()) {
//            switch (buildQueue.peek().unitType) {
//                case SCOUT:
//                    buildScout();
//                    break;
//                case MINER:
//                    buildMiner();
//                    break;
//                case BUILDER:
//                    buildBuilder();
//                    break;
//                default:
//                    break;
//            }
//        }
//        readBlockChain();
        if (!built && rc.canBuildRobot(RobotType.MINER,Direction.NORTH)) {
            built = true;
            rc.buildRobot(RobotType.MINER,Direction.NORTH);
        }

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
            System.out.println("Built fortify HQ Landscaper");
            int[] msg = {utils.BLOCKCHAIN_TAG,rc.getRoundNum(),utils.MINER_TAG,0,0,0,0};
            if (rc.canSubmitTransaction(msg,10)) {
                rc.submitTransaction(msg,10);
                System.out.println("SUBMITTED JOB TRANSACTION");
            }
            buildQueue.remove();
        }
    }

    private void buildScout() throws GameActionException {
        if (rc.getTeamSoup() > 100 && utils.tryBuild(rc,RobotType.MINER)) {
            System.out.println("Built fortify HQ Landscaper");
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
            System.out.println("Built fortify HQ Landscaper");
            int[] msg = {utils.BLOCKCHAIN_TAG,rc.getRoundNum(),utils.BUILDER_TAG,0,0,0,0};
            if (rc.canSubmitTransaction(msg,10)) {
                rc.submitTransaction(msg,10);
                System.out.println("SUBMITTED JOB TRANSACTION");
            }
            buildQueue.remove();
        }
    }


}
