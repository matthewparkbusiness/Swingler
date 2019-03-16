package com.mp.swingler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable {

    private Thread thread;
    private SurfaceHolder holder;
    private boolean threadAlreadyStarted;
    private boolean lockTouch;

    private int width;
    private int height;
    private int scaleWidth = 480;
    private int scaleHeight = 762;
    private int scaledTotalHeight = 1000;
    private int offset;
    private int currentLevel = 0;

    private Pendulum redPendulum;
    private Pendulum yellowPendulum;
    private Pendulum greenPendulum;
    private Pendulum magentaPendulum;

    private Canvas canvas;

    private Paint white;
    private Paint red;
    private Paint yellow;
    private Paint green;
    private Paint magenta;

    private Goal goal = new Goal();

    public GameView(Context c){
        super(c);

        white = new Paint();
        white.setColor(Color.WHITE);
        white.setStyle(Paint.Style.FILL);
        white.setStrokeWidth(20);

        red = new Paint();
        red.setColor(Color.RED);
        red.setStyle(Paint.Style.STROKE);
        red.setStrokeWidth(20);

        yellow = new Paint();
        yellow.setColor(Color.YELLOW);
        yellow.setStyle(Paint.Style.STROKE);
        yellow.setStrokeWidth(20);

        green = new Paint();
        green.setColor(Color.GREEN);
        green.setStyle(Paint.Style.STROKE);
        green.setStrokeWidth(20);

        magenta = new Paint();
        magenta.setColor(Color.MAGENTA);
        magenta.setStyle(Paint.Style.STROKE);
        magenta.setStrokeWidth(20);

        thread = new Thread(this);

        holder = this.getHolder();
        holder.addCallback(

                new SurfaceHolder.Callback(){

                    public void surfaceChanged(SurfaceHolder arg0,
                                               int arg1, int arg2, int arg3) {
                    }

                    public void surfaceCreated(SurfaceHolder arg0) {
                        if(!threadAlreadyStarted){
                            thread.start();
                            threadAlreadyStarted = true;
                        }
                    }

                    public void surfaceDestroyed(SurfaceHolder arg0) {}

                });
    }


    public void run(){

        Canvas c = holder.lockCanvas();
        width = c.getWidth();
        height = c.getHeight();

        holder.unlockCanvasAndPost(c);

        redPendulum = new Pendulum(0, scaleWidth/4, 30);
        yellowPendulum = new Pendulum(1, 3*scaleWidth/4, 30);
        greenPendulum = new Pendulum(2, scaleWidth/4, 60 + scaleWidth/2);
        magentaPendulum = new Pendulum(3, 3*scaleWidth/4, 60 + scaleWidth/2);


        while(true){

            c = holder.lockCanvas();
            if(c == null) break;
            c.drawColor(Color.BLACK);

            canvas = c;
            drawEverything();
            holder.unlockCanvasAndPost(c);
            try{Thread.currentThread().sleep(15);} catch(InterruptedException ie){ }

        }
        System.exit(0);
    }

    boolean pressed;
    float pressedOffset;
    float pressedY;
    float diffY;

    public boolean onTouchEvent(MotionEvent e){
        if(lockTouch) return true;
        float x = (e.getX()*scaleWidth)/width;
        float y = (e.getY()*scaleHeight)/height - offset;

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:

                if(!(redPendulum.touchEvent(x, y) || yellowPendulum.touchEvent(x, y) || greenPendulum.touchEvent(x, y) || magentaPendulum.touchEvent(x, y))){
                    pressed = true;
                    pressedY = e.getY();
                    pressedOffset = offset;
                }
                break;

            case MotionEvent.ACTION_MOVE:

                if(pressed){
                    offset = (int)(pressedOffset + e.getY() - pressedY);
                    if(offset > 0) offset = 0;
                    else if(offset <  scaleHeight - scaledTotalHeight ) offset = scaleHeight - scaledTotalHeight;
                }
                break;
            case MotionEvent.ACTION_UP:

                pressed = false;


        }


        return true;
    }

    public void reset(){
        if(currentLevel == 0){

        }
    }

    public void drawEverything(){
        redPendulum.draw();
        yellowPendulum.draw();
        greenPendulum.draw();
        magentaPendulum.draw();

        goal.draw();
    }




    public void drawRect(float x1, float y1, float x2, float y2, Paint p){
        canvas.drawRect(x1*width/scaleWidth, y1*height/scaleHeight, x2*width/scaleWidth, y2*height/scaleHeight, p);
    }

    public void drawLine(float x1, float y1, float x2, float y2, Paint p){
        canvas.drawLine(x1*width/scaleWidth, y1*height/scaleHeight, x2*width/scaleWidth, y2*height/scaleHeight, p);
    }

    public void drawCircle(float x1, float y1, float radius, Paint p){
        canvas.drawCircle(x1*width/scaleWidth, y1*height/scaleHeight, radius*width/scaleWidth, p);
    }


    public class Pendulum{

        int id;
        Paint p;
        Paint restraintPaint;
        Paint darkRestraintPaint;

        // STAGE ONE, BOTH RESTRAINTS ARE PRESENT
        int restraintX1;
        int restraintY1;
        int restraintX2;
        int restraintY2;

        // STAGE TWO, ONE RESTRAINT, BALL IS SWINGING
        double radius;
        int restraintX;
        int restraintY;
        double cosineCounter = 0.0;
        double amplitude = 0.0;
        double period = 1;

        // STAGE THREE, BALL IS IN FREE-FALL
        int ballX;
        int ballY;
        double vx;
        double vy;
        double gravity = 0.4;

        // STAGE FOUR, BALL EXPLODES - DECORATION EFFECT
        int[] xs = new int[10];
        int[] ys = new int[10];
        double[] vxs = new double[10];
        double[] vys = new double[10];
        int decorationBallSize = 5;
        int decorationsTimer = 0;
        int ballVibration = 0;

        int prevBallX;
        int prevBallY;
        int ballSize = 20;

        int stage = 1;

        int restraintSize = 20;
        int standardWidth = scaleWidth/4;
        int standardHeight = scaleWidth/4;

        int centerX;
        int topY;

        boolean transitionToDeath = false;



        public Pendulum(int _id, int x, int y){

            centerX = x;
            topY = y;

            restraintX1 = x - standardWidth/2 + (int)(Math.random()*standardWidth/4);
            restraintX2 = x + standardWidth/2 - (int)(Math.random()*standardWidth/4);
            restraintY1 = y + (int)(Math.random()*standardWidth/4);
            restraintY2 = y + (int)(Math.random()*standardWidth/4);

            ballX = x;
            ballY = y + standardHeight;

            id = _id;
            restraintPaint = new Paint();
            restraintPaint.setColor(Color.WHITE);
            restraintPaint.setAntiAlias(true);
            darkRestraintPaint = new Paint();
            darkRestraintPaint.setColor(Color.BLACK);


            p = new Paint();
            p.setAntiAlias(true);
            if(id == 0) p.setColor(Color.RED);
            else if(id == 1) p.setColor(Color.YELLOW);
            else if(id == 2) p.setColor(Color.GREEN);
            else p.setColor(Color.MAGENTA);


        }

        public boolean touchEvent(float x, float y){
            if(stage == 1) {
                if (x > restraintX1 - restraintSize / 2.f && x < restraintX1 + restraintSize / 2.f
                        && y > restraintY1 - restraintSize / 2.f && y < restraintY1 + restraintSize / 2.f) {
                    stage = 2;
                    restraintX = restraintX2;
                    restraintY = restraintY2;

                    amplitude = ballX - restraintX;
                    radius = Math.sqrt((restraintX - ballX) * (restraintX - ballX) + (ballY - restraintY2) * (ballY - restraintY2));
                    return true;
                }
                else if (x > restraintX2 - restraintSize / 2.f && x < restraintX2 + restraintSize / 2.f
                        && y > restraintY2 - restraintSize / 2.f && y < restraintY2 + restraintSize / 2.f) {
                    stage = 2;
                    restraintX = restraintX1;
                    restraintY = restraintY1;

                    amplitude = ballX - restraintX;
                    radius = Math.sqrt((restraintX - ballX) * (restraintX - ballX) + (ballY - restraintY1) * (ballY - restraintY1));
                    return true;
                }
            }
            else if(stage == 2){
                if (x > restraintX - restraintSize / 2.f && x < restraintX + restraintSize / 2.f
                        && y > restraintY - restraintSize / 2.f && y < restraintY + restraintSize / 2.f) {
                    lockTouch = true;
                    stage = 3;
                    vx = ballX - prevBallX;
                    vy = ballY - prevBallY;
                    return true;

                }
            }
            return false;

        }

        public void draw(){

            if(stage == 1){
                drawCircle(restraintX1,
                        restraintY1 + offset,
                        restraintSize/2, restraintPaint);

                drawCircle(restraintX2,
                        restraintY2 + offset,
                        restraintSize/2, restraintPaint);

                drawLine(restraintX1,
                        restraintY1 + offset,
                        ballX,
                        ballY + offset, restraintPaint);

                drawLine(restraintX2,
                        restraintY2 + offset,
                        ballX,
                        ballY + offset, restraintPaint);

                drawCircle(ballX ,ballY + offset ,ballSize+2, restraintPaint);
                drawCircle(ballX,
                        ballY + offset,
                        ballSize, p);
            }
            else if(stage == 2){
                prevBallX = ballX;
                prevBallY = ballY;
                ballX = (int)( amplitude*Math.cos(period*cosineCounter) + restraintX );
                ballY = (int)(topY + Math.sqrt(radius*radius - (ballX - restraintX)*(ballX - restraintX)));


                drawRect(restraintX-restraintSize/2.f,
                        restraintY-restraintSize/2 + offset,
                        restraintX+restraintSize/2.f,
                        restraintY+restraintSize/2 + offset, restraintPaint);

                drawLine(restraintX,
                        restraintY + offset,
                        ballX,
                        ballY + offset, restraintPaint);
                drawCircle(ballX,
                        ballY + offset,
                        ballSize, p);

                cosineCounter += 0.05;
            }
            else if(stage == 3){

                ballX = (int) (ballX + vx);
                if(offset > scaleHeight - scaledTotalHeight)
                    offset = (int) (offset - vy);
                ballY = (int)(ballY + vy);
                vy = vy + gravity;

                drawCircle(ballX,
                        ballY + offset,
                        ballSize, p);


                if(ballY > goal.y && ballX > goal.x - goal.size && ballX < goal.x + goal.size){
                    stage = 4;
                    for(int i=0;i<xs.length;i++){
                        xs[i] = goal.x;
                        ys[i] = goal.y;
                        vxs[i] = (int)(Math.random()*10)-5;
                        vys[i] = -1*(int)(Math.random()*20);
                    }
                }
                else if(ballY > scaledTotalHeight){
                    lockTouch = false;
                    offset = 0;
                    stage = 5;
                }
            }
            else if(stage == 4){
                transitionToDeath = true;

                if(decorationsTimer < 300){
                    for(int i=0;i<xs.length;i++){
                        xs[i] = (int)(xs[i] + vxs[i]);
                        ys[i] = (int)(ys[i] + vys[i]);

                        vys[i] = vys[i] + gravity;
                        drawCircle(xs[i], ys[i]+offset, decorationBallSize, p);
                        decorationsTimer ++;

                    }
                }
                else{
                    for(int i=0;i<xs.length;i++){
                        xs[i] = (int)(goal.x - xs[i])/10 + xs[i];
                        ys[i] = (int)(goal.y - ys[i])/10 + ys[i];

                        drawCircle(xs[i], ys[i]+offset, decorationBallSize, p);
                        if((xs[i] < goal.x - goal.size/2 || xs[i] > goal.x + goal.size/2 ||
                                ys[i] < goal.y - goal.size/2 || ys[i] > goal.y + goal.size/2)){

                            transitionToDeath = false;

                        }
                        else{
                            goal.size -= 0.1;
                            goal.x = goal.x + ballVibration;
                            if(ballVibration == 0){
                                ballVibration = 3;
                            }
                            else if(ballVibration == 3){
                                ballVibration = -3;
                            }
                            else if(ballVibration == -3){
                                ballVibration = 0;
                            }
                        }

                    }
                    if(transitionToDeath){
                        stage = 5;
                    }
                }


            }
            else if(stage == 5){
                offset = 9*offset/10;
                if(offset > -5){
                    offset = 0;
                    lockTouch = false;
                    stage = 6;
                }
            }


        }


    }

    public class Goal{


        int x;
        int y;

        double size = 100;

        Paint white = new Paint();
        Paint gray = new Paint();

        public Goal(){
            x = scaleWidth/2;
            y = scaledTotalHeight - 100;

            white.setColor(Color.WHITE);
            gray.setColor(Color.BLUE);
        }



        public void draw(){
            drawCircle(x, y + offset, (int)(size)+5, gray);
            drawCircle(x, y + offset, (int)size, white);
        }

    }



}
