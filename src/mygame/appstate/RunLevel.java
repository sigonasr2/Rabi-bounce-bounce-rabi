package mygame.appstate;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import template.*;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import static com.jme3.shader.VarType.Vector3;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.util.SkyFactory;
import com.jme3.water.WaterFilter;
import mygame.Main;
import mygame.control.PlayableCharacter;
import mygame.control.PlayablePhysicsCharacter;


public class RunLevel extends BaseAppState 
  implements AnimEventListener {
    private SimpleApplication app;
    private Node              rootNode;
    private AssetManager      assetManager;
    private AppStateManager   stateManager;
    private InputManager      inputManager;
    private ViewPort          viewPort;
    private BulletAppState    physics;
    @Override
    protected void initialize(Application app) {
        //It is technically safe to do all initialization and cleanup in the
        //onEnable()/onDisable() methods. Choosing to use initialize() and
        //cleanup() for this is a matter of performance specifics for the
        //implementor.
        //TODO: initialize your AppState, e.g. attach spatials to rootNode
        //super.initialize(stateManager, app);
        this.app = (SimpleApplication) app; // can cast Application to something more specific
        this.rootNode     = this.app.getRootNode();
        this.assetManager = this.app.getAssetManager();
        this.stateManager = this.app.getStateManager();
        this.inputManager = this.app.getInputManager();
        this.viewPort     = this.app.getViewPort();
        this.physics      = this.stateManager.getState(BulletAppState.class);
        
        BulletAppState bulletAppState = new BulletAppState();
        this.stateManager.attach(bulletAppState);
        
        Node reflectedScene = new Node("Reflected Scene");
        rootNode.attachChild(reflectedScene);
        Spatial TestLevel = assetManager.loadModel("Scenes/TestLevel.j3o");
        Node world = (Node)TestLevel;
        TestLevel.addControl(new RigidBodyControl(0));
        bulletAppState.getPhysicsSpace().addAll(TestLevel);
        //System.out.println(world.getLocalLightList().size());
        DirectionalLight sceneLight = (DirectionalLight)world.getLocalLightList().get(0);
        
        Node player =  (Node)assetManager.loadModel("Models/Oto/Oto.mesh.xml"); 
        Node playerNode = new Node();
        playerNode.attachChild(player);
        playerNode.addControl(new PlayableCharacter());
        
        
        ChaseCamera chaseCam = new ChaseCamera(this.app.getCamera(), player, inputManager);
        
        
        //channel.setLoopMode(LoopMode.Cycle);
        world.attachChild(playerNode);
        
        player.move(0,2.5f,0);
        player.setLocalScale(0.5f);
        
        //BetterCharacterControl playerControl = new BetterCharacterControl(1.5f,4f,10f);
        playerNode.addControl(chaseCam);
        
        //System.out.println(world.getChildren());
        reflectedScene.attachChild(world);
        reflectedScene.attachChild(TestLevel);
        reflectedScene.attachChild(SkyFactory.createSky(assetManager,"Textures/Sky/Bright/BrightSky.dds",false));
        
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        //viewPort.addProcessor(fpp);
        Vector3f lightDir = new Vector3f(-2.9f,-1.2f,-5.8f);
        WaterFilter water = new WaterFilter(reflectedScene, lightDir);
        water.setWaterHeight(-1.5f);
        fpp.addFilter(water);
        
        final int SHADOWMAP_SIZE=1024;
        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, SHADOWMAP_SIZE, 3);
        dlsr.setLight(sceneLight);
        viewPort.addProcessor(dlsr);

        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, SHADOWMAP_SIZE, 3);
        dlsf.setLight(sceneLight);
        dlsf.setEnabled(true);
        fpp.addFilter(dlsf);
        viewPort.addProcessor(fpp);
        
    }

    @Override
    protected void cleanup(Application app) {
        //TODO: clean up what you initialized in the initialize method,
        //e.g. remove all spatials from rootNode
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
        
    }

    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
    }

    @Override
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
    }

}