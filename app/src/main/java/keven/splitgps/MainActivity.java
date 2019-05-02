package keven.splitgps;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private static TextView section_label;
    private static long initialTime;
    private static Handler handler;
    private static boolean isRunning = false;
    private static final long MILLIS_IN_SEC = 1000L;
    private static final int SECS_IN_MIN = 60;
    private static final int MIN_HOUR = 60;

    Button btnGps;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        section_label = (TextView) findViewById(R.id.section_label);
        btnGps = (Button) findViewById(R.id.btnGps);
        btnGps.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                isRunning = true;
                initialTime = System.currentTimeMillis();
                handler.postDelayed(runnable, 1000L);
            }
        });
        handler = new Handler();
    }


    private final static Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                long total_time = (System.currentTimeMillis() - initialTime)/MILLIS_IN_SEC;
                long hour = total_time / (MIN_HOUR*SECS_IN_MIN);
                long minute = (total_time / MIN_HOUR) % SECS_IN_MIN;
                long second = (total_time % MIN_HOUR) % SECS_IN_MIN;
                section_label.setText(String.format("%02d:%02d:%02d", hour,minute,second));
                handler.postDelayed(runnable, MILLIS_IN_SEC);
            }
        }
    };

}
