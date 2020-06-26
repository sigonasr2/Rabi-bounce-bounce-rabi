package mygame.control;

import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.export.Savable;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import static mygame.Main.main;
import mygame.server.ServerMain.PlayerActionMessage;

public class NetworkPlayableCharacter extends PlayableCharacter implements Savable, Cloneable, ActionListener, AnalogListener, AnimEventListener {

    public NetworkPlayableCharacter() {
        super();
    }
    /** This method is called when the control is added to the spatial,
      * and when the control is removed from the spatial (setting a null value).
      * It can be used for both initialization and cleanup.
      */
    @Override
    public void setSpatial(Spatial spatial) {
        //super.setSpatial(spatial);
        //control = spatial.getControl(BetterCharacterControl.class);
        
        this.spatial = spatial;
        
        Node myNode = (Node)spatial;
        
        physics = new PhysicsControl(
                0.1f,
                -0.25f,
                5f
        );
        myNode.addControl(physics);
        
        control = ((Node)spatial).getChild(0).getControl(AnimControl.class);
        control.addListener(this);
        channel = control.createChannel();
        channel.setAnim("stand");
        /*channel_lowerbody = control.createChannel();
        channel_lowerbody.addBone("hip.right");
        channel_lowerbody.addBone("hip.left");*/ //There is no strafing animation
        
        /*main.getInputManager().addMapping("WalkForward", new KeyTrigger(KeyInput.KEY_W));
        main.getInputManager().addMapping("WalkBackward", new KeyTrigger(KeyInput.KEY_S));
        main.getInputManager().addMapping("StrafeLeft", new KeyTrigger(KeyInput.KEY_A));
        main.getInputManager().addMapping("StrafeRight", new KeyTrigger(KeyInput.KEY_D));
        main.getInputManager().addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        main.getInputManager().addListener(this, "WalkForward");
        main.getInputManager().addListener(this, "WalkBackward");
        main.getInputManager().addListener(this, "StrafeRight");
        main.getInputManager().addListener(this, "StrafeLeft");
        main.getInputManager().addListener(this, "Jump");*/
    }
    @Override
    protected void controlUpdate(float tpf){
        super.controlUpdate(tpf);
        if (spatial.getUserData("lastActionMessage")!=null &&
                spatial.getUserData("lastActionMessage") instanceof Integer
                && Integer.parseInt(spatial.getName())==(Integer)spatial.getUserData("lastActionMessage")) {
            //System.out.println((String)spatial.getUserData("lastAction")+","+(String)spatial.getUserData("lastData")+","+(Vector3f)spatial.getUserData("lastPosition"));
            //spatial.setUserData("lastActionMessage","NULL");
            //System.out.println(spatial.getUserData("lastActionMessage"));
            String action = (String)spatial.getUserData("lastAction");
            String data = (String)spatial.getUserData("lastData"); 
            Vector3f pos = (Vector3f)spatial.getUserData("lastPosition"); 
            Quaternion dir = (Quaternion)spatial.getUserData("lastRotation"); 
            if (action.equalsIgnoreCase("Jump")) {
                spatial.setLocalTranslation(pos);
                //spatial.setLocalRotation(dir);
                simulateJump(tpf);
            } else {
                if (!Boolean.parseBoolean(data)) {
                    spatial.setLocalTranslation(pos);
                }
                //spatial.setLocalRotation(dir);
                simulateAction(action, Boolean.parseBoolean(data), tpf);
            }
            spatial.setUserData("lastActionMessage", null);
            //System.out.println(getWalkDirection()+"Moving:"+moving+"/"+strafingLeft+"/"+strafingRight+"/"+walkingBackward+"/"+walkingForward);
        }
    }
    
    public void simulateAction(String action, boolean isPressed, float tpf) {
        this.onAction(action, isPressed, tpf);
    }
    public void simulateJump(float tpf) {
        this.onAnalog("Jump", 1.0f, tpf);
    }
}