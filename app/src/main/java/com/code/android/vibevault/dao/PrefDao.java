package com.code.android.vibevault.dao;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface PrefDao {
    // sanitize() prefName before passing it here
    @Query("SELECT prefValue " +
            " FROM prefsTbl " +
            "WHERE prefName = :pref_name")
    String getPref(String pref_name);

    @Query("UPDATE prefsTbl" +
            "  SET prefValue = :pref_value " +
            "WHERE prefName = :pref_name")
    void updatePref(String pref_name, String pref_value);
}
