package appcode;

import javax.annotation.processing.Generated;

import appcode.scenes.*;
import velocity.config.GlobalSceneDefs;

/** 
 * THIS FILE IS AUTO-GENERATED! DO NOT EDIT! (As of Velocity v0.3.5.1 this file
 * must be manually built).
 * 
 * Note: This is one of two {@code appcode} classes meant to be used by the engine 
 * directly! VELOCITY WILL NOT COMPILE WITHOUT THIS CLASS! To use, modify the 
 * constructor to add or remove defined and loadable scenes.
 */
@Generated(value="velocity.build", comments="Generated by the build system.")
public class SceneDefs extends GlobalSceneDefs {
    public SceneDefs() {
        super();
        sceneDefs.put("TilemapEditor", TilemapEditor.class);
        sceneDefs.put("StartScene", StartScene.class);
        sceneDefs.put("TileEditor", TileEditor.class);
    }
}
