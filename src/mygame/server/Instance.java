/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.server;

import com.jme3.math.Vector3f;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import mygame.server.ServerMain.EntityMessage;
import static mygame.server.ServerMain.players;
import static mygame.server.ServerMain.server;

/**
 *
 * @author sigon
 */
public class Instance {
    String levelName; //The scene name.
    protected List<HostedConnection> clients = new ArrayList<>(); //The clients connected to this instance.
    protected HashMap<Integer,Vector3f> lastKnownPositions = new HashMap<>();
    protected List<Entity> entities = new ArrayList<>(); //Entity data specific to this instance.
    List<Integer> nullEntities = new ArrayList<>(); //A list of "null" entities. When adding new entities, these slots will be consumed first.
    public int INSTANCE_ID = -1;
    Instance(String levelName) {
        this.levelName = levelName;
    }
    
    public void addPlayer(HostedConnection connection) {
        //System.out.println("Inside here.");
        //server.broadcast(Filters.in(clients),new PlayerJoinMessage(new Entity(connection.getId(),"NETWORKPLAYER")));
        
        //Update the player's location with this location. They cannot be in two places at once.
        if (players.containsKey(connection.getId())) {
            Instance i = players.get(connection.getId());
            i.removePlayer(connection);
        }
        players.put(connection.getId(), this);
        clients.add(connection);
    }
    public void removePlayer(HostedConnection connection) {
        //System.out.println("Inside here.");
        //server.broadcast(Filters.in(clients),new PlayerJoinMessage(new Entity(connection.getId(),"NETWORKPLAYER")));
        clients.remove(connection);
    }
    public Integer[] getPlayers() {
        Integer[] players = new Integer[clients.size()];
        for (int i=0;i<clients.size();i++) {
            players[i] = clients.get(i).getId();
        }
        return players;
    }
    public Vector3f[] getPlayerPositions() {
        Vector3f[] positions = new Vector3f[clients.size()];
        for (int i=0;i<clients.size();i++) {
            if (lastKnownPositions.containsKey(clients.get(i).getId())) {
                positions[i] = lastKnownPositions.get(clients.get(i).getId());
            } else {
                positions[i] = Vector3f.ZERO;
            }
        }
        return positions;
    }
    /**
     * Adds an entity to the server.
     * @param entity The new entity.
     */
    public void addEntity(String type) {
        int newID = -1;
        if (nullEntities.size()>0) {
            newID = nullEntities.remove(0);
            entities.set(newID, new Entity(newID,type));
        } else {
            newID = entities.size();
            entities.add(new Entity(newID,type));
        }
        updateEntityToClients(newID);
    }
    public void updateEntityToClients(Integer id) {
        Entity ent = entities.get(id);
        server.broadcast(Filters.in(clients),new EntityMessage(ent));
    }
    public Entity[] getEntities() {
        return entities.toArray(new Entity[entities.size()]);
    }
    
    @Override
    public String toString() {
        return "Entities: "+entities+" / Players: "+clients+" / Null Entities: "+nullEntities;
    }

    void updatePosition(int id, Vector3f position) {
        lastKnownPositions.put(id,position);
    }
}
