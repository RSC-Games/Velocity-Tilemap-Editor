package appcode;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.lwjgl.glfw.GLFW;

import appcode.engine.TileBase;
import appcode.engine.TileMap;
import appcode.ui.AnchoredUIImage;
import appcode.ui.TilemapCursor;
import jnafilechooser.api.JnaFileChooser;
import velocity.InputSystem;
import velocity.Scene;
import velocity.renderer.RendererImage;
import velocity.sprite.Sprite;
import velocity.system.Images;
import velocity.util.Persistence;
import velocity.util.Point;
import velocity.util.Popup;
import velocity.util.TextFile;

public class TilemapCreationManager extends Sprite {
    String tilemapPath = null;
    String tileDataPath = null;
    String tilePalettePath = null;
    boolean tilemapLoaded = false;
    boolean isSaved = true;

    // Current editor state.
    boolean editMode = false;
    boolean grabTileMode = false;
    boolean paletteMode = false;

    // Prevent repeating clicks.
    boolean clickOnceLeft = false;
    boolean clickOnceRight = false;

    // Cursor images
    RendererImage currentTileImage;  // Currently paintable tile.
    RendererImage eraser;  // Erase tiles.
    RendererImage eyedropper;  // Grab tile mode.
    RendererImage wrench;  // Edit mode.

    // NOTE: Palette map stored in persistence.
    TileMapRW map;
    TileMap paletteMap;
    AnchoredUIImage selectedTile;
    TilemapCursor cursor;
    int currentLayer = 0;
    TileBase curTile = null;
    AreaLight sceneLighting = null;
    UserCamera mainCamera = null;

    public TilemapCreationManager() {
        super(Point.zero, 0f, "Tilemap Creation Manager");
        this.eraser = Images.loadImage("./res/eraser.png");
        this.eyedropper = Images.loadImage("./res/eyedropper.png");
        this.wrench = Images.loadImage("./res/wrench.png");
    }

    @Override
    public void init() {
        selectedTile = (AnchoredUIImage)Scene.currentScene.getSpriteByName("SelectedTile");
        sceneLighting = Scene.currentScene.getSprite(AreaLight.class);
        cursor = Scene.currentScene.getSprite(TilemapCursor.class);
        mainCamera = Scene.currentScene.getSprite(UserCamera.class);

        // If the tilemap is already on persistence then fetch it back.
        if (Persistence.isPresent("EditableTilemap")) {
            map = (TileMapRW)Scene.currentScene.restoreFromPersistence("EditableTilemap");
            tilemapPath = (String)Persistence.pop("StashedTilemapPath");
            tileDataPath = (String)Persistence.pop("StashedTileDataPath");
            tilePalettePath = "";  // Prevent it from querying again.
            tilemapLoaded = true;

            // Restore the tile from persistence as well and write it back to the tilemap.
            TileBase updatedTile = (TileBase)Persistence.pop("TileToEdit");
            Point worldPos = (Point)Persistence.pop("TileCoords");
            currentLayer = (int)Persistence.pop("TileLayer");
            isSaved = (boolean)Persistence.pop("FileIsSaved");
            map.setTile(worldPos, currentLayer, updatedTile);
            cursor.setHoverImage(wrench);
            editMode = true;
        }
    }
    
    // TODO: Clean up (move actions to their own functions).
    @Override
    public void tick() {
        // Once the paths have been found, if the in-game tilemap hasn't been loaded, then do that.
        if (!tilemapLoaded && tilemapPath != null && tileDataPath != null && tilePalettePath != null) {
            map = new TileMapRW("Editable Tilemap", tilemapPath, tileDataPath);
            paletteMap = new TileMap("Tile Palette", tilePalettePath, tileDataPath);
            Scene.currentScene.addSprite(cursor);
            Scene.currentScene.moveToPersistence("TilemapPalette", paletteMap);
            Scene.currentScene.addSprite(map);
            map.sortOrder = -5;
            tilemapLoaded = true;
        }

        if (tilemapPath == null) {
            // Look for the tilemap file on disk.
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("filter_name", "Tilemap File (*.csv)");
            params.put("filter", "csv");
            params.put("default_name", "new_tilemap.csv");

            tilemapPath = searchForFile("Find Velocity tilemap file to edit...", System.getProperty("user.dir"), params);
            System.out.println("selected tilemap path " + tilemapPath);
        }

        if (tileDataPath == null) {
            // Find the tile data.
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("filter_name", "Tile Map Data (*.vtiledata)");
            params.put("filter", "vtiledata");
            params.put("default_name", "new_tilemap_data.vtiledata");

            tileDataPath = searchForFile("Find Velocity tilemap data file...", System.getProperty("user.dir"), params);
            System.out.println("selected tile data file " + tileDataPath);
        }

        if (tilePalettePath == null) {
            // Find the tile palette to use.
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("filter_name", "Tile Palette (*.vpalette)");
            params.put("filter", "vpalette");
            params.put("default_name", "new_palette.vpalette");

            tilePalettePath = searchForFile("Find Velocity tile palette...", System.getProperty("user.dir"), params);
            System.out.println("selected palette file " + tilePalettePath);
        }

        // Allow the user to place tiles.
        if (InputSystem.clicked(MouseEvent.BUTTON1) && !clickOnceLeft) {
            // Allow modification of placed tiles.
            if (editMode) {
                // Grab tile to edit, then stash it.
                TileBase tileToEdit = map.getTile(cursor.cursorLoc, currentLayer);
                Point tileLoc = cursor.cursorLoc;

                // Can't edit a tile that doesn't exist!
                if (tileToEdit == null)
                    return;

                Persistence.push("TileToEdit", tileToEdit);
                Persistence.push("TileCoords", tileLoc);
                Persistence.push("TileLayer", currentLayer);
                Persistence.push("FileIsSaved", isSaved);

                // Stash the tilemap as well and crucial info.
                Scene.currentScene.moveToPersistence("EditableTilemap", map);
                Persistence.push("StashedTilemapPath", tilemapPath);
                Persistence.push("StashedTileDataPath", tileDataPath);

                Scene.scheduleSceneLoad("TileEditor");
            }
            else if (paletteMode) {
                TileBase tile = paletteMap.getTile(cursor.cursorLoc, 0);

                if (tile != null) {
                    // Extract the tileID from its path.
                    curTile = tile;

                    // Load the image then set the image texture.
                    selectedTile.img = paletteMap.getPalette().lookupTex(tile.tileIDs[0]);
                    selectedTile.pos.setWH(new Point(selectedTile.img.getWidth(), selectedTile.img.getHeight()));
                    currentTileImage = selectedTile.img;
                    cursor.setHoverImage(currentTileImage);
                    paletteMode = false;
                    clickOnceLeft = true;

                    // Switch current map back to the main map.
                    Scene.currentScene.moveToPersistence("TilemapPalette", paletteMap);
                    Scene.currentScene.restoreFromPersistence("EditableTilemap");
                    sceneLighting.setIntensity(0.45f);
                    mainCamera.enterTilemapMode();
                }
            }
            else if (grabTileMode) {
                TileBase tile = map.getTile(cursor.cursorLoc, currentLayer);

                // Only eyedropper the tile if one is available.
                if (tile != null) {
                    // Extract the tileID from its path.
                    curTile = tile;

                    // Load the image then set the image texture.
                    selectedTile.img = map.getPalette().lookupTex(tile.tileIDs[0]);
                    selectedTile.pos.setWH(new Point(selectedTile.img.getWidth(), selectedTile.img.getHeight()));
                    currentTileImage = selectedTile.img;
                    cursor.setHoverImage(currentTileImage);
                    grabTileMode = false;
                    clickOnceLeft = true;
                }
            }
            else if (curTile != null) {
                map.setTile(cursor.cursorLoc, currentLayer, curTile);
                cursor.setHoverImage(currentTileImage);
                isSaved = false;
            }
        }

        // Disable multi-click prevention.
        if (InputSystem.released(MouseEvent.BUTTON1))
            clickOnceLeft = false;

        // Remove tiles
        if (InputSystem.clicked(MouseEvent.BUTTON3) && !clickOnceRight) {
            if (editMode) {
                // Set collidable state.
                TileBase curTile = map.getTile(cursor.cursorLoc, currentLayer);
                if (curTile == null) return;

                map.setTile(cursor.cursorLoc, currentLayer, new TileBase(curTile.tileIDs, curTile.waitTime, !curTile.isCollidable));
                clickOnceRight = true;
            }
            else {
                map.setTile(cursor.cursorLoc, currentLayer, null);
                cursor.setHoverImage(eraser);
            }

            isSaved = false;
        }

        // Disable multi-click prevention.
        if (InputSystem.released(MouseEvent.BUTTON3))
            clickOnceRight = false;

        // Enter/exit edit mode.
        if (InputSystem.getKeyDown(KeyEvent.VK_TAB) || InputSystem.getKeyDown(GLFW.GLFW_KEY_TAB)) {
            editMode = !editMode;
            map.setDrawColliders(editMode);
            cursor.setHoverImage(editMode ? wrench : currentTileImage);
        }

        // Raise/lower the current layer.
        if (InputSystem.getKeyDown(KeyEvent.VK_Q)) {
            currentLayer--;
            map.setCurrentLayer(currentLayer);
        }
        if (InputSystem.getKeyDown(KeyEvent.VK_E)) {
            currentLayer++;
            map.setCurrentLayer(currentLayer);
        }

        // Allow alternate commands.
        if (InputSystem.getKey(KeyEvent.VK_CONTROL) || InputSystem.getKey(GLFW.GLFW_KEY_LEFT_CONTROL)) {
            // Open the palette and allow the user to pick a tile.
            if (!InputSystem.getKey(KeyEvent.VK_SHIFT) && InputSystem.getKeyDown(KeyEvent.VK_T)) {
                // Swap out the current rendering map for the palette map.
                Scene.currentScene.moveToPersistence("EditableTilemap", map);  // move old map here.
                Scene.currentScene.restoreFromPersistence("TilemapPalette");
                sceneLighting.setIntensity(1f);
                cursor.setHoverImage(eyedropper);
                mainCamera.enterPaletteMode();
                paletteMode = true;
            }

            // Try to load a tile from disk if the user requests.
            else if (InputSystem.getKey(KeyEvent.VK_SHIFT) && InputSystem.getKeyDown(KeyEvent.VK_T)) {
                String imgPath = searchForImage("Select a tile to use...", map.getPalette().getPath());
                System.out.println("selected img file " + imgPath);

                if (imgPath == null)
                    return;

                // Extract the tileID from its path.
                int tileID = map.getPalette().parseTileID(imgPath);

                if (tileID == -1)
                    return;

                // Enable collision by default on wall tiles.
                curTile = new TileBase(new int[] {tileID}, 150, currentLayer > 0 ? true : false);

                // Load the image then set the image texture.
                selectedTile.img = Images.loadImage(imgPath);
                selectedTile.pos.setWH(new Point(selectedTile.img.getWidth(), selectedTile.img.getHeight()));
                currentTileImage = selectedTile.img;
                cursor.setHoverImage(editMode ? wrench : currentTileImage);

                // Exit eyedroppers if present to prevent weird bugs.
                grabTileMode = false;
            }

            // Save this current file.
            if (InputSystem.getKeyDown(KeyEvent.VK_S) && !isSaved)
                saveMap();

            // Open a new file.
            if (InputSystem.getKeyDown(KeyEvent.VK_O)) {
                if (!isSaved) {
                    boolean proceed = Popup.query("Are you sure?", "You have unsaved work. Open anyway?");
                    if (!proceed) return;
                }

                Scene.scheduleSceneLoad("TilemapEditor");
            }

            // Enter eyedropper mode. (Forbid in edit mode or palette select mode).
            if (InputSystem.getKeyDown((KeyEvent.VK_G)) && !editMode && !paletteMode) {
                cursor.setHoverImage(eyedropper);
                grabTileMode = true;
            }
        }
    }

    private void saveMap() {
        map.saveToFile(tilemapPath);
        System.out.println("Saved to file " + tilemapPath + "!");
        cursor.setHoverImage(null);
        isSaved = true;
    }

    public String searchForFile(String title, String path, HashMap<String, String> params) {
        // Find the tilemap to use.
        JnaFileChooser fileChooser = new JnaFileChooser(path);
        fileChooser.setCurrentDirectory(path);
        fileChooser.addFilter(params.get("filter_name"), params.get("filter"));
        fileChooser.setDefaultFileName(params.get("default_name"));
        fileChooser.setTitle(title);
        fileChooser.showOpenDialog(null);

        File found = fileChooser.getSelectedFile();
        String filePath = found.getPath();

        // Create the file on disk if not already present.
        if (!found.exists()) {
            try {
                TextFile file = new TextFile(filePath, "w");
                file.close();
            }
            catch (IOException ie) {
                throw new RuntimeException(ie);
            } 
        }

        return filePath;
    }

    // TODO: Allow loading of rule tiles from storage.

    public String searchForImage(String title, String path) {
        // Find the image to use.
        JnaFileChooser fileChooser = new JnaFileChooser(new File(path));
        fileChooser.addFilter("Images (.jpg, .png, .bmp)", "png", "jpg", "bmp");

        fileChooser.setTitle(title);
        fileChooser.showOpenDialog(null);

        File found = fileChooser.getSelectedFile();

        if (found == null)
            return null;

        String filePath = found.getPath();

        return filePath;
    }

    public boolean isSaved() {
        return this.isSaved;
    }

    public TileBase lookupTile(int layer) {
        return map.getTile(cursor.cursorLoc, currentLayer);
    }

    public int getLayer() {
        return this.currentLayer;
    }

    public boolean inEditMode() {
        return this.editMode;
    }
}
