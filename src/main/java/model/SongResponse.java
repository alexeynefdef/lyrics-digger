package model;

public class SongResponse {
    private String id;
    private String artist;
    private String artistId;
    private String title;
    private String lyrics;
    private String imageUlr;
    private String songUlr;
    private String fullTitle;
    private boolean done;

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getImageUlr() {
        return imageUlr;
    }

    public void setImageUlr(String imageUlr) {
        this.imageUlr = imageUlr;
    }

    public String getSongUlr() {
        return songUlr;
    }

    public void setSongUlr(String songUlr) {
        this.songUlr = songUlr;
    }

    public String getFullTitle() {
        return fullTitle;
    }

    public void setFullTitle(String fullTitle) {
        this.fullTitle = fullTitle;
    }

    public SongResponse() {
    }

    @Override
    public String toString() {
        return "SongResponse{" +
                "id='" + id + '\'' +
                ", artist='" + artist + '\'' +
                ", artistId='" + artistId + '\'' +
                ", title='" + title + '\'' +
                ", lyrics='" + lyrics + '\'' +
                ", imageUlr='" + imageUlr + '\'' +
                ", songUlr='" + songUlr + '\'' +
                ", fullTitle='" + fullTitle + '\'' +
                ", done=" + done +
                '}';
    }
}
