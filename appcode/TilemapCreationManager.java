package appcode;

import java.awt.PopupMenu;
import java.awt.RenderingHints.Key;
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
import velocity.sprite.Sprite;
import velocity.system.Images;
import velocity.util.Point;
import velocity.util.Popup;
import velocity.util.TextFile;

public class TilemapCreationManager extends Sprite {
    String tilemapPath = null;
    String palettePath = null;
    boolean tilemapLoaded = false;
    boolean isSaved = true;

    TileMap map;
    AnchoredUIImage selectedTile;
    TilemapCursor cursor;
    int currentLayer = 0;
    int curTileID = -1;

    public TilemapCreationManager() {
        super(Point.zero, 0f, "Tilemap Creation Manager");
    }

    @Override
    public void init() {
        selectedTile = (AnchoredUIImage)Scene.currentScene.getSpriteByName("SelectedTile");
        cursor = Scene.currentScene.getSprite(TilemapCursor.class);
    }
    
    @Override
    public void tick() {
        // Once the paths have been found, if the in-game tilemap hasn't been loaded, then do that.
        if (!tilemapLoaded && tilemapPath != null && palettePath != null) {
            map = new TileMap("Editable Tilemap", tilemapPath, palettePath);
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

        if (palettePath == null) {
            // Find the tile palette to use.
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("filter_name", "Tile Palette (*.vpalette)");
            params.put("filter", "vpalette");
            params.put("default_name", "new_palette.vpalette");

            palettePath = searchForFile("Find Velocity file palette to edit...", System.getProperty("user.dir"), params);
            System.out.println("selected palette file " + palettePath);
        }

        // Try to load a tile if the user requests.
        if (InputSystem.getKeyDown(KeyEvent.VK_Q)) {
            String imgPath = searchForImage("Select a tile to use...", map.getPalette().getPath());
            System.out.println("selected img file " + imgPath);

            if (imgPath == null)
                return;

            // Extract the tileID from its path.
            curTileID = map.getPalette().parseTileID(imgPath);

            // Load the image then set the image texture.
            selectedTile.img = Images.loadImage(imgPath);
            selectedTile.pos.setWH(new Point(selectedTile.img.getWidth(), selectedTile.img.getHeight()));
            cursor.setHoverImage(selectedTile.img);
        }

        // Allow the user to place tiles.
        if (curTileID != -1 && InputSystem.clicked(MouseEvent.BUTTON1)) {
            // TODO: Make a way to set the objects as collidable.
            map.setTile(cursor.cursorLoc, currentLayer, new TileBase(new int[] {curTileID}, 0, false));
            isSaved = false;
        }

        // Remove tiles
        if (InputSystem.clicked(MouseEvent.BUTTON2)) {
            // TODO: Make a way to set the objects as collidable.
            map.setTile(cursor.cursorLoc, currentLayer, null);
            isSaved = false;
        }

        // Allow alternate commands.
        if (InputSystem.getKey(KeyEvent.VK_CONTROL) || InputSystem.getKey(GLFW.GLFW_KEY_LEFT_CONTROL)) {
            // Save this current file.
            if (InputSystem.getKeyDown(KeyEvent.VK_S) && !isSaved) {
                map.saveToFile(tilemapPath);
                System.out.println("Saved to file " + tilemapPath + "!");
                isSaved = true;
            }

            // Open a new file.
            if (InputSystem.getKeyDown(KeyEvent.VK_O)) {
                if (!isSaved) {
                    boolean proceed = Popup.query("Are you sure?", "You have unsaved work. Open anyway?");
                    if (!proceed) return;
                }

                Scene.scheduleSceneLoad("TilemapEditor");
            }
        }
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
}
