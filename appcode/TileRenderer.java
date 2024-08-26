package appcode;

import appcode.engine.TileBase;
import appcode.engine.TilePalette;
import velocity.renderer.DrawInfo;
import velocity.renderer.FrameBuffer;
import velocity.renderer.RendererImage;
import velocity.sprite.Renderable;
import velocity.util.Counter;
import velocity.util.Logger;
import velocity.util.Point;

/**
 * Render a given TileBase on-screen.
 */
public class TileRenderer extends Renderable {
    boolean playing = false;
    int frame;
    TileBase renderedTile;
    TilePalette palette;

    Counter animCounter;

    /**
     * Nothing to say here.
     */
    public TileRenderer() {
        super(Point.zero, 0f, "Tile Renderer");
        animCounter = new Counter();
    }

    /**
     * Set the palette for looking up textures and displaying them.
     * 
     * @param palette Tile palette to use.
     */
    public void setPalette(TilePalette palette) {
        this.palette = palette;
    }
    
    /**
     * Set the tile to edit and render.
     * 
     * @param tile Tile to use.
     */
    public void setRenderedTile(TileBase tile) {
        this.renderedTile = tile;
    }

    /**
     * Get the currently set tile.
     * 
     * @return The tile.
     */
    public TileBase getRenderedTile() {
        return this.renderedTile;
    }

    /**
     * Set a tile ID at the current frame.
     * 
     * @param tileID Tile ID to set.
     */
    public void setTileAtFrame(int tileID) {
        // Regenerate the tile info if the array isn't long enough.
        if (this.frame >= renderedTile.tileIDs.length) {
            int[] newTileIDArray = new int[renderedTile.tileIDs.length+1];
            System.arraycopy(renderedTile.tileIDs, 0, newTileIDArray, 0, renderedTile.tileIDs.length);
            renderedTile = new TileBase(newTileIDArray, renderedTile.waitTime, renderedTile.isCollidable);
        }

        // Set the tile.
        renderedTile.tileIDs[this.frame] = tileID;

        // Shrink the array if too long.
        // TODO: Remove holes in the middle of the animation.
        if (tileID == -1) {
            int newLength = renderedTile.tileIDs.length;
            for (int i = newLength-2; i > 0; i++) {
                if (renderedTile.tileIDs[i] != -1)
                    break;
                
                newLength = i+1;
            }

            int[] newTileIDArray = new int[newLength];
            System.arraycopy(renderedTile.tileIDs, 0, newTileIDArray, 0, newLength);
            renderedTile = new TileBase(newTileIDArray, renderedTile.waitTime, renderedTile.isCollidable); 
        }
    }

    /**
     * Set the wait time, in ms, between each frame.
     * 
     * @param waitTime Wait time between each frame.
     */
    public void setWaitTime(int waitTime) {
        renderedTile = new TileBase(renderedTile.tileIDs, waitTime, renderedTile.isCollidable);
    }

    /**
     * Get the tile ID at this current frame.
     * 
     * @return Tile ID.
     */
    public int getTileIDAtFrame() {
        if (this.frame >= renderedTile.tileIDs.length)
            return -1;
        return renderedTile.tileIDs[this.frame];
    }

    /**
     * Get the corresponding texture for this frame.
     * 
     * @return The tile texture.
     */
    public RendererImage getTileTextureAtFrame() {
        return palette.lookupTex(renderedTile.tileIDs[this.frame]);
    }

    /**
     * Add one to the frame counter. Wraps over at frame count + 1
     * so a new frame can be added to the end.
     */
    public void incrementFrame() {
        this.frame++;
        this.frame %= renderedTile.tileIDs.length + 1;
    }

    /**
     * Subtract one from the frame counter. Wraps to frame count + 1
     * so a new frame can be added at the end.
     */
    public void decrementFrame() {
        this.frame--;
        if (this.frame < 0)
            this.frame = renderedTile.tileIDs.length;
    }

    /**
     * Get the current frame counter.
     * 
     * @return Frame counter.
     */
    public int getFrame() {
        return this.frame;
    }

    /**
     * Enable animation playback.
     * 
     * @param play Whether to play the animation.
     */
    public void play(boolean play) {
        if (play && renderedTile.waitTime == 0)
            Logger.warn("TileRenderer", "Cannot play animation; time between frames is 0!");

        this.playing = play;
        animCounter.reset();
    }

    /**
     * Return whether this animation is currently playing or not.
     * 
     * @return Whether this tile is playing animations.
     */
    public boolean isPlaying() {
        return this.playing;
    }

    @Override
    public void render(DrawInfo d, FrameBuffer fb) {
        if (this.playing && renderedTile.waitTime != 0)
            this.frame = (animCounter.elapsedms() / renderedTile.waitTime) % renderedTile.tileIDs.length;

        if (this.frame < renderedTile.tileIDs.length)
            fb.drawShaded(getTileTextureAtFrame(), d);
    }
    
}
