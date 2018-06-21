package com.notapplicable.xplus1;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Choreographer;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;

import java.util.ArrayList;


public class MainActivity extends Activity
 implements SurfaceHolder.Callback, Choreographer.FrameCallback
{

    Paint red, green, cyan, black, white, lazer, statix, statix2;
    ArrayList<Paint> colors;
    SoundPool soundPool;
    double score;

    private class Rock {
        double x, y, dx, dy;
        int r;
        Paint color;
        int nseg;
        float points[];
        double segrads;

        Rock(double x, double y, double dx, double dy, int r) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.r = r;
            this.color = colors.get((int)(Math.random()*colors.size()));
            this.nseg = r/2;
            points = new float[nseg*4];
            segrads = 2*Math.PI/nseg;
            scale = 0;
        }

        Rock() {
            x = Math.random()*width;
            y = Math.random()*height;
            dx = 3*Math.random();
            dy = 3*Math.random();
            r = 8+(int)(Math.random()*32);
            this.color = colors.get((int)(Math.random()*colors.size()));
            this.nseg = r/2;
            points = new float[nseg*4];
            segrads = 2*Math.PI/nseg;
        }

        boolean exploding = false;

        public void update() {
            double ax = 0, ay = 0;
            for(Rock rock : rocks) {
                if(rock != this) {
                    double dx = x - rock.x;
                    double dy = y - rock.y;
                    double d2 = (dx * dx) + (dy * dy);
                    double d = Math.sqrt(d2);
                    double a1 = Math.PI * r * r;
                    double a2 = Math.PI * r * r;
                    double f = (a1 + a2) / d2 * .15;

                    ax += f * dx / d;
                    ay += f * dy / d;
                }
            }
            dx += ax;
            dy += ay;
            x += dx;
            y += dy;
            if (x > width) { dx = -dx; }
            if (x < 0) { dx = -dx; }
            if (y > height) { dy = -dy; }
            if (y < 0) { dy = -dy; }

            if(beenHit) {
                if(exploding) {
                    scale *= 2;
                    if(scale > 8.0f)
                        spawn();
                } else {
                    scale -= .1;
                    if(scale < 0.1) {
                        exploding = true;
                    }
                }
            } else {
                if(scale < 1.0)
                    scale += .1f;
            }
        }

        float scale = 1.0f;
        boolean beenHit = false;
        public void split() {
            beenHit = true;
        }

        public void spawn() {
            if(r > 17) {
                for(int i = 0; i < 2; i++) {
                    rocksToAdd.add(new Rock(x + 2 * r * Math.random(), y + 2 * r * Math.random(),
                            -1 + 2 * Math.random(), -1 + 2 * Math.random(), r / 2 + (int)(Math.random()*(r/2))));
                }
            }
            rocksToRemove.add(this);
        }

        public void render(Canvas c) {
            for(int i = 0; i < nseg; i++) {
                int r1 = (int)(r*scale+Math.random()*r/4);
                int r2 = (int)(r*scale+Math.random()*r/7);

                double theta1 = i * segrads + Math.random();
                double theta2 = (i + 1) * segrads + Math.random();

                float x1 = (float)(x+r1*Math.sin(theta1));
                float y1 = (float)(y+r1*Math.cos(theta1));
                float x2 = (float)(x+r2*Math.sin(theta2));
                float y2 = (float)(y+r2*Math.cos(theta2));

                int o = i*4;
                points[o+0] = x1;
                points[o+1] = y1;
                points[o+2] = x2;
                points[o+3] = y2;

                //c.drawLine(x1, y1, x2, y2, statix2);
                //c.drawLine(x1, y1, x2, y2, color);

                if(Math.random() < 0.001) {
                    int x3 = (int)(Math.random()*width), y3;
                    if(Math.random() > .5) {
                        y3 = 0;
                    } else {
                        y3 = height;
                    }

                    c.drawLine(x1, y1, x3, y3, lazer);
                }
            }
            c.drawLines(points, statix2);
            c.drawLines(points, color);
        }
    }

    ArrayList<Rock> rocks;
    int dropSound, explodeSound;
    ArrayList<Integer> music;

    SurfaceView surfaceView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_main);

        SurfaceView surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        black = new Paint();
        black.setColor(Color.BLACK);
        black.setAntiAlias(true);

        red = new Paint();
        red.setColor(Color.RED);
        red.setAntiAlias(true);
        red.setTextSize(50);
        red.setStrokeWidth(2.0f);

        green = new Paint();
        green.setColor(Color.GREEN);
        green.setAntiAlias(true);
        green.setStrokeWidth(2.0f);

        lazer = new Paint();
        lazer.setARGB(127, 0, 255, 0);
        lazer.setAntiAlias(true);

        statix = new Paint();
        statix.setARGB(32, 127, 127, 127);
        statix.setAntiAlias(true);
        statix.setStrokeWidth(7.0f);

        statix2 = new Paint();
        statix2.setARGB(168, 127, 127, 127);
        statix2.setAntiAlias(true);
        statix2.setStrokeWidth(10.0f);

        cyan = new Paint();
        cyan.setColor(Color.CYAN);
        cyan.setAntiAlias(true);
        cyan.setStrokeWidth(2.0f);

        white = new Paint();
        white.setColor(Color.WHITE);
        white.setAntiAlias(true);

        colors = new ArrayList<Paint>();
        colors.add(red);
        colors.add(green);
        colors.add(cyan);
        colors.add(white);

        soundPool = new SoundPool(32, AudioManager.STREAM_MUSIC, 0);
        dropSound = soundPool.load(this, R.raw.drop, 0);
        explodeSound = soundPool.load(this, R.raw.explode, 0);

        music = new ArrayList<Integer>();
        music.add(soundPool.load(this, R.raw.a, 0));
        //music.add(soundPool.load(this, R.raw.b, 0));
        music.add(soundPool.load(this, R.raw.c, 0));
        music.add(soundPool.load(this, R.raw.d, 0));
        music.add(soundPool.load(this, R.raw.e, 0));
        music.add(soundPool.load(this, R.raw.f, 0));
        music.add(soundPool.load(this, R.raw.g, 0));
        music.add(soundPool.load(this, R.raw.h, 0));
        music.add(soundPool.load(this, R.raw.i, 0));

        class LoadComplete implements SoundPool.OnLoadCompleteListener {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
               info(Integer.toString(status));
            }
        }
        soundPool.setOnLoadCompleteListener(new LoadComplete());
    }

    boolean paused;

    @Override
    public void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        paused = false;
        Choreographer.getInstance().postFrameCallback(this);
    }

    double ax = 0, ay = 0, dx = 0, dy = 0, x, y;
    double friction = .15;
    int width, height;
    SurfaceHolder surfaceHolder;
    int invincible;

    public void surfaceCreated(SurfaceHolder sh) {
        surfaceHolder = sh;
        Choreographer.getInstance().postFrameCallback(this);
    }

    public void restart() {
        x = width/2;
        y = height/2;
        score = 0;
        lives = 3;
        invincible = 3000;
        bombs = new ArrayList<Bomb>();
        rocks = new ArrayList<Rock>();
        for(int i = 0; i < 4; i++) {
            rocks.add(new Rock(width * Math.random(), height * Math.random(),
                    -1 + 2*Math.random(), -1 + 2*Math.random(),
                    16 + (int)(16 * Math.random())));
        }
    }

    public void surfaceChanged(SurfaceHolder sh, int mode, int width, int height) {
        surfaceHolder = sh;
        this.width = width;
        this.height = height;
        restart();
    }
    public void surfaceDestroyed(SurfaceHolder sh) {
        surfaceHolder = null;
    }

    private class Bomb {
        public double x, y, dx, dy;
        int r = 4;
        int ttl = 77;
        int countdown = 30;
        boolean active = false;
        boolean exploding = false;

        Bomb(double x, double y, double dx, double dy) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
        }

        public void update() {
            x += dx;
            y += dy;

            if(Math.abs(dx) > .1) if(dx > 0) { dx -= friction; } else { dx += friction; } else { dx = 0; }
            if(Math.abs(dy) > .1) if(dy > 0) { dy -= friction; } else { dy += friction; } else { dy = 0; }

            ttl--;
            countdown--;
            if (countdown == 0)
                active = true;
            if (ttl < 0) {
                if(ttl == -1)
                    soundPool.play(explodeSound, (float) (1.0 - (width - x) / width), (float) ((width - x) / width), 1, 0, 1.0f);
                if (ttl > -8) {
                    r += (int)(Math.random()*12 );
                } else {
                    bombsToRemove.add(this);
                }
            }
        }

        public void explode() {
            if(ttl >= 0) {
                countdown = 0;
                ttl = 0;
                active = false;
            }
            exploding = true;
            soundPool.play(explodeSound, (float) (1.0 - (width - x) / width), (float) ((width - x) / width), 1, 0, 1.0f);
        }

        public boolean exploding() {
            return ttl <= 0;

        }

        public void render(Canvas c, Paint p) {
            c.drawCircle((int)x, (int)y, (int)(Math.random()*r), white);
            for(int i = 0; i < r; i++) {
                c.drawLine((float)x, (float)y,
                        (float)(x + -r + 2*Math.random()*r), (float)(y + -r + 2*Math.random()*r), white);
            }

        }
    }

    long frame = 0;

    private ArrayList<Bomb> bombs = new ArrayList<Bomb>();
    private void dropBomb() {
        if(invincible >= 0) {
            bombs.add(new Bomb(x - 4 * dx, y - 4 * dy, dx, dy));
            soundPool.play(dropSound, 1.0f, 1.0f, 0, 1, 1.0f);
            if (score > 0)
                score -= 5;
        }
    }

    ArrayList<Bomb> bombsToRemove = new ArrayList<Bomb>();
    ArrayList<Rock> rocksToRemove = new ArrayList<Rock>();
    ArrayList<Rock> rocksToAdd = new ArrayList<Rock>();

    int lives = 0;
    int text1x = -width;
    int text2x = width;
    int logoTime = 1000;
    char logoFade = 255;
    boolean startPressed = false;

    public void doFrame(long frameTime) {
        if(!paused && surfaceHolder != null) {
            Canvas c = surfaceHolder.lockCanvas();

            if (logoTime > 0 && !(startPressed || buttonPressed)) {
                if (c != null) {
                    if (logoFade < 128) {
                        c.drawARGB(logoFade, 0, 0, 0);
                        logoFade++;
                    } else {
                        c.drawARGB(-logoFade, 0, 0, 0);
                        logoFade--;
                    }
                    if (logoTime > 600) {
                        c.drawText("N/A", width / 2 + (int) (Math.random() * 4), height / 2 + (int) (Math.random() * 4), red);
                        c.drawText("Presents", width / 2 + (int) (Math.random() * 4), 2 * height / 3 + (int) (Math.random() * 4), red);
                    }
                    if (logoTime < 500) {
                        c.drawText("X + 1", width / 2 + (int) (Math.random() * 4), height / 2 + (int) (Math.random() * 4), red);
                    }
                }
                logoTime--;
            } else {
                buttonPressed = false;
                logoTime = 0;
                bombsToRemove.clear();
                rocksToRemove.clear();
                rocksToAdd.clear();

        /* Update player position */
                if (dx < 8)
                    dx += ax;
                if (dx > 8) dx = 8;
                if (dx < -8) dx = -8;
                x += dx;
                if (Math.abs(dx) > .1)
                    if (dx > 0) {
                        dx -= friction;
                    } else {
                        dx += friction;
                    }
                else {
                    dx = 0;
                }
                if (dy < 8)
                    dy += ay;
                if (dy > 8) dy = 8;
                if (dy < -8) dy = -8;
                y += dy;
                if (Math.abs(dy) > .1) if (dy > 0) {
                    dy -= friction;
                } else {
                    dy += friction;
                }
                else {
                    dy = 0;
                }

                if (x > width) {
                    x = 0;
                }
                if (x < 0) {
                    x = width;
                }
                if (y > height) {
                    y = 0;
                }
                if (y < 0) {
                    y = height;
                }

                if(usingTouchscreen)
                    ax = ay = 0;

                boolean playerDead = false;

                for (Rock rock : rocks)
                    rock.update();

        /* Has the player hit a rock? */
                if (invincible == 0) {
                    for (Rock rock : rocks) {
                        double dx = rock.x - x;
                        double dy = rock.y - y;
                        double d = Math.sqrt((dx * dx) + (dy * dy));
                        if (d < (rock.r + 8) && !rock.beenHit) {
                            dx = 0;
                            dy = 0;
                            invincible = -100;
                            lives--;
                            text1x = 0;
                            playerDead = true;
                            afterGameCountdown = 0;
                            rock.split();
                            soundPool.play(explodeSound, (float) (1.0 - (width - x) / width), (float) ((width - x) / width), 1, 0, 1.0f);
                            break;
                        }
                    }
                } else {
                    for (Rock rock : rocks) {
                        double dx = rock.x - x;
                        double dy = rock.y - y;
                        double d = Math.sqrt((dx * dx) + (dy * dy));
                        if (d < (rock.r + 16) && !rock.beenHit) {
                            score += 50;
                            rock.split();
                            soundPool.play(explodeSound, (float) (1.0 - (width - x) / width), (float) ((width - x) / width), 1, 0, 1.0f);
                            break;
                        }
                    }
                }

        /* Has the player hit a bomb? */
        /*
        if(!playerDead) {
            for (Bomb bomb : bombs) {
                if (!bomb.active)
                    continue;
                double dx = x - bomb.x;
                double dy = y - bomb.y;
                double d = Math.sqrt((dx * dx) + (dy * dy));
                if ((int) d <= (8 + bomb.r)) {
                    bomb.explode();
                    x = width / 2;
                    y = height / 2;
                    lives--;
                    playerDead = true;
                    break;
                }
            }
        }
         */
                for (Bomb bomb : bombs) {
                    bomb.update();
                    if (bomb.ttl < -8)
                        bomb.explode();
                }

        /* Has a rock hit a bomb? */
                for (Rock rock : rocks) {
                    for (Bomb bomb : bombs) {
                        if (!bomb.exploding) {
                            double dx = rock.x - bomb.x;
                            double dy = rock.y - bomb.y;
                            double d = Math.sqrt((dx * dx) + (dy * dy));
                            if ((int) d <= (rock.r + bomb.r)) {
                                bomb.explode();
                                rock.split();
                                score += 100;
                                break;
                            }
                        }
                    }
                }


                for (Bomb bomb : bombsToRemove) {
                    bombs.remove(bomb);
                }
                for (Rock rock : rocksToRemove) {
                    rocks.remove(rock);
                }
                for (Rock rock : rocksToAdd) {
                    rocks.add(rock);
                }

                if (Math.random() < 0.0025) {
                    rocks.add(new Rock());
                }

        /* Has a rock hit a rock ?*/
                for (Rock r1 : rocks) {
                    for (Rock r2 : rocks) {
                        if (r1 != r2) {
                            double dx = r1.x - r2.x;
                            double dy = r1.y - r2.y;
                            double d = Math.sqrt((dx * dx) + (dy * dy));
                            if ((int) d <= (r1.r + r2.r)) {
                                r1.dx = -1 + 2 * Math.random();
                                r1.dy = -1 + 2 * Math.random();
                                r2.dx = -1 + 2 * Math.random();
                                r2.dy = -1 * 2 * Math.random();
                            }
                        }
                    }
                }

                if (lives > 0) {
                    if (c != null) {
                        c.drawColor(Color.BLACK);

                        long X = Math.round(x);
                        long Y = Math.round(y);
                        if (invincible < 0) {
                            c.drawCircle(X, Y, (int) (Math.random() * -invincible), red);
                            invincible++;
                            if (invincible == 0) {
                                x = width / 2;
                                y = height / 2;
                                invincible = 10000;
                            }
                        } else {
                            c.drawCircle(X, Y, 8, red);
                            c.drawLine(X - 4, Y - 4, X + 4, Y + 4, cyan);
                            c.drawLine(X - 4, Y + 4, X + 4, Y - 4, cyan);
                        }

                        int nseg = 16;
                        double segrads = 2 * Math.PI / nseg;
                        for (int i = 0; i < nseg; i++) {
                            int r = 8;
                            if (invincible > 0) {
                                invincible--;
                                r = 16;
                            } else if (invincible < 0) {
                                r = -invincible;
                            }

                            int r1 = (int) (r + Math.random() * 4);
                            int r2 = (int) (r + Math.random() * 4);

                            double theta1 = i * segrads + Math.random();
                            double theta2 = (i + 1) * segrads + Math.random();

                            int x1 = (int) (X + r1 * Math.sin(theta1));
                            int y1 = (int) (Y + r1 * Math.cos(theta1));
                            int x2 = (int) (X + r2 * Math.sin(theta2));
                            int y2 = (int) (Y + r2 * Math.cos(theta2));

                            c.drawLine(x1, y1, x2, y2, statix2);
                            c.drawLine(x1, y1, x2, y2, white);
                        }

                        for (Bomb bomb : bombs) {
                            bomb.render(c, white);
                        }

                        for (Rock rock : rocks) {
                            rock.render(c);
                        }

                        c.drawText(Integer.toString((int) score), 10, 60, red);
                        c.drawText(Integer.toString(lives), width - 50, 60, red);
                    }
                    if (score > 0)
                        score -= 0.05;
                } else {
                    for (Rock rock : rocks) {
                        rock.render(c);
                    }

                    if (c != null) {
                        afterGameCountdown++;

                        c.drawARGB(24, 0, 0, 0);
                        c.drawText(Integer.toString((int) score), 10, 60, red);
                        c.drawText(Integer.toString(lives), width - 50, 60, red);
                        c.drawText("GAME OVER", text1x, height / 2, red);
                        c.drawText("PRESS START OR TAP SCREEN", 1 * width / 3 - text1x, 3 * height / 5, red);

                        c.drawText("Burton Samograd - 2015", width / 4, 4 * height / 5, cyan);

                        text1x++;
                    }
                }
            }

            for (int i = 0; i < height; i++) {
                if (Math.random() < 0.01)
                    c.drawLine(0, i, width, i, statix);
            }

            if (frame % 14 == 0)
                for (int i = 0; i < music.size(); i++)
                    if (Math.random() < .0025)
                        soundPool.play((int) (music.get((int) (Math.random() * music.size()))),
                                (float) Math.random() / 2, (float) Math.random() / 2, 0, (int) (4 * Math.random()), 1.0f);


            surfaceHolder.unlockCanvasAndPost(c);
            Choreographer.getInstance().postFrameCallback(this);
        }
    }

    private void info(String msg) { Log.i("x", msg); }

    int afterGameCountdown = 0;

    double bump = .27;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean state = false;

        switch(event.getAction()) {
            case KeyEvent.ACTION_DOWN:
                state = true;
                break;
            case KeyEvent.ACTION_UP:
                state = false;
                break;
        }

        switch(event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_UP:
                if (state) ay = -bump; else ay = 0; break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (state) ay = bump; else ay = 0; break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (state) ax = -bump; else ax = 0; break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (state) ax = bump; else ax = 0; break;
            case KeyEvent.KEYCODE_BUTTON_A:
                dropBomb(); break;
            case KeyEvent.KEYCODE_BUTTON_B:
                if(score > 1000) { invincible = 10000; score -= 1000; }; break;
            case KeyEvent.KEYCODE_BUTTON_X:
                bump += .1; break;
            case KeyEvent.KEYCODE_BUTTON_Y:
                bump -= .1; break;
            case KeyEvent.KEYCODE_BUTTON_L1:
                info("l1"); break;
            case KeyEvent.KEYCODE_BUTTON_R1:
                info("r1"); break;
            case KeyEvent.KEYCODE_BUTTON_START:
                if(state) restart(); startPressed = true; break;
            case KeyEvent.KEYCODE_BUTTON_SELECT:
                info("select"); break;
            case KeyEvent.KEYCODE_BUTTON_MODE:
                info("mode"); break;
            default:
                return super.dispatchKeyEvent(event);
        }
        return true;
    }

    boolean buttonPressed = false;

    float x1[] = new float[2], y1[] = new float[2];
    float x2[] = new float[2], y2[] = new float [2];
    boolean bombDropped = false;
    boolean usingTouchscreen = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        buttonPressed = true;
        //if(Math.random() < .10)
//            dropBomb();

        usingTouchscreen = true;
        float tx = 0, ty = 0;
        for(int i = 0; i < event.getPointerCount() && i < 2 ; i++) {
            float x = event.getX(i);
            float y = event.getY(i);

            int slot, movingSlot = -1;
            if(x < width/2) slot = 0; else slot = 1;

            if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN || event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                bombDropped = false;
            } else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP || event.getActionMasked() == MotionEvent.ACTION_UP) {
                if(!bombDropped)
                    dropBomb();
                bombDropped = true;
                x1[slot] = x;
                y1[slot] = y;
            } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                float dx = x - x1[slot];
                float dy = y - y1[slot];

                double sx = (dx < 0) ? -1 : 1;
                tx += sx * Math.log(Math.abs(1+dx));
                ax = sx * bump;

                double sy = (dy < 0) ? -1 : 1;
                ty += sy * Math.log(Math.abs(1+dy));
                ay = sy * bump;

                x1[slot] += dx/8;
                y1[slot] += dy/8;
            }
            //dx = tx;
            //dy = ty;
        }
        if(lives == 0 && afterGameCountdown > 60)
            restart();

        return true;
    }

}
