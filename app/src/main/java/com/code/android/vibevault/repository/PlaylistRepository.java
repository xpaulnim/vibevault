package com.code.android.vibevault.repository;

import android.content.Context;

import com.code.android.vibevault.ArchiveSongObj;
import com.code.android.vibevault.StaticDb;
import com.code.android.vibevault.dao.PlaylistDao;
import com.code.android.vibevault.data.PlaylistSong;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class PlaylistRepository {
    private PlaylistDao playlistDao;

    public PlaylistRepository(Context context) {
        playlistDao = StaticDb.getDatabase(context).playlistDao();
    }

    public List<ArchiveSongObj> getSongsFromPlaylist(final long playlist_id) {
        Future<List<ArchiveSongObj>> future = StaticDb.DB_EXECUTOR_SERVICE.submit(new Callable<List<ArchiveSongObj>>() {
            @Override
            public List<ArchiveSongObj> call() throws Exception {
                return playlistDao.getSongsFromPlaylist(playlist_id);
            }
        });

        try {
            return future.get();
        } catch(InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addSongToNowPlaying(final int id) {
        StaticDb.DB_EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                playlistDao.addSongToNowPlaying(id);
            }
        });
    }

    public void addToPlaylist(final List<PlaylistSong> playlistSongs) {
        StaticDb.DB_EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                playlistDao.addToPlaylist(playlistSongs);
            }
        });
    }

    public void clearNowPlayingSongs() {
        StaticDb.DB_EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                playlistDao.clearNowPlayingSongs();
            }
        });
    }

    public void insertKnownSongIntoPlaylist(final int playlist_id, final int position, final String fileName) {
        StaticDb.DB_EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                playlistDao.insertKnownSongIntoPlaylist(playlist_id, position, fileName);
            }
        });
    }
}
