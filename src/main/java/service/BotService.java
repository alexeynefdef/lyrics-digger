package service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.NoArgsConstructor;
import model.Song;
import model.SongResponse;
import model.UpdateFromUser;
import properties.GeniusBotProperties;
import properties.MessagesAndLinks;

import java.util.HashMap;
@NoArgsConstructor
public class BotService {

    private final String TOKEN = GeniusBotProperties.getProp("bot.token");
    private final TelegramBot bot = new TelegramBot(TOKEN);
    private final HashMap<Long,UpdateFromUser> usersByChatID = new HashMap<>();

    public void initUpdateListener() {
        this.bot.setUpdatesListener( updates -> {
            for (Update update : updates) {
                if (update.message() != null && (update.message().text() != null || update.message().audio() != null)) {
                    long chatId = update.message().chat().id();
                    String userName = update.message().from().username();
                    if (update.message().audio() != null) {
                        var audio = update.message().audio();
                        if (!usersByChatID.containsKey(chatId)) {
                            usersByChatID.put(chatId, new UpdateFromUser(chatId, userName, audio));
                            System.out.println("Users count: " + usersByChatID.size());
                            usersByChatID.values().forEach(System.out::println);
                        } else {
                            usersByChatID.get(chatId).setAudio(audio);
                        }
                    } else {
                        String text = update.message().text();
                        if (!usersByChatID.containsKey(chatId)) {
                            usersByChatID.put(chatId, new UpdateFromUser(chatId, userName, text));
                            System.out.println("Users count: " + usersByChatID.size());
                            usersByChatID.values().forEach(System.out::println);
                        } else {
                            usersByChatID.get(chatId).setMessageText(text);
                            usersByChatID.get(chatId).setAudio(null);
                        }
                    }
                    analyzeMessage(chatId);
                    //Mock Logger TODO: integrate Logger
                    this.usersByChatID.get(chatId).setStart();
                    this.usersByChatID.get(chatId).setHelp();
                    System.out.println("New update:" + this.usersByChatID.get(chatId).displayUpdate());

                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    public void analyzeMessage(long chatId) {

        if(this.usersByChatID.get(chatId).isStart()) {
            initOnStart(chatId);
        } else if (this.usersByChatID.get(chatId).isHelp()) {
            initOnHelp(chatId);
        } else {
            initBotMessaging(chatId);
        }
    }

    private void initBotMessaging(long chatId) {

        var userUpdate = this.usersByChatID.get(chatId);

        if (userUpdate.getSong() == null) {
            userUpdate.setSong(new Song());

            if (userUpdate.getAudio() != null) {
                userUpdate.getSong().setArtist(userUpdate.getAudio().performer());
                userUpdate.getSong().setTitle(userUpdate.getAudio().title());
            }
        }

        if (userUpdate.getSong().getArtist() == null) {

            userUpdate.getSong().setArtist(userUpdate.getMessageText().trim());
            bot.execute(new SendMessage(userUpdate.getChatID(), MessagesAndLinks.GET_TITLE_FROM_USER.getReference()));

        } else if ((userUpdate.getSong().getArtist() != null && userUpdate.getSong().getTitle() == null) || userUpdate.getAudio() != null) {

            if (userUpdate.getAudio() == null) {
                userUpdate.getSong().setTitle(userUpdate.getMessageText().trim());
            }

            bot.execute(new SendMessage(userUpdate.getChatID(), MessagesAndLinks.SEARCHING.getReference()));

            GeniusApi api = new GeniusApi(userUpdate.getSong());
            SongResponse songResponse = api.connectToGenius();
            String responseMessage = "";

            if (songResponse.isDone()) {
                if (songResponse.getLyrics().length() < 4096) {
                    responseMessage += songResponse.getFullTitle() + "\n\n\n";
                    responseMessage += songResponse.getLyrics() + "\n";
                    responseMessage += "<a href='";
                    responseMessage += songResponse.getImageUlr();
                    responseMessage += "'>&#8204;</a>";
                    InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
                            new InlineKeyboardButton("Support on Patreon").url(MessagesAndLinks.PATREON_LINK.getReference()));
                    SendMessage request = new SendMessage(this.usersByChatID.get(chatId).getChatID(), responseMessage)
                            .parseMode(ParseMode.HTML)
                            .replyMarkup(keyboard);
                    bot.execute(request);
                } else {
                    String firstPart = songResponse.getFullTitle() + "\n\n\n";
                    firstPart += songResponse.getLyrics().substring(0,songResponse.getLyrics().length()/2);
                    String secondPart = songResponse.getLyrics().substring(songResponse.getLyrics().length()/2);
                    bot.execute(new SendMessage(userUpdate.getChatID(), firstPart));
                    responseMessage += secondPart + "\n";
                    responseMessage += "<a href='";
                    responseMessage += songResponse.getImageUlr();
                    responseMessage += "'>&#8204;</a>";
                    InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
                            new InlineKeyboardButton("Support on Patreon").url(MessagesAndLinks.PATREON_LINK.getReference()));
                    SendMessage request = new SendMessage(this.usersByChatID.get(chatId).getChatID(), responseMessage)
                            .parseMode(ParseMode.HTML)
                            .replyMarkup(keyboard);
                    bot.execute(request);
                }
                this.usersByChatID.get(chatId).setSong(null);
            } else if (songResponse.getId() == null){
                bot.execute(new SendMessage(this.usersByChatID.get(chatId).getChatID(), MessagesAndLinks.NOTHING_FOUND.getReference()));
                this.usersByChatID.get(chatId).setSong(null);
            } else {
                bot.execute(new SendMessage(this.usersByChatID.get(chatId).getChatID(), MessagesAndLinks.NOTHING_FOUND.getReference()));
                this.usersByChatID.get(chatId).setSong(null);
            }
        }
    }

    private void initOnHelp(long chatId) {
        bot.execute(new SendMessage(this.usersByChatID.get(chatId).getChatID(), MessagesAndLinks.HELP_MESSAGE_TEXT.getReference()));
    }

    private void initOnStart(long chatId) {
        bot.execute(new SendMessage(this.usersByChatID.get(chatId).getChatID(), MessagesAndLinks.START_MESSAGE_TEXT.getReference()));
    }
}
