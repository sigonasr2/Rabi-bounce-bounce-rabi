package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
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
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;
import com.jme3.water.SimpleWaterProcessor;
import com.jme3.water.WaterFilter;
import mygame.appstate.RunLevel;
import mygame.control.PlayablePhysicsCharacter;

public class Main extends SimpleApplication {
    
    public static Main main;

    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1600,900);
        settings.setVSync(true);
        settings.setFrameRate(120);
        
        Main app = new Main();
        app.setSettings(settings);
        main = app;
        app.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        //flyCam.setMoveSpeed(50);
        BaseAppState level = new RunLevel();
        stateManager.attach(level);
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
