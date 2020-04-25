package com.code.android.vibevault.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recentShowsTbl")
public class RecentShow {
    @PrimaryKey
    private int _id;

    @ColumnInfo(name = "show_id")
    private int showId;


    public int get_id() {
        return this._id;
    }

    public int getShowId() {
        return this.showId;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof RecentShow)) return false;
        final RecentShow other = (RecentShow) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.get_id() != other.get_id()) return false;
        if (this.getShowId() != other.getShowId()) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof RecentShow;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.get_id();
        result = result * PRIME + this.getShowId();
        return result;
    }
}
