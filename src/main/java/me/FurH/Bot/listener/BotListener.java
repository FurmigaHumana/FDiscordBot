package me.FurH.Bot.listener;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import me.FurH.Bot.DiscordBot;
import me.FurH.Bot.users.UsersListener;
import me.FurH.Bot.utils.DUtils;
import static me.FurH.Bot.utils.DUtils.toCmd;
import static me.FurH.BungeeCore.help.HelpData.DELETEQUEST;
import static me.FurH.BungeeCore.help.HelpData.SENDREPLY;
import me.FurH.Core.cache.soft.SoftMap;
import me.FurH.Core.close.Closer;
import me.FurH.Core.consumer.SingleCallback;
import me.FurH.Core.database.SQL;
import me.FurH.Core.database.SQLDb;
import me.FurH.Core.database.SQLTask;
import me.FurH.Core.database.SQLThread;
import me.FurH.Core.executors.TimerExecutor;
import me.FurH.SkyHub.flush.Packet111SetGroup;
import org.javacord.api.AccountType;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.MessageDeleteEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageDeleteListener;

/*
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class BotListener extends Thread implements MessageCreateListener, MessageDeleteListener {
    
    private static final long oficialpublicserver = 01; // @REMOVED
    
    private static final String token;
    
    static {
        token = ""; // @REMOVED
    }

    private final SoftMap<Integer, User> discordids;
    private final HashMap<Integer, Role> vipgroups;
    private final SoftMap<Long, Integer> userids;
    private final HashSet<Long> vipids;
    
    final HashMap<String, TextChannel> channels;
    final BotManager manager;
    final DiscordBot bot;

    private ScheduledFuture<?> reloadtask;
    public DiscordApi api;

    private UsersListener users;
    private Server oficialsv;

    public BotListener(DiscordBot bot) {
        this.discordids = new SoftMap<>();
        this.vipgroups = new HashMap<>();
        this.userids = new SoftMap<>();
        this.manager = new BotManager(this);
        this.channels = new HashMap<>();
        this.vipids = new HashSet<>();
        this.bot = bot;
    }

    public void connected() throws Exception {
        manager.register();
    }

    @Override
    public void run() {

        api = new DiscordApiBuilder()
                .setWaitForServersOnStartup(true)
                .setAccountType(AccountType.BOT)
                .setToken(token)
                .login().join();
        
        System.out.println("Connected");
        
        loadChannels();
        
        api.addMessageCreateListener(this);
        api.addMessageDeleteListener(this);

        oficialsv = api.getServerById(oficialpublicserver).orElse(null);
        
        if (oficialsv == null) {
            
            DUtils.error("Servidor oficial não encontrado.");
            
        } else {

            loadRole(oficialsv, "Vip Esmeralda", 3);
            loadRole(oficialsv, "Vip Ouro", 4);
            loadRole(oficialsv, "Vip Diamante", 5);

            Role everyone = oficialsv.getEveryoneRole();
            vipgroups.put(1, everyone);
        }

        System.out.println("Bot Loaded");
        
        TimerExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                cleanupVips();
            }
        }, 5, TimeUnit.SECONDS);
    }
    
    private void cleanupVips() {
        
        if (oficialsv == null) {
            return;
        }

        Collection<User> userlist = oficialsv.getMembers();
        HashMap<User, List<Role>> vips = new HashMap<>();
        
        System.out.println("Cached members: " + userlist.size());

        for (User user : userlist) {

            List<Role> roles = user.getRoles(oficialsv);

            for (Role role : roles) {
                if (!role.isEveryoneRole() && vipids.contains(role.getId())) {
                    vips.put(user, roles);
                    break;
                }
            }
        }
        
        System.out.println("Users to check: " + vips.size());
        
        Iterator<Entry<User, List<Role>>> it = vips.entrySet().iterator();
        checkNextUser(it);
    }

    private void checkNextUser(Iterator<Entry<User, List<Role>>> it) {
        
        if (!it.hasNext()) {
            System.out.println("User check done.");
            return;
        }
        
        Entry<User, List<Role>> next = it.next();

        SQL.mslow(new SQLTask() {
            
            @Override
            public void execute(SQLDb db, SQLThread t) throws Throwable {

                User user = next.getKey();
                List<Role> list = next.getValue();

                int userid = getUserId(user.getId(), t);
                int groupid;

                if (userid <= 0) {
                    groupid = 1;
                } else {
                    groupid = getUserGroup(userid, t);
                }

                Role newrole = vipgroups.get(groupid);

                if (newrole == null || newrole.isEveryoneRole() || !list.contains(newrole)) {
                    
                    System.out.println("Check user: " + user.getName() + " - " + (newrole == null ? "null" : newrole.getName()) + " contains " + list.contains(newrole));
                    updateRole(user, newrole, list, new SingleCallback<Void>() {
                        @Override
                        public void doInvoke(Void result) {
                            checkNextUser(it);
                        }
                    });
                    
                } else {
                    
                    System.out.println("User: " + user.getName() + " - " + newrole.getName() + " IS OK");
                    checkNextUser(it);
                    
                }
            }
        });
    }
    
    private int getUserGroup(int user, SQLThread t) throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            ps = t.prepare("SELECT player_group FROM `@REMOVED`.`@REMOVED` WHERE `player_id` = ? LIMIT 1;");
            ps.setLong(1, user);
            ps.execute();

            rs = ps.getResultSet();

            if (!rs.next()) {
                return 1;
            }

            return rs.getInt(1);

        } finally {

            Closer.closeQuietly(ps, rs);

        }
    }
    
    private void loadRole(Server server, String name, int groupid) {
        
        name = name.toLowerCase();
        
        Role role = getRoleByName(server, name);
        
        if (role == null) {
            DUtils.error("Não foi possível encontrar o grupo: " + name);
            return;
        }
        
        System.out.println("Loaded role " + role.getName() + " ( " + role.getId() + " ) for group " + groupid);

        vipids.add(role.getId());
        vipgroups.put(groupid, role);
    }
    
    private Role getRoleByName(Server server, String name) {
        
        List<Role> roles = server.getRolesByNameIgnoreCase(name);
        
        if (!roles.isEmpty()) {
            return roles.get(0);
        }
        
        roles = server.getRoles();

        for (Role role : roles) {
            if (role.getName().toLowerCase().contains(name)) {
                return role;
            }
        }
        
        return null;
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        
        TextChannel channel = event.getChannel();
        ChannelType type = channel.getType();
        
        if (type != ChannelType.GROUP_CHANNEL) {
            return;
        }

        String msg = event.getMessageContent().orElse(null);

        if (msg == null || msg.isEmpty()) {
            return;
        }

        if (msg.charAt(0) != '#' || msg.charAt(msg.length() - 1) != '.') {
            return;
        }

        boolean ischannel = bot.lookup.contains(channel.getId());
        
        if (!ischannel) {
            return;
        }

        int j1 = msg.lastIndexOf('r');

        if (j1 <= 0) {
            return;
        }

        int j2 = msg.lastIndexOf(' ');

        if (j2 <= 0) {
            return;
        }

        try {
            
            MessageAuthor author = event.getMessageAuthor().get();
            
            if (author.isBotUser()) {
                return;
            }
            
            String id = msg.substring(j1 + 2, j2);
            int user = Integer.parseInt(id);
        
            BotHelp data = new BotHelp();

            data.action = DELETEQUEST;
            data.user = user;

            manager.write(data);

        } catch (Throwable ex) {
            
            ex.printStackTrace();
            
        }
    }
    
    @Override
    public void onMessageCreate(MessageCreateEvent event) {

        String message = event.getMessageContent();

        if (message.isEmpty()) {
            return;
        }

        TextChannel channel = event.getChannel();
        ChannelType type = channel.getType();
        
        if (type == ChannelType.PRIVATE_CHANNEL) {
            
            if (users == null) {
                users = new UsersListener(this);
            }
            
            users.onMessageCreate(message, channel, event);
            return;
        }

        if (type != ChannelType.GROUP_CHANNEL) {
            return;
        }

        boolean ischannel = bot.lookup.contains(channel.getId());

        if (message.charAt(0) != '.') {
            
            if (!ischannel) {
                return;
            }

            MessageAuthor author = event.getMessageAuthor();
            
            if (!author.isBotUser()) {
                event.deleteMessage();
            }
            
        } else {

            int j1 = message.indexOf(' ');
            String cmd = toCmd(j1, message);
            
            MessageAuthor author = event.getMessageAuthor();

            if (author.isServerAdmin() && manageCommands(cmd, message, event)) {
                event.deleteMessage();
                return;
            }

            if (ischannel) {
                
                event.deleteMessage();

                if (cmd.equals(".r")) {

                    int j2 = message.indexOf(' ', j1 + 1);

                    if (j1 <= 0 || j2 <= 0) {
                        replyAndDelete("Uso correto: .r <id> <mensagem>", event);
                        return;
                    }

                    String user = message.substring(j1 + 1, j2);
                    String text = message.substring(j2 + 1);

                    try {
                        writeReply(author, Integer.parseInt(user), text, event);
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private void writeReply(MessageAuthor author, int user, String reply, MessageCreateEvent event) {
        
        long discord = author.getId();
        Integer replyby = userids.get(discord);
        
        if (replyby != null) {
            
            writeReply(replyby, user, reply, event);
            
        } else {

            SQL.mslow(new SQLTask() {
                
                @Override
                public void execute(SQLDb db, SQLThread t) throws Throwable {

                    int replyid = getUserId(discord, t);
                    userids.put(discord, replyid);

                    writeReply(replyid, user, reply, event);
                }
            });
        }
    }
    
    private void writeReply(int replyby, int user, String reply, MessageCreateEvent event) {
        
        BotHelp data = new BotHelp();
        
        data.action = SENDREPLY;

        data.user = user;
        data.replyby = replyby;
        data.replytext = reply;

        manager.write(data);

        replyAndDelete("Resposta enviada", event);
    }

    private int getUserId(long discord, SQLThread t) throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            ps = t.prepare("SELECT user_id FROM `@REMOVED`.`@REMOVED` WHERE `user_discord` = ? LIMIT 1;");
            ps.setLong(1, discord);
            ps.execute();

            rs = ps.getResultSet();

            if (!rs.next()) {
                return 0;
            }

            return rs.getInt(1);

        } finally {

            Closer.closeQuietly(ps, rs);

        }
    }

    private boolean manageCommands(String cmd, String message, MessageCreateEvent event) {
        
        try {

            switch (cmd) {

                case ".delcat": {
                    
                    String[] split = message.split(" ");
                    
                    if (split.length != 2) {
                        replyAndDelete("Uso correto: .delcat <area>", event);
                    } else {
                        SQL.mslow(new SQLTask() {
                            @Override
                            public void execute(SQLDb db, SQLThread t) throws Throwable {
                                deleteCategory(split[1], event, t);
                            }
                        });
                    }
                    
                    return true;
                }
                case ".delchannel": {
                    
                    String[] split = message.split(" ");
                    
                    if (split.length != 2) {
                        replyAndDelete("Uso correto: .delchannel <area>", event);
                    } else {
                        SQL.mslow(new SQLTask() {
                            @Override
                            public void execute(SQLDb db, SQLThread t) throws Throwable {
                                removeChannel(split[1], event, t);
                            }
                        });
                    }
                    
                    return true;
                }
                case ".addcat": {
                    
                    String[] split = message.split(" ");
                    
                    if (split.length != 2) {
                        replyAndDelete("Uso correto: .addcat <area>", event);
                    } else {
                        SQL.mslow(new SQLTask() {
                            @Override
                            public void execute(SQLDb db, SQLThread t) throws Throwable {
                                addCategory(split[1], event, t);
                            }
                        });
                    }
                    
                    return true;
                }
                case ".setchannel": {
                    
                    String[] split = message.split(" ");
                    
                    if (split.length != 2) {

                        replyAndDelete("Uso correto: .setchannel <area>", event);

                    } else {

                        TextChannel channel = event.getChannel();
                        String category = split[1];

                        channel.sendMessage("Canal de ajuda da categoria: " + category).whenCompleteAsync(new BiConsumer<Message, Throwable>() {
                            @Override
                            public void accept(Message t, Throwable u) {
                                t.pin();
                            }
                        });

                        SQL.mslow(new SQLTask() {
                            @Override
                            public void execute(SQLDb db, SQLThread t) throws Throwable {
                                setChannel(category, channel.getId(), event, t);
                            }
                        });
                    }
                    
                    return true;
                }
                default: {
                    return false;
                }
            }

        } catch (Throwable ex) {

            ex.printStackTrace();

        }
        
        return false;
    }
    
    private void setChannel(String category, long channel, MessageCreateEvent event, SQLThread t) throws SQLException {

        PreparedStatement ps = null;
        
        try {
            
            ps = t.prepare("UPDATE `@REMOVED`.`@REMOVED` SET `channel` = ? WHERE `label` = ?;");
            t.commitNext();
            
            ps.setLong(1, channel);
            ps.setString(2, category);

            ps.execute();

            replyAndDelete("O canal desta categoria foi definido com sucesso", event);
            reloadChannels(t);
            
        } finally {
            
            Closer.closeQuietly(ps);
            
        }
    }
    
    private void addCategory(String category, MessageCreateEvent event, SQLThread t) throws SQLException {

        if (categoryExists(category, t)) {
            replyAndDelete("Esta categoria já existe", event);
            return;
        }

        PreparedStatement ps = null;
        
        try {
            
            ps = t.prepare("INSERT INTO `@REMOVED`.`@REMOVED` (label) VALUES (?);");
            t.commitNext();
            
            ps.setString(1, category);
            
            ps.execute();

            replyAndDelete("Categoria criada com sucesso", event);
            reloadChannels(t);
            
        } finally {
            
            Closer.closeQuietly(ps);
            
        }
    }
    
    private void removeChannel(String category, MessageCreateEvent event, SQLThread t) throws SQLException {

        if (!categoryExists(category, t)) {
            replyAndDelete("Esta categoria não existe", event);
            return;
        }

        PreparedStatement ps = null;
        
        try {
            
            ps = t.prepare("UPDATE `@REMOVED`.`@REMOVED` SET `channel` = NULL WHERE `label` = ?;");
            t.commitNext();
            
            ps.setString(1, category);
            
            ps.execute();

            replyAndDelete("O canal foi removido da categoria", event);
            reloadChannels(t);
            
        } finally {
            
            Closer.closeQuietly(ps);
            
        }
    }
    
    private void deleteCategory(String category, MessageCreateEvent event, SQLThread t) throws SQLException {

        if (!categoryExists(category, t)) {
            replyAndDelete("Esta categoria não existe", event);
            return;
        }

        PreparedStatement ps = null;
        
        try {
            
            ps = t.prepare("DELETE FROM `@REMOVED`.`@REMOVED` WHERE `label` = ?;");
            t.commitNext();
            
            ps.setString(1, category);
            
            ps.execute();
            
            replyAndDelete("Categoria removida com sucesso", event);
            reloadChannels(t);
            
        } finally {
            
            Closer.closeQuietly(ps);
            
        }
    }

    private void reloadChannels(SQLThread t) throws SQLException {
        
        bot.loadChannels(t);

        if (reloadtask != null) {
            reloadtask.cancel(true);
        }

        reloadtask = TimerExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                loadChannels();
                manager.reloadChannels();
            }
        }, 10, TimeUnit.SECONDS);
    }
    
    private boolean categoryExists(String category, SQLThread t) throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            
            ps = t.prepare("SELECT id FROM `@REMOVED`.`@REMOVED` WHERE `label` = ? LIMIT 1;");
            ps.setString(1, category);
            ps.execute();
            
            rs = ps.getResultSet();
            
            return rs.next();
            
        } finally {
            
            Closer.closeQuietly(ps, rs);
            
        }
    }
    
    private void replyAndDelete(String msg, MessageCreateEvent event) {
        event.getChannel().sendMessage(msg).whenCompleteAsync(new BiConsumer<Message, Throwable>() {
            @Override
            public void accept(Message t, Throwable u) {
                deleteLater(t, 5);
            }
        });
    }

    private void deleteLater(Message message, int seconds) {
        TimerExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                message.delete();
            }
        }, seconds, TimeUnit.SECONDS);
    }
    
    private void loadChannels() {
        
        System.out.println("Loading channels... " + bot.channels.size());
        
        channels.clear();

        HashMap<Long, TextChannel> lookup = new HashMap<>();

        for (Map.Entry<String, Long> entry : bot.channels.entrySet()) {
            
            long id = entry.getValue();
            String label = entry.getKey();
            
            TextChannel tc = lookup.get(id);
            
            if (tc == null) {

                System.out.println("Loading channel " + id + " (" + label + ")");

                Channel channel = api.getChannelById(id).orElse(null);

                if (channel == null) {
                    DUtils.error("Missing channel " + id + " (" + label + ")");
                    continue;
                }

                tc = channel.asTextChannel().orElse(null);

                if (tc == null) {
                    DUtils.error("null text channel " + id + " (" + label + ")");
                    continue;
                }

                lookup.put(id, tc);
                cleanupChannel(tc, id, label);
            }

            channels.put(label, tc);
            System.out.println("Loaded channel " + id + " (" + label + ")");
        }
    }
    
    private void cleanupChannel(TextChannel channel, long id, String label) {

        System.out.println("Cleaning channel " + id + " (" + label + ")");

        channel.getMessages(100).whenComplete(new BiConsumer<MessageSet, Throwable>() {
            @Override
            public void accept(MessageSet set, Throwable u) {
                cleanMessageSet(channel, id, label, set);
            }
        });
    }

    private void cleanMessageSet(TextChannel channel, long id, String label, MessageSet set) {

        if (set.isEmpty()) {
            System.out.println("No messages to cleanup on " + id + " (" + label + ")");
            return;
        }

        ArrayList<Message> msgs = new ArrayList<>();
        Iterator<Message> it = set.iterator();

        while (it.hasNext()) {

            Message msg = it.next();

            if (!msg.isPinned()) {
                msgs.add(msg);
            }
        }

        if (msgs.isEmpty()) {
            
            System.out.println("Cleanup completed on " + id + " (" + label + ")");

        } else {
            
            System.out.println("Deleting " + msgs.size() + " messages on " + id + " (" + label + ")");
            
            channel.deleteMessages(msgs).whenComplete(new BiConsumer<Void, Throwable>() {
                @Override
                public void accept(Void t, Throwable u) {
                    cleanupChannel(channel, id, label);
                }
            });
        }
    }

    public void setGroup(Packet111SetGroup packet) {
        
        if (oficialsv == null) {
            return;
        }
        
        Role newrole = vipgroups.get(packet.group);

        if (newrole == null) {
            return;
        }
                
        if (discordids.containsKey(packet.userId)) {

            updateRole(discordids.get(packet.userId), newrole);

        } else {
            
            SQL.mslow(new SQLTask() {
                
                @Override
                public void execute(SQLDb db, SQLThread t) throws Throwable {

                    PreparedStatement ps = null;
                    ResultSet rs = null;

                    try {

                        ps = t.prepare("SELECT user_discord FROM `@REMOVED`.`@REMOVED` WHERE `user_id` = ? LIMIT 1;");
                        ps.setInt(1, packet.userId);
                        ps.execute();

                        rs = ps.getResultSet();
                        User user = null;

                        if (rs.next()) {
                            long discordid = rs.getLong(1);
                            user = oficialsv.getMemberById(discordid).orElse(null);
                        }

                        discordids.put(packet.userId, user);
                        updateRole(user, newrole);

                    } finally {

                        Closer.closeQuietly(ps, rs);

                    }
                    
                }
            });
        }
    }

    private void updateRole(User user, Role newrole) {
        
        if (user == null) {
            return;
        }

        updateRole(user, newrole, oficialsv.getRoles(user), null);
    }

    private void updateRole(User user, Role newrole, List<Role> roles, SingleCallback<Void> callback) {

        ArrayList<Role> toremove = new ArrayList<>();

        for (Role role : roles) {
            if (!role.isEveryoneRole() && vipids.contains(role.getId())) {
                toremove.add(role);
            }
        }

        boolean addrole = newrole != null && !newrole.isEveryoneRole();
        Iterator<Role> it = toremove.iterator();
        
        while (it.hasNext()) {
            
            Role role = it.next();
            boolean callnow = !it.hasNext();
            
            oficialsv.removeRoleFromUser(user, role).whenComplete(new BiConsumer<Void, Throwable>() {
                
                @Override
                public void accept(Void t, Throwable u) {
                    
                    if (u != null) {
                        System.err.println("Failed to remove role '"+ role.getName() +"' of " + user.getName() + " ( " + user.getId() + " )");
                        u.printStackTrace();
                    }

                    if (!addrole && callnow && callback != null) {
                        callback.invoke(null);
                    }
                }
            });
        }

        if (!addrole) {

            if (callback != null && toremove.isEmpty()) {
                callback.invoke(null);
            }

        } else {
            
            oficialsv.addRoleToUser(user, newrole).whenComplete(new BiConsumer<Void, Throwable>() {
                
                @Override
                public void accept(Void t, Throwable u) {
                    
                    if (u != null) {
                        System.err.println("Failed to add role '"+ newrole.getName() +"' of " + user.getName() + " ( " + user.getId() + " )");
                        u.printStackTrace();
                    }
                    
                    if (callback != null) {
                        callback.invoke(null);
                    }
                }
            });
        }
    }
}