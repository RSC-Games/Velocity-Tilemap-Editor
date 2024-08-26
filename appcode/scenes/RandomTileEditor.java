package appcode.scenes;

import java.awt.Color;

import appcode.*;
import appcode.ui.AnchoredUIImage;
import appcode.ui.CenterPoint;
import appcode.ui.TilemapControlsImage;
import appcode.ui.MemTracer;
import appcode.ui.PointerLoc;
import appcode.ui.SavedTracker;
import appcode.ui.TileInfoPrint;
import appcode.ui.TilemapCursor;
import velocity.util.*;
import velocity.*;
import velocity.sprite.ui.FPSCounter;

/**
 * Edit a random tile. Random tiles are a special kind of rule tile that place a random
 * tile id when selected. Based on the animator tile editor.
 */
public class RandomTileEditor extends Scene {
    public RandomTileEditor(String name, int uuid) {
        super(name, uuid);
        sprites.clear();

        /* Add sprites here that need to be added in multiple areas.
         * Keep in mind that these sprites will still need to be added to the scene list.
         */
        /* End of reusable sprite section. */

        /* Add sprites here (in sort order from 0 - array.length) */
        sprites.add(new CenterPoint(Point.zero, 0f));
        sprites.add(new AreaLight(0.45f));
        /* Stop adding sprites here. */

        /* Baked collision data here (written in by hand by developer) */
        /* End baked collision data. */

        /* UI Panel here */
        sprites.add(new RandomTileCreationManager());
        sprites.add(new TilemapCursor());
        sprites.add(new AnchoredUIImage("bottomleft", new Point(300, -120), "SelectedTile", "./res/no_tile.png"));
        sprites.add(new AnchoredUIImage("bottomleft", new Point(155, -115), "Controls", "./res/Controls.png"));
        sprites.add(new TilemapControlsImage("bottomright", new Point(-155, -95), "Controls2", "./res/Controls2.png", "./res/Controls2_edit.png"));
        sprites.add(new SavedTracker("topright", new Point(-75, 25)));
        sprites.add(new FPSCounter(new Point(3, 12), 0, "FPS", Color.green));
        sprites.add(new MemTracer(new Point(3, 26), 0, "MemTracker", Color.green));
        sprites.add(new PointerLoc(new Point(3, 42), 0, "Cursor Loc", Color.yellow));
        sprites.add(new TileInfoPrint(new Point(3, 58), 0f, "Tile Info Printout", Color.white));
        //sprites.add(new TestUIElement(Point.zero, 0, "Thing", "./images/tutorial_knight.png"));
        /* End UI Panel */

        // Camera required for rendering. DO NOT FORGET!
        sprites.add(new UserCamera(Point.zero));
    }
}
