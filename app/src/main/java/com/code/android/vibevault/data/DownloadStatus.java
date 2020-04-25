package com.code.android.vibevault.data;

import androidx.room.ColumnInfo;

public class DownloadStatus {
    @ColumnInfo(name = "downloaded")
    public int downloaded;

    @ColumnInfo(name = "total")
    public int total;
}
