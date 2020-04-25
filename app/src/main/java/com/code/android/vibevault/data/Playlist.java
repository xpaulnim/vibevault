package com.code.android.vibevault.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "playlistTbl")
public class Playlist {
    @PrimaryKey
    private int _id;

    private String playlistName;

    public int get_id() {
        return this._id;
    }

    public String getPlaylistName() {
        return this.playlistName;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Playlist)) return false;
        final Playlist other = (Playlist) o;
        if (!other.canEqual(this)) return false;
        if (this.get_id() != other.get_id()) return false;
        final Object this$playlistName = this.getPlaylistName();
        final Object other$playlistName = other.getPlaylistName();
        return Objects.equals(this$playlistName, other$playlistName);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Playlist;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.get_id();
        final Object $playlistName = this.getPlaylistName();
        result = result * PRIME + ($playlistName == null ? 43 : $playlistName.hashCode());
        return result;
    }
}
