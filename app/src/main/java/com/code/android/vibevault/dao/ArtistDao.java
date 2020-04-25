package com.code.android.vibevault.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.code.android.vibevault.data.Artist;

import java.util.List;

@Dao
public interface ArtistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertArtist(Artist artist);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertArtist(List<Artist> artists);

    @Query("DELETE FROM artistTbl")
    void deleteArtists();

    @Query("SELECT artistName FROM artistTbl")
    List<String> getArtistName();

    @Query("SELECT * FROM artistTbl WHERE artistName NOT LIKE 'a%' AND artistName NOT LIKE 'b%' " +
            "AND artistName NOT LIKE 'c%' AND artistName NOT LIKE 'd%' AND artistName NOT LIKE 'e%' " +
            "AND artistName NOT LIKE 'f%' AND artistName NOT LIKE 'g%' AND artistName NOT LIKE 'h%' " +
            "AND artistName NOT LIKE 'i%' AND artistName NOT LIKE 'j%' AND artistName NOT LIKE 'k%' " +
            "AND artistName NOT LIKE 'l%' AND artistName NOT LIKE 'm%' AND artistName NOT LIKE 'n%' " +
            "AND artistName NOT LIKE 'o%' AND artistName NOT LIKE 'p%' AND artistName NOT LIKE 'q%' " +
            "AND artistName NOT LIKE 'r%' AND artistName NOT LIKE 's%' AND artistName NOT LIKE 't%' " +
            "AND artistName NOT LIKE 'u%' AND artistName NOT LIKE 'v%' AND artistName NOT LIKE 'w%' " +
            "AND artistName NOT LIKE 'x%' AND artistName NOT LIKE 'y%' AND artistName NOT LIKE 'z%' " +
            "ORDER BY artistName")
    List<Artist> getArtistNot();

    @Query("SELECT * " +
            " FROM artistTbl " +
            "WHERE artistName LIKE :firstLetter " +
            "ORDER BY artistName")
    List<Artist> getArtist(String firstLetter);
}
