package id.sch.smktelkom_mlg.project2.xirpl50714193336.qiblacompass;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Config;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.sql.Time;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Compass";

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SampleView kiblat;
    private float[] mValues;
    //Untuk mencari arah utara
    private final SensorEventListener mListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (Config.DEBUG) Log.d(TAG,
                    "sensorChanged (" + event.values[0] + ", " + event.values[1] + ", " + event.values[2] + ")");
            mValues = event.values;
            if (kiblat != null) {
                kiblat.invalidate();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private double lonMosque;
    private double latMosque;
    private LocationManager lm;
    private LocationListener locListenD;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        kiblat = new SampleView(this);
        setContentView(kiblat);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        changeStatusBarColor();

        // untuk memanggil gps
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location loc = lm.getLastKnownLocation("gps");

        // meminta location manager untuk mengirim lokasi updates
        locListenD = new DispLocListener();
        lm.requestLocationUpdates("gps", 30000L, 10.0f, locListenD);

        locListenD = new DispLocListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.requestLocationUpdates("gps", 30000L, 10.0f, locListenD);
    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    // mencari arah ka'bah
    private double QiblaCount(double lngMasjid, double latMasjid) {
        double lngKabah = 39.82616111;
        double latKabah = 21.42250833;
        double lKlM = (lngKabah - lngMasjid);
        double sinLKLM = Math.sin(lKlM * 2.0 * Math.PI / 360);
        double cosLKLM = Math.cos(lKlM * 2.0 * Math.PI / 360);
        double sinLM = Math.sin(latMasjid * 2.0 * Math.PI / 360);
        double cosLM = Math.cos(latMasjid * 2.0 * Math.PI / 360);
        double tanLK = Math.tan(latKabah * 2 * Math.PI / 360);
        double denominator = (cosLM * tanLK) - sinLM * cosLKLM;

        double Qibla;
        double direction;

        Qibla = Math.atan2(sinLKLM, denominator) * 180 / Math.PI;
        direction = Qibla < 0 ? Qibla + 360 : Qibla;
        return direction;

    }

    //resume location update ketika onresume
    @Override
    protected void onResume() {
        if (Config.DEBUG) Log.d(TAG, "onResume");
        super.onResume();

        mSensorManager.registerListener(mListener, mSensor,
                SensorManager.SENSOR_DELAY_GAME);
    }

    //stop location update ketika onstop
    @Override
    protected void onStop() {
        if (Config.DEBUG) Log.d(TAG, "onStop");
        mSensorManager.unregisterListener(mListener);
        super.onStop();
    }

    private class SampleView extends View {
        private Paint mPaint = new Paint();
        private Path mPath = new Path();
        private boolean mAnimate;


        public SampleView(Context context) {
            super(context);

            // membuat anak panah
            mPath.moveTo(0, -100);
            mPath.lineTo(20, 120);
            mPath.lineTo(0, 100);
            mPath.lineTo(-20, 120);
            mPath.close();
        }

        // membuat anak panah memanah

        protected void onDraw(Canvas canvas) {
            Paint paint = mPaint;


            paint.setAntiAlias(true);
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);

            int w = canvas.getWidth();
            int h = canvas.getHeight();
            int cx = w / 2;
            int cy = h / 2;
            float Qibla = (float) QiblaCount(lonMosque, latMosque);
            //   float Qiblat = mValues [0] + Qibla;
            canvas.translate(cx, cy);
            if (mValues != null) {
                canvas.rotate(-(mValues[0] + Qibla));
            }
            canvas.drawPath(mPath, mPaint);

            int hours = new Time(System.currentTimeMillis()).getHours();

            if (hours == 4 || hours == 5 || hours == 6 || hours == 7 || hours == 8) {
                this.setBackgroundResource(R.drawable.pagi);
            } else if (hours == 9 || hours == 10 || hours == 11 || hours == 12 || hours == 13 || hours == 14) {
                this.setBackgroundResource(R.drawable.siang);
            } else if (hours == 15 || hours == 16 || hours == 17 || hours == 18 || hours == 19) {
                this.setBackgroundResource(R.drawable.sore);
            } else {
                this.setBackgroundResource(R.drawable.malam);
            }
        }

        @Override
        protected void onAttachedToWindow() {
            mAnimate = true;
            if (Config.DEBUG) Log.d(TAG, "onAttachedToWindow. mAnimate=" + mAnimate);
            super.onAttachedToWindow();
        }

        @Override
        protected void onDetachedFromWindow() {
            mAnimate = false;
            if (Config.DEBUG) Log.d(TAG, "onDetachedFromWindow. mAnimate=" + mAnimate);
            super.onDetachedFromWindow();
        }

    }

    private class DispLocListener implements LocationListener {
        public void onLocationChanged(Location loc) {
            // update TextViews
            latMosque = loc.getLatitude();
            lonMosque = loc.getLongitude();
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }
}
