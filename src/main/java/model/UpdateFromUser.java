package model;

import com.pengrad.telegrambot.model.Audio;
import com.pengrad.telegrambot.model.Message;
import lombok.Getter;

@Getter
public class UpdateFromUser {
    private long chatID;
    private String userName;
    private String messageText;
    private Audio audio;
    private boolean start;
    private boolean help;
    private Song song;

    public UpdateFromUser(long chatID, String userName, String messageText) {
        this.chatID = chatID;
        this.userName = userName;
        this.messageText = messageText;
    }

    public UpdateFromUser(long chatID, String userName, Audio audio) {
        this.chatID = chatID;
        this.userName = userName;
        this.audio = audio;
    }

    public void setAudio(Audio audio) {
        this.audio = audio;
    }

    public void setChatID(long chatID) {
        this.chatID = chatID;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public boolean isStart() {
        return start;
    }

    public boolean isHelp() {
        return help;
    }

    public void setStart() {
        if(this.messageText != null) {
            this.start = this.messageText.equalsIgnoreCase("/start") || this.messageText.equalsIgnoreCase("start");
        }
    }

    public void setHelp() {
        if (this.messageText != null) {
            this.help = this.messageText.equalsIgnoreCase("/help") || this.messageText.equalsIgnoreCase("help");
        }
    }

    public void setSong(Song song) {
        this.song = song;
    }

    @Override
    public String toString() {
        return "{" +
                "chat_id=" + chatID +
                ", user_name='" + userName +
                '}';
    }

    public String displayUpdate() {
        return "{" +
                "chatID=" + chatID +
                ", userName='" + userName + '\'' +
                ", messageText='" + messageText + '\'' +
                ", start=" + start +
                ", help=" + help +
                ", song=" + song +
                '}';
    }
}
