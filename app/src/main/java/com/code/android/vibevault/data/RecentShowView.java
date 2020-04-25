package com.code.android.vibevault.data;

import androidx.room.DatabaseView;

@DatabaseView(
        value = "SELECT show.* " +
                "  FROM recentShowsTbl recent " +
                " INNER JOIN showTbl show ON recent.show_id = show._id" +
                " ORDER BY recent._id DESC",
        viewName = "recentShowsVw")
public class RecentShowView {
    private int _id;
    private String showIdent;
    private String showTitle;
    private String showArtist;
    private String showExists;
    private String showSource;
    private String hasVBR;
    private String hasLBR;


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

    public String getHasVBR() {
        return this.hasVBR;
    }

    public String getHasLBR() {
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

    public void setHasVBR(String hasVBR) {
        this.hasVBR = hasVBR;
    }

    public void setHasLBR(String hasLBR) {
        this.hasLBR = hasLBR;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof RecentShowView)) return false;
        final RecentShowView other = (RecentShowView) o;
        if (!other.canEqual(this)) return false;
        if (this.get_id() != other.get_id()) return false;
        final Object this$showIdent = this.getShowIdent();
        final Object other$showIdent = other.getShowIdent();
        if (this$showIdent == null ? other$showIdent != null : !this$showIdent.equals(other$showIdent))
            return false;
        final Object this$showTitle = this.getShowTitle();
        final Object other$showTitle = other.getShowTitle();
        if (this$showTitle == null ? other$showTitle != null : !this$showTitle.equals(other$showTitle))
            return false;
        final Object this$showArtist = this.getShowArtist();
        final Object other$showArtist = other.getShowArtist();
        if (this$showArtist == null ? other$showArtist != null : !this$showArtist.equals(other$showArtist))
            return false;
        final Object this$showExists = this.getShowExists();
        final Object other$showExists = other.getShowExists();
        if (this$showExists == null ? other$showExists != null : !this$showExists.equals(other$showExists))
            return false;
        final Object this$showSource = this.getShowSource();
        final Object other$showSource = other.getShowSource();
        if (this$showSource == null ? other$showSource != null : !this$showSource.equals(other$showSource))
            return false;
        final Object this$hasVBR = this.getHasVBR();
        final Object other$hasVBR = other.getHasVBR();
        if (this$hasVBR == null ? other$hasVBR != null : !this$hasVBR.equals(other$hasVBR))
            return false;
        final Object this$hasLBR = this.getHasLBR();
        final Object other$hasLBR = other.getHasLBR();
        return this$hasLBR == null ? other$hasLBR == null : this$hasLBR.equals(other$hasLBR);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof RecentShowView;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.get_id();
        final Object $showIdent = this.getShowIdent();
        result = result * PRIME + ($showIdent == null ? 43 : $showIdent.hashCode());
        final Object $showTitle = this.getShowTitle();
        result = result * PRIME + ($showTitle == null ? 43 : $showTitle.hashCode());
        final Object $showArtist = this.getShowArtist();
        result = result * PRIME + ($showArtist == null ? 43 : $showArtist.hashCode());
        final Object $showExists = this.getShowExists();
        result = result * PRIME + ($showExists == null ? 43 : $showExists.hashCode());
        final Object $showSource = this.getShowSource();
        result = result * PRIME + ($showSource == null ? 43 : $showSource.hashCode());
        final Object $hasVBR = this.getHasVBR();
        result = result * PRIME + ($hasVBR == null ? 43 : $hasVBR.hashCode());
        final Object $hasLBR = this.getHasLBR();
        result = result * PRIME + ($hasLBR == null ? 43 : $hasLBR.hashCode());
        return result;
    }
}
