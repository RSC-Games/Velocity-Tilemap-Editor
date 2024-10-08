package appcode;

import java.awt.event.MouseEvent;

import velocity.InputSystem;
import velocity.sprite.camera.Base2DCamera;
import velocity.util.Point;

public class UserCamera extends Base2DCamera {
    int speed = -1;
    Point lastFrameMousePos = Point.zero;
    Point tilemapCameraLoc = Point.zero;
    Point paletteCameraLoc = Point.zero;

    public UserCamera(Point pos) {
        super(pos);
    }
    
    @Override
    public void tick() {
        super.tick();

        // Update mouse relative.
        Point mouseCoords = InputSystem.getMousePos();
        Point relative = mouseCoords.sub(lastFrameMousePos);

        // Allow camera panning if the middle mouse button is pressed.
        if (InputSystem.clicked(MouseEvent.BUTTON2)) {
            Point moveCameraVec = relative; //new Point(InputSystem.getAxis(KeyEvent.VK_D, KeyEvent.VK_A), InputSystem.getAxis(KeyEvent.VK_S, KeyEvent.VK_W));
            this.pos.translate(moveCameraVec.mult(speed));
        }

        lastFrameMousePos = mouseCoords;
    }

    // Save camera state for tilemap editor mode.
    public void enterPaletteMode() {
        tilemapCameraLoc = this.pos.getPos();
        this.pos.setPos(paletteCameraLoc);
    }

    // Save camera state for palette mode.
    public void enterTilemapMode() {
        paletteCameraLoc = this.pos.getPos();
        this.pos.setPos(tilemapCameraLoc);
    }
}
