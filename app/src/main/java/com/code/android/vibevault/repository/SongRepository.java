package com.code.android.vibevault.repository;

import android.content.Context;

import com.code.android.vibevault.ArchiveSongObj;
import com.code.android.vibevault.StaticDb;
import com.code.android.vibevault.dao.SongDao;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SongRepository {
    private SongDao songDao;

    public SongRepository(Context context) {
        StaticDb staticDb = StaticDb.getDatabase(context);
        songDao = staticDb.songDao();
    }

    public void insert(final String song_show_identifier,
                       final String song_title,
                       final String song_filename) {
        StaticDb.DB_EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                songDao.insertSong(song_show_identifier, song_title, song_filename.replaceAll("'", "''"));
            }
        });
    }

    public List<Integer> songIsDownloaded(final String song_filename) {
        final Future<List<Integer>> future = StaticDb.DB_EXECUTOR_SERVICE.submit(new Callable<List<Integer>>() {
            @Override
            public List<Integer> call() throws Exception {
                return songDao.songIsDownloaded(song_filename);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void setSongDeleted(final String song_filename) {
        StaticDb.DB_EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                songDao.setSongDeleted(song_filename);
            }
        });
    }

    public void setSongDownloaded(final String song_filename) {
        StaticDb.DB_EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                songDao.setSongDownloaded(song_filename);
            }
        });
    }

    public int getSongIsDownloading(String song_filename) {
        return songDao.getSongIsDownloading(song_filename);
    }

    public void setSongIsDownloading(final long download_id, final String show_identifier) {
        StaticDb.DB_EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                songDao.setSongIsDownloading(download_id, show_identifier);
            }
        });
    }

    public void setSongDownloaded(final long download_id) {
        StaticDb.DB_EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                songDao.setSongDownloaded(download_id);
            }
        });
    }

    public List<ArchiveSongObj> getSongsFromShow(final String show_identifier) {
        final Future<List<ArchiveSongObj>> future = StaticDb.DB_EXECUTOR_SERVICE.submit(new Callable<List<ArchiveSongObj>>() {
            @Override
            public List<ArchiveSongObj> call() throws Exception {
                return songDao.getSongsFromShow(show_identifier);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<ArchiveSongObj> getDownloadedSongsFromShow(String showIdent) {
        return songDao.getDownloadedSongsFromShow(showIdent);
    }

    public Long getSongIdByFilename(final String song_filename) {
        final Future<Long> future = StaticDb.DB_EXECUTOR_SERVICE.submit(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return songDao.getSongIdByFilename(song_filename);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setFolderName(final long song_id, final String folderName) {
        StaticDb.DB_EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                songDao.setSongFolderName(song_id, folderName);
            }
        });
    }

    public List<ArchiveSongObj> getSongsFromShowKey(final int show_id) {
        final Future<List<ArchiveSongObj>> future = StaticDb.DB_EXECUTOR_SERVICE.submit(new Callable<List<ArchiveSongObj>>() {
            @Override
            public List<ArchiveSongObj> call() throws Exception {
                return songDao.getSongsFromShowKey(show_id);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }
}
