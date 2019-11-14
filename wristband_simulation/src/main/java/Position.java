package main.java;

import java.lang.Math;

public class Position {
    public double Latitude;
    public double Longitude;
    public double Height;

    public Position(double Latitude, double Longitude) {
        this.Latitude = Latitude;
        this.Longitude = Longitude;
        this.Height = 0;
    }

    public Position(Position position) {
        this.Latitude = position.Latitude;
        this.Longitude = position.Longitude;
        this.Height = position.Height;
    }

    public void AcquireECEFPosition(PositionECEF posECEF) {
        double x2 = posECEF.X * posECEF.X;
        double y2 = posECEF.Y * posECEF.Y;
        double z2 = posECEF.Z * posECEF.Z;

        double e = Math.sqrt(1 - ((PositionECEF.b / PositionECEF.a) * (PositionECEF.b / PositionECEF.a)));
        double b2 = PositionECEF.b * PositionECEF.b;
        double e2 = e * e;
        double ep = e * (PositionECEF.a / PositionECEF.b);
        double r = Math.sqrt(x2 + y2);
        double r2 = r * r;
        double E2 = (PositionECEF.a * PositionECEF.a) - (PositionECEF.b * PositionECEF.b);
        double F = 54 * b2 * z2;
        double G = r2 + (1 - e2) * z2 - e2 * E2;
        double c = (e2 * e2 * F * r2) / (G * G * G);
        double s = Math.pow(1 + c + Math.sqrt(c * c + 2 * c), 1 / 3);
        double P = F / (3 * ((s + 1 / s + 1) * (s + 1 / s + 1)) * (G * G));
        double Q = Math.sqrt(1 + 2 * e2 * e2 * P);
        double ro = -(P * e2 * r) / (1 + Q) + Math.sqrt(
                (PositionECEF.a * PositionECEF.a / 2) * (1 + 1 / Q) - (P * (1 - e2) * z2) / (Q * (1 + Q)) - P * r2 / 2);
        double tmp = (r - e2 * ro) * (r - e2 * ro);
        double U = Math.sqrt(tmp + z2);
        double V = Math.sqrt(tmp + (1 - e2) * z2);
        double zo = (b2 * posECEF.Z) / (PositionECEF.a * V);

        this.Height = U * (1 - b2 / (PositionECEF.a * V));

        this.Latitude = Math.atan((posECEF.Z + ep * ep * zo) / r);

        double temp = Math.atan(posECEF.Y / posECEF.X);
        if (posECEF.X >= 0)
            this.Longitude = temp;
        else if (posECEF.X < 0 && posECEF.Y >= 0)
            this.Longitude = Math.PI + temp;
        else
            this.Longitude = temp - Math.PI;

        this.Latitude = Math.toDegrees(this.Latitude);
        this.Longitude = Math.toDegrees(this.Longitude);

    }

    Position CalculateNewPositionWith2DDistance(PositionENU enuPosition) {
        PositionECEF positionECEF = new PositionECEF(this);
        Position llhNewPosition = new Position(0, 0);

        positionECEF.AcquireENUPosition(enuPosition, positionECEF);

        llhNewPosition.AcquireECEFPosition(positionECEF);

        return llhNewPosition;
    }

}