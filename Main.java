// Application main code. Must call into VelocityMain.
// NOT PART OF VELOCITY!

import velocity.VelocityMain;
import velocity.util.Version;
import appcode.AppConfig;
import appcode.SceneDefs;

public class Main {
    public static void main(String[] args) {
        // Require a minimum Velocity version for running.
        Version velocityVersion = new Version(0, 6, 3, 0);
        if (velocityVersion.isNewer(VelocityMain.VELOCITY_VER)
            && !velocityVersion.equals(VelocityMain.VELOCITY_VER)) {
            
            System.err.println("ERROR! Provided Velocity binary is too old!");
            System.err.println("Requested " + velocityVersion + ", got " 
                               + VelocityMain.VELOCITY_VER + "!");
            System.err.println("Fatal. Exiting application.");
            System.exit(1);
        }

        // Versions of velocity that are too new may pose a compatibility issue.
        Version unsupportedVersion = new Version(0, 6, 4, 0);
        if (unsupportedVersion.isOlder(VelocityMain.VELOCITY_VER)) {
            System.err.println("ERROR! Provided Velocity version is unsupported!");
            System.err.println("Got " + VelocityMain.VELOCITY_VER + ", minimum supported "
                               + velocityVersion + "!");
            System.err.println("Fatal. Exiting application.");
            System.exit(1);
        }

        // Run the application.
        VelocityMain.app_main(new AppConfig(), new SceneDefs());
    }
}