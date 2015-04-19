package ca.uottawa.ljin027.iproject;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

/**
 * Created by ljin027 on 17/04/2015.
 */

public class ServiceMusic extends Service
{
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
    public int  onStartCommand(Intent intent, int flags, int startId) {
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