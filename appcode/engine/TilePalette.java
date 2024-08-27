package appcode.engine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

import velocity.renderer.RendererImage;
import velocity.system.Images;
import velocity.util.TextFile;

public class TilePalette {
    HashMap<Integer, RendererImage> tex2id;
    String folderPath;
    String assetPrefix;
    String fileExt;
    int stride = 0;

    // TODO: Palette GC?
    // TODO: Add stride to the palette file.
    /**
     * Load a tile palette from disk. Tile Palettes are stored like this:
     * PALETTE_PATH (first line, has the path to the palette folder)
     * PALETTE_PREFIX (second line, has the filename prefix)
     * FILE_EXT (third line, has the file extension for loading)
     * 
     * @param palettePath File path to the palette.
     */
    // TODO: Add matched key/value pairs for tile palettes and allow autopaletting.
    public TilePalette(String palettePath) {
        try {
            loadTilePaletteData(palettePath);
        }
        catch (IOException ie) {
            throw new RuntimeException(ie);
        }

        this.tex2id = new HashMap<Integer, RendererImage>();
    }

    private void loadTilePaletteData(String palettePath) throws IOException {
        TextFile paletteFile = new TextFile(palettePath, "r");
        this.folderPath = paletteFile.readLine().strip();
        this.assetPrefix = paletteFile.readLine().strip();
        this.fileExt = paletteFile.readLine().strip();
        paletteFile.close();
    }

    public RendererImage lookupTex(int texId) {
        // If the image is already loaded, pass that back to the tilemap.
        if (this.tex2id.containsKey(texId))
            return tex2id.get(texId);

        // Otherwise load it from disk.
        RendererImage texture = loadImage(texId);
        tex2id.put(texId, texture);
        
        // Update stride so the tilemap understands how to render these.
        if (stride == 0) stride = texture.getWidth();
        return texture;
    }

    public int parseTileID(String inPath) {
        // Convert this path to relative so the tileID can be parsed out.
        Path path = Path.of(inPath);
        Path cwd = Path.of(System.getProperty("user.dir"));
        String relPath = cwd.relativize(path).toString();

        if (!path.toFile().exists())
            return -1;
        
        // Get the tileID.
        String id = relPath.substring((folderPath + "/" + assetPrefix).length() - 2, relPath.length() - (fileExt.length() + 1));
        return Integer.parseInt(id);
    }

    /**
     * Get the tile size (in px) of this palette. If no tile size is known, compute it, then return
     * the computed tile size.
     * 
     * @return Tile size.
     */
    public int stride() {
        if (stride == 0)
            lookupTex(0);

        return stride;
    }

    public String getPath() {
        return this.folderPath;
    }

    private RendererImage loadImage(int texId) {
        return Images.loadImage(folderPath + "/" + assetPrefix + texId + "." + fileExt);
    }
}