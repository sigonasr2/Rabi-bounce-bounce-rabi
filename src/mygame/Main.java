package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.util.SkyFactory;
import com.jme3.water.SimpleWaterProcessor;
import com.jme3.water.WaterFilter;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class Main extends SimpleApplication {

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setMoveSpeed(50);
        CreateLevel();
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    private void CreateLevel() {
        Node reflectedScene = new Node("Reflected Scene");
        rootNode.attachChild(reflectedScene);
        Spatial TestLevel = assetManager.loadModel("Scenes/TestLevel.j3o");
        Node world = (Node)TestLevel;
        System.out.println(world.getChildren());
        reflectedScene.attachChild(TestLevel);
        reflectedScene.attachChild(SkyFactory.createSky(assetManager,"Textures/Sky/Bright/BrightSky.dds",false));
        
        /*SimpleWaterProcessor waterProcessor = new SimpleWaterProcessor(assetManager);
        waterProcessor.setReflectionScene(reflectedScene);
        waterProcessor.setWaterColor(new ColorRGBA(0.0f,0.0f,0.0f,1.0f));
        waterProcessor.setWaterDepth(2);
        waterProcessor.setWaterTransparency(0.9f);
        waterProcessor.setWaveSpeed(0.02f);
        waterProcessor.setDistortionScale(0.3f);
        waterProcessor.setDistortionMix(0.6f);
        waterProcessor.
        viewPort.addProcessor(waterProcessor);
        world.getChild("WaterNode").setMaterial(waterProcessor.getMaterial());*/
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        viewPort.addProcessor(fpp);
        Vector3f lightDir = new Vector3f(-2.9f,-1.2f,-5.8f);
        WaterFilter water = new WaterFilter(reflectedScene, lightDir);
        fpp.addFilter(water);
        //world.getChild("WaterNode").setQueueBucket(Bucket.Transparent);
    }
}
