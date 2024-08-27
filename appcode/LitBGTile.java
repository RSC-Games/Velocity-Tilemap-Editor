package appcode;

import velocity.renderer.DrawInfo;
import velocity.renderer.FrameBuffer;
import velocity.sprite.ImageSprite;
import velocity.util.Point;

/**
 * A basic sprite with an image that can be drawn on-screen.
 */
public class LitBGTile extends ImageSprite {

    public LitBGTile(Point pos, float rot, String name, String image) {
        super(pos, rot, name, image);
    }

    /**
     * Draw the image on screen.
     * 
     * @param d Draw transform
     * @param fb Rendering framebuffer.
     */
    @Override
    public void render(DrawInfo d, FrameBuffer fb) {
        fb.drawShaded(this.img, d);
    }
}
