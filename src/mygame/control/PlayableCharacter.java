package mygame.control;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.collision.CollisionResults;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.network.Message;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import static mygame.Main.level;
import static mygame.Main.main;
import mygame.camera.CustomChaseCamera;
import mygame.server.ServerMain.PlayerActionMessage;
import mygame.server.ServerMain.PlayerPositionMessage;
import mygame.server.ServerMain.ServerMessage;

public class PlayableCharacter extends AbstractControl implements Savable, Cloneable, ActionListener, AnalogListener, AnimEventListener {
    
    float speed = 10.0f;
    
    float rotation_time = 3f;
    float current_time = 0.0f;
    
    Quaternion prevRot;
    
    boolean walkingForward = false;
    boolean walkingBackward = false;
    boolean strafingLeft = false;
    boolean strafingRight = false;
    boolean moving = false;
    AnimChannel channel;
    //AnimChannel channel_lowerbody;
    AnimControl control;
    
    PhysicsControl physics;
    Vector3f walkDirection;
    
    float lastActionPerformed = 0.0f;
    static final float FREECAMERATIME = 0.5f;
    float cameraTransition = 0.0f;
    float oldRotation = 0.0f;
            
    // empty serialization constructor

    /** This method is called when the control is added to the spatial,
      * and when the control is removed from the spatial (setting a null value).
      * It can be used for both initialization and cleanup.
      */
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial!=null) {
            //control = spatial.getControl(BetterCharacterControl.class);
            Node myNode = (Node)spatial;

            physics = new PhysicsControl(
                    0.1f,
                    -0.25f,
                    5f
            );
            myNode.addControl(physics);

            control = (((Node)spatial).getChild(0)).getControl(AnimControl.class);
            //System.out.println(control.getAnimationNames());
            control.addListener(this);
            channel = control.createChannel();
            channel.setAnim("stand");
            channel.setLoopMode(LoopMode.Cycle);
            /*channel_lowerbody = control.createChannel();
            channel_lowerbody.addBone("hip.right");
            channel_lowerbody.addBone("hip.left");*/ //There is no strafing animation

            main.getInputManager().addMapping("WalkForward", new KeyTrigger(KeyInput.KEY_W));
            main.getInputManager().addMapping("WalkBackward", new KeyTrigger(KeyInput.KEY_S));
            main.getInputManager().addMapping("StrafeLeft", new KeyTrigger(KeyInput.KEY_A));
            main.getInputManager().addMapping("StrafeRight", new KeyTrigger(KeyInput.KEY_D));
            main.getInputManager().addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
            //main.getInputManager().addListener(this, "WalkForward");
            //main.getInputManager().addListener(this, "WalkBackward");
            main.getInputManager().addListener(this, "StrafeRight");
            main.getInputManager().addListener(this, "StrafeLeft");
            main.getInputManager().addListener(this, "Jump");
        } else {
            main.getInputManager().removeListener(this);
        }
    }

    /** Implement your spatial's behaviour here.
      * From here you can modify the scene graph and the spatial
      * (transform them, get and set userdata, etc).
      * This loop controls the spatial while the Control is enabled.
      */
    @Override
    protected void controlUpdate(float tpf){
        //System.out.println(((Geometry)(((Node)((Node)spatial).getChild(0)).getChild(0))).getName()); //Possibility of using geometry node names.
        /*if (this instanceof NetworkPlayableCharacter) {
            System.out.println("1:"+getWalkDirection()+"Moving:"+moving+"/"+strafingLeft+"/"+strafingRight+"/"+walkingBackward+"/"+walkingForward);
        }*/
        main.getCamera().setLocation(spatial.getLocalTranslation().add(-20,7f,0));
        if (moving) {
            if (!(this instanceof NetworkPlayableCharacter)) {
                cameraTransition+=tpf*4;
                spatial.getControl(CustomChaseCamera.class).setHorizontalRotation(FastMath.interpolateLinear(cameraTransition, (float)(oldRotation), (float)Math.PI));
                lastActionPerformed = 0.0f;
            }
            if (!channel.getAnimationName().equalsIgnoreCase("Walk")) {   
                channel.setAnim("Walk");
                channel.setLoopMode(LoopMode.Loop);
            }
            
            moving=false;
            
            if (this instanceof NetworkPlayableCharacter) {
                walkDirection = getWalkDirection((Vector3f)spatial.getUserData("lastCamDir"),(Vector3f)spatial.getUserData("lastCamLeftDir"));
            } else {
                walkDirection = getWalkDirection(main.getCamera().getDirection(),main.getCamera().getLeft());
            }
            /*if (this instanceof NetworkPlayableCharacter) {
                System.out.println(" 2:"+getWalkDirection()+"Moving:"+moving+"/"+strafingLeft+"/"+strafingRight+"/"+walkingBackward+"/"+walkingForward);
            }*/
            if (moving) {
                SmoothMoveWalk(walkDirection, tpf);
                //Message msg = new PlayerPositionMessage(spatial.getLocalTranslation());
                //main.client.send(msg);
            } else {
                channel.setAnim("stand");
                channel.setLoopMode(LoopMode.DontLoop);
            }
        } else {
            if (!(this instanceof NetworkPlayableCharacter)) {
                //System.out.println(spatial.getControl(CustomChaseCamera.class).getHorizontalRotation()+","+(spatial.getControl(CustomChaseCamera.class).getHorizontalRotation()%(2*Math.PI)));
                oldRotation = spatial.getControl(CustomChaseCamera.class).getHorizontalRotation();
                cameraTransition = 0.0f;
            }
        }
        if (!(this instanceof NetworkPlayableCharacter)) {
            lastActionPerformed+=tpf;
        }
    }

    private void SmoothMoveWalk(Vector3f walkDirection, float tpf) {
        walkDirection.multLocal(speed).multLocal(tpf);
        spatial.move(walkDirection);
        /*if (this instanceof NetworkPlayableCharacter) {
            System.out.println("Moving. My speed is "+speed+" walkDir is "+walkDirection);
        }*/
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
        lastActionPerformed = 0.0f;
        switch (name) {
            case "StrafeLeft":{
                current_time = 0.0f;
                prevRot = spatial.getLocalRotation();
                strafingLeft = isPressed;
                moving = true;
                if (!(this instanceof NetworkPlayableCharacter)) { //Only send if this is the source client.
                    PlayerActionMessage action = new PlayerActionMessage(name,Boolean.toString(isPressed),main.client.getId(),spatial.getLocalTranslation(),spatial.getLocalRotation(),main.getCamera().getDirection(),main.getCamera().getLeft());
                    main.client.send(action);
                }
            }break;
            case "StrafeRight":{
                current_time = 0.0f;
                prevRot = spatial.getLocalRotation();
                strafingRight = isPressed;
                moving = true;
                if (!(this instanceof NetworkPlayableCharacter)) { //Only send if this is the source client.
                    PlayerActionMessage action = new PlayerActionMessage(name,Boolean.toString(isPressed),main.client.getId(),spatial.getLocalTranslation(),spatial.getLocalRotation(),main.getCamera().getDirection(),main.getCamera().getLeft());
                    main.client.send(action);
                }
            }break;
            /*case "WalkBackward":{
                current_time = 0.0f;
                prevRot = spatial.getLocalRotation(); 
                walkingBackward = isPressed;
                moving = true;
                if (!(this instanceof NetworkPlayableCharacter)) { //Only send if this is the source client.
                    PlayerActionMessage action = new PlayerActionMessage(name,Boolean.toString(isPressed),main.client.getId(),spatial.getLocalTranslation(),spatial.getLocalRotation(),main.getCamera().getDirection(),main.getCamera().getLeft());
                    main.client.send(action);
                }
            }break;
            case "WalkForward":{
                current_time = 0.0f;
                prevRot = spatial.getLocalRotation();
                walkingForward = isPressed;
                moving = true;
                if (!(this instanceof NetworkPlayableCharacter)) { //Only send if this is the source client.
                    PlayerActionMessage action = new PlayerActionMessage(name,Boolean.toString(isPressed),main.client.getId(),spatial.getLocalTranslation(),spatial.getLocalRotation(),main.getCamera().getDirection(),main.getCamera().getLeft());
                    main.client.send(action);
                }
            }break;*/
        }
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        switch (name) {
            case "Jump":{
                if (isOnGround() || physics.airTime<=physics.walkOffTime) {
                    //System.out.println("Jump");
                    physics.jump();
                    if (!(this instanceof NetworkPlayableCharacter)) { //Only send if this is the source client.
                        PlayerActionMessage action = new PlayerActionMessage(name,"",main.client.getId(),spatial.getLocalTranslation(),spatial.getLocalRotation(),main.getCamera().getDirection(),main.getCamera().getLeft());
                        main.client.send(action);
                    }
                }
            }break;
        }
    }
    
    public boolean isOnGround() {
        return physics.isOnGround();
    }

    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName)  {
        
    }

    @Override
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
    }

    public Vector3f getWalkDirection(Vector3f camDir, Vector3f camLeftDir) {
        camDir.y=0; camDir.normalizeLocal();
        camLeftDir.y=0; camLeftDir.normalizeLocal();

        Vector3f walkDirection = new Vector3f(0,0,0);
        
        if (strafingLeft) {
            walkDirection.addLocal(0,0,-1);
            moving=true;
        }
        if (strafingRight) {
            walkDirection.addLocal(0,0,1);
            moving=true;
        }

        /*if (walkingForward) {
            walkDirection.addLocal(camDir);
            moving=true;
        }  
        if (walkingBackward) {
            walkDirection.addLocal(camDir.negate());
            moving=true;
        } */ 
        return walkDirection;
    }
}