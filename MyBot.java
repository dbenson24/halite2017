import hlt.*;
import utils.GameState;

import java.util.*;

public class MyBot {

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("Tamagocchi");

        final ArrayList<Move> moveList = new ArrayList<>();

        final GameState state = new GameState();

        HashMap<Integer, Position> targetMap = new HashMap<Integer, Position>();

        boolean firstTurn = true;

        for (;;) {
            moveList.clear();
            gameMap.updateMap(Networking.readLineIntoMetadata());
            state.updateState(gameMap);

            if (firstTurn) {

                Ship ship1 = null;
                Ship ship2 = null;
                Ship ship3 = null;
                for (Ship ship: gameMap.getMyPlayer().getShips().values()) {
                    if (ship1 == null) {
                        ship1 = ship;
                    } else if (ship2 == null) {
                        ship2 = ship;
                    } else if (ship3 == null) {
                        ship3 = ship;
                    }
                }

                final Ship cmpShip = ship1;
                final Ship cmpShip2 = ship2;
                final Ship cmpship3 = ship3;

                ArrayList<Planet> closestPlanets = state.getDockablePlanets();
                closestPlanets.sort((o1, o2) -> (int) (cmpShip.getDistanceTo(o1) - cmpShip.getDistanceTo(o2)));
                Planet target1 = closestPlanets.get(0);
                closestPlanets.sort((o1, o2) -> (int) (cmpShip2.getDistanceTo(o1) - cmpShip2.getDistanceTo(o2)));
                Planet target2 = closestPlanets.get(0);
                if (target1.getId() == target2.getId()) {
                    target2 = closestPlanets.get(1);
                }
                closestPlanets.sort((o1, o2) -> (int) (cmpship3.getDistanceTo(o1) - cmpship3.getDistanceTo(o2)));
                Planet target3 = closestPlanets.get(0);
                int i = 1;
                while (target3.getId() == target1.getId() || target3.getId() == target2.getId()) {
                    target3 = closestPlanets.get(i);
                    i++;
                }

                targetMap.put(ship1.getId(), ship1.getClosestPoint(target1));
                targetMap.put(ship2.getId(), ship2.getClosestPoint(target2));
                targetMap.put(ship3.getId(), ship3.getClosestPoint(target3));


                DebugLog.addLog(ship1.getId() + "\tHas an order");
                DebugLog.addLog(ship2.getId() + "\tHas an order");
                DebugLog.addLog(ship3.getId() + "\tHas an order");

                firstTurn = false;
            }



            for (final Ship ship : gameMap.getMyPlayer().getShips().values()) {
                if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                    continue;
                }

                if (targetMap.containsKey(ship.getId())) {
                    DebugLog.addLog(ship.getId() + "\tHad an order");
                    Position target = targetMap.get(ship.getId());
                    if (ship.getDistanceTo(target) < 1.0) {
                        targetMap.remove(ship.getId());
                    } else {
                        final ThrustMove newThrustMove = Navigation.navigateShipTowardsTarget(gameMap, ship, target, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
                        if (newThrustMove != null) {
                            moveList.add(newThrustMove);
                        } else {
                            targetMap.remove(ship.getId());
                        }
                        DebugLog.addLog(ship.getId() + "\tMoving to Target");
                        continue;
                    }
                }

                Ship closestHostile = null;
                boolean fight = false;
                for (final Ship hostile: state.getHostileShips()) {
                    double dist = ship.getDistanceTo(hostile);
                    if (dist < 15.0) {
                        fight = true;
                        if (dist < 5.0) {
                            break;
                        }
                        final ThrustMove newThrustMove = Navigation.navigateShipTowardsTarget(gameMap, ship, hostile, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
                        if (newThrustMove != null) {
                            moveList.add(newThrustMove);
                        }
                        DebugLog.addLog(ship.getId() + "\tProximity Attack on " + hostile.getId());
                        break;
                    }
                    if (closestHostile == null || dist < ship.getDistanceTo(closestHostile)) {
                        closestHostile = hostile;
                    }
                }

                if (fight)
                    continue;

                Planet closestPlanet = null;
                for (final Planet planet : state.getDockablePlanets()) {
                    if (closestPlanet == null || ship.getDistanceTo(planet) < ship.getDistanceTo(closestPlanet)){
                        closestPlanet = planet;
                    }
                }
                if (closestPlanet != null) {
                    if (ship.canDock(closestPlanet)) {
                        moveList.add(new DockMove(ship, closestPlanet));
                        DebugLog.addLog(ship.getId() + "\tStarting Docking");
                    } else {
                        final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, closestPlanet, Constants.MAX_SPEED);
                        if (newThrustMove != null) {
                            moveList.add(newThrustMove);
                            DebugLog.addLog(ship.getId() + "\tMoving to Dock");
                        }
                    }
                    continue;
                }


                for (final Planet planet : state.getHostilePlanets()) {
                    if (closestPlanet == null || ship.getDistanceTo(planet) < ship.getDistanceTo(closestPlanet)){
                        closestPlanet = planet;
                    }
                }

                if (closestPlanet != null && (3 * ship.getDistanceTo(closestPlanet) < ship.getDistanceTo(closestHostile))) {
                    final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, closestPlanet, Constants.MAX_SPEED);
                    if (newThrustMove != null) {
                        moveList.add(newThrustMove);
                        DebugLog.addLog(ship.getId() + "\tMoving to Hostile Planet");
                    }
                } else {
                    final ThrustMove newThrustMove = Navigation.navigateShipTowardsTarget(gameMap, ship, closestHostile, Constants.MAX_SPEED, true, Constants.MAX_NAVIGATION_CORRECTIONS, Math.PI/180.0);
                    if (newThrustMove != null) {
                        moveList.add(newThrustMove);
                        DebugLog.addLog(ship.getId() + "\tAttacking Closest Ship");
                    }
                }

            }
            Networking.sendMoves(moveList);
        }
    }
}
