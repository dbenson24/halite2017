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
    protected Collection<Ship> myShips;
    protected Collection<Ship> allShips;

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


    public GameState() {
        friendly = 0;
        hostile = 0;
        myPlanets = new ArrayList<Planet>();
        freePlanets = new ArrayList<Planet>();
    }

    public void updateState(GameMap map) {
        Player me = map.getMyPlayer();
        Collection<Ship> allShips = map.getAllShips();
        Collection<Ship> myShips = me.getShips().values();
        friendly = myShips.size();
        hostile = allShips.size() - friendly;

        myPlanets = new ArrayList<>();
        freePlanets = new ArrayList<>();

        for (final Planet planet: map.getAllPlanets().values()) {
            if (planet.isOwned()) {
                if (planet.getOwner() == me.getId()) {
                    myPlanets.add(planet);
                }
            } else {
                freePlanets.add(planet);
            }
        }
    }



}
