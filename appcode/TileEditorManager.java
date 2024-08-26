package appcode;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.lwjgl.glfw.GLFW;

import appcode.engine.TileBase;
import appcode.ui.AnchoredUIImage;
import jnafilechooser.api.JnaFileChooser;
import velocity.InputSystem;
import velocity.Scene;
import velocity.renderer.RendererImage;
import velocity.sprite.Sprite;
import velocity.system.Images;
import velocity.util.Persistence;
import velocity.util.Point;
import velocity.util.TextFile;

public class TileEditorManager extends Sprite {
    // Current editor state.
    boolean animMode = true;
    boolean lightMode = false;
    boolean collisionMode = false;
    boolean saved = true;

    // Prevent repeating clicks.
    boolean clickOnceLeft = false;
    boolean clickOnceRight = false;

    // Tilemap (not in the scene so not rendered).
    TileMapRW map;

    // Cursor images
    RendererImage currentTileImage;  // Currently paintable tile.

    // Animation frame images
    HashMap<Integer, RendererImage> frames;

    // Store selected palette image UID.
    AnchoredUIImage selectedTile;
    int tileID = -1;

    // Currently edited tile.
    TileRenderer renderer;

    public TileEditorManager() {
        super(Point.zero, 0f, "Tile Editor Manager");
    }

    // TODO: Show colliders in edit mode!
    @Override
    public void init() {
        selectedTile = (AnchoredUIImage)Scene.currentScene.getSpriteByName("SelectedTile");
        renderer = Scene.currentScene.getSprite(TileRenderer.class);
         
        // Fetch the tile from persistence, but leave the coordinates.
        saved = (boolean)Persistence.pop("FileIsSaved");
        renderer.setRenderedTile((TileBase)Persistence.pop("TileToEdit"));
        map = (TileMapRW)Persistence.pop("EditableTilemap");
        renderer.setPalette(map.getPalette());
    }
    
    @Override
    // NOTE: Reimplement as FSM?
    public void tick() {
        // Return to the tile menu (and assume tile is finished).
        if (InputSystem.getKeyUp(KeyEvent.VK_ESCAPE) || InputSystem.getKeyUp(GLFW.GLFW_KEY_ESCAPE)) {
            Persistence.push("EditableTilemap", map);
            Persistence.push("TileToEdit", renderer.getRenderedTile());
            Persistence.push("FileIsSaved", saved);

            Scene.scheduleSceneLoad("TilemapEditor");
        }

        // Action 1. (Animation mode: set tile at frame; No other modes.).
        if (InputSystem.clicked(MouseEvent.BUTTON1) && animMode && !clickOnceLeft) {
            // Set a tile if one is selected (Animation mode)
            if (this.tileID != -1) {
                renderer.setTileAtFrame(this.tileID);
                saved = false;
            }
        }

        // Shift + Left click: (Anim mode: set multiple tiles. No other modes.).
        // TODO: Shift + left click.

        // Disable multi-click prevention.
        if (InputSystem.released(MouseEvent.BUTTON1))
            clickOnceLeft = false;

        // Alt action (Anim mode: remove tile; Light mode: en/dis light; Collision: en/dis collider).
        if (InputSystem.released(MouseEvent.BUTTON3)) {
            if (animMode) {
                renderer.setTileAtFrame(-1);
                saved = false;
            }
        }

        // Change the wait duration for the animation.
        if (InputSystem.getKeyUp(KeyEvent.VK_W) && animMode) {
            int newWaitTime = popupGetInt("How long should each animation frame show on screen (in milliseconds)?", 
                                          0, Integer.MAX_VALUE, 150);

            renderer.setWaitTime(newWaitTime);
            saved = false;
        }

        // Play/pause the animation.
        if (InputSystem.getKeyDown(KeyEvent.VK_SPACE)) {
            renderer.play(!renderer.isPlaying());
        }

        // Change the current frame (if in anim mode).
        // TODO: Gray out the other layers.
        if (InputSystem.getKeyDown(KeyEvent.VK_EQUALS) && animMode)  // Plus key
            renderer.incrementFrame();
        else if (InputSystem.getKeyDown(KeyEvent.VK_MINUS) && animMode)
            renderer.decrementFrame();

        // Allow alternate commands.
        if (InputSystem.getKey(KeyEvent.VK_CONTROL) || InputSystem.getKey(GLFW.GLFW_KEY_LEFT_CONTROL)) {
            // Try to load a tile if the user requests.
            if (InputSystem.getKeyDown(KeyEvent.VK_T)) {
                String imgPath = searchForImage("Select a tile to use...", map.getPalette().getPath());
                System.out.println("selected img file " + imgPath);

                if (imgPath == null)
                    return;

                // Extract the tileID from its path.
                tileID = map.getPalette().parseTileID(imgPath);

                if (tileID == -1)
                    return;

                // Load the image then set the image texture.
                selectedTile.img = Images.loadImage(imgPath);
                selectedTile.pos.setWH(selectedTile.img.getWidth(), selectedTile.img.getHeight());
                currentTileImage = selectedTile.img;
            }

            // Copy the current tile ID. Only in anim mode.
            if (InputSystem.getKeyDown((KeyEvent.VK_C)) && animMode) {
                this.tileID = renderer.getTileIDAtFrame();

                // Only eyedropper the tile if one is available.
                if (tileID != -1) {
                    RendererImage tileImage = renderer.getTileTextureAtFrame();

                    // Load the image then set the image texture.
                    selectedTile.img = tileImage;
                    selectedTile.pos.setWH(selectedTile.img.getWidth(), selectedTile.img.getHeight());
                    currentTileImage = selectedTile.img;
                }
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

    private int popupGetInt(String message, int min, int max, int initial) {
        while (true) {
            String res = JOptionPane.showInputDialog(message, initial);

            // Empty string indicates a cancel event.
            if (res == "") return -1;

            // Sanity checking.
            try {
                int val = Integer.parseInt(res);

                if (val >= min && val < max)
                    return val;
            }
            catch (NumberFormatException ie) {}
        }
    }

    public boolean isAnimMode() {
        return animMode;
    }

    public boolean isLightingMode() {
        return lightMode;
    }

    public boolean isColliderMode() {
        return collisionMode;
    }
}
