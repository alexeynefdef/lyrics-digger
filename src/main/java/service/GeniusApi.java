package service;

import com.google.gson.*;
import model.Song;
import model.SongResponse;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import properties.GeniusBotProperties;
import properties.MessagesAndLinks;

import java.io.*;
import java.util.*;

public class GeniusApi {

    private final OkHttpClient client;
    private final String ACCESS_TOKEN = GeniusBotProperties.getProp("genius.token");
    private SongResponse songResponse;
    private final Song songRequest;

    public GeniusApi(Song song) {
        this.client = new OkHttpClient();
        this.songRequest = song;
    }

    public SongResponse connectToGenius() {
        return initSearch();
    }

    private String searchSong() throws IOException {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(MessagesAndLinks.GENIUS_URL_SEARCH.getReference())).newBuilder();
        String artist = this.songRequest.getArtist();
        String title = this.songRequest.getTitle();
        String searchTerm = artist + " - " + title;
        urlBuilder.addQueryParameter("q", searchTerm);
        urlBuilder.addQueryParameter("access_token", ACCESS_TOKEN);
        String urlGenius = urlBuilder.build().toString();
        Request request = new Request.Builder().url(urlGenius).build();
        Response response = client.newCall(request).execute();
        return Objects.requireNonNull(response.body()).string();
    }

    private SongResponse initSearch() {
        try {
            String responseFromGenius = this.searchSong();
            Gson gson = new Gson();
            var javaRootMapObject = gson.fromJson(responseFromGenius, Map.class);
            var res = (Map) javaRootMapObject.get("response");
            String artistFromUser = this.songRequest.getArtist();
            String titleFromUser = this.songRequest.getTitle();
            var hits = (ArrayList) res.get("hits");// [] JSON Array
            List matched = getMatchedByTitle(getMatchedByArtist(hits,artistFromUser),titleFromUser);

            if (matched.size() > 0) {
                var songFirstResultRaw = (Map) matched.get(0);
                var songFirst = (Map) songFirstResultRaw.get("result");
                var artistObj = (Map) songFirst.get("primary_artist");
                var title = songFirst.get("title_with_featured").toString();
                var songApiPath = songFirst.get("api_path").toString();
                var artistName = artistObj.get("name").toString();
                var fullTitle = songFirst.get("full_title").toString();
                var coverUrl = songFirst.get("song_art_image_url").toString();

                this.songResponse = new SongResponse();
                songResponse.setArtistId(artistObj.get("api_path").toString().split("/")[2]);
                songResponse.setId(songApiPath.split("/")[2]);
                songResponse.setArtist(artistName);
                songResponse.setFullTitle(fullTitle);
                songResponse.setSongUlr(songFirst.get("url").toString());
                songResponse.setImageUlr(coverUrl);
                songResponse.setTitle(title);
                var lyrics = parseLyrics();
                songResponse.setLyrics(lyrics);
                songResponse.setDone(true);
                return songResponse;
            }
            return new SongResponse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new SongResponse();
    }

    private List getMatchedByArtist(List hits, String artistFromUser) {

        ArrayList<Object> artistMatch = new ArrayList<>();

        for (Object hit : hits) {
            var songInfo = (Map) hit;
            var result = (Map) songInfo.get("result");
            var pa = (Map) result.get("primary_artist");
            var artistName = pa.get("name").toString().toLowerCase();

            if (artistName.equalsIgnoreCase(artistFromUser)
                    || artistName.contains(artistFromUser.toLowerCase())
                    || artistName.startsWith(artistFromUser.split(" ")[0].toLowerCase())) {
                artistMatch.add(hit);
            }
        }
        return artistMatch;
    }

    private List getMatchedByTitle(List hits, String titleFromUser) {

        ArrayList<Object> titleMatch = new ArrayList<>();

        for (Object hit : hits) {
            var songInfo = (Map) hit;
            var result = (Map) songInfo.get("result");
            var title = result.get("title").toString().toLowerCase();
            String[] titleFromUserArray = titleFromUser.toLowerCase().split(" ");
            boolean titleContainsRequest = false;
            for (String word:titleFromUserArray) {
                if (title.contains(word.toLowerCase())) {
                    titleContainsRequest = true;
                }
            }

            if (title.equalsIgnoreCase(titleFromUser) || title.startsWith(titleFromUserArray[0]) || titleContainsRequest) {
                titleMatch.add(hit);
            }
        }
        return titleMatch;
    }

    private String parseLyrics() throws IOException {
        Document document = Jsoup.connect(MessagesAndLinks.GENIUS_URL_SONG.getReference() + songResponse.getId() + "/embed.js").userAgent("Mozilla").get();
        var lyricsRaw = document.body().getElementsByTag("p").text();
        return getReadable(lyricsRaw);
    }

    private String getReadable(String rawLyrics) {
        //Remove start
        rawLyrics = rawLyrics.replaceAll("[\\S\\s]*<div class=\\\\\\\\\\\\\"rg_embed_body\\\\\\\\\\\\\">[ (\\\\\\\\n)]*", "");
        //Remove end
        rawLyrics = rawLyrics.replaceAll("[ (\\\\\\\\n)]*<\\\\/div>[\\S\\s]*", "");
        //Remove tags between
        rawLyrics = rawLyrics.replaceAll("<[^<>]*>", "");
        //Unescape spaces
        rawLyrics = rawLyrics.replaceAll("\\\\\\\\n","\n");
        //Unescape '
        rawLyrics = rawLyrics.replaceAll("\\\\'", "'");
        //Unescape "
        rawLyrics = rawLyrics.replaceAll("\\\\\\\\\\\\\"", "\"");

        return rawLyrics;
    }
}
