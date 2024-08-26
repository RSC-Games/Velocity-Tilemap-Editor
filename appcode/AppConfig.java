package appcode;

import velocity.util.Point;
import velocity.config.GlobalAppConfig;

/** 
 * Configure Velocity to how it best suits your game.
 */
public class AppConfig extends GlobalAppConfig {
    public AppConfig() {
        APP_NAME = "Velocity Tilemap Editor";  // Name shown on the window.
        ICON_PATH = "./velocity/resources/rsc_games.ico";  // Default icon.
        APP_RES_DEFAULT = new Point(1280, 720);  // Sets default window resolution.
        WINDOW_RESIZABLE = true;  // Window resizes are fully supported.
        WINDOW_FULLSCREEN = false;  // Auto-starts the application in fullscreen.

        /********************** RENDERER CONFIG *************************/
        DEFAULT_RENDERER = "LumaViper";  // By default uses the LV Rendering Engine.
        RENDER_BACKEND = "OpenGL";  // Supported: CPU, OpenGL, DirectX11.
        ENABLE_ERP_FALLBACK = true;  // It's a tilemap editor. What could break?
        WARN_RENDERER_INIT_FAIL = true; 
        EN_DEBUG_RENDERER = false;  // Don't ship a build this way.
        REND_WORKER_COUNT = Runtime.getRuntime().availableProcessors() - 1; // CPUs

        /********************* SCENE LOAD CONFIG ************************/
        START_SCENE = "StartScene";  // Scene loaded by default by the engine.
        SCENE_LOAD_FAILURE_FATAL = true;  // By default failed scene loads will warn.

        /******************** WARNINGS AND ERRORS ***********************/
        MISSING_IMAGE_FATAL = false;  // Default to warning on missing images.
        WARNINGS_FATAL = false;  // Halt execution on warn messages.

        /************************ DEBUGGING *****************************/
        LOG_GC = true;  // Enable GC logging messages. Useful for engine debugging.
        LOG_MEMORY = true;  // Log memory allocation/deallocations for sprites.

        /********************* RENDERER DEBUGGING ***********************/
        SUPPRESS_UNSTABLE_RENDERER_WARNING = true;  // The unsupported renderer message is annoying.
        EN_RENDERER_LOGS = false;  // Enable renderer swapchain and draw messages.
        EN_RENDERER_PROFILER = false;  // Track drawtime.
        PROFILE_SHADERTIME = false;  // Not implemented.
    }
}
