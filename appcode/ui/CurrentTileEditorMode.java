package appcode.ui;

import java.awt.Color;

import appcode.TileEditorManager;
import velocity.util.Point;
import velocity.Scene;
import velocity.sprite.ui.UIText;

public class CurrentTileEditorMode extends UIText {
    TileEditorManager tileEditor;

    public CurrentTileEditorMode(Point pos, float rot, String name, Color c) {
        super(pos, rot, name, "Serif", c);
        this.setSize(17);
        this.sortOrder = 1;
    }

    public void init() {
        tileEditor = Scene.currentScene.getSprite(TileEditorManager.class);
    }

    public void tick() {
        if (tileEditor.isAnimMode())
            this.text = "Animation Mode";
        else if (tileEditor.isLightingMode())
            this.text = "Lighting Mode";
        else if (tileEditor.isColliderMode())
            this.text = "Collider Mode";
        else
            this.text = "UNRECOGNIZED MODE!";
    }

}
