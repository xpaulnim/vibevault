package com.code.android.vibevault.repository;

import android.content.Context;

import com.code.android.vibevault.ArchiveShowObj;
import com.code.android.vibevault.StaticDb;
import com.code.android.vibevault.dao.FavouriteShowDao;
import com.code.android.vibevault.dao.RecentShowDao;
import com.code.android.vibevault.dao.ShowDao;
import com.code.android.vibevault.data.Show;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ShowRepository {

    private ShowDao showDao;
    private RecentShowDao recentShowDao;
    private FavouriteShowDao favouriteShowDao;

    public ShowRepository(Context context) {
        showDao = StaticDb.getDatabase(context).showDao();
        recentShowDao = StaticDb.getDatabase(context).recentShowDao();
        favouriteShowDao = StaticDb.getDatabase(context).favouriteShowDao();
    }

    public ArchiveShowObj getShow(final String identifier) {
        Future<ArchiveShowObj> future = StaticDb.DB_EXECUTOR_SERVICE.submit(new Callable<ArchiveShowObj>() {
            @Override
            public ArchiveShowObj call() throws Exception {
                List<ArchiveShowObj> matching_shows = showDao.getShow(identifier);
                if (matching_shows.isEmpty()) {
                    return null;
                }

                return matching_shows.get(0);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<ArchiveShowObj> getRecentShows() {
        return recentShowDao.getRecentShows();
    }

    public int getShowExists(final String show_identifier) {
        Future<Integer> future = StaticDb.DB_EXECUTOR_SERVICE.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return showDao.getShowExists(show_identifier);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<ArchiveShowObj> getBadShows() {
        return showDao.getBadShows();
    }

    public void insertShow(final Show show) {
        StaticDb.DB_EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                showDao.insertShow(show);
            }
        });
    }

    public void insertRecentShow(final String show_identifier) {
        StaticDb.DB_EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                recentShowDao.insertRecentShow(show_identifier);
            }
        });
    }

    public void setShowExists(final String show_identifier) {
        StaticDb.DB_EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                showDao.setShowExists(show_identifier);
            }
        });
    }

    public void setShowDeleted(final String show_identifier) {
        StaticDb.DB_EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                showDao.setShowDeleted(show_identifier);
            }
        });
    }

    public void deleteRecentShow(long show_id) {
        recentShowDao.deleteRecentShow(show_id);
    }

    public List<ArchiveShowObj> getFavoriteShows() {
        return favouriteShowDao.getFavouriteShows();
    }

    public void deleteFavoriteShow(long show_id) {
        favouriteShowDao.deleteFavouriteShow(show_id);
    }

    public void insertFavouriteShow(String show_identifier) {
        favouriteShowDao.insertFavoriteShow(show_identifier);
    }

    public void deleteFavoriteShows() {
        favouriteShowDao.deleteFavouriteShows();
    }

    public void updateShow(final String show_identifier, final String show_title, final String show_artist) {
        StaticDb.DB_EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                showDao.updateShow(
                        show_identifier,
                        show_title.replaceAll("'", "''"),
                        show_artist.replaceAll("'", "''"));
            }
        });
    }

    public void clearRecentShows() {
        StaticDb.DB_EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                recentShowDao.clearRecentShows();
            }
        });
    }
}
