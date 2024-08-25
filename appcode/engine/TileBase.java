package appcode.engine;

public class TileBase {
    public final int[] tileIDs;
    public final int waitTime;
    public final boolean isCollidable;

    public TileBase(int[] tileIDs, int waitTime, boolean isCollidable) {
        this.tileIDs = tileIDs;
        this.waitTime = waitTime;
        this.isCollidable = isCollidable;
    }
}
