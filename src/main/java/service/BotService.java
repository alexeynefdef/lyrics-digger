package service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.SendMessage;
import model.Song;
import model.SongResponse;
import model.UpdateFromUser;
import properties.GeniusBotProperties;
import properties.MessagesAndLinks;

import java.util.HashMap;

public class BotService {

    private final String TOKEN = GeniusBotProperties.getProp("bot.token");
    private final TelegramBot bot = new TelegramBot(TOKEN);
    private final HashMap<Long,UpdateFromUser> usersByChatID = new HashMap<>();

    public BotService() {
    }

    public void initUpdateListener() {

        this.bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                if (update.message() != null && update.message().text() != null) {

                    long chatId = update.message().chat().id();
                    String userName = update.message().from().username();
                    String text = update.message().text();

                    if (!usersByChatID.containsKey(chatId)) {
                        usersByChatID.put(chatId, new UpdateFromUser(chatId, userName, text));
                        System.out.println("Users count: " + usersByChatID.size());
                        usersByChatID.values().forEach(System.out::println);
                    } else {
                        usersByChatID.get(chatId).setMessageText(text);
                    }
                    this.usersByChatID.get(chatId).setStart();
                    this.usersByChatID.get(chatId).setHelp();
                    analyzeMessage(chatId);
                    //Mock Logger TODO: integrate Logger
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
        if (this.usersByChatID.get(chatId).getSong() == null) {
            this.usersByChatID.get(chatId).setSong(new Song());
        }
        if (this.usersByChatID.get(chatId).getSong().getArtist() == null) {

            this.usersByChatID.get(chatId).getSong().setArtist(this.usersByChatID.get(chatId).getMessageText().trim());
            bot.execute(new SendMessage(this.usersByChatID.get(chatId).getChatID(), MessagesAndLinks.GET_TITLE_FROM_USER.getReference()));

        } else if (this.usersByChatID.get(chatId).getSong().getArtist() != null && this.usersByChatID.get(chatId).getSong().getTitle() == null) {

            this.usersByChatID.get(chatId).getSong().setTitle(this.usersByChatID.get(chatId).getMessageText().trim());
            bot.execute(new SendMessage(this.usersByChatID.get(chatId).getChatID(), MessagesAndLinks.SEARCHING.getReference()));

            GeniusApi api = new GeniusApi(this.usersByChatID.get(chatId).getSong());
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
                    bot.execute(new SendMessage(this.usersByChatID.get(chatId).getChatID(), firstPart));
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
