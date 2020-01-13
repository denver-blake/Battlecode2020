package sprint;

import battlecode.common.*;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class FulfillmentCenter implements Robot {

    enum UnitType {
        SCOUT,
        TRANSPORT_MINERS,
        DEFENSIVE

    }
    private class Unit {
        UnitType unitType;
        public Unit(UnitType unitType) {
            this.unitType = unitType;
        }
    }

    RobotController rc;
    Queue<Unit> buildQueue;
    MapLocation hqLocation;
    int currentBlockChainRound;
    boolean built = false;

    public FulfillmentCenter(RobotController rc) throws GameActionException {
        this.rc = rc;
        buildQueue = new LinkedList<Unit>();
        hqLocation = utils.hqPosition(rc);
        currentBlockChainRound = 1;


    }
    public void run() throws GameActionException {
        if (rc.canBuildRobot(RobotType.DELIVERY_DRONE,Direction.NORTH) && !built)  {
            rc.buildRobot(RobotType.DELIVERY_DRONE,Direction.NORTH);
            built = true;
        }
//        if(!buildQueue.isEmpty()) {
//            switch (buildQueue.peek().unitType) {
//                case SCOUT:
//                    buildScout();
//                    break;
//                case DEFENSIVE:
//                    break;
//                case TRANSPORT_MINERS:
//                    buildTransportMiners();
//                    break;
//                default:
//                    break;
//            }
//        }
//        readBlockChain();

    }
    private void readBlockChain() throws GameActionException {
        for (Transaction transaction : rc.getBlock(currentBlockChainRound)) {
            int[] msg = transaction.getMessage();
            if (msg.length != 7) continue;
            if (msg[0] == utils.BLOCKCHAIN_TAG) {
                //Transaction tags to look for
                if (msg[1] == utils.UNREACHABLE_DEPOSIT_TAG) {

                }
            }
        }
        currentBlockChainRound++;
        if (Clock.getBytecodesLeft() > 500 && currentBlockChainRound < rc.getRoundNum()) {
            readBlockChain();
        }

    }


    private void buildScout() throws GameActionException {
        if (rc.getTeamSoup() > 200 && utils.tryBuild(rc,RobotType.DELIVERY_DRONE)) {
            System.out.println("Built fortify HQ Landscaper");
            int[] msg = {utils.BLOCKCHAIN_TAG,rc.getRoundNum(),utils.SCOUT_DRONE_TAG,0,0,0,0};
            if (rc.canSubmitTransaction(msg,10)) {
                rc.submitTransaction(msg,10);
                System.out.println("SUBMITTED JOB TRANSACTION");
            }
            buildQueue.remove();
        }
    }

    private void buildTransportMiners() throws GameActionException {
        if (rc.getTeamSoup() > 200 && utils.tryBuild(rc,RobotType.DELIVERY_DRONE)) {
            System.out.println("Built fortify HQ Landscaper");
            int[] msg = {utils.BLOCKCHAIN_TAG,rc.getRoundNum(),utils.TRANSPORT_MINERS_TAG,0,0,0,0};
            if (rc.canSubmitTransaction(msg,10)) {
                rc.submitTransaction(msg,10);
                System.out.println("SUBMITTED JOB TRANSACTION");
            }
            buildQueue.remove();
        }
    }
}
