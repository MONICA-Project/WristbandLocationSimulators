package main.java;

import java.lang.Math;

public class PositionENU {
    public double East;
    public double North;
    public double Up;

    public PositionENU() {
        this.East = 0;
        this.North = 0;
        this.Up = 0;
    }

    public void acquireECEFPositions(PositionECEF posECEF, PositionECEF refPos) {
        Position llhRef = new Position(0, 0);

        llhRef.AcquireECEFPosition(refPos);

        double phi = Math.toRadians(llhRef.Latitude);
        double lam = Math.toRadians(llhRef.Longitude);

        double dX = posECEF.X - refPos.X;
        double dY = posECEF.Y - refPos.Y;
        double dZ = posECEF.Z - refPos.Z;

        double sinphi = Math.sin(phi);
        double cosphi = Math.cos(phi);
        double sinlam = Math.sin(lam);
        double coslam = Math.cos(lam);
        // R = [ -sinlam coslam 0 ; ...
        // -sinphi*coslam -sinphi*sinlam cosphi; ...
        // cosphi*coslam cosphi*sinlam sinphi];
        // enu = R*difxyz;

        this.East = (-1 * sinlam * dX) + (coslam * dY);
        this.North = (cosphi * coslam * dX) + (cosphi * sinlam * dY) + (sinphi * dZ);

    }
}