package com.code.android.vibevault;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.code.android.vibevault.dao.ArtistDao;
import com.code.android.vibevault.dao.DownloadDao;
import com.code.android.vibevault.dao.FavouriteShowDao;
import com.code.android.vibevault.dao.PlaylistDao;
import com.code.android.vibevault.dao.PrefDao;
import com.code.android.vibevault.dao.RecentShowDao;
import com.code.android.vibevault.dao.ShowDao;
import com.code.android.vibevault.dao.SongDao;
import com.code.android.vibevault.data.Artist;
import com.code.android.vibevault.data.AutoComplete;
import com.code.android.vibevault.data.DownloadedShowVw;
import com.code.android.vibevault.data.FavoriteShow;
import com.code.android.vibevault.data.FavoriteShowVw;
import com.code.android.vibevault.data.Playlist;
import com.code.android.vibevault.data.PlaylistSong;
import com.code.android.vibevault.data.Pref;
import com.code.android.vibevault.data.RecentShow;
import com.code.android.vibevault.data.RecentShowView;
import com.code.android.vibevault.data.Show;
import com.code.android.vibevault.data.Song;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {
        Artist.class,
        AutoComplete.class,
        FavoriteShow.class,
        Playlist.class,
        PlaylistSong.class,
        Pref.class,
        RecentShow.class,
        Show.class,
        Song.class
}, views = {
        DownloadedShowVw.class,
        RecentShowView.class,
        FavoriteShowVw.class
}, version = 1, exportSchema = false)
public abstract class StaticDb extends RoomDatabase {

    public abstract ArtistDao artistDao();

    public abstract DownloadDao downloadDao();

    public abstract FavouriteShowDao favouriteShowDao();

    public abstract PlaylistDao playlistDao();

    public abstract PrefDao prefDao();

    public abstract RecentShowDao recentShowDao();

    public abstract ShowDao showDao();

    public abstract SongDao songDao();

    private static final int NUMBER_OF_THREADS = 2;
    public static final ExecutorService DB_EXECUTOR_SERVICE = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private static volatile StaticDb INSTANCE;

    public static StaticDb getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (StaticDb.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), StaticDb.class, "archivedb_v2")
                            .addCallback(prepopulatePrefsTblCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback prepopulatePrefsTblCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            db.execSQL("INSERT INTO prefsTbl(prefName,prefValue) SELECT 'downloadFormat','VBR'");
            db.execSQL("INSERT INTO prefsTbl(prefName,prefValue) SELECT 'downloadPath','/archiveApp/'");
            db.execSQL("INSERT INTO prefsTbl(prefName,prefValue) SELECT 'numResults','10'");
            db.execSQL("INSERT INTO prefsTbl(prefName,prefValue) SELECT 'streamFormat','VBR'");
            db.execSQL("INSERT INTO prefsTbl(prefName,prefValue) SELECT 'sortOrder','Date'");
            db.execSQL("INSERT INTO prefsTbl(prefName,prefValue) SELECT 'artistUpdate','2010-01-01'");
            db.execSQL("INSERT INTO prefsTbl(prefName,prefValue) SELECT 'splashShown','false'");
            db.execSQL("INSERT INTO prefsTbl(prefName,prefValue) SELECT 'userId','0'");
            db.execSQL("INSERT INTO prefsTbl(prefName,prefValue) SELECT 'artistResultType','Top All Time Artists'");
            db.execSQL("INSERT INTO prefsTbl(prefName,prefValue) SELECT 'showResultType','Top All Time Shows'");
            db.execSQL("INSERT INTO prefsTbl(prefName,prefValue) SELECT 'showsByArtistResultType','Top All Time Shows'");
            db.execSQL("INSERT INTO prefsTbl(prefName,prefValue) SELECT 'nowPlayingPosition','0'");
        }
    };
}

