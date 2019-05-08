package keven.splitgps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

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

    private static int create_new_path(String title, int split_count, JSONArray split_names, JSONArray latitude, JSONArray longitude){
        JSONArray PB = new JSONArray();
        for(int i = 0; i < split_count;i++)
            PB.put(0);
        ContentValues values = new ContentValues();
        values.put(DataBase.Path.TITLE, title);
        values.put(DataBase.Path.RUN_COUNT, 0);
        values.put(DataBase.Path.SPLIT_COUNT, split_count);
        values.put(DataBase.Path.PB, PB.toString());
        values.put(DataBase.Path.BEST, PB.toString());
        values.put(DataBase.Path.MEAN, PB.toString());
        values.put(DataBase.Path.LAST, PB.toString());
        values.put(DataBase.Path.SPLIT_NAMES, split_names.toString());
        values.put(DataBase.Path.LONGITUDE, latitude.toString());
        values.put(DataBase.Path.LATITUDE, longitude.toString());
        return (int)db.insert(DataBase.Path.TABLE_NAME, null, values);
    }

    private static int create_new_run(JSONArray segment_time){
        ContentValues values = new ContentValues();
        values.put(DataBase.Run.PATH_ID, path_id);
        values.put(DataBase.Run.SEGMENT, segment_time.toString());
        return (int)db.insert(DataBase.Run.TABLE_NAME, null, values);
    }

    private static void inicia_banco() throws JSONException {
        JSONArray name = new JSONArray("[\"RP001\",\"RP002\",\"RP003\",\"RP004\",\"RP005\",\"RP006\"," +
                "\"RP007\",\"RP008\",\"RP009\",\"RP010\",\"RP011\",\"RP012\",\"RP013\",\"RP014\"," +
                "\"RP015\",\"RP016\",\"RP017\",\"RP018\",\"RP019\",\"RP020\",\"RP021\"]");
        JSONArray latitude = new JSONArray("[-3.006296,-3.010520,-3.010866,-3.008667,-3.019701,-3.025148," +
                "-3.037674,-3.045935,-3.053276,-3.056718,-3.059772,-3.064938,-3.069382," +
                "-3.077965,-3.099480,-3.113986,-3.121817,-3.128036,-3.133940,-3.139739,-3.144813]");
        JSONArray longitude = new JSONArray("[-59.974644,-59.970508,-59.958931,-59.939935,-59.937966,-59.933978," +
                "-59.940667,-59.943343,-59.946225,-59.947821,-59.949208,-59.951612,-59.953673," +
                "-59.956060,-59.947189,-59.947049,-59.953185,-59.957308,-59.987887,-59.990078,-60.001224]");

        int error = create_new_path("Casa para VTI", name.length(),name,latitude,longitude);


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

    private void reset_splits() throws JSONException {
        segment_time_json = new JSONArray();
        Cursor c = get_path_by_id(path_id);
        c.moveToFirst();
        run_count = c.getInt(c.getColumnIndexOrThrow(DataBase.Path.RUN_COUNT));
        split_count = c.getInt(c.getColumnIndexOrThrow(DataBase.Path.SPLIT_COUNT));
        pb_time_json = new JSONArray(c.getString(c.getColumnIndexOrThrow(DataBase.Path.PB)));
        best_time_json = new JSONArray(c.getString(c.getColumnIndexOrThrow(DataBase.Path.BEST)));
        mean_time_json = new JSONArray(c.getString(c.getColumnIndexOrThrow(DataBase.Path.MEAN)));
        last_time_json = new JSONArray(c.getString(c.getColumnIndexOrThrow(DataBase.Path.LAST)));
        split_names = new JSONArray(c.getString(c.getColumnIndexOrThrow(DataBase.Path.SPLIT_NAMES)));
        latitude = new JSONArray(c.getString(c.getColumnIndexOrThrow(DataBase.Path.LATITUDE)));
        longitude = new JSONArray(c.getString(c.getColumnIndexOrThrow(DataBase.Path.LONGITUDE)));

        System.out.println(run_count);
        System.out.println(split_count);
        System.out.println(pb_time_json.toString());
        System.out.println(best_time_json.toString());
        System.out.println(mean_time_json.toString());
        System.out.println(last_time_json.toString());
        System.out.println(split_names.toString());
        System.out.println(latitude.toString());
        System.out.println(longitude.toString());


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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDbHelper = new PostDbHelper(getApplicationContext());


        db = mDbHelper.getWritableDatabase();

        try {
            inicia_banco();
            reset_splits();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
