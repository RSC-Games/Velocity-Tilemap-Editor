package appcode.ui;

import velocity.sprite.ImageSprite;
import velocity.util.Point;

public class CenterPoint extends ImageSprite {

    public CenterPoint(Point pos, float rot) {
        super(pos, rot, "Center Point", "./res/center.png");
        this.sortOrder = 5000;
        //TODO Auto-generated constructor stub
    }
    
}
