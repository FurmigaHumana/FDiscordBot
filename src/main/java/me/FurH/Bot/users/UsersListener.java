package me.FurH.Bot.users;

import java.sql.PreparedStatement;
import me.FurH.Bot.listener.BotListener;
import me.FurH.Bot.utils.DUtils;
import static me.FurH.Bot.utils.DUtils.toCmd;
import me.FurH.Core.close.Closer;
import me.FurH.Core.database.SQL;
import me.FurH.Core.database.SQLDb;
import me.FurH.Core.database.SQLTask;
import me.FurH.Core.database.SQLThread;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.event.message.MessageCreateEvent;

/*
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class UsersListener {

    public UsersListener(BotListener listener) {
//        this.listener = listener;
    }

    public void onMessageCreate(String message, TextChannel channel, MessageCreateEvent event) {

        MessageAuthor author = event.getMessageAuthor();

        if (author.isBotUser()) {
            return;
        }
        
        if (message.charAt(0) != '.') {

            channel.sendMessage("Comando disponível: \n"
                    + ".discord - Liga o seu discord a sua conta do servidor");

            return;
        }

        String cmd = toCmd(message);

        if (cmd.equalsIgnoreCase(".discord")) {
            
            SQL.mslow(new SQLTask() {

                @Override
                public void execute(SQLDb db, SQLThread t) throws Throwable {

                    long time = System.currentTimeMillis() / 1000l;
                    long user = author.getId();

                    int code = DUtils.randomCode(user, 5, t);
                                        
                    PreparedStatement ps = null;
                    
                    try {
                        
                        ps = t.prepare("INSERT INTO `@REMOVED`.`@REMOVED` (code, type, user, date) VALUES (?, 5, ?, ?) ON DUPLICATE KEY UPDATE `date` = ?;");
                        t.commitNext();
                        
                        ps.setInt(1, code);
                        ps.setLong(2, user);
                        ps.setLong(3, time);
                        ps.setLong(4, time);
                        
                        ps.execute();
                        
                    } finally {
                        
                        Closer.closeQuietly(ps);
                        
                    }

                    channel.sendMessage("Agora entre no servidor e digite \"/discord " + code + "\" para confirmar.");
                }
            });
        }
    }
}