package frc.robot.subsystems;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.command.Subsystem;
import frc.robot.RobotMap;
import frc.robot.commands.SwerveDriveController;
import frc.robot.subsystems.modules.ProtoModule;
import frc.robot.subsystems.modules.SwerveModule;
import frc.robot.util.SwerveMixerData;

/**
 * 0: Front Right 1: Front Left 2: Back Left 3: Back Right
 */
public class SwerveDrive extends Subsystem {

  private SwerveModule[] modules;
  private AHRS navx;

  public SwerveDrive() {

    if (navx == null) {
      try {
        this.navx = new AHRS(RobotMap.Ports.AHRSPort);
        System.out.println("ahrs is coolio!");
        this.navx.reset();
        this.navx.zeroYaw();
      } catch (RuntimeException e) {
        System.out.println("DT- The navx broke.");
        navx = null;
      }
    } else {
      this.navx.reset();
    }
    modules = new SwerveModule[1];

    System.out.println("SwerveDrive");

    modules[0] = new ProtoModule(0, RobotMap.Ports.FrontRightAzi, RobotMap.Ports.FrontRightDrive,
        RobotMap.Ports.FrontRightEncoder, RobotMap.Ports.FrontRightZero);
    // modules[1] = new ProtoModule(1, RobotMap.Ports.FrontLeftAzi,
    // RobotMap.Ports.FrontLeftDrive, RobotMap.Ports.FrontLeftEncoder,
    // RobotMap.Ports.FrontLeftZero);
    // modules[2] = new ProtoModule(2, RobotMap.Ports.BackLeftAzi,
    // RobotMap.Ports.BackLeftDrive, RobotMap.Ports.BackLeftEncoder,
    // RobotMap.Ports.BackLeftZero);
    // modules[3] = new ProtoModule(3, RobotMap.Ports.BackRightAzi,
    // RobotMap.Ports.BackRightDrive, RobotMap.Ports.BackRightEncoder,
    // RobotMap.Ports.BackRightZero);
    System.out.println("WHAT THE HEKKKK");
  }

  public SwerveDrive(boolean testConstructor) {
  }

  /**
   * Basically 95% leveraged from Jack In The Bot
   */
  public SwerveMixerData SwerveMixer(double forward, double strafe, double rotation, boolean isFieldOriented) {

    SwerveMixerData smd = new SwerveMixerData();
    smd.setForward(forward);
    smd.setStrafe(strafe);
    smd.setRotate(rotation);

    if (isFieldOriented) {
      double angleRad = Math.toRadians(getGyroAngle());
      double temp = forward * Math.cos(angleRad) + strafe * Math.sin(angleRad);
      strafe = -forward * Math.sin(angleRad) + strafe * Math.cos(angleRad);
      forward = temp;
    }

    double a = strafe - rotation * (RobotMap.Values.WHEELBASE / RobotMap.Values.TRACKWIDTH);
    double b = strafe + rotation * (RobotMap.Values.WHEELBASE / RobotMap.Values.TRACKWIDTH);
    double c = forward - rotation * (RobotMap.Values.TRACKWIDTH / RobotMap.Values.WHEELBASE);
    double d = forward + rotation * (RobotMap.Values.TRACKWIDTH / RobotMap.Values.WHEELBASE);

    double[] angles = new double[] { Math.atan2(b, c) * 180 / Math.PI, Math.atan2(b, d) * 180 / Math.PI,
        Math.atan2(a, d) * 180 / Math.PI, Math.atan2(a, c) * 180 / Math.PI };

    double[] speeds = new double[] { Math.sqrt(b * b + c * c), Math.sqrt(b * b + d * d), Math.sqrt(a * a + d * d),
        Math.sqrt(a * a + c * c) };

    double max = speeds[0];

    for (double speed : speeds) {
      if (speed > max) {
        max = speed;
      }
    }

    double mod = 1;
    if (max > 1) {
      mod = 1 / max;

      for (int i = 0; i < 4; i++) {
        speeds[i] *= mod;

        angles[i] %= 360;
        if (angles[i] < 0)
          angles[i] += 360;
      }
    }

    smd.setAngles(angles);
    smd.setSpeeds(speeds);
    return smd;
  }

  public void setSwerveInput(SwerveMixerData smd) {
    for (int i = 0; i < modules.length; i++) {
      if (Math.abs(smd.getForward()) > 0.05 || Math.abs(smd.getStrafe()) > 0.05 || Math.abs(smd.getRotate()) > 0.05) {
        modules[i].setTargetAngle(smd.getAngles()[i]);
      } else {
        modules[i].setTargetAngle(modules[i].getTargetAngle());
      }
      modules[i].setTargetSpeed(smd.getSpeeds()[i]);
    }
  }

  public void updateSmartDashboard() {
    for (SwerveModule mod : modules) mod.updateSmartDashboard();
  }

  public double getRawGyroAngle() {
    double angle = navx.getAngle();
    angle %= 360;
    if (angle < 0)
      angle += 360;

    return angle;
  }

  public double getGyroAngle() {
    double a = navx.getAngle();
    a %= 360;
    if (a < 0)
      a += 360;
    return a;
  }

  public SwerveModule getModule(int index) {
    return modules[index];
  }

  public SwerveModule[] getModules() { return modules; }

  @Override
  public void initDefaultCommand() {
    setDefaultCommand(new SwerveDriveController());
  }
}
