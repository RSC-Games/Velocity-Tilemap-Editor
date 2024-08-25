package appcode;

import velocity.util.Point;
import velocity.lighting.SunLight;
import velocity.sprite.Sprite;

public class AreaLight extends Sprite {
    SunLight light;

    public AreaLight(float intensity) {
        super(Point.zero, 0f, "AreaLight");
        this.light = new SunLight(intensity);
    }
}
