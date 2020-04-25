package com.code.android.vibevault.data;

import androidx.room.DatabaseView;

import java.util.Objects;

@DatabaseView(
        value = "SELECT show.* " +
                "  FROM favoriteShowsTbl fav " +
                " INNER JOIN showTbl show ON fav.show_id = show._id " +
                " ORDER BY fav._id DESC",
        viewName = "favoriteShowsVw")
public class FavoriteShowVw {
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
        if (!(o instanceof FavoriteShowVw)) return false;
        final FavoriteShowVw other = (FavoriteShowVw) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.get_id() != other.get_id()) return false;
        final Object this$showIdent = this.getShowIdent();
        final Object other$showIdent = other.getShowIdent();
        if (!Objects.equals(this$showIdent, other$showIdent))
            return false;
        final Object this$showTitle = this.getShowTitle();
        final Object other$showTitle = other.getShowTitle();
        if (!Objects.equals(this$showTitle, other$showTitle))
            return false;
        final Object this$showArtist = this.getShowArtist();
        final Object other$showArtist = other.getShowArtist();
        if (!Objects.equals(this$showArtist, other$showArtist))
            return false;
        final Object this$showExists = this.getShowExists();
        final Object other$showExists = other.getShowExists();
        if (!Objects.equals(this$showExists, other$showExists))
            return false;
        final Object this$showSource = this.getShowSource();
        final Object other$showSource = other.getShowSource();
        if (!Objects.equals(this$showSource, other$showSource))
            return false;
        final Object this$hasVBR = this.getHasVBR();
        final Object other$hasVBR = other.getHasVBR();
        if (!Objects.equals(this$hasVBR, other$hasVBR))
            return false;
        final Object this$hasLBR = this.getHasLBR();
        final Object other$hasLBR = other.getHasLBR();
        if (!Objects.equals(this$hasLBR, other$hasLBR))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof FavoriteShowVw;
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
