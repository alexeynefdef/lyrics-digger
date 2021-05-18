package bot;


import service.BotService;

public class Bot {

    private final BotService botService;

    public Bot(BotService botService) {
        this.botService = botService;
    }

    public void initBot() {
        botService.initUpdateListener();
    }
}
