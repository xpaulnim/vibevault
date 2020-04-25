package com.code.android.vibevault.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "showTbl")
public class Show {
    @PrimaryKey(autoGenerate = true)
    private int _id;
    private String showIdent;
    private String showTitle;
    private String showArtist;
    private String showExists;
    private String showSource;
    private boolean hasVBR;
    private boolean hasLBR;

    public Show(String showIdent, String showTitle, String showArtist, String showExists, String showSource, boolean hasVBR, boolean hasLBR) {
        this.showIdent = showIdent;
        this.showTitle = showTitle;
        this.showArtist = showArtist;
        this.showExists = showExists;
        this.showSource = showSource;
        this.hasVBR = hasVBR;
        this.hasLBR = hasLBR;
    }

    public int get_id() {
        return this._id;
    }

    public String getShowIdent() {
        return this.showIdent;
    }

    public String getShowTitle() {
        return this.showTitle;
    }

    public String getShowArtist() {
        return this.showArtist;
    }

    public String getShowExists() {
        return this.showExists;
    }

    public String getShowSource() {
        return this.showSource;
    }

    public boolean getHasVBR() {
        return this.hasVBR;
    }

    public boolean getHasLBR() {
        return this.hasLBR;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void setShowIdent(String showIdent) {
        this.showIdent = showIdent;
    }

    public void setShowTitle(String showTitle) {
        this.showTitle = showTitle;
    }

    public void setShowArtist(String showArtist) {
        this.showArtist = showArtist;
    }

    public void setShowExists(String showExists) {
        this.showExists = showExists;
    }

    public void setShowSource(String showSource) {
        this.showSource = showSource;
    }

    public void setHasVBR(boolean hasVBR) {
        this.hasVBR = hasVBR;
    }

    public void setHasLBR(boolean hasLBR) {
        this.hasLBR = hasLBR;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Show show = (Show) o;
        return hasVBR == show.hasVBR &&
                hasLBR == show.hasLBR &&
                Objects.equals(showIdent, show.showIdent) &&
                Objects.equals(showTitle, show.showTitle) &&
                Objects.equals(showArtist, show.showArtist) &&
                Objects.equals(showExists, show.showExists) &&
                Objects.equals(showSource, show.showSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(showIdent, showTitle, showArtist, showExists, showSource, hasVBR, hasLBR);
    }
}
