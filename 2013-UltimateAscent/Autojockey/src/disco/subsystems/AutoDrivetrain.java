/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package disco.subsystems;

import disco.HW;
import disco.utils.DiscoGyro;
import disco.utils.FeatureReporter;
import disco.utils.MaxbotixSonar;
import disco.utils.MaxbotixSonarYellowDot;
import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.command.Subsystem;
import lejos.FRC.GyroPilot;
import lejos.FRC.OdometryGyroPoseProvider;
import lejos.FRC.RegulatedDrivetrain;
import lejos.geom.Line;
import lejos.geom.Rectangle;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.mapping.LineMap;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.objectdetection.FeatureDetector;
import lejos.robotics.objectdetection.FusorDetector;
import lejos.robotics.objectdetection.RangeFeatureDetector;
import lejos.robotics.pathfinding.Path;

public class AutoDrivetrain extends Subsystem {

    //Arbitrary constants
    private int accel = 7;//in/sec/sec
    private double max_speed = 40;
    //Motors
    private Victor leftDrive1;
    private Victor leftDrive2;
    private Victor rightDrive1;
    private Victor rightDrive2;
    private RobotDrive manualDrive;
    //Sonars
    private MaxbotixSonar frontSonar;
    private MaxbotixSonar leftSonar;
    private MaxbotixSonar rightSonar;
    private MaxbotixSonar backSonar;
    private FusorDetector sonars;
    //Encoders
    private Encoder leftEncoder;
    private Encoder rightEncoder;
    //Gyro
    private DiscoGyro gyro;
    //leJOS stuff
    public RegulatedDrivetrain leftDrive, rightDrive;
    private GyroPilot pilot;
    private OdometryPoseProvider op;
    private Navigator nav;//Formerly NavPathController
    //leJOS navigation objects
    private LineMap env;
    public Path default_path;

    public AutoDrivetrain() {
	super("Drivetrain");
	//Create motor and encoder objects
	motorsInit();
	encodersInit();
	gyroInit();
	leJOSDriveInit();
	sonarsInit();
	generateMap();
	generatePath();
    }

    public void initDefaultCommand() {
    }

    private void motorsInit() {
	leftDrive1 = new Victor(HW.LeftDrive1Slot, HW.LeftDrive1Channel);
	leftDrive2 = new Victor(HW.LeftDrive2Slot, HW.LeftDrive2Channel);
	rightDrive1 = new Victor(HW.RightDrive1Slot, HW.RightDrive1Channel);
	rightDrive2 = new Victor(HW.RightDrive2Slot, HW.RightDrive2Channel);
        manualDrive=new RobotDrive(leftDrive1,leftDrive2,rightDrive1,rightDrive2);
        manualDrive.setInvertedMotor(RobotDrive.MotorType.kFrontRight, true);
	manualDrive.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);
	manualDrive.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);
	manualDrive.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
        manualDrive.setSafetyEnabled(false);
    }

    private void encodersInit() {
	leftEncoder = new Encoder(HW.leftEncoderSlot, HW.leftEncoderAChannel,
		HW.leftEncoderSlot, HW.leftEncoderBChannel, false, EncodingType.k1X);
	rightEncoder = new Encoder(HW.rightEncoderSlot, HW.rightEncoderAChannel,
		HW.rightEncoderSlot, HW.rightEncoderBChannel, false, EncodingType.k1X);
	leftEncoder.setDistancePerPulse(HW.distancePerTick);
	leftEncoder.setReverseDirection(true);
	rightEncoder.setDistancePerPulse(HW.distancePerTick);
	leftEncoder.start();
	rightEncoder.start();
    }

    private void sonarsInit() {
        //Set up sonars
	frontSonar = new MaxbotixSonarYellowDot(HW.frontsonarSlot, HW.frontsonarChannel, MaxbotixSonar.Unit.kInches);
        leftSonar = new MaxbotixSonar(HW.leftsonarSlot,HW.leftsonarChannel,MaxbotixSonar.Unit.kInches);
        rightSonar = new MaxbotixSonar(HW.rightsonarSlot,HW.rightsonarChannel,MaxbotixSonar.Unit.kInches);
        backSonar = new MaxbotixSonar(HW.backsonarSlot,HW.backsonarChannel,MaxbotixSonar.Unit.kInches);
	//set up detectors
        RangeFeatureDetector frontSonar_detector=new RangeFeatureDetector(frontSonar,(float)frontSonar.MAX_PEOPLE_RANGE,1000,0);
        RangeFeatureDetector leftSonar_detector=new RangeFeatureDetector(leftSonar,(float)leftSonar.MAX_PEOPLE_RANGE,1000,90);
        RangeFeatureDetector rightSonar_detector=new RangeFeatureDetector(rightSonar,(float)rightSonar.MAX_PEOPLE_RANGE,1000,-90);
        RangeFeatureDetector backSonar_detector=new RangeFeatureDetector(backSonar,(float)backSonar.MAX_PEOPLE_RANGE,1000,180);
        //set up fusor
        sonars=new FusorDetector();
	sonars.addDetector(frontSonar_detector);
	sonars.addDetector(leftSonar_detector);
	sonars.addDetector(rightSonar_detector);
	sonars.addDetector(backSonar_detector);
        //set up listening
	sonars.setPoseProvider(op);
	sonars.enableDetection(true);
        sonars.addListener(new FeatureReporter());
    }

    private void gyroInit(){
	gyro = new DiscoGyro(HW.gyroSlot, HW.gyroChannel);
	gyro.setSensitivity(0.007);
    }

    private void leJOSDriveInit() {
	leftDrive = new RegulatedDrivetrain(leftDrive1, leftDrive2, true, true, leftEncoder, HW.encoderTicksPerRev);
	rightDrive = new RegulatedDrivetrain(rightDrive1, rightDrive2, false, false, rightEncoder, HW.encoderTicksPerRev);

	pilot = new GyroPilot(gyro,(float)(2 * HW.wheelRadius),(float) HW.wheelSeparation, leftDrive, rightDrive);
	pilot.setAcceleration(accel);
	pilot.setTravelSpeed(max_speed);
	pilot.setRotateSpeed(30);
	op = new OdometryPoseProvider(pilot);
	//This ensures that the position is correct when we do moves not using the navigator
	pilot.addMoveListener(op);
	//Tell it that we are initially pointing in the positive Y direction, instead of positive X.
	op.setPose(new Pose(0, 0, 90));
	nav = new Navigator(pilot, op,false);
    }

    private void generateMap() {
	Line[] lines = new Line[4];
	Rectangle boundary = new Rectangle(-30, 60, 60, 60);
	lines[0] = new Line(-30, 60, 30, 60);
	lines[0] = new Line(30, 60, 30, 0);
	lines[0] = new Line(30, 0, -30, 0);
	lines[0] = new Line(-30, 0, -30, 60);
	env = new LineMap(lines, boundary);
    }

    public void generatePath() {
	Path p = new Path();
	p.add(new Waypoint(-65, 118));
	p.add(new Waypoint(48, 156));
	p.add(new Waypoint(0, 240));
	p.add(new Waypoint(0, 0));
	default_path = p;
    }

    public void tankDrive(double left, double right) {
	manualDrive.tankDrive(left, right, false);
    }

    public void arcadeDrive(double move, double turn) {
	//drive.arcadeDrive(move, turn, true);
	//Use DiferentialPilot or whatever it is
    }

    public double getFrontSonar() {
	return frontSonar.getMedianRange();
    }

    public double getLeftSonar() {
	return leftSonar.getMedianRange();
    }

    public double getRightSonar() {
	return rightSonar.getMedianRange();
    }

    public double getBackSonar() {
	return backSonar.getMedianRange();
    }

    public int getLeftEncoder() {
	return leftEncoder.get();
    }

    public double getLeftRate() {
	return leftEncoder.getRate() / 12.0;
    }

    public int getRightEncoder() {
	return rightEncoder.get();
    }

    public double getRightRate() {
	return rightEncoder.getRate() / 12.0;
    }

    public double getGyroAngle() {
	return gyro.getAngle();
    }

    public double getPWMLeft() {
	return leftDrive.getRawPWM();
    }

    public double getPWMRight() {
	return rightDrive.getRawPWM();
    }

    public DifferentialPilot getPilot() {
	return pilot;
    }

    public PoseProvider getPoseProvider() {
	return op;
    }

    public Navigator getNavigator() {
	return nav;
    }

    public void disableControl() {
	rightDrive.flt(true);
	leftDrive.flt(true);
    }

    public LineMap getMap() {
	return env;
    }

    public void setMap(LineMap map) {
	env = map;
    }
}