package com.code.android.vibevault.repository;

import android.content.Context;

import com.code.android.vibevault.StaticDb;
import com.code.android.vibevault.dao.ArtistDao;
import com.code.android.vibevault.data.Artist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ArtistRepository {

    private ArtistDao artistDao;

    public ArtistRepository(Context context) {
        artistDao = StaticDb.getDatabase(context).artistDao();
    }

    public List<String> getArtistName() {
        Future<List<String>> future = StaticDb.DB_EXECUTOR_SERVICE.submit(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                return artistDao.getArtistName();
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void deleteArtists() {
        StaticDb.DB_EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                artistDao.deleteArtists();
            }
        });
    }

    public void insertArtist(final Artist artist) {
        StaticDb.DB_EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                artistDao.insertArtist(artist);
            }
        });
    }

    public void insertArtists(final List<Artist> artists) {
        StaticDb.DB_EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                artistDao.insertArtist(artists);
            }
        });
    }

    public List<Artist> getArtist(final String firstLetter) {
        Future<List<Artist>> future = StaticDb.DB_EXECUTOR_SERVICE.submit(new Callable<List<Artist>>() {
            @Override
            public List<Artist> call() throws Exception {
                //Apparently you have to manually implement regular expressions, so fuck that
                //Ugly hack that works
                List<Artist> artists = new ArrayList<>();
                if (firstLetter.equalsIgnoreCase("!")) {
                    artists.clear();
                    artists.addAll(artistDao.getArtistNot());
                } else {
                    artists.clear();
                    artists.addAll(artistDao.getArtist(firstLetter + "%"));
                }

                return artists;
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
