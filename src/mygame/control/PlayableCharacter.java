package mygame.control;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.collision.CollisionResults;
import template.*;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import mygame.Main;
import static mygame.Main.main;

public class PlayableCharacter extends AbstractControl implements Savable, Cloneable, ActionListener, AnalogListener, AnimEventListener {
    
    float speed = 1000.0f;
    
    boolean walkingForward = false;
    boolean walkingBackward = false;
    boolean strafingLeft = false;
    boolean strafingRight = false;
    boolean moving = false;
    AnimChannel channel;
    //AnimChannel channel_lowerbody;
    AnimControl control;

    public PlayableCharacter() {
    } // empty serialization constructor

    /** This method is called when the control is added to the spatial,
      * and when the control is removed from the spatial (setting a null value).
      * It can be used for both initialization and cleanup.
      */
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        //control = spatial.getControl(BetterCharacterControl.class);
        
        
        control = ((Node)spatial).getChild(0).getControl(AnimControl.class);
        control.addListener(this);
        channel = control.createChannel();
        channel.setAnim("stand");
        /*channel_lowerbody = control.createChannel();
        channel_lowerbody.addBone("hip.right");
        channel_lowerbody.addBone("hip.left");*/ //There is no strafing animation
        
        main.getInputManager().addMapping("WalkForward", new KeyTrigger(KeyInput.KEY_W));
        main.getInputManager().addMapping("WalkBackward", new KeyTrigger(KeyInput.KEY_S));
        main.getInputManager().addMapping("StrafeLeft", new KeyTrigger(KeyInput.KEY_A));
        main.getInputManager().addMapping("StrafeRight", new KeyTrigger(KeyInput.KEY_D));
        main.getInputManager().addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        main.getInputManager().addListener(this, "WalkForward");
        main.getInputManager().addListener(this, "WalkBackward");
        main.getInputManager().addListener(this, "StrafeRight");
        main.getInputManager().addListener(this, "StrafeLeft");
        main.getInputManager().addListener(this, "Jump");
    }

    /** Implement your spatial's behaviour here.
      * From here you can modify the scene graph and the spatial
      * (transform them, get and set userdata, etc).
      * This loop controls the spatial while the Control is enabled.
      */
    @Override
    protected void controlUpdate(float tpf){
        if (moving) {
            if (!channel.getAnimationName().equalsIgnoreCase("Walk")) {   
                channel.setAnim("Walk");
                channel.setLoopMode(LoopMode.Loop);
            }
            Vector3f camDir = main.getCamera().getDirection(); camDir.y=0; camDir.normalizeLocal();
            Vector3f camLeftDir = main.getCamera().getLeft(); camLeftDir.y=0; camLeftDir.normalizeLocal();

            Vector3f walkDirection = new Vector3f(0,0,0);
            //spatial.setLocalRotation(new Quaternion().fromAngleAxis(spatial.getControl(ChaseCamera.class).getHorizontalRotation(),Vector3f.UNIT_Y));
            //System.out.println(camDir);
            moving=false;
            if (strafingLeft) {
                walkDirection.addLocal(camLeftDir);
                moving=true;
            }
            if (strafingRight) {
                walkDirection.addLocal(camLeftDir.negate());
                moving=true;
            }

            if (walkingForward) {
                walkDirection.addLocal(camDir);
                moving=true;
            }  
            if (walkingBackward) {
                walkDirection.addLocal(camDir.negate());
                moving=true;
            }  

            if (moving) {
                walkDirection.multLocal(speed).multLocal(tpf);
            } else {
                channel.setAnim("stand");
                channel.setLoopMode(LoopMode.DontLoop);
            }
        }
    }

    @Override
    public Control cloneForSpatial(Spatial spatial){
        final PlayableCharacter control = new PlayableCharacter();
        /* Optional: use setters to copy userdata into the cloned control */
        // control.setIndex(i); // example
        control.setSpatial(spatial);
        return control;
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp){
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        switch (name) {
            case "StrafeLeft":{
                strafingLeft = isPressed;
                moving = true;
            }break;
            case "StrafeRight":{
                strafingRight = isPressed;
                moving = true;
            }break;
            case "WalkBackward":{
                walkingBackward = isPressed;
                moving = true;
            }break;
            case "WalkForward":{
                walkingForward = isPressed;
                moving = true;
            }break;
        }
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        switch (name) {
            case "Jump":{
            }break;
        }
    }

    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName)  {
        
    }

    @Override
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
    }
}