import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Bot extends TelegramLongPollingBot {
	@Override
    public void onUpdateReceived(Update update) {
		 // We check if the update has a message and the message has text
	    if (update.hasMessage() && update.getMessage().hasText()) {
	        String message_text = update.getMessage().getText();
	        long chat_id = update.getMessage().getChatId();
	        String str = Long.toString(chat_id);
	        SendMessage message = new SendMessage();
	        message.setChatId(str);
	        Mathclass a = new Mathclass();
	        message.setText(a.IOControl(message_text));
	       //Pray to the Garbage Collection Gods
	        try {
	            execute(message); // Sending our message object to user
	        } catch (TelegramApiException e) {
	            e.printStackTrace();
	        }
	    }
    }
    @Override
    public String getBotUsername() {
        return "coronatraderbot";
    }

    @Override
    public String getBotToken() {
        return "BOT_TOKEN_HERE";
    }


}
