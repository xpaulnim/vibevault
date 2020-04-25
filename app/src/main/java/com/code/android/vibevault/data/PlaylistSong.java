package com.code.android.vibevault.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "playlistSongsTbl")
public class PlaylistSong {
    @PrimaryKey
    private int _id;

    @ColumnInfo(name = "playlist_id")
    private int playlistId;
    @ColumnInfo(name = "song_id")
    private int songId;
    private int trackNum;

    public int get_id() {
        return this._id;
    }

    public int getPlaylistId() {
        return this.playlistId;
    }

    public int getSongId() {
        return this.songId;
    }

    public int getTrackNum() {
        return this.trackNum;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void setPlaylistId(int playlistId) {
        this.playlistId = playlistId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public void setTrackNum(int trackNum) {
        this.trackNum = trackNum;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof PlaylistSong)) return false;
        final PlaylistSong other = (PlaylistSong) o;
        if (!other.canEqual(this)) return false;
        if (this.get_id() != other.get_id()) return false;
        if (this.getPlaylistId() != other.getPlaylistId()) return false;
        if (this.getSongId() != other.getSongId()) return false;
        return this.getTrackNum() == other.getTrackNum();
    }

    protected boolean canEqual(final Object other) {
        return other instanceof PlaylistSong;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.get_id();
        result = result * PRIME + this.getPlaylistId();
        result = result * PRIME + this.getSongId();
        result = result * PRIME + this.getTrackNum();
        return result;
    }
}
