package com.code.android.vibevault.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "artistTbl")
public class Artist {
    @PrimaryKey(autoGenerate = true)
    private int _id;

    private String artistName;
    private String numShows;

    public int get_id() {
        return this._id;
    }

    public String getArtistName() {
        return this.artistName;
    }

    public String getNumShows() {
        return this.numShows;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setNumShows(String numShows) {
        this.numShows = numShows;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artist artist = (Artist) o;
        return Objects.equals(artistName, artist.artistName) &&
                Objects.equals(numShows, artist.numShows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artistName, numShows);
    }
}
