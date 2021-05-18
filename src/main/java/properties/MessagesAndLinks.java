package properties;

public enum MessagesAndLinks {
    GENIUS_URL_SEARCH("http://api.genius.com/search"),
    GENIUS_URL_SONG("http://genius.com/songs/"),
    PATREON_LINK("https://www.patreon.com/devongroove"),

    START_MESSAGE_TEXT("Hi, I'm Lyrics digger!\n\nHere you can find the lyrics of any song.\n\nSend me first an artist's name or a name of the band\nor just send me 'help' if you're lost"),
    HELP_MESSAGE_TEXT("How does it work? \n\n 1.Send me Artist's or a Band name with your first message \n\n 2. Send me a song title with your second message \n\n 3.Enjoy the lyrics and sing along"),
    GET_TITLE_FROM_USER("OK, now send me a song title"),
    NOTHING_FOUND("Oooops nothing found...\n\n One more try?"),
    SEARCHING("Searching...");

    private final String reference;

    MessagesAndLinks(String reference) {
        this.reference = reference;
    }

    public String getReference() {
        return reference;
    }
}
