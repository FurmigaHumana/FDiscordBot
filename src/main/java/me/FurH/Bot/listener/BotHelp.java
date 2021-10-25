package me.FurH.Bot.listener;

import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import me.FurH.BungeeCore.help.HelpData;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;

/*
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class BotHelp extends HelpData {
    
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    private Message message;
    TextChannel channel;

    public BotHelp() {
    }

    void remove() {
        if (message != null) {
            channel.deleteMessages(message);
            message = null;
        }
    }

    void hide() {
        remove();
    }

    void show() {
        
        String display = STRIP_COLOR_PATTERN.matcher(username).replaceAll("");

        String msg = 
                "#\n" +
                display + ", area: " + area + "\n"
                + question + "\n" +
                "Responda com .r " + user + " <mensagem>.";

        channel.sendMessage(msg).whenCompleteAsync(new BiConsumer<Message, Throwable>() {
            @Override
            public void accept(Message message, Throwable u) {
                BotHelp.this.message = message;
            }
        });
    }
}