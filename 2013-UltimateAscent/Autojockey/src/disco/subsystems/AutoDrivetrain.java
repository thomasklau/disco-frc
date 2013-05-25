/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package disco.subsystems;

import disco.HW;
import disco.commands.drivetrain.LerpDrive;
import disco.utils.MaxbotixSonar;
import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Gyro;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.command.Subsystem;
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
    //Sonars
    private MaxbotixSonar frontSonar1;
    private MaxbotixSonar frontSonar2;
    private MaxbotixSonar leftSonar;
    private FusorDetector sonars;
    //Encoders
    private Encoder leftEncoder;
    private Encoder rightEncoder;
    //Gyro
    private Gyro gyro;
    //leJOS stuff
    public RegulatedDrivetrain leftDrive, rightDrive;
    private DifferentialPilot pilot;
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
	sonarsInit();
	gyroInit();
	leJOSDriveInit();
	generateMap();
	generatePath();
    }

    public void initDefaultCommand() {
	new LerpDrive();
    }

    private void motorsInit() {
	leftDrive1 = new Victor(HW.LeftDrive1Slot, HW.LeftDrive1Channel);
	leftDrive2 = new Victor(HW.LeftDrive2Slot, HW.LeftDrive2Channel);
	rightDrive1 = new Victor(HW.RightDrive1Slot, HW.RightDrive1Channel);
	rightDrive2 = new Victor(HW.RightDrive2Slot, HW.RightDrive2Channel);
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
//	frontSonar1 = new MaxbotixSonar(HW.frontsonar1Slot, HW.frontsonar1Channel, MaxbotixSonar.Unit.kInches);
//	FeatureDetector frontSonar1_detector=new RangeFeatureDetector(frontSonar1,(float)frontSonar1.MAX_PEOPLE_RANGE,frontSonar1.MIN_READING_DELAY,0);
//	frontSonar2 = new MaxbotixSonar(HW.frontsonar2Slot, HW.frontsonar2Channel, MaxbotixSonar.Unit.kInches);
//	FeatureDetector frontSonar2_detector=new RangeFeatureDetector(frontSonar2,(float)frontSonar2.MAX_PEOPLE_RANGE,frontSonar2.MIN_READING_DELAY,0);
//	leftSonar = new MaxbotixSonar(HW.leftsonarSlot,HW.leftsoarChannel,MaxbotixSonar.Unit.kInches);
//	FeatureDetector leftSonar_detector=new RangeFeatureDetector(leftSonar,(float)leftSonar.MAX_PEOPLE_RANGE,leftSonar.MIN_READING_DELAY,90);
//	sonars=new FusorDetector();
//	sonars.addDetector(frontSonar1_detector);
//	sonars.addDetector(frontSonar2_detector);
//	sonars.addDetector(leftSonar_detector);
//	sonars.enableDetection(false);
    }

    private void gyroInit(){
//	gyro = new Gyro(HW.gyroSlot, HW.gyroChannel);
//	gyro.setSensitivity(0.007);
    }

    private void leJOSDriveInit() {
	leftDrive = new RegulatedDrivetrain(leftDrive1, leftDrive2, true, true, leftEncoder, HW.encoderTicksPerRev);
	rightDrive = new RegulatedDrivetrain(rightDrive1, rightDrive2, false, false, rightEncoder, HW.encoderTicksPerRev);

	pilot = new DifferentialPilot(2 * HW.wheelRadius, HW.wheelSeparation, leftDrive, rightDrive);
	pilot.setAcceleration(accel);
	pilot.setTravelSpeed(max_speed);
	pilot.setRotateSpeed(30);
	op = new OdometryPoseProvider(pilot);
	//This ensures that the position is correct when we do moves not using the navigator
	pilot.addMoveListener(op);
	//Tell it that we are initially pointing in the positive Y direction, instead of positive X.
	op.setPose(new Pose(0, 0, 90));
	nav = new Navigator(pilot, op);
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
	//set up for tank
	//pilot.setAcceleration(9999);
	leftDrive.setSpeed(java.lejoslang.Math.round(HW.maxFPS * Math.abs(left) * 360 / (2 * Math.PI * HW.wheelRadius)));
	rightDrive.setSpeed(java.lejoslang.Math.round(HW.maxFPS * Math.abs(right) * 360 / (2 * Math.PI * HW.wheelRadius)));
	if (right > 0) {
	    rightDrive.forward();
	} else {
	    rightDrive.backward();
	}
	if (left > 0) {
	    leftDrive.forward();
	} else {
	    leftDrive.backward();
	}
	//reset
	pilot.setAcceleration(accel);
	pilot.setTravelSpeed(max_speed);
    }

    public void arcadeDrive(double move, double turn) {
	//drive.arcadeDrive(move, turn, true);
	//Use DiferentialPilot or whatever it is
    }

    public double getFrontSonar1() {
	return frontSonar1.getMedianRange();
    }

    public double getFrontSonar2() {
	return frontSonar2.getMedianRange();
    }

    public double getLeftSonar() {
	return leftSonar.getMedianRange();
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