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
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import static com.jme3.shader.Shader.ShaderType.Geometry;
import static com.jme3.shader.VarType.Vector3;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.util.SkyFactory;
import com.jme3.water.WaterFilter;
import mygame.Main;
import static mygame.Main.main;
import mygame.control.PhysicsControl;
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
        
        //rootNode.setShadowMode(ShadowMode.CastAndReceive);
        
        //BulletAppState bulletAppState = new BulletAppState();
        //this.stateManager.attach(bulletAppState);
        
        DirectionalLight sceneLight = new DirectionalLight(new Vector3f(-0.57735026f, -0.57735026f, -0.57735026f));
        
        Node reflectedScene = new Node("Reflected Scene");
        rootNode.attachChild(reflectedScene);
        //rootNode.addLight(sceneLight);
        Spatial TestLevel = assetManager.loadModel("Scenes/TestLevel.j3o");
        Node world = (Node)TestLevel;
        //TestLevel.addControl(new RigidBodyControl(0));
        //bulletAppState.getPhysicsSpace().addAll(TestLevel);
        //System.out.println(world.getLocalLightList().size());
        //DirectionalLight sceneLight = (DirectionalLight)world.getLocalLightList().get(0);
        
        Node player =  (Node)assetManager.loadModel("Models/Oto/Oto.mesh.xml"); 
        Node playerNode = new Node();
        playerNode.attachChild(player);
        playerNode.addControl(new PlayableCharacter());
        playerNode.setUserData("Level", world);
        
        for (int i=0;i<100;i++) {
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
        }
        
        ChaseCamera chaseCam = new ChaseCamera(this.app.getCamera(), player, inputManager);
        chaseCam.setLookAtOffset(new Vector3f(0,2.5f,0));
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