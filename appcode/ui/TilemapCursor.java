package appcode.ui;

import appcode.engine.TileMap;
import velocity.InputSystem;
import velocity.Scene;
import velocity.renderer.RendererImage;
import velocity.sprite.ui.AnchorPoint;
import velocity.sprite.ui.UIImage;
import velocity.util.Point;

public class TilemapCursor extends UIImage {
    int snap = 0;
    RendererImage pointerImg;
    public Point cursorLoc = Point.zero;

    public TilemapCursor() {
        super(Point.zero, 0f, "Cursor", "./res/cursor.png");
        this.pointerImg = this.img;
        this.sortOrder = -5;
    }

    @Override
    public void tick() {
        if (snap == 0) {
            TileMap map = Scene.currentScene.getSprite(TileMap.class);
            
            // Prevent a crash if no stride has yet been registered.
            if (map != null)
                this.snap = map.getStride();
            return;
        }

        Point mouseLoc = Scene.currentScene.screenToWorldPoint(InputSystem.getMousePos().add(new Point(snap, snap).div(2)));
        Point adjPos = adjustPoint(mouseLoc.div(snap), mouseLoc).mult(snap);
        Point finalPos = Scene.currentScene.worldToScreenPoint(adjPos);
        this.pos.setPos(finalPos.add(AnchorPoint.getAnchor("center")));
        this.cursorLoc = adjPos;
    }

    public void setHoverImage(RendererImage img) {
        if (img == null)
            this.img = this.pointerImg;
        else
            this.img = img;
        this.pos.setWH(new Point(this.img.getWidth(), this.img.getHeight()));
    }

    private Point adjustPoint(Point in, Point mouseLoc) {
        if (mouseLoc.x < 0) in.x--;
        if (mouseLoc.y < 0) in.y--;
        return in;
    }
}
