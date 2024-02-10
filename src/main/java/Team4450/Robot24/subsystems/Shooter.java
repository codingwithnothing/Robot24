package Team4450.Robot24.subsystems;

import com.revrobotics.CANSparkLowLevel.MotorType;

import Team4450.Lib.Util;

import static Team4450.Robot24.Constants.SHOOTER_MOTOR_TOP;
import static Team4450.Robot24.Constants.SHOOTER_PIVOT_FACTOR;
import static Team4450.Robot24.Constants.SHOOTER_MOTOR_BOTTOM;
import static Team4450.Robot24.Constants.SHOOTER_MOTOR_FEEDER;
import static Team4450.Robot24.Constants.SHOOTER_MOTOR_PIVOT;

import static Team4450.Robot24.Constants.SHOOTER_SPEED;
import static Team4450.Robot24.Constants.NOTE_SENSOR_SHOOTER;
import static Team4450.Robot24.Constants.SHOOTER_FEED_SPEED;


import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkPIDController;
// import com.revrobotics.SparkRelativeEncoder;
import com.revrobotics.CANSparkBase.ControlType;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * Subsystem for the Shooter subassemebly on the 2024 robot
 */
public class Shooter extends SubsystemBase {
    private CANSparkMax motorTop = new CANSparkMax(SHOOTER_MOTOR_TOP, MotorType.kBrushless);
    private CANSparkMax motorBottom = new CANSparkMax(SHOOTER_MOTOR_BOTTOM, MotorType.kBrushless);
    private CANSparkMax motorFeeder = new CANSparkMax(SHOOTER_MOTOR_FEEDER, MotorType.kBrushless);
    private CANSparkMax motorPivot = new CANSparkMax(SHOOTER_MOTOR_PIVOT, MotorType.kBrushless);
    private final DigitalInput shooterNoteSensor = new DigitalInput(NOTE_SENSOR_SHOOTER);

    private RelativeEncoder pivotEncoder;

    private double shooterSpeed = SHOOTER_SPEED;
    private double feedSpeed = SHOOTER_FEED_SPEED;

    private SparkPIDController pivotPID;

    // NOTE: I removed the shuffleboard speed setting because they were too
    // much of a hassle to handle with all of the different speed states the shooter could be in
    // (feeding, slow feeding, inverse feeding, shooting, etc.)

    public Shooter() {
        Util.consoleLog();

        motorBottom.follow(motorTop);
        motorFeeder.setInverted(true);

        pivotEncoder = motorPivot.getEncoder();
        pivotPID = motorPivot.getPIDController();

        pivotPID.setP(0.1);
        pivotPID.setI(0);
        pivotPID.setD(0);
        pivotPID.setIZone(0);
        pivotPID.setFF(0);
        pivotPID.setOutputRange(-1, 1);
    }

    public boolean hasNote() {
        return shooterNoteSensor.get();
    }

    /**
     * enables the feed motor (sushi rollers) to push the Note into
     * the rolling shooter wheels (which must be enabled seperately)
     */
    public void startFeeding(double speedfactor) {
        motorFeeder.set(Util.clampValue(speedfactor, 1) * feedSpeed);
    }
    /** stops the feed motor */
    public void stopFeeding() {
        motorFeeder.set(0);
    }

    /** spins the shooter wheels in preperation for a Note */
    public void startShooting() {
        motorTop.set(shooterSpeed);
    }
    /** stops the shooter wheels */
    public void stopShooting() {
        motorTop.set(0);
    }

    /**
     * Sets the shooter assembly to a given angle
     * @param angle the angle with 0deg straight down
     */
    public void pointPivot(double angle) {
        // math...
        pivotPID.setReference(angleToEncoderCounts(angle), ControlType.kPosition);
    }

    public boolean isAtAngle(double angle) {
        return Math.abs(pivotEncoder.getPosition() - angleToEncoderCounts(angle)) > 0.1;
    }

    /**
     * given an angle, return the encoder counts
     * @param angle angle of shooter position: 0 is nominal angle in degrees
     * @return the raw encoder position
     */
    private double angleToEncoderCounts(double angle) {
        return (angle / 360.0) / SHOOTER_PIVOT_FACTOR;
    }

    /**
     * Sets the shooter assembly speed (for manual joystick use)
     * @param speed the speed
     */
    public void movePivotRelative(double speed) {
        motorPivot.set(speed);
    }
}