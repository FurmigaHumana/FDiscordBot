package me.FurH.Bot.utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import me.FurH.Bot.DiscordBot;
import me.FurH.Bot.listener.BotListener;
import me.FurH.Core.close.Closer;
import me.FurH.Core.database.SQLThread;
import me.FurH.Core.number.NumberUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.user.User;

/*
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class DUtils {
    
    private static final long adminid = -1; // @REMOVED
    
    public static String toCmd(String message) {
        return toCmd(message.indexOf(' '), message);
    }

    public static String toCmd(int j1, String message) {
        
        String cmd = message;

        if (j1 > 0) {
            cmd = message.substring(0, j1);
        }
        
        return cmd;
    }
    
    public static int randomCode(long user, int type, SQLThread t) throws SQLException {
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            
            ps = t.prepare("SELECT code FROM `@REMOVED`.`@REMOVED` WHERE `user` = ? AND `type` = ? LIMIT 1;");
            
            ps.setLong(1, user);
            ps.setInt(2, type);
            
            ps.execute();
            
            rs = ps.getResultSet();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } finally {
            
            Closer.closeQuietly(ps, rs);
            
        }

        return newRandomCode(t);
    }
    
    private static int newRandomCode(SQLThread t) throws SQLException {
        
        for (int j1 = 0; j1 < 5; j1++) {
            
            int code = NumberUtils.nextInt(100000, 999999);
            
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {

                ps = t.prepare("SELECT user FROM `@REMOVED`.`@REMOVED` WHERE `code` = ? LIMIT 1;");
                ps.setInt(1, code);
                ps.execute();

                rs = ps.getResultSet();

                if (!rs.next()) {
                    return code;
                }

            } finally {

                Closer.closeQuietly(ps, rs);

            }
        }
        
        return 0;
    }

    public static void error(String error) {
        
        System.err.println(error);
        Thread.dumpStack();

        DiscordBot bot = DiscordBot.instance;
        BotListener listener = bot.bot;

        if (listener == null) {
            return;
        }
        
        DiscordApi api = listener.api;
        
        api.getUserById(adminid).whenComplete(new BiConsumer<User, Throwable>() {
           
            @Override
            public void accept(User t, Throwable u) {
                
                if (u != null) {
                    u.printStackTrace();
                }
                
                if (t != null) {
                    t.sendMessage(error);
                }
            }
        });
    }
}