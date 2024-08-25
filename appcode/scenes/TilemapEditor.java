package appcode.scenes;

import java.awt.Color;

import appcode.*;
import appcode.ui.AnchoredUIImage;
import appcode.ui.CenterPoint;
import appcode.ui.MemTracer;
import appcode.ui.PointerLoc;
import appcode.ui.SavedTracker;
import appcode.ui.TilemapCursor;
import velocity.util.*;
import velocity.*;
import velocity.sprite.ui.FPSCounter;

public class TilemapEditor extends Scene {
    public TilemapEditor(String name, int uuid) {
        super(name, uuid);
        sprites.clear();

        /* Add sprites here that need to be added in multiple areas.
         * Keep in mind that these sprites will still need to be added to the scene list.
         */
        /* End of reusable sprite section. */

        /* Add sprites here (in sort order from 0 - array.length) */
        sprites.add(new CenterPoint(Point.zero, 0f));
        sprites.add(new AreaLight(1f));
        /* Stop adding sprites here. */

        /* Baked collision data here (written in by hand by developer) */
        /* End baked collision data. */

        /* UI Panel here */
        sprites.add(new TilemapCreationManager());
        sprites.add(new TilemapCursor());
        sprites.add(new AnchoredUIImage("bottomleft", new Point(300, -120), "SelectedTile", "./res/no_tile.png"));
        sprites.add(new AnchoredUIImage("bottomleft", new Point(155, -85), "Controls", "./res/Controls.png"));
        sprites.add(new AnchoredUIImage("bottomright", new Point(-155, -120), "Controls2", "./res/Controls2.png"));
        sprites.add(new SavedTracker("topright", new Point(-50, 25)));
        sprites.add(new FPSCounter(new Point(3, 12), 0, "FPS", Color.green));
        sprites.add(new MemTracer(new Point(3, 26), 0, "MemTracker", Color.green));
        sprites.add(new PointerLoc(new Point(3, 40), 0, "Cursor Loc", Color.green));
        //sprites.add(new TestUIElement(Point.zero, 0, "Thing", "./images/tutorial_knight.png"));
        /* End UI Panel */

        // Camera required for rendering. DO NOT FORGET!
        sprites.add(new UserCamera(Point.zero));
    }
}
