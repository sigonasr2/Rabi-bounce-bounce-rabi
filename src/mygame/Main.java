package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.terrain.geomipmap.TerrainQuad;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mygame.appstate.RunLevel;
import static mygame.appstate.RunLevel.networkedPlayersNode;
import static mygame.appstate.RunLevel.queuedPlayerActionMessages;
import mygame.control.NetworkPlayableCharacter;
import mygame.server.ServerMain;
import mygame.server.ServerMain.EntityMessage;
import mygame.server.ServerMain.JoinMessage;
import mygame.server.ServerMain.PlayerActionMessage;
import mygame.server.ServerMain.PlayerLeaveMessage;
import mygame.server.ServerMain.PlayerPositionMessage;
import mygame.server.ServerMain.ServerMessage;
import mygame.server.ServerMain.SyncLevelMessage;

public class Main extends SimpleApplication implements ClientStateListener{
    
    public static Main main;
    public static Client client;
    public static RunLevel level;
    
    Main(){
        try {
            client = Network.connectToServer("Rabi-Bounce-Bounce-Rabi", ServerMain.VERSION, "localhost", 19919, 19919);
            //client = Network.connectToServer("localhost",19919);
            client.addMessageListener(new ClientListener(), ServerMessage.class);
            client.addMessageListener(new ClientListener(), PlayerPositionMessage.class);
            client.addMessageListener(new ClientListener(), SyncLevelMessage.class);
            client.addMessageListener(new ClientListener(), EntityMessage.class);
            client.addMessageListener(new ClientListener(), JoinMessage.class);
            //client.addMessageListener(new ClientListener(), PlayerJoinMessage.class);
            client.addMessageListener(new ClientListener(), PlayerActionMessage.class);
            client.addMessageListener(new ClientListener(), PlayerLeaveMessage.class);
            client.addClientStateListener(this);
            
            client.start();   
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1600,900);
        settings.setVSync(true);
        settings.setFrameRate(120);
        Main app = new Main();
        app.setSettings(settings);
        main = app;
        app.setPauseOnLostFocus(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        //flyCam.setMoveSpeed(50);
        level = new RunLevel("TestLevel2");
        stateManager.attach(level);
        //stateManager.detach(level);
    }

    @Override
    public void simpleUpdate(float tpf) {
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    @Override
    public void clientConnected(Client c) {
        System.out.println("Client successfully connected!");
    }

    @Override
    public void clientDisconnected(Client c, DisconnectInfo info) {
        System.out.println("Client disconnected. Reason:"+((info!=null)?info.reason+". Error: "+info.error.getMessage():"Leave"));
    }
    
    public class ClientListener implements MessageListener<Client> {
        public void messageReceived(Client source, Message message) {
          if (message instanceof ServerMessage) {
            System.out.println("Client #"+source.getId()+" received: "+message);
          } else
          if (message instanceof PlayerPositionMessage) {
            System.out.println("Client #"+source.getId()+" position updated: "+message);
          } else
          if (message instanceof SyncLevelMessage) {
            level.getSyncLevelMessage((SyncLevelMessage)message);
          } else
          /*if (message instanceof PlayerJoinMessage) {
            level.getPlayerJoinMessage((PlayerJoinMessage)message);
          } else*/
          if (message instanceof PlayerActionMessage) {
            level.getPlayerActionMessage((PlayerActionMessage)message);
          } else
          if (message instanceof JoinMessage) {
            level.getPlayerJoinMessage((JoinMessage)message);
          } else
          if (message instanceof PlayerLeaveMessage) {
            level.getPlayerLeaveMessage((PlayerLeaveMessage)message);
          }
        }
      }
    
    public static TerrainQuad SearchForTerrain(Node node) {
        for (Spatial s : node.getChildren()) {
            if (s instanceof TerrainQuad) {
                return (TerrainQuad)s;
            } else {
                Node n = (Node)s;
                return SearchForTerrain(n);
            }
        }
        return null;
    }
    
    @Override
    public void destroy() {
        client.close();
        super.destroy();   
    }
}
