/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.camera;

import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;

/**
 *
 * @author sigon
 */
public class CustomChaseCamera extends ChaseCamera{
    
    public boolean canZoom = true;
    
    public CustomChaseCamera(Camera cam, Spatial target, InputManager inputManager) {
        super(cam, target, inputManager);
    }
    
    public void setHorizontalRotation(float newRotation) {
       targetRotation = newRotation; 
    }
    /*
    public float getRotation() {
        return targetRotation;
    }*/
    
    @Override
    protected void updateCamera(float tpf) {
        super.updateCamera(tpf);
        targetRotation = (float)(targetRotation % (2*Math.PI));
    }
    
    @Override
    protected void zoomCamera(float value) {
        if (!canZoom || !enabled) {
            return;
        }

        zooming = false;
        targetDistance += value * zoomSensitivity;
        if (targetDistance > maxDistance) {
            targetDistance = maxDistance;
        }
        if (targetDistance < minDistance) {
            targetDistance = minDistance;
        }
        if (veryCloseRotation) {
            if ((targetVRotation < minVerticalRotation) && (targetDistance > (minDistance + 1.0f))) {
                targetVRotation = minVerticalRotation;
            }
        }
        distance = targetDistance;
            System.out.println(targetDistance);
    }
}
