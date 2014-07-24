package com.tondol.btpingpong.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by hosaka on 2014/07/24.
 */
public class MyRenderer implements GLSurfaceView.Renderer {
    private MainActivity mActivity = null;
    private FloatBuffer mSquareBuffer = null;
    private FloatBuffer mTextureBuffer = null;
    private int[] mTextures = null;

    private Player mPlayer = null;
    private Square mSquare = null;
    private double mDisplayRatio = 1.0;

    public MyRenderer(MainActivity activity) {
        mActivity = activity;
        // 初期値はテキトーに与える
        mPlayer = new Player(0.0, 0.5, 0.1);
        mSquare = new Square(0.0, 0.0, 0.5, 0.0, 0.0);
    }

    public double getDisplayRatio() {
        return mDisplayRatio;
    }

    public Square getSquare() {
        return mSquare;
    }

    public void setSquarePosition(double x, double y, double velocityX, double velocityY) {
        mSquare.setPosition(x, y, velocityX, velocityY);
    }

    public void setPlayerPosition(double x) {
        mPlayer.setPosition(x);
    }


    public class Player {
        private double mX;
        private double mWidth;
        private double mHeight;
        private boolean mIntersected = false;

        public Player(double x, double width, double height) {
            mX = x;
            mWidth = width;
            mHeight = height;
        }

        public double getX() {
            return mX;
        }

        public double getWidth() {
            return mWidth;
        }

        public double getHeight() {
            return mHeight;
        }

        public void setPosition(double x) {
            mX = x;
            if (mX < -(1.0 - mWidth / 2.0)) {
                mX = -(1.0 - mWidth / 2.0);
            }
            if (mX > 1.0 - mWidth / 2.0) {
                mX = 1.0 - mWidth / 2.0;
            }
        }

        private void update(GL10 gl10, boolean intersected) {
            mIntersected = intersected;
            gl10.glMatrixMode(GL10.GL_MODELVIEW);
            gl10.glLoadIdentity();
            gl10.glTranslatef((float) mX, (float) -(mDisplayRatio - mHeight / 2.0), 0.0f);
            gl10.glScalef((float) mWidth, (float) mHeight, 1.0f);
        }

        public void render(GL10 gl10) {
            gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl10.glVertexPointer(2, GL10.GL_FLOAT, 0, mSquareBuffer);
            if (mIntersected) {
                gl10.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
            } else {
                gl10.glColor4f(0.0f, 0.0f, 1.0f, 1.0f);
            }
            gl10.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
            gl10.glDisableClientState(GL10.GL_VERTEX_ARRAY);

            mIntersected = false;
        }
    }

    public class Square {
        private double mX;
        private double mY;
        private double mSide;
        private double mVelocityX;
        private double mVelocityY;

        public Square(double x, double y, double side, double velocityX, double velocityY) {
            mX = x;
            mY = y;
            mSide = side;
            mVelocityX = velocityX;
            mVelocityY = velocityY;
        }

        public double getX() {
            return mX;
        }

        public double getY() {
            return mY;
        }

        public double getVelocityX() {
            return mVelocityX;
        }

        public double getVelocityY() {
            return mVelocityY;
        }

        public double getSide() {
            return mSide;
        }

        public void setPosition(double x, double y, double velocityX, double velocityY) {
            mX = x;
            mY = y;
            mVelocityX = velocityX;
            mVelocityY = velocityY;
        }

        private void updatePosition(boolean intersected) {
            mX += mVelocityX;
            mY += mVelocityY;

            if (mX < -(1.0 - mSide / 2.0)) {
                mX = -(1.0 - mSide / 2.0);
                mVelocityX *= -1.0;
                mActivity.getSoundManager().playReflect();
            }
            if (mX > 1.0 - mSide / 2.0) {
                mX = 1.0 - mSide / 2.0;
                mVelocityX *= -1.0;
                mActivity.getSoundManager().playReflect();
            }

            if (intersected && mVelocityY < 0.0) {
                // !向きをランダムにする!
                double theta = Math.random() * 2.0 / 3.0 * Math.PI + 1.0 / 6.0 * Math.PI;
                mVelocityX = 0.05 * Math.cos(theta);
                mVelocityY = 0.05 * Math.sin(theta);
                mActivity.getSoundManager().playReflect();
            }
        }

        private void updateMatrix(GL10 gl10) {
            gl10.glMatrixMode(GL10.GL_MODELVIEW);
            gl10.glLoadIdentity();
            gl10.glTranslatef((float) mX, (float) mY, 0.0f);
            gl10.glScalef((float) mSide, (float) mSide, 1.0f);
        }

        public void update(GL10 gl10, boolean intersected) {
            updatePosition(intersected);
            updateMatrix(gl10);
        }

        public void render(GL10 gl10) {
            gl10.glEnable(GL10.GL_TEXTURE_2D);
            gl10.glBindTexture(GL10.GL_TEXTURE_2D, mTextures[0]);
            gl10.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
            gl10.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextureBuffer);
            gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl10.glVertexPointer(2, GL10.GL_FLOAT, 0, mSquareBuffer);
            gl10.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            gl10.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
            gl10.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl10.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
            gl10.glDisable(GL10.GL_TEXTURE_2D);
        }
    }


    private static final FloatBuffer createFloatBuffer(float[] array){
        ByteBuffer bb = ByteBuffer.allocateDirect(array.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(array);
        fb.position(0);
        return fb;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int w, int h) {
        mDisplayRatio = (double) h / (double) w;

        gl10.glViewport(0, 0, w, h);
        gl10.glMatrixMode(GL10.GL_PROJECTION);
        gl10.glLoadIdentity();
        gl10.glOrthof(-1.0f, 1.0f, (float) -mDisplayRatio, (float) mDisplayRatio, 0.5f, -0.5f); // X範囲，Y範囲，Z範囲

        float[] vertices = {
                -0.5f, -0.5f,
                +0.5f, -0.5f,
                -0.5f, +0.5f,
                +0.5f, +0.5f,
        };
        mSquareBuffer = createFloatBuffer(vertices);
        float[] uv = {
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
        };
        mTextureBuffer = createFloatBuffer(uv);

        mTextures = new int[1];
        Bitmap image = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.texture);
        gl10.glGenTextures(1, mTextures, 0);
        gl10.glBindTexture(GL10.GL_TEXTURE_2D, mTextures[0]);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, image, 0);
        gl10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
        gl10.glBindTexture(GL10.GL_TEXTURE_2D, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        gl10.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);

        // 状態を変化させる
        if (mActivity.getGameState() == MainActivity.GameState.MyTurn) {
            // 交差判定
            // バーの上半分のみを接触範囲とする
            // コマの下半分のみを接触範囲とする
            boolean intersected = mPlayer.getX() - mPlayer.getWidth() / 2.0 < mSquare.getX() + mSquare.getSide() / 2.0 &&
                    mPlayer.getX() + mPlayer.getWidth() / 2.0 > mSquare.getX() - mSquare.getSide() / 2.0 &&
                    -(mDisplayRatio - mPlayer.getHeight() / 2.0) < mSquare.getY() &&
                    -(mDisplayRatio - mPlayer.getHeight()) > mSquare.getY() - mSquare.getSide() / 2.0;

            if (mSquare.getY() < -mDisplayRatio) {
                mActivity.setGameState(MainActivity.GameState.Default, true);
                mActivity.getSoundManager().playLose();
            } else if (mSquare.getY() > mDisplayRatio) {
                mActivity.setGameState(MainActivity.GameState.YourTurn, true);
                mActivity.getSoundManager().playWin();
            }

            mPlayer.update(gl10, intersected);
            mPlayer.render(gl10);

            mSquare.update(gl10, intersected);
            mSquare.render(gl10);
        } else {
            mPlayer.update(gl10, false);
            mPlayer.render(gl10);
        }
    }
}
