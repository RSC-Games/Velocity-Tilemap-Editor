package appcode.scenes;

import java.awt.Color;

import appcode.*;
import appcode.ui.AnchoredUIImage;
import appcode.ui.CurrentTileEditorMode;
import appcode.ui.MemTracer;
import appcode.ui.TileEditorInfo;
import appcode.ui.TilemapCursor;
import velocity.util.*;
import velocity.*;
import velocity.sprite.ui.FPSCounter;

/**
 * Edit a selected tile in the TilemapEditor scene (selected in edit mode).
 */
// TODO: Lights from the main TileMap aren't hidden when entering tile edit mode.
public class TileEditor extends Scene {
    public TileEditor(String name, int uuid) {
        super(name, uuid);
        sprites.clear();

        /* Add sprites here that need to be added in multiple areas.
         * Keep in mind that these sprites will still need to be added to the scene list.
         */
        /* End of reusable sprite section. */

        /* Add sprites here (in sort order from 0 - array.length) */
        sprites.add(new LitBGTile(Point.zero, 0f, "Background Tile", "./res/bgtile.png"));
        sprites.add(new AreaLight(0.25f));
        /* Stop adding sprites here. */

        /* Baked collision data here (written in by hand by developer) */
        /* End baked collision data. */

        /* UI Panel here */
        sprites.add(new TileEditorManager());
        sprites.add(new TileRenderer());
        sprites.add(new TilemapCursor());
        sprites.add(new AnchoredUIImage("bottomleft", new Point(300, -120), "SelectedTile", "./res/no_tile.png"));
        sprites.add(new AnchoredUIImage("bottomleft", new Point(155, -115), "Controls", "./res/tile_editor/Controls.png"));
        //sprites.add(new TilemapControlsImage("bottomright", new Point(-155, -95), "Controls2", "./res/Controls2.png", "./res/Controls2_edit.png"));
        sprites.add(new FPSCounter(new Point(3, 12), 0, "FPS", Color.green));
        sprites.add(new MemTracer(new Point(3, 26), 0, "MemTracker", Color.green));
        sprites.add(new TileEditorInfo(new Point(3, 42), 0, "Tile Editor Info", Color.white));
        sprites.add(new CurrentTileEditorMode(new Point(3, 58), 0, "Current Mode", Color.white));
        //sprites.add(new TestUIElement(Point.zero, 0, "Thing", "./images/tutorial_knight.png"));
        /* End UI Panel */

        // Camera required for rendering. DO NOT FORGET!
        sprites.add(new UserCamera(Point.zero));
    }
}
