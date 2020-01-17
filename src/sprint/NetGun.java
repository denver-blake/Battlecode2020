package sprint;


import battlecode.common.*;

public class NetGun implements Robot {

    RobotController rc;
    public NetGun(RobotController rc) {
        this.rc = rc;

    }

    /***
     * Shooting priority
     * 1. Cows above water
     * 2. Cows above land by greatest distance from net gun
     * 3. Allied miner above land by greatest amount of soup carrying
     * 4. Allied  landscaper above land by greatest amount of dirt carrying
     * 5. Enemy miner above deepest water
     * 6. Enemy miner above land
     * 7. Enemy landscaper above land
     * 8. Empty drone
     *
     * @throws GameActionException
     */
    public void run() throws GameActionException {
        if (!rc.isReady()) return;
        RobotInfo[] robots = rc.senseNearbyRobots(Math.min(15,rc.getCurrentSensorRadiusSquared()),rc.getTeam().opponent());
        int waterLevel = utils.getWaterLevel(rc.getRoundNum());
        RobotInfo bestTarget = null;
        int bestValue = -1;
        //1. drones carrying miners under water
        for (RobotInfo robot : robots) {
            if (robot.type != RobotType.DELIVERY_DRONE) return;
            int value = -1;
            if (robot.currentlyHoldingUnit) {
                final RobotInfo  heldRobot = rc.senseRobot(robot.heldUnitID);
                if (heldRobot.type == RobotType.COW) {
                    if (rc.senseFlooding(robot.location)) {
                        value = 90000000;
                    } else {
                        value = 80000000 + rc.getLocation().distanceSquaredTo(robot.location);
                    }
                } else if (heldRobot.team == rc.getTeam()) {
                    if (!rc.senseFlooding(robot.location)) {
                        if (heldRobot.type == RobotType.MINER) {
                            value = 900000 + heldRobot.soupCarrying;
                        } else if (heldRobot.type == RobotType.LANDSCAPER) {
                            value = 800000 + heldRobot.dirtCarrying;
                        }
                    }
                } else if (heldRobot.team == robot.team) {
                    if (rc.senseFlooding(robot.location)) {
                        if (heldRobot.type == RobotType.MINER) {
                            value = 90000 + heldRobot.soupCarrying;
                        } else if (heldRobot.type == RobotType.LANDSCAPER) {
                            if (rc.senseElevation(robot.location) + heldRobot.dirtCarrying <= waterLevel) {
                                value = 80000 + heldRobot.dirtCarrying;
                            } else {
                                value = 70000 + heldRobot.dirtCarrying;
                            }
                        }
                    } else {
                        if (heldRobot.type == RobotType.MINER) {
                            value = 60000 + heldRobot.soupCarrying;
                        } else if (heldRobot.type == RobotType.LANDSCAPER) {
                            value = 50000 + heldRobot.dirtCarrying;
                        }
                    }
                }
            } else {
                value = 5000;
            }
            if (value > bestValue) {
                bestTarget = robot;
                bestValue = value;

            }
        }
        if (bestValue > -1 && rc.canShootUnit(bestTarget.ID)) {
            rc.shootUnit(bestTarget.ID);
        }
    }



}

