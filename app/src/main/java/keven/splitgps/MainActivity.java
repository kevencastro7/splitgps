package keven.splitgps;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
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

    private static long[] segment_time = new long[16];
    private static long[] pb_time = {142, 173, 304, 177, 323, 91, 128, 209, 272, 282, 211, 376, 190,
            264, 209, 316};

    private static long[] best_time = {132, 163, 278, 167, 308, 90, 118, 183, 262, 267, 210, 366,
            164, 254, 194, 301};

    private static JSONArray pb_time_json, best_time_json, segment_time_json;

    private void init_static_vars(){
        splitlayout_vertical =  (LinearLayout)findViewById(R.id.splitlayout);
        last_split =  (LinearLayout)findViewById(R.id.last_split);
        split_total = (TextView)findViewById(R.id.split_total);
        split_time = (TextView)findViewById(R.id.split_time);
        split_pb = (TextView)findViewById(R.id.split_pb);
        split_besttime = (TextView)findViewById(R.id.split_besttime);
        split_previous = (TextView)findViewById(R.id.split_previous);
        split_pts = (TextView)findViewById(R.id.split_pts);
        split_bpt = (TextView)findViewById(R.id.split_bpt);
        split_sob = (TextView)findViewById(R.id.split_sob);
        title = (TextView)findViewById(R.id.title);
        pb_time_json = new JSONArray();
        best_time_json = new JSONArray();
        segment_time_json = new JSONArray();

        for (int i = 0; i < 16; i++){
            pb_time_json.put(pb_time[i]);
            best_time_json.put(best_time[i]);
        }
    }

    private void reset_splits() throws JSONException {
        segment_time_json = new JSONArray();
        for(int i = 0; i < splits.length; i++){
            splitdiffs[i].setText("");
            splittimes[i].setText(long_to_string(sum_until_i(pb_time, i)));
            splitdiffs[i].setTextColor(Color.rgb(0,0,0));
        }

        split_total.setText(long_to_string(0));
        split_total.setTextColor(Color.rgb(0, 0, 0));
        split_time.setText(long_to_string(0));
        split_previous.setText("-");
        split_previous.setTextColor(Color.rgb(0,0,0));
        split_pb.setText(long_to_string(pb_time[0]));
        split_besttime.setText(long_to_string(best_time[0]));
        split_pts.setText(long_to_string(pb_time[0]-best_time[0]));
        split_bpt.setText(long_to_string(sum_until_i(best_time,splits.length-1)));
        split_sob.setText(long_to_string(sum_until_i(best_time,splits.length-1)));

    }

    private void init_splits(){
        String[] names = {"Cap","Cascade","Sand","Lake","Wooded","Cloud","Lost","Mecha Wiggler","Metro",
                "Snow", "Seaside", "Luncheon", "Ruined", "Bunnies", "Chinatown", "Escape"};

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
            splittimes[i].setText(long_to_string(sum_until_i(pb_time, i)));
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

        split_pb.setText(long_to_string(pb_time[0]));
        split_besttime.setText(long_to_string(best_time[0]));
        split_pts.setText(long_to_string(pb_time[0]-best_time[0]));
        split_bpt.setText(long_to_string(sum_until_i(best_time,splits.length-1)));
        split_sob.setText(long_to_string(sum_until_i(best_time,splits.length-1)));

    }

    private void save_splittime() throws JSONException {
        splittimes[state-1].setText(long_to_string(total_time));
        split_pb.setText(long_to_string(pb_time[state]));
        split_besttime.setText(long_to_string(best_time[state]));
        split_pts.setText(long_to_string(pb_time[state]-best_time[state]));
        split_bpt.setText(long_to_string(sum_until_i_json(segment_time_json,state -1 ) + sum_past_i(best_time,state-1)));
        splitdiffs[state-1].setText(diff_long_to_string(total_time ,sum_until_i(pb_time, state-1)));
        split_previous.setText(diff_long_to_string(segment_time_json.getLong(state-1) ,best_time[state-1]));

        if (segment_time_json.getLong(state-1) < best_time[state -1]){
            split_previous.setTextColor(Color.rgb(255,215,0));
            best_time[state-1] = segment_time_json.getLong(state-1);
            splitdiffs[state-1].setTextColor(Color.rgb(255,215,0));
        }
        else if (total_time < sum_until_i(pb_time, state-1) ){
            split_previous.setTextColor(Color.rgb(255,0,0));
            if (segment_time_json.getLong(state-1) < pb_time[state-1]) {
                splitdiffs[state - 1].setTextColor(Color.rgb(0, 255, 0));
            }
            else {
                splitdiffs[state - 1].setTextColor(Color.rgb(0, 150, 0));
            }
        }
        else{
            split_previous.setTextColor(Color.rgb(255,0,0));
            if (segment_time_json.getLong(state-1) < pb_time[state-1]) {
                splitdiffs[state - 1].setTextColor(Color.rgb(150, 0, 0));
            }
            else {
                splitdiffs[state - 1].setTextColor(Color.rgb(255, 0, 0));
            }
        }
        split_sob.setText(long_to_string(sum_until_i(best_time,splits.length-1)));
    }

    private void end_splittime() throws JSONException {
        splittimes[splits.length-1].setText(long_to_string(total_time));
        split_pts.setText(long_to_string(pb_time[splits.length-1]-best_time[splits.length-1]));
        split_bpt.setText(long_to_string(sum_until_i_json(segment_time_json,splits.length-1 ) + sum_past_i(best_time,splits.length-1)));
        splitdiffs[splits.length-1].setText(diff_long_to_string(total_time ,sum_until_i(pb_time, splits.length-1)));
        split_previous.setText(diff_long_to_string(segment_time_json.getLong(splits.length-1) ,best_time[splits.length-1]));

        if (segment_time_json.getLong(splits.length-1) < best_time[splits.length-1]){
            split_previous.setTextColor(Color.rgb(255,215,0));
            best_time[splits.length-1] = segment_time_json.getLong(splits.length-1);
            splitdiffs[splits.length-1].setTextColor(Color.rgb(255,215,0));
        }
        else if (total_time < sum_until_i(pb_time, splits.length-1) ){
            split_previous.setTextColor(Color.rgb(255,0,0));
            if (segment_time_json.getLong(splits.length-1) < pb_time[splits.length-1]) {
                splitdiffs[splits.length-1].setTextColor(Color.rgb(0, 255, 0));
            }
            else {
                splitdiffs[splits.length-1].setTextColor(Color.rgb(0, 150, 0));
            }
        }
        else{
            split_previous.setTextColor(Color.rgb(255,0,0));
            if (segment_time_json.getLong(splits.length-1) < pb_time[splits.length-1]) {
                splitdiffs[splits.length-1].setTextColor(Color.rgb(150, 0, 0));
            }
            else {
                splitdiffs[splits.length-1].setTextColor(Color.rgb(255, 0, 0));
            }
        }
        split_sob.setText(long_to_string(sum_until_i(best_time,splits.length-1)));

        if (total_time < sum_until_i(pb_time, splits.length -1)){
            for (int i = 0; i< splits.length;i++)
                pb_time[i] = segment_time_json.getLong(i);
            split_total.setTextColor(Color.rgb(255, 215, 0));
        }
        else
            split_total.setTextColor(Color.rgb(255, 0, 0));
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
                if (state == -1){
                    try {
                        reset_splits();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if (state == 0) {
                    initialTime = System.currentTimeMillis();
                    handler.postDelayed(runnable, 100);
                }
                else if (state < splits.length) {
                    try {
                        save_splittime();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    state = -2;
                    try {
                        end_splittime();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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

    private static String diff_long_to_string(long time1, long time2){
        long time = time1 - time2;
        if (time >= 0) return "+" + long_to_string(time);
        else return  "-" + long_to_string(time*-1);

    }

    public static int sum_until_i( long[] vector, int i){
        int sum = 0;
        for (int j = 0; j <= i; j++){
            sum += vector[j];
        }
        return sum;
    }

    public static int sum_past_i( long[] vector, int i){
        int sum = 0;
        for (int j = splits.length - 1; j > i; j--){
            sum += vector[j];
        }
        return sum;
    }

    public static int sum_until_i_json( JSONArray vector, int i) throws JSONException {
        int sum = 0;
        for (int j = 0; j <= i; j++){
            sum += vector.getLong(i);
        }
        return sum;
    }

    private final static Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(state >= 0){
                total_time = (System.currentTimeMillis() - initialTime)/100;
                long aux_time = total_time;
                for (int i = state -1 ; i >= 0; i--) {
                    try {
                        aux_time -= segment_time_json.getLong(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    segment_time_json.put(state, aux_time);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    if (segment_time_json.getLong(state) >= pb_time[state] || segment_time_json.getLong(state) >= best_time[state] ||
                            total_time >= sum_until_i(pb_time, state)){
                        splitdiffs[state].setText(diff_long_to_string(total_time,sum_until_i(pb_time, state)));
                        split_bpt.setText(long_to_string(sum_until_i_json(segment_time_json,state ) + sum_past_i(best_time,state)));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                split_total.setText(long_to_string(total_time));
                try {
                    split_time.setText(long_to_string(segment_time_json.getLong(state)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                handler.postDelayed(runnable, 100);

            }
        }
    };
}
