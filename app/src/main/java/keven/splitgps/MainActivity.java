package keven.splitgps;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private static LinearLayout splitlayout_vertical;
    private static LinearLayout last_split;
    private static LinearLayout[] splits = new LinearLayout[16];
    private static TextView[] splitnames = new TextView[16];
    private static TextView[] splitdiffs = new TextView[16];
    private static TextView[] splittimes = new TextView[16];
    private static TextView split_total, split_time, split_pb, split_besttime, split_previous,
            split_pts, split_bpt, split_sob, title;
    private static long initialTime;
    private static Handler handler;
    private static final long MILLIS_IN_SEC = 1000L;
    private static final int SECS_IN_MIN = 60;
    private static final int MIN_HOUR = 60;
    private static Button bt_split;
    private static int state = -1;
    private static long total_time;


    private void init_static_vars(){
        splitlayout_vertical =  (LinearLayout)findViewById(R.id.splitlayout);
        last_split =  (LinearLayout)findViewById(R.id.last_split);
        split_total = (TextView)findViewById(R.id.split_total);
        split_time = (TextView)findViewById(R.id.split_time);
        split_pb = (TextView)findViewById(R.id.split_pb);
        split_besttime = (TextView)findViewById(R.id.split_previous);
        split_previous = (TextView)findViewById(R.id.split_previous);
        split_pts = (TextView)findViewById(R.id.split_pts);
        split_bpt = (TextView)findViewById(R.id.split_bpt);
        split_sob = (TextView)findViewById(R.id.split_sob);
        title = (TextView)findViewById(R.id.title);
    }

    private void init_splits(){
        String[] names = {"Cap","Cascade","Sand","Lake","Wooded","Cloud","Lost","Mecha Wiggler","Metro",
                "Snow", "Seaside", "Luncheon", "Ruined", "Bunnies", "Chinatown", "Escape"};
        String[] diffs = {"-00:06","-00:03","-05:04","-06:00","-04:00","-04:03","-02:09","-04:07","-02:00",
                "-27:05","-27:04","-27:09","-28:05","-29:06","-26:01","-30:07"};
        String[] times = {"2:22","5:15","10:19","13:16","18:39","20:10","22:18","25:47","30:19",
                "35:01","38:32","44:48","47:58","52:22","55:51","1:01:07"};



        for(int i = 0; i < splits.length; i++){
            splits[i] = new LinearLayout(this);

            splitnames[i] = new TextView(this);
            splitnames[i].setText(names[i]);
            splitnames[i].setWidth(630);
            splitnames[i].setTextSize(30);
            if (i != splits.length -1)
                splits[i].addView(splitnames[i]);
            else
                last_split.addView(splitnames[i]);

            splitdiffs[i] = new TextView(this);
            splitdiffs[i].setText("");
            splitdiffs[i].setWidth(200);
            splitdiffs[i].setTextSize(20);
            if (i != splits.length -1)
                splits[i].addView(splitdiffs[i]);
            else
                last_split.addView(splitdiffs[i]);

            splittimes[i] = new TextView(this);
            splittimes[i].setText(times[i]);
            splittimes[i].setWidth(250);
            splittimes[i].setTextSize(20);
            splittimes[i].setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            if (i != splits.length -1)
                splits[i].addView(splittimes[i]);
            else
                last_split.addView(splittimes[i]);

            if (i != splits.length -1)
                splitlayout_vertical.addView(splits[i]);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init_static_vars();
        init_splits();

        bt_split = (Button) findViewById(R.id.bt_split);
        bt_split.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                state++;
                if (state == 0) {
                    initialTime = System.currentTimeMillis();
                    handler.postDelayed(runnable, 20);
                }
            }
        });
        handler = new Handler();
    }

    private static String long_to_string(long time){
        String string_time;
        long hour = time / (MIN_HOUR*SECS_IN_MIN);
        long minute = (time / MIN_HOUR) % SECS_IN_MIN;
        long second = (time % MIN_HOUR) % SECS_IN_MIN;
        if (minute >= 1 || hour >=1){
            if (hour >= 1){
                string_time = String.format("%d:%02d:%02d", hour,minute,second);
            }
            else{
                string_time = String.format("%d:%02d", minute,second);
            }
        }
        else{
            string_time = String.format("%d",second);
        }
        return string_time;
    }

    private final static Runnable runnable = new Runnable() {
        @Override
        public void run() {
            switch (state){
                case 0:
                    total_time = (System.currentTimeMillis() - initialTime)/20;
                    split_total.setText(long_to_string(total_time));
                    break;

                default:
                    break;
            }
            handler.postDelayed(runnable, 20);
        }
    };
}
