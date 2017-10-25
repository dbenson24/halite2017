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

                ArrayList<Ship> ships = state.getMyShips();
                ships.sort((o1, o2) -> (int) Math.round(o1.getYPos() - o2.getYPos()));

                final Ship ship1 = ships.get(0);
                final Ship ship2 = ships.get(1);
                final Ship ship3 = ships.get(2);


                ArrayList<Planet> planets = state.getDockablePlanets();
                planets.sort((o1, o2) -> (int) (ship1.getDistanceTo(o1) - ship1.getDistanceTo(o2)));
                List<Planet> closestPlanets = planets.subList(0, 3);
                closestPlanets.sort((o1, o2) -> (int) Math.round(o1.getYPos() - o2.getYPos()));

                targetMap.put(ship1.getId(), ship1.getClosestPoint(closestPlanets.get(0)));
                targetMap.put(ship2.getId(), ship2.getClosestPoint(closestPlanets.get(1)));
                targetMap.put(ship3.getId(), ship3.getClosestPoint(closestPlanets.get(2)));


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
                    if (dist < 22.0) {
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

                Planet closestDockablePlanet = null;
                for (final Planet planet : state.getDockablePlanets()) {
                    if (closestDockablePlanet == null || ship.getDistanceTo(planet) < ship.getDistanceTo(closestDockablePlanet)){
                        closestDockablePlanet = planet;
                    }
                }

                Planet closestHostilePlanet = null;
                for (final Planet planet : state.getHostilePlanets()) {
                    if (closestHostilePlanet == null || ship.getDistanceTo(planet) < ship.getDistanceTo(closestHostilePlanet)){
                        closestHostilePlanet = planet;
                    }
                }


                if (closestDockablePlanet != null && (closestHostilePlanet == null || closestDockablePlanet.getDistanceTo(ship) < closestHostilePlanet.getDistanceTo(ship))) {
                    if (ship.canDock(closestDockablePlanet)) {
                        moveList.add(new DockMove(ship, closestDockablePlanet));
                        DebugLog.addLog(ship.getId() + "\tStarting Docking");
                    } else {
                        final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, closestDockablePlanet, Constants.MAX_SPEED);
                        if (newThrustMove != null) {
                            moveList.add(newThrustMove);
                            DebugLog.addLog(ship.getId() + "\tMoving to Dock");
                        }
                    }
                    continue;
                }


                if (closestHostilePlanet != null && (3 * ship.getDistanceTo(closestHostilePlanet) < ship.getDistanceTo(closestHostile))) {
                    final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, closestHostilePlanet, Constants.MAX_SPEED);
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
