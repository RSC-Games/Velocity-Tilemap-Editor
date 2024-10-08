package appcode.ui;

import java.awt.Color;

import appcode.engine.TileMap;
import velocity.util.Point;
import velocity.Scene;
import velocity.sprite.ui.UIText;

public class PointerLoc extends UIText {
    TilemapCursor cursor;

    public PointerLoc(Point pos, float rot, String name, Color c) {
        super(pos, rot, name, "Serif", c);
        this.setSize(17);
        this.sortOrder = 1;
    }

    public void init() {
        cursor = Scene.currentScene.getSprite(TilemapCursor.class);
    }

    public void tick() {
        Point cPos = cursor.cursorLoc;
        TileMap map = Scene.currentScene.getSprite(TileMap.class);

        if (map == null) return;

        Point worldTile = map.worldToWorldTile(cPos);

        this.text = "Cursor: world " + cPos + " world tile " + worldTile + " chunk " + map.worldTileToChunk(worldTile);
    }

}
