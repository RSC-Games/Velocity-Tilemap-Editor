package appcode.ui;

import java.awt.Color;

import appcode.TileRenderer;
import appcode.engine.TileBase;
import velocity.util.Point;
import velocity.Scene;
import velocity.sprite.ui.UIText;

public class TileEditorInfo extends UIText {
    TileRenderer tileRenderer;

    public TileEditorInfo(Point pos, float rot, String name, Color c) {
        super(pos, rot, name, "Serif", c);
        this.setSize(17);
        this.sortOrder = 1;
    }

    public void init() {
        tileRenderer = Scene.currentScene.getSprite(TileRenderer.class);
    }

    public void tick() {
        TileBase tile = tileRenderer.getRenderedTile();
        int tileID = tileRenderer.getTileIDAtFrame();
        int frame = tileRenderer.getFrame() + 1;

        this.text = " Frame " + frame + " (of " + tile.tileIDs.length + "): ms " + tile.waitTime + " tileID " + tileID + " collidable " + tile.isCollidable;
        this.text = (tileRenderer.isPlaying() ? "Playing Animation" : "Stopped") + this.text;
    }

}
