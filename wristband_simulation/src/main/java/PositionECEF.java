package main.java;

import java.lang.Math;

public class PositionECEF {
    public static final double PI = 3.141592654;
    public static final double a = 6378137.0000; // earth semimajor axis in meters
    public static final double b = 6356752.3142; // earth semiminor axis in meters

    public double X;
    public double Y;
    public double Z;

    public PositionECEF(Position LLHRefPos) {
        this.X = 0;
        this.Y = 0;
        this.Z = 0;

        // double value = Matrix.getConstantTest();

        // this.Z = value;

        AcquireLLHPosition(LLHRefPos);
    }

    public PositionECEF() {
        this.X = 0;
        this.Y = 0;
        this.Z = 0;
    }

    public static double convertDegreeToRad(double degree) {
        return degree * PI / 180.0;
    }

    public void AcquireLLHPosition(Position LLHRefPos) {
        double phi = PositionECEF.convertDegreeToRad(LLHRefPos.Latitude);
        double lambda = PositionECEF.convertDegreeToRad(LLHRefPos.Longitude);
        double h = LLHRefPos.Height;

        double e = Math.sqrt(1 - ((b / a) * (b / a)));

        double sinphi = Math.sin(phi);
        double cosphi = Math.cos(phi);
        double coslam = Math.cos(lambda);
        double sinlam = Math.sin(lambda);
        double tan2phi = (Math.tan(phi));

        tan2phi = tan2phi * tan2phi;

        double tmp = 1 - e * e;
        double tmpden = Math.sqrt(1 + tmp * tan2phi);

        this.X = (a * coslam) / tmpden + h * coslam * cosphi;

        this.Y = (a * sinlam) / tmpden + h * sinlam * cosphi;

        double tmp2 = Math.sqrt(1 - e * e * sinphi * sinphi);
        this.Z = (a * tmp * sinphi) / tmp2 + h * sinphi;

    }

    public void AcquireENUPosition(PositionENU enuPosition, PositionECEF origin) {
        Position originLLH = new Position(0, 0);

        originLLH.AcquireECEFPosition(origin);

        double phi = PositionECEF.convertDegreeToRad(originLLH.Latitude);
        double lambda = PositionECEF.convertDegreeToRad(originLLH.Longitude);
        double h = originLLH.Height;

        double e = Math.sqrt(1 - ((b / a) * (b / a)));

        double sinphi = Math.sin(phi);
        double cosphi = Math.cos(phi);
        double coslam = Math.cos(lambda);
        double sinlam = Math.sin(lambda);
        double tan2phi = (Math.tan(phi));

        tan2phi = tan2phi * tan2phi;

        double tmp = 1 - e * e;
        double tmpden = Math.sqrt(1 + tmp * tan2phi);
        double[][] referenceMatrix = new double[3][3];

        referenceMatrix[0][0] = -1 * sinlam;
        referenceMatrix[0][1] = coslam;
        referenceMatrix[0][2] = 0;
        referenceMatrix[1][0] = -1 * sinphi * coslam;
        referenceMatrix[1][1] = -1 * sinphi * sinlam;
        referenceMatrix[1][2] = cosphi;
        referenceMatrix[2][0] = cosphi * coslam;
        referenceMatrix[2][1] = cosphi * sinlam;
        referenceMatrix[2][2] = sinphi;

        double[] enuVector = new double[3];

        enuVector[0] = enuPosition.East;
        enuVector[1] = enuPosition.North;
        enuVector[2] = enuPosition.Up;

        double[][] inverseReferenceMatrix = Matrix.inverse(referenceMatrix);

        double[] diffECEF = Matrix.multiplyWithVector(inverseReferenceMatrix, enuVector);

        this.X = origin.X + diffECEF[0];
        this.Y = origin.Y + diffECEF[1];
        this.Z = origin.Z + diffECEF[2];
    }

}