package me.FurH.Bot;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TimerTask;
import me.FurH.Bot.conn.BotConnection;
import me.FurH.Bot.listener.BotListener;
import me.FurH.Core.close.Closer;
import me.FurH.Core.config.FProperties;
import me.FurH.Core.database.SQL;
import me.FurH.Core.database.SQLDb;
import me.FurH.Core.database.SQLTask;
import me.FurH.Core.database.SQLThread;
import me.FurH.Core.util.Sleeper;
import me.FurH.Logger.InputReader;
import me.FurH.Logger.LogFactory;
import me.FurH.SkyHub.flush.Packet111SetGroup;

/*
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class DiscordBot {
    
    public static DiscordBot instance;
    public final BotConnection connection;

    public final HashMap<String, Long> channels;
    public final HashSet<Long> lookup;
    
    public final int port;
    public final String host;
    
    public BotListener bot;
    
    public DiscordBot() throws IOException {

        this.connection = new BotConnection(this);
        this.channels = new HashMap<>();
        this.lookup = new HashSet<>();
        
        FProperties props = new FProperties(new File("server.properties"));
        props.load();

        host    = props.getProperty("server-ip", "127.0.0.1");
        port    = Integer.parseInt(props.getProperty("server-port", "8081"));
    }

    public void shutdown() {
        System.exit(0);
    }

    public void connected() throws Exception {
        bot.connected();
    }

    public void initialize() throws Exception {

        InputReader in = new InputReader(System.in) {
            @Override
            public void input(String line) {
                doCommand(line);
            }
        };

        in.start(false);
        
        SQL.mslow(new SQLTask() {
            @Override
            public void execute(SQLDb db, SQLThread t) throws Throwable {
                loadChannels(t);
                completeInitialize();
            }
        });
    }

    public void loadChannels(SQLThread t) throws SQLException {
        
        System.out.println("Load database channels");
        
        channels.clear();
        lookup.clear();
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            
            ps = t.prepare("SELECT label, channel FROM `@REMOVED`.`@REMOVED` WHERE `channel` IS NOT NULL;");
            ps.execute();
            
            rs = ps.getResultSet();
            
            while (rs.next()) {
                
                String label = rs.getString(1);
                long channel = rs.getLong(2);

                channels.put(label, channel);
            }

            for (Entry<String, Long> entry : channels.entrySet()) {
                lookup.add(entry.getValue());
            }

        } finally {
            
            Closer.closeQuietly(ps, rs);
            
        }
    }

    private void completeInitialize() throws Exception {

        System.out.println("Done");

        bot = new BotListener(this);
        
        bot.setName("Listener Thread");
        bot.start();
        
        connection.initialize();
    }
    
    public void setGroup(Packet111SetGroup packet) {
        bot.setGroup(packet);
    }
    
    public static void main(String[] args) throws Exception {

        Sleeper.schedule(new Runnable() {
            @Override
            public void run() {
                Sleeper.timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Sleeper.hardExirOrKill();
                    }
                }, 300000);
            }
        });

        LogFactory.initialize(new File("logs"));

        instance = new DiscordBot();
        instance.initialize();
    }
    
    private void doCommand(String line) {

        switch (line) {

            case "stop": {

                shutdown();

                break;
            }
        }
    }
}