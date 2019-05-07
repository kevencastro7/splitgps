package keven.splitgps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static final class DataBase {

        private DataBase() {}

        public static class Path implements BaseColumns {
            public static final String TABLE_NAME = "path";
            public static final String TITLE = "title";
            public static final String RUN_COUNT = "run_count";
            public static final String SPLIT_COUNT = "split_count";
            public static final String PB = "personal_best";
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PostDbHelper mDbHelper = new PostDbHelper(getApplicationContext());


        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        /**
        ContentValues values = new ContentValues();
        values.put(PostContract.PostEntry.COLUMN_NAME_TITLE, "Titulo do Post");
        values.put(PostContract.PostEntry.COLUMN_NAME_SUBTITLE, "Subtitulo do Post");
        long newRowId = db.insert(PostContract.PostEntry.TABLE_NAME, null, values);*/
    }
}
