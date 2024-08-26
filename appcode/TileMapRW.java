package appcode;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;

import appcode.engine.TileBase;
import appcode.engine.TileMap;
import velocity.Rect;
import velocity.Scene;
import velocity.renderer.DrawInfo;
import velocity.renderer.FrameBuffer;
import velocity.renderer.RendererImage;
import velocity.util.Point;

/**
 * Standard tilemap extended with useful rendering features and hits to allow easier editing of the
 * tilemap.
 */
public class TileMapRW extends TileMap {
    int currentLayer = 0;
    boolean drawColliders = false;

    public TileMapRW(String name, String mapPath, String palletePath) {
        super(name, mapPath, palletePath);
    }

    public void setCurrentLayer(int layer) {
        this.currentLayer = layer;
    }

    public void setDrawColliders(boolean drawColliders) {
        this.drawColliders = drawColliders;
    }

    /**
     * Draw this tilemap and all layers that are currently within the camera rect on screen.
     * TileMapRW only renders layers up to the selected layer, and the current layer is unshaded.
     * @see Renderable.render()
     */
    @Override
    public void render(DrawInfo d, FrameBuffer fb) {
        //palette.stride()

        // TODO: Cull all chunks that aren't visible on screen to reduce drawcall count.
        //System.out.println("RENDERING TILEMAP!");
        for (String chunkCoords : tilemap.keySet()) {
            //System.out.println("rendering chunk " + chunkCoords);
            String[] partialCoords = chunkCoords.split("~");
            Point loc = new Point(Integer.parseInt(partialCoords[0]), Integer.parseInt(partialCoords[1]));
            loc = loc.mult(CHUNK_SIZE).mult(palette.stride());
            //System.out.println("loc " + loc);
            //System.out.println(Arrays.toString(partialCoords));

            Point chunkCorner = loc.sub(Scene.currentScene.getCamera().pos.getDrawLoc());
            //System.out.println("chunkCorner " + chunkCorner);

            drawChunk(fb, d, chunkCorner, tilemap.get(chunkCoords));
        }
    }

    private void drawChunk(FrameBuffer fb, DrawInfo d, Point corner, HashMap<Integer, TileBase[][]> chunk) {
        // Sort the list of keys for z-sorted rendering.
        int[] sortedKeys = new int[chunk.size()];

        int i = 0;
        for (int key : chunk.keySet())
            sortedKeys[i++] = key;

        Arrays.sort(sortedKeys);

        for (int layer : sortedKeys) {
            // Don't draw layers above the current layer.
            // TODO: Maybe do this? idk.
            if (layer > currentLayer) return;

            TileBase[][] tileArray = chunk.get(layer);

            // Draw all of the tiles if present.
            for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                TileBase tile = tileArray[x][y];
                if (tile == null) continue;

                // Calculate the final render location.
                int stride = palette.stride();
                Point finalLoc = new Point(x, y).mult(stride).add(corner);

                Rect tileRect = new Rect(finalLoc, new Point(0, 0));
                tileRect.setWH(new Point(stride, stride));

                // Calculate frame to display.
                int frame = 0;
                if (tile.tileIDs.length > 1 && tile.waitTime > 0)
                    frame = (animCounter.elapsedms() / tile.waitTime) % tile.tileIDs.length;

                RendererImage texture = palette.lookupTex(tile.tileIDs[frame]);
                DrawInfo info = new DrawInfo(tileRect, d.rot, d.scale, d.drawLayer);

                if (layer == currentLayer)
                    fb.blit(texture, info);
                else
                    fb.drawShaded(texture, info);

                if (tile.isCollidable && drawColliders)
                    fb.drawRect(info.drawRect, 1, Color.yellow, false, 10);
            }}
        }
    }
}
