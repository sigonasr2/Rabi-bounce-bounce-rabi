package mygame.control;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import template.*;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static mygame.appstate.RunLevel.world;

public class PhysicsControl extends AbstractControl implements Savable, Cloneable {
    
    final static float FRICTION = 0.4f;
    
    float jumpSpd = 0.1f;
    float vspd = 0.0f;
    float hspd = 0.0f; //Determines movement along the Z axis.
    float gravity = -0.25f;
    
    float acceleration = 80f; //80 units/s acceleration.
    float maxSpd = 10f; //Default max speed is 10.
    
    float modelHeight = 2.5f;
    
    float walkOffTime = 0.25f; //How long you can jump after becoming airborne.
    float airTime = 0.0f; //Amount of time in air.
    
    Geometry standingOn;

    public PhysicsControl(float jumpSpd, float gravity, float modelHeight){
        this.jumpSpd=jumpSpd;
        this.gravity=gravity;
        this.modelHeight=modelHeight;
    }

    /** This method is called when the control is added to the spatial,
      * and when the control is removed from the spatial (setting a null value).
      * It can be used for both initialization and cleanup.
      */
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        spatial.setShadowMode(ShadowMode.CastAndReceive);
        //spatial.setUserData("Level", levelData);
    }

    /** Implement your spatial's behaviour here.
      * From here you can modify the scene graph and the spatial
      * (transform them, get and set userdata, etc).
      * This loop controls the spatial while the Control is enabled.
      */
    @Override
    protected void controlUpdate(float tpf){
       if (!isOnGround()) {
            vspd+=gravity*tpf;
            airTime+=tpf;
        } else {
            vspd=0;
            airTime=0;
            if (standingOn!=null && standingOn.getUserData("friction")!=null) {
                if (hspd<0) {
                    hspd=Math.min(hspd+(float)standingOn.getUserData("friction"),0);
                } else {
                    hspd=Math.max(hspd-(float)standingOn.getUserData("friction"),0);
                }
            } else {
                if (hspd<0) {
                    hspd=Math.min(hspd+FRICTION,0);
                } else {
                    hspd=Math.max(hspd-FRICTION,0);
                }
            }
        }
        spatial.move(0,vspd,hspd*tpf);
    }

    @Override
    public Control cloneForSpatial(Spatial spatial){
        final PhysicsControl control = new PhysicsControl(jumpSpd,gravity,modelHeight);
        /* Optional: use setters to copy userdata into the cloned control */
        // control.setIndex(i); // example
        control.setSpatial(spatial);
        return control;
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp){
        /* Optional: rendering manipulation (for advanced users) */
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        // im.getCapsule(this).read(...);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        // ex.getCapsule(this).write(...);
    }

    private Node GetLevel() {
        return (Node)(spatial.getUserData("Level"));
    }
    
    void jump() {
        setVerticalSpeed(jumpSpd);
    }
    
    void setVerticalSpeed(float spd) {
        vspd = spd;
    }
    
    void addVerticalSpeed(float spd) {
        vspd += spd;
    }
    
    float getVerticalSpeed() {
        return vspd;
    }
    
    void addHorizontalSpeed(float spd) {
        hspd += spd;
    }
    void setHorizontalSpeed(float spd) {
        hspd = spd;
    }
    
    void setAcceleration(float value) {
        acceleration = value;
    }
    void setMaxSpd(float value) {
        maxSpd = value;
    }
    
    float getAcceleration() {
        return acceleration;
    }
    float getMaxSpd() {
        return maxSpd;
    }
    
    /***
     * 
     * @param positive Use false if we are accelerating in the -Z direction. (Left). Use true to say we are accelerating to the right.
     * @param tpf 
     */
    void accelerate(boolean right, float tpf) {
        if (right) {
            if (hspd+acceleration*tpf<maxSpd) {
                hspd += acceleration*tpf;
            } else {
                hspd = maxSpd;
            }
        } else {
            if (hspd-acceleration*tpf>-maxSpd) {
                hspd -= acceleration*tpf;
            } else {
                hspd = -maxSpd;
            }
        }
    }
    
    float getHorizontalSpeed() {
        return hspd;
    }
    
    boolean isOnGround() {
        if (vspd>0) {
            //System.out.println(vspd);
            return false;
        }
        CollisionResults results = new CollisionResults();
        CollisionResults newResults = new CollisionResults();
        Ray r = new Ray(spatial.getLocalTranslation().add(0,(modelHeight/2)-vspd,0),Vector3f.UNIT_Y.negate());
        //world.updateGeometricState();
        world.collideWith(r, results);
        //Get closest collision that doesn't include myself.
        List<CollisionResult> collisions = new ArrayList<>();
        for (int i=0;i<results.size();i++) {
           if (results.getCollision(i).getGeometry()!=spatial) {
            newResults.addCollision(results.getCollision(i));
           }
        }
        if (newResults.size()>0) {
            //System.out.println(newResults.getCollision(0));
            if (newResults.getClosestCollision().getContactPoint().x!=0 ||
                    newResults.getClosestCollision().getContactPoint().y!=0 ||
                    newResults.getClosestCollision().getContactPoint().z!=0) {
                //System.out.println(newResults.getClosestCollision());
                if (newResults.getClosestCollision().getDistance()<=(modelHeight/2)+0.1-vspd) {
                    spatial.setLocalTranslation(newResults.getClosestCollision().getContactPoint());
                    standingOn = newResults.getClosestCollision().getGeometry();
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