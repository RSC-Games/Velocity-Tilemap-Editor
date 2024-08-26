package appcode.ui;

import appcode.TilemapCreationManager;
import velocity.Scene;
import velocity.renderer.RendererImage;
import velocity.sprite.ui.AnchorPoint;
import velocity.sprite.ui.UIImage;
import velocity.system.Images;
import velocity.util.Point;

public class TilemapControlsImage extends UIImage {
    Point offset = null;
    String anchor = null;
    RendererImage imgStd = null;
    RendererImage imgEdit = null;

    TilemapCreationManager tilemapSystem;

    public TilemapControlsImage(String anchor, Point offset, String name, String std, String edit) {
        super(offset, 0f, name, std);
        this.imgStd = this.img;
        this.imgEdit = Images.loadImage(edit);
        this.anchor = anchor;
        this.offset = offset;
    }

    @Override
    public void init() {
        tilemapSystem = Scene.currentScene.getSprite(TilemapCreationManager.class);
    }

    @Override
    public void tick() {
        this.img = tilemapSystem.inEditMode() ? imgEdit : imgStd;
        this.pos.setPos(AnchorPoint.getAnchor(anchor).add(offset));
    }
}