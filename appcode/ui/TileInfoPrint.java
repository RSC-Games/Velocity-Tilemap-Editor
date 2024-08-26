package appcode.ui;

import java.awt.Color;

import appcode.TilemapCreationManager;
import appcode.engine.TileBase;
import appcode.engine.TileMap;
import velocity.util.Point;
import velocity.Scene;
import velocity.sprite.ui.UIText;

public class TileInfoPrint extends UIText {
    TilemapCreationManager tileMapDriver;

    public TileInfoPrint(Point pos, float rot, String name, Color c) {
        super(pos, rot, name, "Serif", c);
        this.setSize(17);
        this.sortOrder = 1;
    }

    public void init() {
        tileMapDriver = Scene.currentScene.getSprite(TilemapCreationManager.class);
    }

    public void tick() {
        // No tilemap; no info.
        if (Scene.currentScene.getSprite(TileMap.class) == null) 
            return;

        int layer = tileMapDriver.getLayer();
        TileBase tile = tileMapDriver.lookupTile(layer);

        if (tile == null)
            this.text = "Layer " + layer + ": no tile selected";
        else
            this.text = "Layer " + layer + ": tile: tid " + tile.tileIDs[0] + " collidable " + tile.isCollidable + " frame_cnt " + tile.tileIDs.length;
    }

}
