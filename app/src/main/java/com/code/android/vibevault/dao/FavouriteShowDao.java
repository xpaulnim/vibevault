package com.code.android.vibevault.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.code.android.vibevault.ArchiveShowObj;

import java.util.List;

@Dao
public interface FavouriteShowDao {

    @Query("DELETE FROM favoriteShowsTbl WHERE show_id = :show_ident")
    void deleteFavouriteShow(long show_ident);

    @Query("INSERT INTO favoriteShowsTbl(show_id) "
            + "SELECT show._id "
            + "FROM showTbl show "
            + "WHERE show.showIdent = :show_ident "
            + "AND NOT EXISTS (SELECT 1 FROM favoriteShowsVw fav WHERE fav.showIdent = :show_ident)")
    void insertFavoriteShow(String show_ident);

    @Query("SELECT showIdent     as identifier " +
            "    , showTitle     as showTitle " +
            "    , showArtist    as showArtist " +
            "    , showSource    as source " +
            "    , hasVBR        as hasVBR " +
            "    , hasLBR        as hasLBR " +
            "    , _id           as DBID " +
            " FROM favoriteShowsVw")
    List<ArchiveShowObj> getFavouriteShows();

    @Query("DELETE FROM favoriteShowsTbl")
    void deleteFavouriteShows();
}
