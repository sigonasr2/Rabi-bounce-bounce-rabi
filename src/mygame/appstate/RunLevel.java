package mygame.appstate;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.util.SkyFactory;
import com.jme3.water.WaterFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static mygame.Main.main;
import mygame.camera.CustomChaseCamera;
import mygame.control.NetworkPlayableCharacter;
import mygame.control.PhysicsControl;
import mygame.control.PlayableCharacter;
import mygame.server.ServerMain.JoinMessage;
import mygame.server.ServerMain.SyncLevelMessage;
import mygame.server.Entity;
import mygame.server.ServerMain;
import mygame.server.ServerMain.PlayerActionMessage;
import mygame.server.ServerMain.PlayerLeaveMessage;


public class RunLevel extends BaseAppState 
  implements AnimEventListener {
    private SimpleApplication app;
    private Node              rootNode;
    private AssetManager      assetManager;
    private AppStateManager   stateManager;
    private InputManager      inputManager;
    private ViewPort          viewPort;
    private BulletAppState    physics;
    private String level;
    private List<Integer> players = new ArrayList<>();
    private Vector3f[] player_locations;
    public static List<PlayerActionMessage> queuedPlayerActionMessages = new ArrayList<>();
    public static List<JoinMessage> queuedPlayerJoinMessages = new ArrayList<>();
    public static List<PlayerLeaveMessage> queuedPlayerLeaveMessages = new ArrayList<>();
    public static List<SyncLevelMessage> queuedSyncLevelMessages = new ArrayList<>();
    public static Node world;
    Node entityNode;
    public static Node networkedPlayersNode;
    float timer;
    
    public RunLevel(String levelName) {
        //System.out.println("In here. Initialize");
        this.level = levelName;
    }
    
    @Override
    protected void initialize(Application app) {
        //It is technically safe to do all initialization and cleanup in the
        //onEnable()/onDisable() methods. Choosing to use initialize() and
        //cleanup() for this is a matter of performance specifics for the
        //implementor.
        //TODO: initialize your AppState, e.g. attach spatials to rootNode
        //super.initialize(stateManager, app);
        //System.out.println("In here.");
        this.app = (SimpleApplication) app; // can cast Application to something more specific
        this.rootNode     = this.app.getRootNode();
        this.assetManager = this.app.getAssetManager();
        this.stateManager = this.app.getStateManager();
        this.inputManager = this.app.getInputManager();
        this.viewPort     = this.app.getViewPort();
        
        //rootNode.setShadowMode(ShadowMode.CastAndReceive);
        
        //BulletAppState bulletAppState = new BulletAppState();
        //this.stateManager.attach(bulletAppState);
        
        DirectionalLight sceneLight = new DirectionalLight(new Vector3f(-0.57735026f, -0.57735026f, -0.57735026f));
        
        Node reflectedScene = new Node("Reflected Scene");
        rootNode.attachChild(reflectedScene);
        //rootNode.addLight(sceneLight);
        Spatial TestLevel = assetManager.loadModel("Scenes/"+level+".j3o");
        world = (Node)TestLevel;
        //TestLevel.addControl(new RigidBodyControl(0));
        //bulletAppState.getPhysicsSpace().addAll(TestLevel);
        //System.out.println(world.getLocalLightList().size());
        //DirectionalLight sceneLight = (DirectionalLight)world.getLocalLightList().get(0);
        
        Node player =  (Node)assetManager.loadModel("Models/Oto/Oto.mesh.xml"); 
        Node playerNode = new Node("Player");
        playerNode.attachChild(player);
        playerNode.addControl(new PlayableCharacter());
        
        entityNode = new Node();
        networkedPlayersNode = new Node();
        
        //Node networkPlayer =  (Node)assetManager.loadModel("Models/Oto/Oto.mesh.xml"); 
        /*Node networkPlayerNode = new Node();
        networkPlayer.move(0,2.5f,0);
        networkPlayerNode.attachChild(networkPlayer);
        networkPlayerNode.setLocalTranslation(7f,15f,-14f);
        networkPlayerNode.addControl(new NetworkPlayableCharacter());
        world.attachChild(networkPlayerNode);*/
        
        /*for (int i=0;i<100;i++) {
            Node sphereNode = new Node();
            Geometry sphere = new Geometry("PhysicsSphere",new Sphere((int)(Math.random()*10)+3,(int)(Math.random()*10)+3,3f));
            Material mat = new Material(assetManager, "Materials/newMatDef.j3md");
            mat.setColor("Color", ColorRGBA.randomColor());
            sphere.setMaterial(mat);
            sphere.setLocalTranslation(0.01f,1.5f,0.01f);
            sphereNode.attachChild(sphere);
            sphereNode.addControl(new PhysicsControl(world,0.0f,-0.2f,3f));
            sphereNode.setLocalTranslation(0.01f+75f*(float)Math.random()-75f*(float)Math.random(),25f+300f*(float)Math.random(),0.01f+75f*(float)Math.random()-75f*(float)Math.random());
            reflectedScene.attachChild(sphereNode);
        }*/
        
        Entity ent = new Entity(main.client.getId(),"NETWORKPLAYER");
        ent.position = playerNode.getLocalTranslation();
        JoinMessage msg = new JoinMessage(level,ent);
        main.client.send(msg);
        
        //System.out.println("In here.,");
        
        CustomChaseCamera chaseCam = new CustomChaseCamera(this.app.getCamera(), player, inputManager);
        chaseCam.setLookAtOffset(new Vector3f(0,2.5f,0));
        chaseCam.setDefaultHorizontalRotation((float)Math.PI);
        chaseCam.setSmoothMotion(false);
        chaseCam.setTrailingEnabled(false);
        //this.app.getCamera().setFrustumPerspective(this.app.getCamera().getFr, aspect, near, far);
        //this.app.getCamera().setFrustumPerspective(90, 16f/9, 0, 2000f);
        //float fov = 50;
        //float aspect = (float)this.app.getCamera().getWidth() / (float)this.app.getCamera().getHeight();
        //this.app.getCamera().setFrustumPerspective(fov, aspect, this.app.getCamera().getFrustumNear(), this.app.getCamera().getFrustumFar());
        //channel.setLoopMode(LoopMode.Cycle);
        //world.attachChild(playerNode);
        
        player.move(0,2.5f,0);
        player.setLocalScale(0.5f);
        
        //BetterCharacterControl playerControl = new BetterCharacterControl(1.5f,4f,10f);
        playerNode.addControl(chaseCam);
        playerNode.move(0.01f,3f,0.01f);
        
        //System.out.println(world.getChildren());
        //.addLight(sceneLight);
        //world.addLight(sceneLight);
        reflectedScene.addLight(sceneLight);
        world.attachChild(entityNode); //ONLY PUT COLLIDEABLES IN WORLD NODE!!
        //world.attachChild(networkedPlayersNode);
        reflectedScene.attachChild(networkedPlayersNode);
        reflectedScene.attachChild(world);
        reflectedScene.attachChild(TestLevel);
        reflectedScene.attachChild(playerNode);
        reflectedScene.attachChild(SkyFactory.createSky(assetManager,"Textures/Sky/Bright/BrightSky.dds",false));

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        //viewPort.addProcessor(fpp);
        Vector3f lightDir = new Vector3f(-2.9f,-1.2f,-5.8f);
        WaterFilter water = new WaterFilter(reflectedScene, lightDir);
        water.setWaterHeight(-1.5f);
        fpp.addFilter(water);
        final int SHADOWMAP_SIZE=4096;
        
        /*DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, SHADOWMAP_SIZE, 4);
        dlsr.setLight(sceneLight);
        viewPort.addProcessor(dlsr);*/

        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, SHADOWMAP_SIZE, 4);
        dlsf.setLight(sceneLight);
        dlsf.setEnabled(true);
        fpp.addFilter(dlsf);
        viewPort.addProcessor(fpp);
        
        /*
        SSAOFilter ssaoFilter = new SSAOFilter(12.94f, 43.92f, 0.33f, 0.61f);
        fpp.addFilter(ssaoFilter);
        viewPort.addProcessor(fpp);*/
    }
    
    public void getSyncLevelMessage(SyncLevelMessage msg) {
        System.out.println("Received sync level message: "+msg);
        queuedSyncLevelMessages.add(msg);
    }
    
    public void createPlayers() {
        for (int i=0;i<players.size();i++) {
            if (players.get(i)!=main.client.getId()) {
                if (networkedPlayersNode.getChild(Integer.toString(players.get(i)))==null) {
                    MakeNetworkPlayer(players.get(i),player_locations[i]);
                    System.out.println("Created a new Networked Player w/ID "+i);
                }
            }
        }
    }

    @Override
    protected void cleanup(Application app) {
        //TODO: clean up what you initialized in the initialize method,
        //e.g. remove all spatials from rootNode
        //((Node)rootNode.getChild("Player")).removeControl(PlayableCharacter.class);
        ((Node)rootNode.getChild("Reflected Scene")).detachAllChildren();
    }

    //onEnable()/onDisable() can be used for managing things that should
    //only exist while the state is enabled. Prime examples would be scene
    //graph attachment or input listener attachment.
    @Override
    protected void onEnable() {
        //Called when the state is fully enabled, ie: is attached and
        //isEnabled() is true or when the setEnabled() status changes after the
        //state is attached.
    }

    @Override
    protected void onDisable() {
        //Called when the state was previously enabled but is now disabled
        //either because setEnabled(false) was called or the state is being
        //cleaned up.
    }

    @Override
    public void update(float tpf) {
        for (SyncLevelMessage msg : queuedSyncLevelMessages) {
            CreateObjects(msg.getEntities());
            players.addAll(Arrays.asList(msg.getPlayers()));
            player_locations = msg.getPositions();
            createPlayers();
        }
        queuedSyncLevelMessages.clear();
        for (JoinMessage msg : queuedPlayerJoinMessages) {
            MakeNetworkPlayer(msg.getEntity().id,msg.getEntity().position); 
            players.add(msg.getEntity().id);
            createPlayers();   
            System.out.println(msg);
        }
        queuedPlayerJoinMessages.clear();
        for (PlayerLeaveMessage msg : queuedPlayerLeaveMessages) {
            for (int i=0;i<networkedPlayersNode.getChildren().size();i++) {
                Spatial s = networkedPlayersNode.getChildren().get(i);
                if (s.getName().equalsIgnoreCase(Integer.toString(msg.getId()))) {
                    System.out.println("Removed "+s);
                    networkedPlayersNode.detachChild(s);
                    players.remove((Integer)msg.getId());
                    i--;
                }
            }
        }
        queuedPlayerLeaveMessages.clear();
        /*timer+=tpf;
        if (timer>5) {
            main.getStateManager().detach(this);
            this.setEnabled(false);
        }*/
    }

    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
    }

    @Override
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
    }

    private void CreateObjects(Entity[] entities) {
        for (Entity e : entities) {
            switch (e.type) {
                case "PhysicsSphere":{ //MODELDATA: COL,ZSAMPLEs,RADIALSAMPLES,RADIUS
                    String[] data = e.modelData.split(",");
                    Geometry sphere = new Geometry(e.type+"_"+e.id,new Sphere(Integer.parseInt(data[1]),Integer.parseInt(data[2]),Float.parseFloat(data[1])));
                    Material mat = new Material(assetManager, "Materials/newMatDef.j3md");
                    mat.setColor("Color", new ColorRGBA().fromIntRGBA(Integer.parseInt(data[0])));
                    sphere.setMaterial(mat);
                    sphere.setLocalTranslation(e.position);
                    sphere.addControl(new PhysicsControl(0.0f,-0.2f,3f));
                    entityNode.attachChild(sphere);
                }break;
            }
        }
    }

    public void getPlayerJoinMessage(JoinMessage playerJoinMessage) {
        JoinMessage msg = playerJoinMessage;
        queuedPlayerJoinMessages.add(msg);
    }

    private void MakeNetworkPlayer(int id, Vector3f pos) {
        Node networkPlayer =  (Node)assetManager.loadModel("Models/Oto/Oto.mesh.xml");
        Node networkPlayerNode = new Node();
        networkPlayer.move(0,2.5f,0);
        networkPlayer.setLocalScale(0.5f);
        networkPlayerNode.attachChild(networkPlayer);
        networkPlayerNode.setLocalTranslation(pos);
        networkPlayerNode.addControl(new NetworkPlayableCharacter());
        networkPlayerNode.setUserData("id", id);
        networkPlayerNode.setName(Integer.toString(id));
        //networkPlayerNode.move(0.01f,3f,0.01f);
        //world.attachChild(networkPlayerNode);
        networkedPlayersNode.attachChild(networkPlayerNode);
    }

    public void getPlayerActionMessage(PlayerActionMessage playerActionMessage) {
        Node networkPlayer = (Node)networkedPlayersNode.getChild(Integer.toString(playerActionMessage.getClientID()));
        networkPlayer.setUserData("lastActionMessage", playerActionMessage.getClientID());
        networkPlayer.setUserData("lastAction", playerActionMessage.getAction());
        networkPlayer.setUserData("lastData", playerActionMessage.getData());
        networkPlayer.setUserData("lastPosition", playerActionMessage.getPosition());
        networkPlayer.setUserData("lastRotation", playerActionMessage.getRotation());
        networkPlayer.setUserData("lastCamDir", playerActionMessage.getCamera());
        networkPlayer.setUserData("lastCamLeftDir", playerActionMessage.getCameraLeft());
    }

    public void getPlayerLeaveMessage(PlayerLeaveMessage playerLeaveMessage) {
        PlayerLeaveMessage msg = playerLeaveMessage;
        queuedPlayerLeaveMessages.add(msg);
    }

}