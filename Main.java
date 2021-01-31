import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
public class Main {

	public static void main(String[] args) {
		// Initialize Api Context
        // Instantiate Telegram Bots API
	     // Register our bot
        TelegramBotsApi botsApi;
		try {
			botsApi = new TelegramBotsApi((Class<? extends BotSession>) DefaultBotSession.class);
			botsApi.registerBot(new Bot());
		} catch (TelegramApiException e1) {
			e1.printStackTrace();
		}

   

}
	}
