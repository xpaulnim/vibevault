package com.code.android.vibevault.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "autoCompleteTbl")
public class AutoComplete {
    @PrimaryKey
    private int _id;
    private String searchText;

    public int get_id() {
        return this._id;
    }

    public String getSearchText() {
        return this.searchText;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AutoComplete)) return false;
        final AutoComplete other = (AutoComplete) o;
        if (!other.canEqual(this)) return false;
        if (this.get_id() != other.get_id()) return false;
        final Object this$searchText = this.getSearchText();
        final Object other$searchText = other.getSearchText();
        return Objects.equals(this$searchText, other$searchText);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AutoComplete;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.get_id();
        final Object $searchText = this.getSearchText();
        result = result * PRIME + ($searchText == null ? 43 : $searchText.hashCode());
        return result;
    }
}
