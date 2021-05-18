package app;

import bot.Bot;
import service.BotService;

public class App {
    public static void main(String[] args) {
        BotService service = new BotService();
        Bot bot = new Bot(service);
        bot.initBot();
    }
}
