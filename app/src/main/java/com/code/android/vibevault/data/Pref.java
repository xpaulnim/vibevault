package com.code.android.vibevault.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "prefsTbl")
public class Pref {
    @PrimaryKey
    private int _id;

    @ColumnInfo(name = "prefName")
    private String name;

    @ColumnInfo(name = "prefValue")
    private String value;

    public int get_id() {
        return this._id;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Pref)) return false;
        final Pref other = (Pref) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.get_id() != other.get_id()) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (!Objects.equals(this$name, other$name)) return false;
        final Object this$value = this.getValue();
        final Object other$value = other.getValue();
        if (!Objects.equals(this$value, other$value))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Pref;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.get_id();
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $value = this.getValue();
        result = result * PRIME + ($value == null ? 43 : $value.hashCode());
        return result;
    }
}
