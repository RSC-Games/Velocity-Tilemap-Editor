package appcode.ui;

import velocity.sprite.ui.AnchorPoint;
import velocity.sprite.ui.UIImage;
import velocity.util.Point;

public class AnchoredUIImage extends UIImage {
    Point offset = null;
    String anchor = null;

    public AnchoredUIImage(String anchor, Point offset, String name, String imagename) {
        super(offset, 0f, name, imagename);
        this.anchor = anchor;
        this.offset = offset;
    }

    @Override
    public void tick() {
        this.pos.setPos(AnchorPoint.getAnchor(anchor).add(offset));
    }
}