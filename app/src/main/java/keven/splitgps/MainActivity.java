package keven.splitgps;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
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

    public static SQLiteDatabase db;
    public static PostDbHelper mDbHelper;
    public static int path_id = 1;
    private static JSONArray pb_time_json, best_time_json, segment_time_json, mean_time_json, last_time_json, split_names, latitude, longitude;
    public static int run_count, split_count;

    public static final class DataBase {

        private DataBase() {}

        public static class Path implements BaseColumns {
            public static final String TABLE_NAME = "path";
            public static final String TITLE = "title";
            public static final String RUN_COUNT = "run_count";
            public static final String SPLIT_COUNT = "split_count";
            public static final String PB = "personal_best";
            public static final String BEST = "best";
            public static final String MEAN = "mean";
            public static final String LAST = "last";
            public static final String SPLIT_NAMES = "split_names";
            public static final String LONGITUDE = "longitude";
            public static final String LATITUDE = "latitude";
        }

        public static class Run implements BaseColumns {
            public static final String TABLE_NAME = "run";
            public static final String PATH_ID = "path_id";
            public static final String SEGMENT = "segment";
        }

    }

    public class PostDbHelper extends SQLiteOpenHelper {
        private static final String TEXT_TYPE = " TEXT";
        private static final String INTEGER_TYPE = " INTEGER";
        private static final String COMMA_SEP = ",";
        private static final String SQL_CREATE_PATH =
                "CREATE TABLE " + DataBase.Path.TABLE_NAME + " (" + DataBase.Path._ID + " INTEGER PRIMARY KEY," +
                        DataBase.Path.TITLE + TEXT_TYPE + COMMA_SEP +
                        DataBase.Path.RUN_COUNT + INTEGER_TYPE + COMMA_SEP +
                        DataBase.Path.SPLIT_COUNT + INTEGER_TYPE + COMMA_SEP +
                        DataBase.Path.PB + TEXT_TYPE + COMMA_SEP +
                        DataBase.Path.BEST + TEXT_TYPE + COMMA_SEP +
                        DataBase.Path.MEAN + TEXT_TYPE + COMMA_SEP +
                        DataBase.Path.LAST + TEXT_TYPE + COMMA_SEP +
                        DataBase.Path.SPLIT_NAMES + TEXT_TYPE + COMMA_SEP +
                        DataBase.Path.LONGITUDE + TEXT_TYPE + COMMA_SEP +
                        DataBase.Path.LATITUDE + TEXT_TYPE + " )";

        private static final String SQL_CREATE_RUN =
                "CREATE TABLE " + DataBase.Run.TABLE_NAME + " (" + DataBase.Run._ID + " INTEGER PRIMARY KEY," +
                        DataBase.Run.PATH_ID + INTEGER_TYPE + COMMA_SEP +
                        DataBase.Run.SEGMENT + TEXT_TYPE + " )";

        private static final String SQL_DELETE_PATH =
                "DROP TABLE IF EXISTS " + DataBase.Path.TABLE_NAME;
        private static final String SQL_DELETE_RUN=
                "DROP TABLE IF EXISTS " + DataBase.Run.TABLE_NAME;

        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "SplitGPS.db";

        public PostDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_PATH);
            db.execSQL(SQL_CREATE_RUN);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_PATH);
            db.execSQL(SQL_DELETE_RUN);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    private static int create_new_run(JSONArray segment_time){
        ContentValues values = new ContentValues();
        values.put(DataBase.Run.PATH_ID, path_id);
        values.put(DataBase.Run.SEGMENT, segment_time.toString());
        return (int)db.insert(DataBase.Run.TABLE_NAME, null, values);
    }

    private Cursor get_path_by_id(int id){
        String[] projection = {
                DataBase.Path._ID,
                DataBase.Path.TITLE,
                DataBase.Path.RUN_COUNT,
                DataBase.Path.SPLIT_COUNT,
                DataBase.Path.PB,
                DataBase.Path.BEST,
                DataBase.Path.MEAN,
                DataBase.Path.LAST,
                DataBase.Path.SPLIT_NAMES,
                DataBase.Path.LONGITUDE,
                DataBase.Path.LATITUDE,
        };
        String selection = DataBase.Path._ID + " = ?";
        String[] selectionArgs = { String.format("%d", id) };
        String sortOrder =
                DataBase.Path._ID + " DESC";

        return db.query(
                DataBase.Path.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

    }

    private void update_path_json(int id, String COLUMN, JSONArray Value){
        ContentValues values = new ContentValues();
        values.put(COLUMN, Value.toString());
        String selection = DataBase.Path._ID + " = ?";
        String[] selectionArgs = { String.format("%d", id) };
        int count = db.update(
                DataBase.Path.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    private void update_path_count(int id, String COLUMN, int Value){
        ContentValues values = new ContentValues();
        values.put(COLUMN, Value);
        String selection = DataBase.Path._ID + " = ?";
        String[] selectionArgs = { String.format("%d", id) };
        int count = db.update(
                DataBase.Path.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    private void init_static_vars() throws JSONException {
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
        get_from_db();
    }

    private void get_from_db() throws JSONException {
        segment_time_json = new JSONArray();
        Cursor c = get_path_by_id(path_id);
        c.moveToFirst();
        title.setText(c.getString(c.getColumnIndexOrThrow(DataBase.Path.TITLE)));
        run_count = c.getInt(c.getColumnIndexOrThrow(DataBase.Path.RUN_COUNT));
        split_count = c.getInt(c.getColumnIndexOrThrow(DataBase.Path.SPLIT_COUNT));
        pb_time_json = new JSONArray(c.getString(c.getColumnIndexOrThrow(DataBase.Path.PB)));
        best_time_json = new JSONArray(c.getString(c.getColumnIndexOrThrow(DataBase.Path.BEST)));
        mean_time_json = new JSONArray(c.getString(c.getColumnIndexOrThrow(DataBase.Path.MEAN)));
        last_time_json = new JSONArray(c.getString(c.getColumnIndexOrThrow(DataBase.Path.LAST)));
        split_names = new JSONArray(c.getString(c.getColumnIndexOrThrow(DataBase.Path.SPLIT_NAMES)));
        latitude = new JSONArray(c.getString(c.getColumnIndexOrThrow(DataBase.Path.LATITUDE)));
        longitude = new JSONArray(c.getString(c.getColumnIndexOrThrow(DataBase.Path.LONGITUDE)));
    }

    private void reset_splits() throws JSONException {
        for(int i = 0; i < splits.length; i++){
            splitdiffs[i].setText("");
            splittimes[i].setText(long_to_string(sum_until_i_json(pb_time_json, i)));
            splitdiffs[i].setTextColor(Color.rgb(0,0,0));
        }

        split_total.setText(long_to_string(0));
        split_total.setTextColor(Color.rgb(0, 0, 0));
        split_time.setText(long_to_string(0));
        split_previous.setText("-");
        split_previous.setTextColor(Color.rgb(0,0,0));
        split_pb.setText(long_to_string(pb_time_json.getLong(0)));
        split_besttime.setText(long_to_string(best_time_json.getLong(0)));
        split_pts.setText(long_to_string(pb_time_json.getLong(0)-best_time_json.getLong(0)));
        split_bpt.setText(long_to_string(sum_until_i_json(best_time_json,splits.length-1)));
        split_sob.setText(long_to_string(sum_until_i_json(best_time_json,splits.length-1)));

    }

    private void init_splits() throws JSONException {

        for(int i = 0; i < splits.length; i++){
            splits[i] = new LinearLayout(this);

            splitnames[i] = new TextView(this);
            splitnames[i].setText(split_names.getString(i));
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
            splittimes[i].setText(long_to_string(sum_until_i_json(pb_time_json, i)));
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

        split_pb.setText(long_to_string(pb_time_json.getLong(0)));
        split_besttime.setText(long_to_string(best_time_json.getLong(0)));
        split_pts.setText(long_to_string(pb_time_json.getLong(0)-best_time_json.getLong(0)));
        split_bpt.setText(long_to_string(sum_until_i_json(best_time_json,splits.length-1)));
        split_sob.setText(long_to_string(sum_until_i_json(best_time_json,splits.length-1)));

    }

    private void save_splittime() throws JSONException {
        splittimes[state-1].setText(long_to_string(total_time));
        split_pb.setText(long_to_string(pb_time_json.getLong(state)));
        split_besttime.setText(long_to_string(best_time_json.getLong(state)));
        split_pts.setText(long_to_string(pb_time_json.getLong(state)-best_time_json.getLong(state)));
        split_bpt.setText(long_to_string(sum_until_i_json(segment_time_json,state -1 ) + sum_past_i_json(best_time_json,state-1)));
        splitdiffs[state-1].setText(diff_long_to_string(total_time ,sum_until_i_json(pb_time_json, state-1)));
        split_previous.setText(diff_long_to_string(segment_time_json.getLong(state-1) ,best_time_json.getLong(state-1)));

        if (segment_time_json.getLong(state-1) < best_time_json.getLong(state-1)){
            split_previous.setTextColor(Color.rgb(255,215,0));
            best_time_json.put(state-1, segment_time_json.getLong(state-1));
            update_path_json(path_id, DataBase.Path.BEST,best_time_json);
            splitdiffs[state-1].setTextColor(Color.rgb(255,215,0));
        }
        else if (total_time < sum_until_i_json(pb_time_json, state-1) ){
            split_previous.setTextColor(Color.rgb(255,0,0));
            if (segment_time_json.getLong(state-1) < pb_time_json.getLong(state-1)) {
                splitdiffs[state - 1].setTextColor(Color.rgb(0, 255, 0));
            }
            else {
                splitdiffs[state - 1].setTextColor(Color.rgb(0, 150, 0));
            }
        }
        else{
            split_previous.setTextColor(Color.rgb(255,0,0));
            if (segment_time_json.getLong(state-1) < pb_time_json.getLong(state-1)) {
                splitdiffs[state - 1].setTextColor(Color.rgb(150, 0, 0));
            }
            else {
                splitdiffs[state - 1].setTextColor(Color.rgb(255, 0, 0));
            }
        }
        split_sob.setText(long_to_string(sum_until_i_json(best_time_json,splits.length-1)));
    }

    private void end_splittime() throws JSONException {
        splittimes[splits.length-1].setText(long_to_string(total_time));
        split_pts.setText(long_to_string(pb_time_json.getLong(splits.length-1)-segment_time_json.getLong(splits.length-1)));
        split_bpt.setText(long_to_string(sum_until_i_json(segment_time_json,splits.length-1 ) + sum_past_i_json(best_time_json,splits.length-1)));
        splitdiffs[splits.length-1].setText(diff_long_to_string(total_time ,sum_until_i_json(pb_time_json, splits.length-1)));
        split_previous.setText(diff_long_to_string(segment_time_json.getLong(splits.length-1) ,segment_time_json.getLong(splits.length-1)));

        if (split_count > 0) {
            for (int i = 0; i < splits.length; i++) {
                mean_time_json.put(i, (mean_time_json.getLong(i) * run_count + pb_time_json.getLong(i)) / (split_count + 1));
            }
            update_path_json(path_id, DataBase.Path.MEAN, mean_time_json);
        }
        else{
            update_path_json(path_id, DataBase.Path.PB, mean_time_json);
            update_path_json(path_id, DataBase.Path.PB, best_time_json);
        }
        split_count++;
        update_path_count(path_id, DataBase.Path.SPLIT_COUNT,split_count);
        create_new_run(segment_time_json);
        update_path_json(path_id, DataBase.Path.LAST, pb_time_json);

        if (segment_time_json.getLong(splits.length-1) < segment_time_json.getLong(splits.length-1)){
            split_previous.setTextColor(Color.rgb(255,215,0));
            segment_time_json.put(splits.length-1, segment_time_json.getLong(splits.length-1));
            splitdiffs[splits.length-1].setTextColor(Color.rgb(255,215,0));
        }
        else if (total_time < sum_until_i_json(pb_time_json, splits.length-1) ){
            split_previous.setTextColor(Color.rgb(255,0,0));
            if (segment_time_json.getLong(splits.length-1) < pb_time_json.getLong(splits.length-1)) {
                splitdiffs[splits.length-1].setTextColor(Color.rgb(0, 255, 0));
            }
            else {
                splitdiffs[splits.length-1].setTextColor(Color.rgb(0, 150, 0));
            }
        }
        else{
            split_previous.setTextColor(Color.rgb(255,0,0));
            if (segment_time_json.getLong(splits.length-1) < pb_time_json.getLong(splits.length-1)) {
                splitdiffs[splits.length-1].setTextColor(Color.rgb(150, 0, 0));
            }
            else {
                splitdiffs[splits.length-1].setTextColor(Color.rgb(255, 0, 0));
            }
        }
        split_sob.setText(long_to_string(sum_until_i_json(best_time_json,splits.length-1)));

        if (total_time < sum_until_i_json(pb_time_json, splits.length -1)){
            for (int i = 0; i< splits.length;i++)
                pb_time_json.put(i, segment_time_json.getLong(i));
            update_path_json(path_id, DataBase.Path.PB,pb_time_json);
            split_total.setTextColor(Color.rgb(255, 215, 0));
        }
        else
            split_total.setTextColor(Color.rgb(255, 0, 0));
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

    public static int sum_past_i_json( JSONArray vector, int i) throws JSONException {
        int sum = 0;
        for (int j = splits.length - 1; j > i; j--){
            sum += vector.getLong(j);
        }
        return sum;
    }

    public static int sum_until_i_json( JSONArray vector, int i) throws JSONException {
        int sum = 0;
        for (int j = 0; j <= i; j++){
            sum += vector.getLong(j);
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
                    if (segment_time_json.getLong(state) >= pb_time_json.getLong(state) || segment_time_json.getLong(state) >= best_time_json.getLong(state) ||
                            total_time >= sum_until_i_json(pb_time_json, state)){
                        splitdiffs[state].setText(diff_long_to_string(total_time,sum_until_i_json(pb_time_json, state)));
                        split_bpt.setText(long_to_string(sum_until_i_json(segment_time_json,state ) + sum_past_i_json(best_time_json,state)));
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDbHelper = new PostDbHelper(getApplicationContext());
        db = mDbHelper.getWritableDatabase();
        try {
            init_static_vars();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            init_splits();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        bt_split = (Button) findViewById(R.id.bt_split);
        bt_split.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                state++;
                if (state == -1){
                    try {
                        get_from_db();
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
}
