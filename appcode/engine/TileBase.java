package appcode.engine;

// TODO: Make TileBase intelligent instead of a basic struct.
// Changes to make:
// Allow it to change its own internal tiles.
// Allow it to store a light source (if it has one).
public class TileBase {
    public final int[] tileIDs;
    public final int waitTime;
    public final boolean isCollidable;
    public final float intensity;
    public final float lightRadius;

    public TileBase(int[] tileIDs, int waitTime, boolean isCollidable) {
        this.tileIDs = tileIDs;
        this.waitTime = waitTime;
        this.isCollidable = isCollidable;
        this.intensity = 0f;
        this.lightRadius = 0f;
    }

    public TileBase(int[] tileIDs, int waitTime, boolean isCollidable, float intensity, float lightRadius) {
        this.tileIDs = tileIDs;
        this.waitTime = waitTime;
        this.isCollidable = isCollidable;
        this.intensity = intensity;
        this.lightRadius = lightRadius;
    }
}
