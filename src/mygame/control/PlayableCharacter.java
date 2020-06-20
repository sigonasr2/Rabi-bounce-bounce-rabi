package mygame.control;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.collision.CollisionResults;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.FastMath;
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
import static mygame.Main.main;

public class PlayableCharacter extends AbstractControl implements Savable, Cloneable, ActionListener, AnalogListener, AnimEventListener {
    
    float speed = 10.0f;
    float jumpSpd = 0.1f;
    float vspd = 0.0f;
    float gravity = -0.25f;
    
    float walkOffTime = 0.25f; //How long you can jump after becoming airborne.
    float airTime = 0.0f; //Amount of time in air.
    
    float rotation_time = 3f;
    float current_time = 0.0f;
    Spatial standingOn = null;
    
    Quaternion prevRot;
    
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
        Node myNode = (Node)spatial;
        
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
        //System.out.println(((Geometry)(((Node)((Node)spatial).getChild(0)).getChild(0))).getName()); //Possibility of using geometry node names.
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
                SmoothMoveWalk(walkDirection, tpf);
            } else {
                channel.setAnim("stand");
                channel.setLoopMode(LoopMode.DontLoop);
            }
        }
        //isOnGround();
        if (!isOnGround()) {
            vspd+=gravity*tpf;
            airTime+=tpf;
        } else {
            vspd=0;
            airTime=0;
        }
        spatial.move(0,vspd,0);
    }

    private Node GetLevel() {
        return (Node)(spatial.getUserData("Level"));
    }

    private void SmoothMoveWalk(Vector3f walkDirection, float tpf) {
        walkDirection.multLocal(speed).multLocal(tpf);
        spatial.move(walkDirection);
        Quaternion q = new Quaternion().fromAngleAxis((float)FastMath.atan2(walkDirection.x,walkDirection.z),Vector3f.UNIT_Y);
        Quaternion q2 = spatial.getLocalRotation();
        q2.slerp(q,Math.min(current_time/rotation_time,1));
        spatial.setLocalRotation(q2);
        current_time+=tpf;
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
                current_time = 0.0f;
                prevRot = spatial.getLocalRotation();
                strafingLeft = isPressed;
                moving = true;
            }break;
            case "StrafeRight":{
                current_time = 0.0f;
                prevRot = spatial.getLocalRotation();
                strafingRight = isPressed;
                moving = true;
            }break;
            case "WalkBackward":{
                current_time = 0.0f;
                prevRot = spatial.getLocalRotation();
                walkingBackward = isPressed;
                moving = true;
            }break;
            case "WalkForward":{
                current_time = 0.0f;
                prevRot = spatial.getLocalRotation();
                walkingForward = isPressed;
                moving = true;
            }break;
        }
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        switch (name) {
            case "Jump":{
                if (isOnGround() || airTime<=walkOffTime) {
                    vspd=jumpSpd;
                }
            }break;
        }
    }

    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName)  {
        
    }

    @Override
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
    }

    private boolean isOnGround() {
        if (vspd>0) {
            System.out.println(vspd);
            return false;
        }
        CollisionResults results = new CollisionResults();
        Ray r = new Ray(spatial.getLocalTranslation().add(0,2.5f-vspd,0),Vector3f.UNIT_Y.negate());
        GetLevel().updateGeometricState();
        GetLevel().collideWith(r, results);
        System.out.println("Collisions("+results.size()+"):");
        for (int i=0;i<results.size();i++) {
            System.out.println("Collision with "+results.getCollision(i).getGeometry().getName());
        }
        if (results.size()>0) {
            //System.out.println(results.getCollision(0));
            if (results.getClosestCollision().getContactPoint().x!=0 ||
                    results.getClosestCollision().getContactPoint().y!=0 ||
                    results.getClosestCollision().getContactPoint().z!=0) {
                System.out.println(results.getClosestCollision());
                if (results.getClosestCollision().getDistance()<=2.6-vspd) {
                    spatial.setLocalTranslation(results.getClosestCollision().getContactPoint());
                    return true;
                } else {
                    return false;
                }
            } else {
                vspd=jumpSpd; //???Undefined behavior.
            }
        }
        /*if (results.size()>0) {
            System.out.println("Distance: "+results.getClosestCollision().getDistance());
            //if (results.getClosestCollision().getDistance()<=5.0f) {
                
            //}
        }*/
        return false;
    }
}