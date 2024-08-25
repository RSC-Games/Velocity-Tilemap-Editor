package appcode;

import java.awt.event.KeyEvent;

import org.lwjgl.glfw.GLFW;

import velocity.InputSystem;
import velocity.sprite.camera.Base2DCamera;
import velocity.util.Point;

public class UserCamera extends Base2DCamera {
    int speed = 10;

    public UserCamera(Point pos) {
        super(pos);
    }
    
    @Override
    public void tick() {
        super.tick();

        // Don't simulate if control is being used.
        if (InputSystem.getKey(KeyEvent.VK_CONTROL) || InputSystem.getKey(GLFW.GLFW_KEY_LEFT_CONTROL)) 
            return;

        Point moveCameraVec = new Point(InputSystem.getAxis(KeyEvent.VK_D, KeyEvent.VK_A), InputSystem.getAxis(KeyEvent.VK_S, KeyEvent.VK_W));
        this.pos.translate(moveCameraVec.mult(speed));
    }
}
