package appcode;

import java.awt.Color;
import java.awt.event.MouseEvent;

import velocity.InputSystem;
import velocity.Scene;
import velocity.sprite.ui.AnchorPoint;
import velocity.sprite.ui.UIText;
import velocity.util.Point;

public class SelectFileText extends UIText {
    public SelectFileText() {
        super(Point.zero, 0f, "SelectFile", "SERIF", Color.white);
        this.setSize(50);
        this.text = "Click to create a new tilemap file.";
    }

    public void tick() {
        if (InputSystem.released(MouseEvent.BUTTON1))
            Scene.scheduleSceneLoad("TilemapEditor");

        this.pos.setPos(AnchorPoint.getAnchor("center").sub(new Point(350, 0)));
    }
    
}
