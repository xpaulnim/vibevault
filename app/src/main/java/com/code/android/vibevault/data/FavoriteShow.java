package com.code.android.vibevault.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "favoriteShowsTbl")
public class FavoriteShow {
    @PrimaryKey
    private int _id;

    @ColumnInfo(name = "show_id")
    private String showId;

    public int get_id() {
        return this._id;
    }

    public String getShowId() {
        return this.showId;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void setShowId(String showId) {
        this.showId = showId;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof FavoriteShow)) return false;
        final FavoriteShow other = (FavoriteShow) o;
        if (!other.canEqual(this)) return false;
        if (this.get_id() != other.get_id()) return false;
        final Object this$showId = this.getShowId();
        final Object other$showId = other.getShowId();
        return Objects.equals(this$showId, other$showId);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof FavoriteShow;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.get_id();
        final Object $showId = this.getShowId();
        result = result * PRIME + ($showId == null ? 43 : $showId.hashCode());
        return result;
    }
}
