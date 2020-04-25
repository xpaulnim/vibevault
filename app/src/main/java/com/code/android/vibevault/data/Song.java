package com.code.android.vibevault.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "songTbl")
public class Song {
    @PrimaryKey
    private long _id;

    private String fileName;

    private String songTitle;

    @ColumnInfo(name = "show_id")
    private long showId;

    private String isDownloaded;

    @ColumnInfo(name = "download_id")
    private Long downloadId;

    private String folderName;

    public long get_id() {
        return this._id;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getSongTitle() {
        return this.songTitle;
    }

    public long getShowId() {
        return this.showId;
    }

    public String getIsDownloaded() {
        return this.isDownloaded;
    }

    public Long getDownloadId() {
        return this.downloadId;
    }

    public String getFolderName() {
        return this.folderName;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public void setShowId(long showId) {
        this.showId = showId;
    }

    public void setIsDownloaded(String isDownloaded) {
        this.isDownloaded = isDownloaded;
    }

    public void setDownloadId(long downloadId) {
        this.downloadId = downloadId;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }
}
