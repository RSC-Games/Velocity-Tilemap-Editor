package appcode.engine;

import velocity.Rect;
import velocity.Scene;
import velocity.renderer.DrawInfo;
import velocity.renderer.FrameBuffer;
import velocity.renderer.RendererImage;
import velocity.sprite.Renderable;
import velocity.util.Counter;
import velocity.util.Logger;
import velocity.util.Point;
import velocity.util.TextFile;

import java.util.Arrays;
import java.util.HashMap;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Velocity's flexible Tile map system. Tilemaps are stored on disk in a compact format.
 * They may be animated tiles or static tiles. The tile lookups are done by the TilePalette.
 * TODO: In the future add support for multiple tile palettes on one tilemap.
 * 
 * The tilemaps are stored in this format:
 * TILE_X, TILE_Y, TILE_Z, TILE_COUNT, PALETTE_TILE_ID0...N, WAIT_TIME, IS_COLLIDABLE\n
 * 
 * Where the parameters are:
 *  - TILE_X: Tile x location in tile space (converted to world space via the palette stride).
 *  - TILE_Y: Tile y location in tile space (converted to world space via the palette stride).
 *  - TILE_COUNT: Tile ID count for this tile. > 1 means this tile is animated.
 *  - PALETTE_TILE_IDx: Comma-separated list of tile ids for animation.
 *  - WAIT_TIME: If TILE_COUNT > 1, then this field is present. Wait duration (in ms) between
 *      state changes in the palette tile id.
 *  - IS_COLLIDABLE: Indicate whether this tile will have collision geometry generated (1 is yes,
 *      0 is no). Only tile Z coordinates > 0 can use this flag. Otherwise it's not present.
 */
public class TileMap extends Renderable {
    /**
     * Chunk size of the tile array. The tilemap itself is made of a large amount of chunks.
     * Larger chunks may reduce hashmap collisions but also use more memory.
     */
    private static final int CHUNK_SIZE = 16;

    /**
     * Internal tilemap and chunk lookup. Stored in 3 dimensions. The outermost index is the X and
     * Y index for locating a chunk. The inner index, Z, is fully walked for tile layering and
     * sorting if a given chunk is rendered.
     */
    HashMap<String, HashMap<Integer, TileBase[][]>> tilemap;

    /**
     * The tile palette used for texture lookups.
     */
    TilePalette palette;

    /**
     * Counter for keeping track of the elapsed milliseconds for animations.
     */
    Counter animCounter;

    /**
     * 
     * @param name
     * @param mapPath
     * @param palletePath
     */
    public TileMap(String name, String mapPath, String palletePath) {
        super(Point.zero, 0f, name);

        this.tilemap = new HashMap<String, HashMap<Integer, TileBase[][]>>();
        this.palette = new TilePalette(palletePath);
        
        try {
            loadMapFromFile(mapPath);
        }
        catch (IOException ie) {
            throw new RuntimeException(ie);
        }
    }

    /**
     * Get the stride of this tilemap.
     * 
     * @return the stride (in pixels)
     */
    public int getStride() {
        return this.palette.stride();
    }

    /**
     * Load the tilemap from a provided file and palette.
     * 
     * @param mapPath Relative path to the tilemap from project root.
     * @throws IOException If the file cannot be accessed.
     */
    private void loadMapFromFile(String mapPath) throws IOException {
        TextFile mapFile = new TextFile(mapPath, "r");

        // Read the tiles from the file and decode them.
        String tileData;
        int lineno = 0;
        while (true) {
            lineno++;  // Track the line number for easier error reporting.

            tileData = mapFile.readLine();

            // No data left. Tilemap loading finished.
            if (tileData.equals(""))
                break;
            
            // Skip lines with comments.
            if (tileData.charAt(0) == '#') 
                continue;

            tileData.strip();
            String[] tileInfo = tileData.split("[\\s,]+");
            System.out.println(Arrays.toString(tileInfo));

            // First index is empty. Remaining data will be on the next indices.
            // This index will be trimmed out.
            if (tileInfo[0] == "") {
                String[] newTileInfo = new String[tileInfo.length - 1];
                System.arraycopy(tileInfo, 1, newTileInfo, 0, newTileInfo.length);
                tileInfo = newTileInfo;
            }

            // Decode the tile coordinates and look up the chunk. (Indices 0 and 1)
            Point tileCoords = new Point(Integer.parseInt(tileInfo[0]), Integer.parseInt(tileInfo[1]));
            HashMap<Integer, TileBase[][]> chunk = getChunkSafe(worldTileToChunk(tileCoords));

            // Get the layer to insert this tile in. (Index 2)
            int layer = Integer.parseInt(tileInfo[2]);
            TileBase[][] layerArray = getLayerFromChunk(chunk, layer);

            // Extract all animation frames. (Frame count in Index 3, frames in Index 5+i)
            int animFrames = Integer.parseInt(tileInfo[3]);
            int[] frameVals = new int[animFrames];

            // All frames are found at index 4 and after.
            for (int i = 0; i < animFrames; i++)
                frameVals[i] = Integer.parseInt(tileInfo[4+i]);

            // Jump past the end of the frames.
            int readPtr = 3 + animFrames;
            int waitTime = Integer.parseInt(tileInfo[readPtr++]);

            // Collidable only seached for if not a floor tile (< 1 layer). 
            boolean isCollidable = false;
            if (layer > 0)
                isCollidable = Integer.parseInt(tileInfo[readPtr++]) == 1 ? true : false;

            // Create the tile and insert it into the layer.
            TileBase tile = new TileBase(frameVals, waitTime, isCollidable);
            System.out.println("frames (cnt " + animFrames + ") " + Arrays.toString(frameVals) + " waitTime " + waitTime + " collidable " + isCollidable);
            addTileToLayer(layerArray, worldTileToLocal(tileCoords), tile);
        }
    }

    /**
     * Save this tilemap to a provided path on the filesystem.
     * 
     * @param mapPath Path to the map file on disk.
     */
    public void saveToFile(String mapPath) {
        TextFile mapFile;
        
        try {
           mapFile = new TextFile(Path.of(mapPath).toAbsolutePath().toString(), "w");
        }
        catch (IOException ie) {
            throw new RuntimeException(ie);
        }

        // Read the tiles from the file and decode them.
        for (String coords : tilemap.keySet()) {
            System.out.println("Saving chunk " + coords);
            HashMap<Integer, TileBase[][]> chunk = tilemap.get(coords);
            String[] partialCoords = coords.split("~");

            // Convert chunk coordinates to global tilespace.
            Point trueChunkCoords = new Point(Integer.parseInt(partialCoords[0]), Integer.parseInt(partialCoords[1]));
            trueChunkCoords = trueChunkCoords.mult(CHUNK_SIZE);
            System.out.println("Chunk coords in tile space " + trueChunkCoords);

            for (int layer : chunk.keySet()) {
                System.out.println("Saving layer " + layer);
                TileBase[][] tileArray = chunk.get(layer);

                for (int x = 0; x < CHUNK_SIZE; x++) {
                for (int y = 0; y < CHUNK_SIZE; y++) {
                    TileBase tile = tileArray[x][y];
                    if (tile == null) continue;

                    Point trueTilePos = trueChunkCoords.add(new Point(x, y));
                    System.out.println("trueTilePos " + trueTilePos);

                    System.out.println("Saving tile index " + x + ", " + y);
                    String tileData = encodeTile(tile, trueTilePos, layer);
                    System.out.println("tile data: " + tileData);

                    try {
                        mapFile.write(tileData);
                        mapFile.write("\n");
                    }
                    catch (IOException ie) {
                        throw new RuntimeException(ie);
                    }
                }
                }
            }
        }
    }

    /**
     * Encode the tile for writeout. File is encoded as follows:
     * TILE_X, TILE_Y, TILE_Z, TILE_COUNT, PALETTE_TILE_ID0...N, WAIT_TIME, IS_COLLIDABLE
     * 
     * @param tile The tile to encode.
     * @param tilePos The tile's location in world tile space.
     * @param layer The z-layer of the tile.
     * @return The encoded tile.
     */
    private String encodeTile(TileBase tile, Point tilePos, int layer) {
        StringBuilder out = new StringBuilder();
        StringBuilder paletteAnimIDs = new StringBuilder("" + tile.tileIDs[0]);

        for (int i = 1; i < tile.tileIDs.length; i++) {
            paletteAnimIDs.append(", ");
            paletteAnimIDs.append(tile.tileIDs[i]);
        }

        out.append(tilePos.x);
        out.append(", ");
        out.append(tilePos.y);
        out.append(", ");
        out.append(layer);
        out.append(", ");
        out.append(tile.tileIDs.length);
        out.append(", ");
        out.append(paletteAnimIDs);

        // Insert frame time if this is an animated tile.
        if (tile.tileIDs.length > 1) {
            out.append(", ");
            out.append(tile.waitTime);
        }

        // Add collider information for wall tiles.
        if (layer > 0) {
            out.append(", ");
            out.append(tile.isCollidable ? 1 : 0);
        }

        return out.toString();
    }

    /**
     * Load a chunk from the given coordinates in tile space. If no such chunk exists, 
     * create a new one.
     * 
     * @param chunkCoords Chunk coordinates in global space. 
     * @return The chunk at the location.
     */
    private HashMap<Integer, TileBase[][]> getChunkSafe(Point chunkCoords) {
        System.out.println("reading chunk index " + chunkCoords);

        String lookupCoords = chunkCoords.x + "~" + chunkCoords.y;
        if (tilemap.containsKey(lookupCoords))
            return tilemap.get(lookupCoords);

        // No chunk found. Create a new one.
        HashMap<Integer, TileBase[][]> chunk = new HashMap<Integer, TileBase[][]>();
        tilemap.put(lookupCoords, chunk);
        return chunk;
    }

    /**
     * Load a tile layer from the given chunk and layer id. If no layer exists, create a new
     * one and return it.
     * 
     * @param chunk Chunk to load the layer from.
     * @param layer Layer id.
     * @return The requested layer from the chunk.
     */
    private TileBase[][] getLayerFromChunk(HashMap<Integer, TileBase[][]> chunk, int layer) {
        if (chunk.containsKey(layer))
            return chunk.get(layer);

        // No layer found. Create a new one.
        TileBase[][] tileLayer = new TileBase[CHUNK_SIZE][CHUNK_SIZE];
        chunk.put(layer, tileLayer);
        return tileLayer;
    }

    /**
     * Write a tile into the tilemap, if one doesn't already exist.
     * 
     * @param layer Layer to add the tile.
     * @param tileCoords Location of the tile in world space.
     * @param tile Tile to add.
     */
    private void addTileToLayer(TileBase[][] layer, Point tileCoords, TileBase tile) {
        Point localTileCoords = tileCoords.mod(CHUNK_SIZE);

        // Ensure a tile isn't already present.
        if (layer[localTileCoords.x][localTileCoords.y] != null)
            throw new IllegalStateException("Tile already present on layer at tileCoords " + tileCoords);

        layer[localTileCoords.x][localTileCoords.y] = tile;
    }

    /**
     * Write a tile into the tilemap, overwriting one already present.
     * 
     * @param layer Layer to add the tile.
     * @param tileCoords Location of the tile in world space.
     * @param tile Tile to add.
     */
    private void writeTileOnLayer(TileBase[][] layer, Point tileCoords, TileBase tile) {
        Point localTileCoords = tileCoords.mod(CHUNK_SIZE);

        // Ensure a tile isn't already present.
        //if (layer[localTileCoords.x][localTileCoords.y] != tile)
        Logger.log("tilemap", "Overwrote old tile at " + tileCoords + " (local " + localTileCoords + ")");

        layer[localTileCoords.x][localTileCoords.y] = tile;
    }

    /**
     * Set a tile on the tilemap.
     * 
     * @param worldPos Pointer location in world space.
     * @param inLayer Chunk layer.
     * @param tile Tile to inject.
     */
    public void setTile(Point worldPos, int inLayer, TileBase tile) {
        //Point tilePos = worldPos.div(palette.stride());
        Point tilePos = worldToWorldTile(worldPos);
        Point chunkPos = worldTileToChunk(tilePos);
        HashMap<Integer, TileBase[][]> chunk = getChunkSafe(chunkPos);

        // Get the layer to insert this tile in. (Index 3)
        System.out.println("locations: worldPos " + worldPos + " tilePos " + tilePos + " chunkPos " + chunkPos + " chunkTilePos" + worldTileToLocal(tilePos));
        TileBase[][] layerArray = getLayerFromChunk(chunk, inLayer);
        writeTileOnLayer(layerArray, worldTileToLocal(tilePos), tile);
    }

    /**
     * 
     * @param worldSpace
     * @return
     */
    public Point worldToWorldTile(Point worldSpace) {
        float x = (float)worldSpace.x / palette.stride();
        float y = (float)worldSpace.y / palette.stride();

        // Negative numbers should be higher than lower numbers.
        return new Point(
            x >= 0f ? (int)x : (int)Math.floor(x),
            y >= 0f ? (int)y : (int)Math.floor(y)
        );
    }

    /**
     * 
     * @param worldTile
     * @return
     */
    public static Point worldTileToLocal(Point worldTile) {
        float x = (float)worldTile.x % CHUNK_SIZE;
        float y = (float)worldTile.y % CHUNK_SIZE;

        // Negative numbers should be higher than lower numbers.
        return new Point(
            x >= 0f ? (int)x : CHUNK_SIZE - Math.abs((int)Math.floor(x)),
            y >= 0f ? (int)y : CHUNK_SIZE - Math.abs((int)Math.floor(y))
        );
    }

    /**
     * 
     * @param tileSpace
     * @return
     */
    public Point worldTileToChunk(Point tileSpace) {
        float x = (float)tileSpace.x / CHUNK_SIZE;
        float y = (float)tileSpace.y / CHUNK_SIZE;

        // Negative numbers should be higher than lower numbers.
        return new Point(
            x >= 0f ? (int)x : (int)Math.floor(x),
            y >= 0f ? (int)y : (int)Math.floor(y)
        );
    }

    /**
     * Get the internal palette of this tilemap.
     * 
     * @return The internal palette.
     */
    public TilePalette getPalette() {
        return palette;
    }

    /**
     * Draw this tilemap and all layers that are currently within the camera rect on screen.
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
            TileBase[][] tileArray = chunk.get(layer);

            // Draw all of the tiles if present.
            for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                TileBase tile = tileArray[x][y];
                if (tile == null) continue;

                // Calculate the final render location.
                int stride = palette.stride();
                Point finalLoc = new Point(x, y).mult(stride).add(corner);
                //System.out.println(finalLoc);

                Rect tileRect = new Rect(finalLoc, new Point(0, 0));
                tileRect.setWH(new Point(stride, stride));

                RendererImage texture = palette.lookupTex(tile.tileIDs[0]);
                DrawInfo info = new DrawInfo(tileRect, d.rot, d.scale, d.drawLayer);
                fb.drawShaded(texture, info);
            }}
        }
    }
}
