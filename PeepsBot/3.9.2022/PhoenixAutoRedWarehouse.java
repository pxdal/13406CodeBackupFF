package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import AutoTools.EzrasLaw;

@Autonomous

public class PhoenixAutoRedWarehouse extends EzrasLaw {
    public void initAuto(){
        this.initVals();
        
        this.setupIMU();
        
        this.initArm();
        
        this.initializeSwivel();
        
        this.globalTime = new ElapsedTime();
        
        // setup motor directions
        this.frontLeft.setDirection(DcMotorSimple.Direction.FORWARD);
        this.frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        this.backLeft.setDirection(DcMotorSimple.Direction.FORWARD);
        this.backRight.setDirection(DcMotorSimple.Direction.REVERSE);
    
        this.frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        this.frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        this.backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        this.backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        
        initVuforia();
        initTfod();
        
        if (tfod != null) {
            tfod.activate();
            
            tfod.setZoom(1.1, 16.0/9.0);
        }
    }
    
    @Override
    public void runOpMode(){
        this.initAuto();
        
        telemetry.addData("Status", "Initialized");
        telemetry.update();
        
        waitForStart();
        
        // immediately clamp
        this.clamp();
        
        // get position of pipe in frame
        float pipeDetection = waitForPipeRecognition(0.5);
        
        int hubLevel = 2; // 0 = bottom, 1 = middle, 2 = top
        
        // middle
        if(pipeDetection < 200 && pipeDetection > 0){
            hubLevel = 1;
        } else if(pipeDetection > 200){ // right
            hubLevel = 2;
        } else { // right
            // this should be default anyways, but set it just for fun
            hubLevel = 0;
        }
        
        telemetry.addData("hubLevel", hubLevel + "");
        
        double armPosition = this.armPositions[hubLevel+1];
        
        // move arm up
        this.setArmRotation(this.armWaitPosition);
        
        // start strafing
        this.compoundMove(-3.0, -24.0, 26.0, 0.0);
        
        // wait till arm is done to set swivel angle
        while(this.armMotor.isBusy() && opModeIsActive());
        
        // set swivel rotation to 90
        this.setSwivelAngle(90.0);
        
        // once partially out, set arm rotation to armPosition
        while(this.getSwivelRotation() < this.swivelWaitPosition && opModeIsActive());
        
        this.setArmRotation(armPosition);
        
        // deposit when drive train finishes
        while(!this.isDone() && opModeIsActive());
        
        this.unclamp();
        
        this.compoundMove(3.0, 25.0, 26.0, 0.0);
        
        while(!this.isDone() && opModeIsActive());
        
        this.setArmRotation(this.armWaitPosition);
        
        this.compoundMoveStraight(40.0, 4.0, 26.0, 0.0);
        
        while(!this.armMotor.isBusy() && opModeIsActive());
        
        this.setSwivelAngle(this.swivelRestingPosition);
        
        while(!this.isDone() && opModeIsActive());
    }
}