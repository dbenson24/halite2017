package utils;

import hlt.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GameState {
    protected int friendly;
    protected int hostile;
    protected ArrayList<Planet> myPlanets;
    protected ArrayList<Planet> freePlanets;
    protected ArrayList<Planet> dockablePlanets;
    protected ArrayList<Planet> hostilePlanets;
    protected ArrayList<Ship> myShips;
    protected ArrayList<Ship> hostileShips;

    public GameState() {
        friendly = 0;
        hostile = 0;
        myPlanets = new ArrayList<Planet>();
        freePlanets = new ArrayList<Planet>();
        hostilePlanets = new ArrayList<Planet>();
        dockablePlanets = new ArrayList<Planet>();
    }

    public ArrayList<Planet> getDockablePlanets() {
        return dockablePlanets;
    }

    public ArrayList<Planet> getHostilePlanets() {
        return hostilePlanets;
    }

    public ArrayList<Ship> getMyShips() {
        return myShips;
    }

    public ArrayList<Ship> getHostileShips() {
        return hostileShips;
    }

    public ArrayList<Planet> getMyPlanets() {
        return myPlanets;
    }

    public ArrayList<Planet> getFreePlanets() {
        return freePlanets;
    }

    public int getFriendly() {
        return friendly;
    }

    public int getHostile() {
        return hostile;
    }

    public void updateState(GameMap map) {
        Player me = map.getMyPlayer();
        Collection<Ship> allShips = map.getAllShips();
        friendly = me.getShips().size();
        hostile = allShips.size() - friendly;

        myPlanets.clear();
        freePlanets.clear();
        hostilePlanets.clear();
        dockablePlanets.clear();

        for (final Planet planet: map.getAllPlanets().values()) {
            if (planet.isOwned()) {
                if (planet.getOwner() == me.getId()) {
                    myPlanets.add(planet);
                    if (planet.getDockingSpots() > planet.getDockedShips().size()) {
                        dockablePlanets.add(planet);
                    }
                } else {
                    hostilePlanets.add(planet);
                }
            } else {
                freePlanets.add(planet);
                dockablePlanets.add(planet);
            }
        }

        myShips = new ArrayList<Ship>();
        hostileShips = new ArrayList<Ship>();

        for (final Ship ship: map.getAllShips()) {
            if (ship.getOwner() == me.getId()) {
                myShips.add(ship);
            } else {
                hostileShips.add(ship);
            }
        }
    }



}
