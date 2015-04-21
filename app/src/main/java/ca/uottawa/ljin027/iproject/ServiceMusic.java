package ca.uottawa.ljin027.iproject;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

/**
 * This class is implemented for CSI5175 Assignment 3.
 * This class implements a music playing service. The service should be created when the app starts,
 * and destroyed when the app is switched out.
 *
 * @author Ling Jin
 * @version 1.0
 * @since 17/04/2015
 */
public class ServiceMusic extends Service {
    MediaPlayer mPlayer;

    public IBinder onBind(Intent arg) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = MediaPlayer.create(this, R.raw.music);
        mPlayer.setLooping(true);
        mPlayer.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
        super.onDestroy();
    }
}