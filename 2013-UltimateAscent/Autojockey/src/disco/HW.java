package disco;

import edu.wpi.first.wpilibj.Preferences;

/**
 * The HW is a mapping from the ports sensors and actuators are wired into to a
 * variable name. This provides flexibility changing wiring, makes checking the
 * wiring easier and significantly reduces the number of magic numbers floating
 * around.
 */
public class HW {

    /**
     * Hardware
     */
    //REGULAR 3CIM GEARBOXES. MUST UPDATE VALUES AFTER LSR
    public static final double wheelRadius = 1.985;//in
    public static final double wheelSeparation = 19;//in
    public static final double robotLength = 40;//in //KNOWN INCORRECT
    public static final double driveReduction = 15.0 / 24.0;  //NOT EXACT //wheel side : encoder side
    public static final double maxFPS = 7;    //KNOWN INCORRECT
    public static final double encoderTicks = 128;//should be 256? is WPIlib doing something wrong? but 128 works.
    public static final float wheel_slip_factor = 1 + 5 / 240;
    //CUSTOM 3CIM SHIFTING GEARBOXES. FIND VALUES.
//    public static final double wheelRadius=2.0;//in
//    public static final double wheelSeparation=18.0;//in
//    public static final double driveReduction=0.5;
//    public static final double maxFPS=9;    //TODO: Test
    //DON'T NEED TO CHANGE THESE
    public static final double encoderTicksPerRev = encoderTicks / driveReduction;
    public static final double distancePerRev = 2 * Math.PI * wheelRadius;
    public static final double distancePerTick = distancePerRev / encoderTicksPerRev;
    public static final double encoderDegreesPerRev = 360 / driveReduction;
    /**
     * ------------------------------------------------------- Motors
     * -------------------------------------------------------
     */
    public static final int LeftDrive1Channel = 1, LeftDrive1Slot = 1,
            LeftDrive2Channel = 2, LeftDrive2Slot = 1,
            RightDrive1Channel = 3, RightDrive1Slot = 1,
            RightDrive2Channel = 4, RightDrive2Slot = 1,
            ShooterFrontChannel = 5, ShooterFrontSlot = 1, // 6
            ShooterBackChannel = 6, ShooterBackSlot = 1; // 4
    /**
     * ------------------------------------------------------- Relays
     * -------------------------------------------------------
     */
    public static final int compressorChannel = 8, compressorSlot = 1;
    /**
     * ------------------------------------------------------- Solenoids
     * -------------------------------------------------------
     */
    public static final int shootPneumaticChannel = 6,
            clearShooterChannel = 5,
            encoder1PowerChannel = 7,
            encoder2PowerChannel = 8,
            leftShiftChannel = 2,
            rightShiftChannel = 3;
    /**
     * ------------------------------------------------------- Sensor
     * ------------------------------------------------------
     */
    public static final int //Digital
            leftEncoderAChannel = 8,
            leftEncoderBChannel = 9, leftEncoderSlot = 1,
            rightEncoderAChannel = 4,
            rightEncoderBChannel = 5, rightEncoderSlot = 1,
            shooterEncoderFrontChannel = 1, shooterEncoderFrontSlot = 1,
            shooterEncoderBackChannel = 2, shooterEncoderBackSlot = 1,
            pressureSwitchChannel = 14, pressureSwitchSlot = 1,
            limitSwitchLeftChannel = 7, limitSwitchLeftSlot = 1,
            limitSwitchRightChannel = 12, limitSwitchRightSlot = 1,
            arduinoChannel = 2, arduinoSlot = 2,
            //Analog
            gyroChannel = 1, gyroSlot = 1,
            frontsonarChannel = 2, frontsonarSlot = 1,
            leftsonarChannel = 3, leftsonarSlot = 1,
            rightsonarChannel = 4, rightsonarSlot = 1,
            backsonarChannel = 5, backsonarSlot = 1;
}