/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.server;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import java.lang.reflect.Field;

/**
 *
 * @author sigon
 */
@Serializable
public class Entity {
    public Integer id;
    public String type;
    public String modelData;
    public Vector3f position = Vector3f.ZERO;
    public String stateData;
    public Entity() {
        
    }
    public Entity(Integer id, String type) {
        this.id=id;
        this.type=type;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getName()+"(");
        boolean first=false;
        for (Field f : this.getClass().getDeclaredFields()) {
            if (!first) {
                try {
                    sb.append(f.getName()+"="+f.get(this));
                    first=true;
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    sb.append(","+f.getName()+"="+f.get(this));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        sb.append(")");
        return sb.toString();
    }     
}
