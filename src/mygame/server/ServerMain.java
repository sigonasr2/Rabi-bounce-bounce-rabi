/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.server;

import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.*;
import com.jme3.network.HostedConnection;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.network.serializing.Serializable;
import com.jme3.network.serializing.Serializer;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sigon
 */
public class ServerMain extends SimpleApplication{
    
    public final static int VERSION = 00001;
    public static Server server;
    int clientIDMax = 0;
    public static HashMap<String,Instance> instances = new HashMap<>();
    public static HashMap<Integer,Instance> players = new HashMap<>(); //The last instance location this player was seen in.
    
    ServerMain() {
        try {
            server = Network.createServer("Rabi-Bounce-Bounce-Rabi", VERSION, 19919, 19919);
            //server = Network.createServer(19919);
            server.addConnectionListener(new ConnectionListener() {
                @Override
                public void connectionAdded(Server server, HostedConnection conn) {
                    System.out.println("Client connected: "+conn);
                }

                @Override
                public void connectionRemoved(Server server, HostedConnection conn) {
                    System.out.println("Client disconnected: "+conn);
                    if (players.containsKey(conn.getId())) {
                        Instance i = players.get(conn.getId());
                        i.removePlayer(conn);
                    }
                }
            });
            
            Serializer.registerClass(ServerMessage.class);
            Serializer.registerClass(PlayerPositionMessage.class);
            Serializer.registerClass(EntityMessage.class);
            Serializer.registerClass(JoinMessage.class);
            Serializer.registerClass(SyncLevelMessage.class);
            Serializer.registerClass(Entity.class);
            //Serializer.registerClass(PlayerJoinMessage.class);
            Serializer.registerClass(PlayerActionMessage.class);
            
            server.addMessageListener(new ServerListener(), ServerMessage.class);
            server.addMessageListener(new ServerListener(), PlayerPositionMessage.class);
            server.addMessageListener(new ServerListener(), EntityMessage.class);
            server.addMessageListener(new ServerListener(), JoinMessage.class);
            server.addMessageListener(new ServerListener(), SyncLevelMessage.class);
            //server.addMessageListener(new ServerListener(), PlayerJoinMessage.class);
            server.addMessageListener(new ServerListener(), PlayerActionMessage.class);
            
            server.start();
        } catch (IOException ex) {
            Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }
    
    public static void main(String[] args) {
        if (args.length>0) {
            if (Boolean.parseBoolean(args[0])) { //To enable fine logging, set first arg to "true"
                System.out.println("Setting logging to max");
                Logger networkLog = Logger.getLogger("com.jme3.network"); 
                networkLog.setLevel(Level.FINEST);
                // And we have to tell JUL's handler also   
                // turn up logging in a very convoluted way
                Logger rootLog = Logger.getLogger("");
                if( rootLog.getHandlers().length > 0 ) {
                    rootLog.getHandlers()[0].setLevel(Level.FINEST);
                }        
            }
        }
        
        AppSettings settings = new AppSettings(true);
        settings.setFrameRate(120);
        ServerMain app = new ServerMain();
        app.start(JmeContext.Type.Headless); 
    }

    @Override
    public void simpleInitApp() {
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        /*if (server!=null) {
            System.out.println("Clients connected: "+server.getConnections().size());
            for (HostedConnection c : server.getConnections()) {
                if (c.getAttribute("id")==null) {
                    c.setAttribute("id", clientIDMax++);
                    System.out.println("New client detected! Assigned ID "+c.getAttribute("id"));
                }
            }
        }*/
    }
    
    public class ServerListener implements MessageListener<HostedConnection> {
        public void messageReceived(HostedConnection source, Message message) {
          if (message instanceof ServerMessage) {
            System.out.println("Server received '"+message+"' from client #"+source.getId());
          } else
          if (message instanceof PlayerPositionMessage) {
            System.out.println("Position update for client "+source.getId()+". Broadcasting to others.");
            server.broadcast(Filters.notEqualTo(source), message);
            server.broadcast(Filters.in(source), new ServerMessage("Sent an update to other clients!"));
          } else 
          if (message instanceof JoinMessage) {
              JoinMessage msg = (JoinMessage)message;
              System.out.println("New join request: "+msg);
              Instance instance;
             //System.out.println("Got here.");
            if (instances.containsKey(msg.levelName)) {
                //We will load the entities and players already in this room.
                instance = instances.get(msg.levelName);
                System.out.println("Instance "+msg.levelName+" already exists. Loading "+instance);
            } else {
                instance = new Instance(msg.levelName);
                System.out.println("Instance "+msg.levelName+" does not exist. Creating... "+instance);
                CreateTestObj(instance);
                System.out.println(instance);
                instances.put(msg.levelName, instance);
            }
            //System.out.println("Got here 2.");
            instance.addPlayer(source);
            SyncLevelMessage sync = new SyncLevelMessage(instance.getEntities(),instance.getPlayers(),instance.getPlayerPositions());
            server.broadcast(Filters.in(source),sync);
            server.broadcast(Filters.notEqualTo(source),message);
          } else
          if (message instanceof PlayerActionMessage) {
            System.out.println("Received player action message: "+message);
            server.broadcast(Filters.notEqualTo(source), message);
            PlayerActionMessage msg = (PlayerActionMessage)message;
            if (players.containsKey(source.getId())) {
                Instance i = players.get(source.getId());
                i.updatePosition(source.getId(),msg.getPosition());
            }
          } /*else
          if (message instanceof PlayerJoinMessage) {
            System.out.println("Player has joined "+message);
            server.broadcast(Filters.notEqualTo(source), message);
          }*/
        }
      }
    
    public void CreateTestObj(Instance instance) {
        for (int i=0;i<30;i++) {
            Entity sphere = new Entity(i,"PhysicsSphere");
            sphere.modelData = (ColorRGBA.randomColor().asIntRGBA()+","+Integer.toString((int)(Math.random()*10)+3)+","+Integer.toString((int)(Math.random()*10)+3)+","+(3f));
            sphere.position = new Vector3f(0.01f+75f*(float)Math.random()-75f*(float)Math.random(),25f+20f*(float)Math.random(),0.01f+75f*(float)Math.random()-75f*(float)Math.random());
            instance.entities.add(sphere);
        }
    }
    
    @Serializable
    public static class ServerMessage extends AbstractMessage {
        String message;

        public ServerMessage() {
        }

        public ServerMessage(String message) {
            this.message=message;
        }

        @Override
        public String toString() {
            return message;
        }
    }
    
    @Serializable
    public static class PlayerPositionMessage extends AbstractMessage {
        Vector3f pos;

        public PlayerPositionMessage() {
        }

        public PlayerPositionMessage(Vector3f pos) {
            this.pos=pos;
        }

        @Override
        public String toString() {
            return pos.toString();
        }
    }
    
    @Serializable
    public static class EntityMessage extends AbstractMessage {
        Integer id;
        String type;

        public EntityMessage() {
        }

        public EntityMessage(Entity ent) {
            this.id = ent.id;
            this.type = ent.type;
        }

        @Override
        public String toString() {
            return "Entity ID: "+ id + " / Entity type: "+type;
        }
    }
    @Serializable
    public static class JoinMessage extends AbstractMessage {
        String levelName;
        Entity ent;

        public JoinMessage() {
        }

        public JoinMessage(String levelName, Entity ent) {
            this.levelName = levelName;
            this.ent = ent;
        }

        @Override
        public String toString() {
            return "Client ID "+ent.id+" joined. Entity data: "+ent+". In Instance "+levelName.toString();
        }
        
        public Entity getEntity() {
            return ent;
        }
    }
    @Serializable
    public static class SyncLevelMessage extends AbstractMessage {
        Entity[] entities;
        Integer[] clients;
        Vector3f[] positions;

        public SyncLevelMessage() {
        }

        public SyncLevelMessage(Entity[] entities, Integer[] clients, Vector3f[] positions) {
            this.entities = entities;
            this.clients = clients;
            this.positions = positions;
        }
        
        public Entity[] getEntities() {
            return entities;
        }
        
        public Integer[] getPlayers() {
            return clients;
        }
        
        public Vector3f[] getPositions() {
            return positions;
        }

        @Override
        public String toString() {
            return "Level entities: "+Arrays.asList(entities).toString()+"/ Clients Connected: "+Arrays.asList(clients).toString();
        }
    }
    
    @Serializable
    public static class PlayerActionMessage extends AbstractMessage {
        String action;
        String value;
        int id;
        Vector3f pos,camera,cameraLeft;
        Quaternion rotation;
        
        public PlayerActionMessage(){}

        public PlayerActionMessage(String action, String value, int id, Vector3f pos, Quaternion rotation, Vector3f camera, Vector3f cameraLeft) {
            this.action=action;
            this.value=value;
            this.id=id;
            this.pos=pos;
            this.rotation=rotation;
            this.camera=camera;
            this.cameraLeft=cameraLeft;
        }

        @Override
        public String toString() {
            return "Player Action: "+action+"-"+value+" at pos: "+pos+" from Client "+id;
        }
        
        public String getAction() {
            return action;
        }
        
        public String getData() {
            return value;
        }
        
        public int getClientID() {
            return id;
        }
        
        public Vector3f getPosition() {
            return pos;
        }
        
        public Quaternion getRotation() {
            return rotation;
        }
        
        public Vector3f getCamera() {
            return camera;
        }
        
        public Vector3f getCameraLeft() {
            return cameraLeft;
        }
    }
    
    @Override
    public void destroy() {
        server.close();
        super.destroy();   
    }
}
