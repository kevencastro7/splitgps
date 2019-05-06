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

    public static final class PostContract {

        private PostContract() {}

        public static class PostEntry implements BaseColumns {
            public static final String TABLE_NAME = "post";
            public static final String COLUMN_NAME_TITLE = "titulo";
            public static final String COLUMN_NAME_SUBTITLE = "subtitulo";
        }
    }

    public class PostDbHelper extends SQLiteOpenHelper {
        private static final String TEXT_TYPE = " TEXT";
        private static final String COMMA_SEP = ",";
        private static final String SQL_CREATE_POSTS =
                "CREATE TABLE " + PostContract.PostEntry.TABLE_NAME + " (" +
                        PostContract.PostEntry._ID + " INTEGER PRIMARY KEY," +
                        PostContract.PostEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                        PostContract.PostEntry.COLUMN_NAME_SUBTITLE + TEXT_TYPE + " )";
        private static final String SQL_DELETE_POSTS =
                "DROP TABLE IF EXISTS " + PostContract.PostEntry.TABLE_NAME;
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "FeedReader.db";
        public PostDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_POSTS);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_POSTS);
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
        ContentValues values = new ContentValues();
        values.put(PostContract.PostEntry.COLUMN_NAME_TITLE, "Titulo do Post");
        values.put(PostContract.PostEntry.COLUMN_NAME_SUBTITLE, "Subtitulo do Post");
        long newRowId = db.insert(PostContract.PostEntry.TABLE_NAME, null, values);
    }
}
