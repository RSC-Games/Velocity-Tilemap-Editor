package appcode.ui;

import appcode.TilemapCreationManager;
import velocity.Scene;
import velocity.renderer.RendererImage;
import velocity.sprite.ui.AnchorPoint;
import velocity.sprite.ui.UIImage;
import velocity.system.Images;
import velocity.util.Point;

public class SavedTracker extends UIImage {
    RendererImage saved = null;
    RendererImage modified = null;
    Point offset = null;
    String anchor = null;

    TilemapCreationManager tilemapSystem;

    public SavedTracker(String anchor, Point offset) {
        super(offset, 0f, "Save Tracker", "./res/saved.png");
        modified = Images.loadImage("./res/modified.png");
        saved = this.img;
        this.offset = offset;
        this.anchor = anchor;
    }

    @Override
    public void init() {
        tilemapSystem = Scene.currentScene.getSprite(TilemapCreationManager.class);
    }
    
    @Override
    public void tick() {
        this.img = tilemapSystem.isSaved() ? saved : modified;
        this.pos.setPos(AnchorPoint.getAnchor(anchor).add(offset));
    }
}
