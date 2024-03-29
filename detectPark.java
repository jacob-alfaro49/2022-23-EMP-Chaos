package org.firstinspires.ftc.teamcode.Autonomous;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;
import org.openftc.easyopencv.OpenCvWebcam;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;


@Autonomous(name = "detectPark", group = "Auto2022")
public class detectPark extends LinearOpMode {

    private ElapsedTime runtime = new ElapsedTime();

    static final double COUNTS_PER_MOTOR_REV = 537.7;    // eg: GoBilda 5203 Motor Encoder
    static final double DRIVE_GEAR_REDUCTION = 2.0;     // This is < 1.0 if geared UP
    static final double WHEEL_DIAMETER_INCHES = 4.0;     // For figuring circumference
    static final double COUNTS_PER_INCH = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * 3.1415);
    static final double DRIVE_SPEED = 0.5;
    static final double TURN_SPEED = 0.3;

    private DcMotor leftFront = null;
    private DcMotor rightFront = null;
    private DcMotor leftBack = null;
    private DcMotor rightBack = null;
    private DcMotor raiseLeft = null;
    private DcMotor raiseRight = null;
    private Servo grip = null;
    private Servo flip = null;

    OpenCvCamera camera;
    FrenzyPipeline pipeline;
    int positionValue;

    public void runOpMode() {
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        leftBack = hardwareMap.get(DcMotor.class, "leftBack");
        rightBack = hardwareMap.get(DcMotor.class, "rightBack");
        raiseLeft = hardwareMap.get(DcMotor.class, "raiseLeft");
        raiseRight = hardwareMap.get(DcMotor.class, "raiseRight");
        grip = hardwareMap.get(Servo.class, "grip");
        flip = hardwareMap.get(Servo.class, "flip");

        leftFront.setDirection(DcMotor.Direction.REVERSE);
        rightFront.setDirection(DcMotor.Direction.FORWARD);
        leftBack.setDirection(DcMotor.Direction.REVERSE);
        rightBack.setDirection(DcMotor.Direction.FORWARD);
        raiseLeft.setDirection(DcMotor.Direction.FORWARD);
        raiseRight.setDirection(DcMotor.Direction.REVERSE);
        grip.setDirection(Servo.Direction.FORWARD);
        flip.setDirection(Servo.Direction.FORWARD);

        WebcamName cameraName = hardwareMap.get(WebcamName.class, "cam");
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        camera = OpenCvCameraFactory.getInstance().createWebcam(cameraName, cameraMonitorViewId);
        pipeline = new FrenzyPipeline();
        camera.setPipeline(pipeline);
        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                camera.startStreaming(1920, 1080, OpenCvCameraRotation.UPSIDE_DOWN);
            }

            @Override
            public void onError(int errorCode) {
                /*
                 * This will be called if the camera could not be opened
                 */
            }
        });

        //From Pushbot
        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Set all motors to zero power
        leftFront.setPower(0);
        rightFront.setPower(0);
        leftBack.setPower(0);
        rightBack.setPower(0);


        // Send telemetry message to signify robot waiting;
        telemetry.addData("Status", "Running Servos");
        telemetry.update();

        leftFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        leftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Send telemetry message to indicate successful Encoder reset
        telemetry.addData("Path0", "Starting at %7d :%7d",
                leftFront.getCurrentPosition(),
                rightFront.getCurrentPosition());

        telemetry.update();

        while (!opModeIsActive()) {
            telemetry.addData("analysis of middle", pipeline.getAnalysisMid());
            telemetry.addData("position", pipeline.returnPosition());
            telemetry.update();
        }

        // Wait for the game to start (driver presses PLAY)
        positionValue = pipeline.returnPosition();
        waitForStart();
        flip.setPosition(0.05);
        grip.setPosition(1);
        //actual motion of robot
        runtime.reset();
        sleep(1000);
        encoderDrive(0.5, 30, 30, 30, 30, 2.0);
        sleep(1000);
        encoderDrive(0.25, -25, -25, -25, -25, 2.0);
        sleep(1000);
        encoderDrive(0.25,-6,6,6,-6,2.0);
        sleep(500);
        runtime.reset();
        while (runtime.seconds() < 1.0){ //3.5 seconds = HIGH  2 seconds = MEDIUM   1.5 seconds = LOW
            raiseLeft.setPower(0.75);
            raiseRight.setPower(0.75);
        }
        encoderDrive(0.25,3,3,3,3,2.0);
        sleep(500);
        grip.setPosition(0.65);
        sleep(500);
        grip.setPosition(1);
        encoderDrive(0.25,-3,-3,-3,-3,2.0);
        while (runtime.seconds() < 2.5){ //2.5 = LOW    3 = MEDIUM
            raiseLeft.setPower(-0.25);
            raiseRight.setPower(-0.25);
        }
        encoderDrive(0.25,7,-7,-7,7,2.0);
        sleep(500);
        raiseLeft.setPower(0);
        raiseRight.setPower(0);
        if(positionValue == 1){
            encoderDrive(0.25,-17,17,17,-17,2.0);
        }
        else if(positionValue == 2){
            encoderDrive(0.25, 0,0,0,0, 2.0);
        }
        else if(positionValue==3){
            encoderDrive(0.25,24,-24,-24,24,2.0);
            sleep(500);
        }
        sleep(1000);


        telemetry.addData("Path", "Complete");
        telemetry.update();

    }

    public void encoderDrive(double speed,
                             double left1Inches, double right1Inches, double left2Inches, double right2Inches, double timeoutS) {
        int newLeftTarget;
        int newRightTarget;
        int newLeftBTarget;
        int newRightBTarget;


        // Ensure that the opmode is still active
        if (opModeIsActive()) {

            // Determine new target position, and pass to motor controller

            newLeftTarget = Math.abs(leftFront.getCurrentPosition() + (int) (left1Inches * COUNTS_PER_INCH));
            newRightTarget = Math.abs(rightFront.getCurrentPosition() + (int) (right1Inches * COUNTS_PER_INCH));
            newLeftBTarget = Math.abs(leftBack.getCurrentPosition() + (int) (left2Inches * COUNTS_PER_INCH));
            newRightBTarget = Math.abs(rightBack.getCurrentPosition() + (int) (right2Inches * COUNTS_PER_INCH));


            leftFront.setTargetPosition(newLeftTarget);
            rightFront.setTargetPosition(newRightTarget);
            leftBack.setTargetPosition(newLeftBTarget);
            rightBack.setTargetPosition(newRightBTarget);

            // Turn On RUN_TO_POSITION
            leftFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            rightFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            leftBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            rightBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // reset the timeout time and start motion.
            runtime.reset();

            leftFront.setPower(Math.abs(speed));
            rightFront.setPower(Math.abs(speed));
            leftBack.setPower(Math.abs(speed));
            rightBack.setPower(Math.abs(speed));

            while (opModeIsActive() &&
                    (runtime.seconds() < timeoutS) &&
                    (leftFront.isBusy() && rightFront.isBusy()) && (leftBack.isBusy() && rightBack.isBusy())) {
                // Display it for the driver.
                telemetry.addData("Path1", "Running to %7d", newLeftTarget);
                telemetry.addData("Path2", "Running at %7d",
                        leftFront.getCurrentPosition());
                telemetry.update();
            }
            // Stop all motion;
            leftFront.setPower(0);
            rightFront.setPower(0);
            leftBack.setPower(0);
            rightBack.setPower(0);

            // Turn off RUN_TO_POSITION
            leftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            leftBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rightBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            //  sleep(250);   // optional pause after each move
        }
    }

    public static class FrenzyPipeline extends OpenCvPipeline {
        public enum positions {
            middle
        }

        static final Scalar GREEN = new Scalar(0, 255, 0);

        static final Point midTopLeft = new Point(1205, 375);

        static final int width = 250;
        static final int height = 350;

        final int one_low_threshold = 100;
        final int one_high_threshold = 125;
        final int two_low_threshold = 150;
        final int two_high_threshold = 165;
        final int three_low_threshold = 130;
        final int three_high_threshold = 145;

        Point midPointA = new Point(midTopLeft.x, midTopLeft.y);
        Point midPointB = new Point(midTopLeft.x + width, midTopLeft.y + height);

        //variables
        Mat mid_Cb;
        Mat YCrCb = new Mat();
        Mat Cb = new Mat();
        int avg2;
        int conePosition;
        private volatile FrenzyPipeline.positions position2 = FrenzyPipeline.positions.middle;
        private volatile FrenzyPipeline.positions actualPosition = FrenzyPipeline.positions.middle;

        void inputToCb(Mat input) {
            Imgproc.cvtColor(input, YCrCb, Imgproc.COLOR_RGB2YCrCb);
            Core.extractChannel(YCrCb, Cb, 1);
        }

        public void init(Mat firstFrame) {
            inputToCb(firstFrame);

            mid_Cb = Cb.submat(new Rect(midPointA, midPointB));

        }

        public Mat processFrame(Mat input) {
            inputToCb(input);
            avg2 = (int) Core.mean(mid_Cb).val[0];
            Imgproc.rectangle(input, midPointA, midPointB, GREEN, 10);
            actualPosition = FrenzyPipeline.positions.middle;
            conePosition = 0;
            if (avg2 >= one_low_threshold && avg2 <= one_high_threshold){
                actualPosition = FrenzyPipeline.positions.middle;
                conePosition = 1;
            }
            else if (avg2 >= two_low_threshold && avg2 <= two_high_threshold){
                actualPosition = FrenzyPipeline.positions.middle;
                conePosition = 2;
            }
            else if (avg2 >= three_low_threshold && avg2 <= three_high_threshold){
                actualPosition = FrenzyPipeline.positions.middle;
                conePosition = 3;
            }

            Imgproc.rectangle(input, midPointA, midPointB, GREEN, 10);

            return input;

        }

        public int returnPosition() {
            return conePosition;
        }

        public int getAnalysisMid() {
            return avg2;
        }

    }
}

