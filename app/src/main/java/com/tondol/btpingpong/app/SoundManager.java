package com.tondol.btpingpong.app;

import android.app.Activity;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 * Created by hosaka on 2014/07/25.
 */
public class SoundManager {
    private SoundPool mSoundPool = null;
    private int mWinSoundId = 0;
    private int mLoseSoundId = 0;
    private int mReflectSoundId = 0;

    public SoundManager(Activity activity) {
        mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        mWinSoundId = mSoundPool.load(activity, R.raw.win, 0);
        mLoseSoundId = mSoundPool.load(activity, R.raw.lose, 0);
        mReflectSoundId = mSoundPool.load(activity, R.raw.reflect, 0);
    }

    public void playWin() {
        mSoundPool.play(mWinSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
    }

    public void playLose() {
        mSoundPool.play(mLoseSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
    }

    public void playReflect() {
        mSoundPool.play(mReflectSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
    }
}
