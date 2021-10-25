package me.FurH.Bot.listener;

import java.io.IOException;
import java.util.HashMap;
import static me.FurH.BungeeCore.help.HelpData.CANCEL;
import static me.FurH.BungeeCore.help.HelpData.CANCELREPLY;
import static me.FurH.BungeeCore.help.HelpData.CREATE_NEW;
import static me.FurH.BungeeCore.help.HelpData.DELETEQUEST;
import static me.FurH.BungeeCore.help.HelpData.INITREPLY;
import static me.FurH.BungeeCore.help.HelpData.RELOADCAT;
import static me.FurH.BungeeCore.help.HelpData.SENDREPLY;
import static me.FurH.BungeeCore.help.HelpData.UPDATE_ID;
import me.FurH.NIO.stream.NetInputStream;
import me.FurH.SkyHub.sets.NetReader;
import me.FurH.SkyHub.sets.Packet129SetValue;
import org.javacord.api.entity.channel.TextChannel;

/*
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class BotManager extends NetReader<BotHelp> {

    private final HashMap<Integer, BotHelp> datas;
    private final BotListener listener;
    
    public BotManager(BotListener listener) {
        this.datas = new HashMap<>();
        this.listener = listener;
    }
    
    @Override
    public String key() {
        return "@REMOVED";
    }

    @Override
    public BotHelp newInstance(NetInputStream in) throws IOException {
        return new BotHelp();
    }

    void reloadChannels() {

        BotHelp data = new BotHelp();

        data.action = RELOADCAT;
        data.user = 0;
        
        write(data);
    }

    void write(BotHelp data) {
        try {
            listener.bot.connection.write(serialize(data, "bungeecord"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handle(Packet129SetValue packet, BotHelp obj) {

        switch (obj.action) {
            
            case CREATE_NEW: {

                TextChannel channel = listener.channels.get(obj.area);
                
                if (channel == null) {
                    return;
                }
                
                BotHelp old = datas.put(obj.user, obj);
                obj.channel = channel;
               
                if (old != null) {
                    old.remove();
                }
                
                obj.show();
                
                break;
            }
            case CANCEL: {

                removeData(obj);

                break;
            }
            case INITREPLY: {
                
                BotHelp data = datas.get(obj.user);

                if (data != null) {
                    data.hide();
                }
                
                break;
            }
            case UPDATE_ID: {
                break;
            }
            case CANCELREPLY: {
                
                BotHelp data = datas.get(obj.user);

                if (data != null) {
                    data.show();
                }
                
                break;
            }
            case RELOADCAT: {
                
                break;
            }
            case SENDREPLY: {
                
                removeData(obj);
                
                break;
            }
            case DELETEQUEST: {
                
                removeData(obj);
                
                break;
            }
        }
    }

    private void removeData(BotHelp obj) {
        
        BotHelp data = datas.remove(obj.user);

        if (data != null) {
            data.remove();
        }
    }
}