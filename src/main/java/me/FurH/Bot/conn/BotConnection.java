package me.FurH.Bot.conn;

import java.io.IOException;
import java.util.ArrayList;
import me.FurH.Bot.DiscordBot;
import me.FurH.Core.util.Utils;
import me.FurH.NIO.client.ServerConnection;
import me.FurH.SkyHub.PacketListener;
import me.FurH.SkyHub.PacketLoader;
import me.FurH.SkyHub.auth.Packet14UserAuth;
import me.FurH.SkyHub.balancer.Packet146BalancerPing;
import me.FurH.SkyHub.balancer.Packet147OpenRequest;
import me.FurH.SkyHub.balancer.Packet148OpenResult;
import me.FurH.SkyHub.balancer.Packet149CheckServer;
import me.FurH.SkyHub.bank.Packet112BankRegister;
import me.FurH.SkyHub.bank.Packet11BankDeposit;
import me.FurH.SkyHub.bank.Packet12TryPurchase;
import me.FurH.SkyHub.bank.Packet13PurchaseResult;
import me.FurH.SkyHub.bm.Packet124AskBMTheme;
import me.FurH.SkyHub.bm.Packet125BMThemeReply;
import me.FurH.SkyHub.chains.Packet39JoinCmd;
import me.FurH.SkyHub.chains.Packet40PlotLoad;
import me.FurH.SkyHub.chains.Packet41PlotUnload;
import me.FurH.SkyHub.chains.Packet99TimeSync;
import me.FurH.SkyHub.commands.Packet102CommandRegister;
import me.FurH.SkyHub.exp.Packet68GiveExp;
import me.FurH.SkyHub.faq.Packet154OpenFaq;
import me.FurH.SkyHub.files.Packet119FileRead;
import me.FurH.SkyHub.files.Packet120FileWrite;
import me.FurH.SkyHub.files.Packet121FileData;
import me.FurH.SkyHub.flush.Packet111SetGroup;
import me.FurH.SkyHub.flush.Packet25UnbanUser;
import me.FurH.SkyHub.flush.Packet30FlushGroup;
import me.FurH.SkyHub.flush.Packet94FlushWardobre;
import me.FurH.SkyHub.friends.Packet113FriendAction;
import me.FurH.SkyHub.gamelib.Packet142ArenaStatus;
import me.FurH.SkyHub.gamelib.Packet143ChangeMap;
import me.FurH.SkyHub.gamelib.Packet150SelectArena;
import me.FurH.SkyHub.gamelib.Packet153ProfileCache;
import me.FurH.SkyHub.games.Packet101TopStats;
import me.FurH.SkyHub.games.Packet122AskMapList;
import me.FurH.SkyHub.games.Packet123MapList;
import me.FurH.SkyHub.games.Packet127SaveRejoin;
import me.FurH.SkyHub.games.Packet128CancelRejoin;
import me.FurH.SkyHub.games.Packet71UpdateRequest;
import me.FurH.SkyHub.games.Packet88AskNextWorld;
import me.FurH.SkyHub.games.Packet89NextWorld;
import me.FurH.SkyHub.handshake.ClientType;
import me.FurH.SkyHub.handshake.Packet1Handshake;
import me.FurH.SkyHub.handshake.Packet2ServerName;
import me.FurH.SkyHub.listener.ListenerAction;
import me.FurH.SkyHub.listener.ListenerType;
import me.FurH.SkyHub.listener.Packet4ListenerAction;
import me.FurH.SkyHub.messages.Packet10BroadcastMessage;
import me.FurH.SkyHub.messages.Packet131GlobalChat;
import me.FurH.SkyHub.messages.Packet132GlobalToggle;
import me.FurH.SkyHub.msgs.Packet140DirectMessage;
import me.FurH.SkyHub.msgs.Packet35MessageTo;
import me.FurH.SkyHub.newchain.Packet91PlayerTags;
import me.FurH.SkyHub.newchain.Packet93VanishPlayer;
import me.FurH.SkyHub.party.Packet72PartyCreate;
import me.FurH.SkyHub.party.Packet73PartyLeave;
import me.FurH.SkyHub.party.Packet74PartyInvite;
import me.FurH.SkyHub.party.Packet75PartyAccept;
import me.FurH.SkyHub.party.Packet76PartyKick;
import me.FurH.SkyHub.party.Packet77PartyLeader;
import me.FurH.SkyHub.party.Packet78PartyTicket;
import me.FurH.SkyHub.party.Packet79PartyResult;
import me.FurH.SkyHub.php.Packet24ActionPhP;
import me.FurH.SkyHub.playerlist.Packet114CheckUser;
import me.FurH.SkyHub.playerlist.Packet134RemoteQuery;
import me.FurH.SkyHub.playerlist.Packet80AddConnection;
import me.FurH.SkyHub.playerlist.Packet81RemoveConnection;
import me.FurH.SkyHub.playerlist.Packet82ServerAddPlayer;
import me.FurH.SkyHub.playerlist.Packet83ServerRemovePlayer;
import me.FurH.SkyHub.playerlist.Packet8CounterSync;
import me.FurH.SkyHub.players.Packet100PlayerShield;
import me.FurH.SkyHub.players.Packet22PlayerJoin;
import me.FurH.SkyHub.players.Packet38UserCreated;
import me.FurH.SkyHub.players.Packet9DisconnectPlayer;
import me.FurH.SkyHub.proxy.Packet136ProxyRead;
import me.FurH.SkyHub.proxy.Packet137ProxyReply;
import me.FurH.SkyHub.proxy.Packet138ProxyList;
import me.FurH.SkyHub.punish.Packet23PunishCache;
import me.FurH.SkyHub.ranks.Packet133RanksUpdated;
import me.FurH.SkyHub.rewards.Packet21PlayerReward;
import me.FurH.SkyHub.servers.Packet115SafeReboot;
import me.FurH.SkyHub.servers.Packet126ServerTps;
import me.FurH.SkyHub.servers.Packet139DestPacket;
import me.FurH.SkyHub.servers.Packet145LobbyStatus;
import me.FurH.SkyHub.servers.Packet151DefenseMode;
import me.FurH.SkyHub.servers.Packet17ConnectTo;
import me.FurH.SkyHub.servers.Packet37DropServer;
import me.FurH.SkyHub.servers.Packet61SyncPing;
import me.FurH.SkyHub.servers.Packet62SyncPong;
import me.FurH.SkyHub.servers.Packet63AsyncPing;
import me.FurH.SkyHub.servers.Packet64AsyncPong;
import me.FurH.SkyHub.servers.Packet67Shutdown;
import me.FurH.SkyHub.servers.Packet6ServerStatus;
import me.FurH.SkyHub.servers.Packet7MirrorDown;
import me.FurH.SkyHub.sets.Packet129SetValue;
import me.FurH.SkyHub.sets.Packet152ValueListener;
import me.FurH.SkyHub.shield.Packet95CloseShield;
import me.FurH.SkyHub.shield.Packet96OpenShield;
import me.FurH.SkyHub.shield.Packet97AskShield;
import me.FurH.SkyHub.shield.Packet98ShieldData;
import me.FurH.SkyHub.skins.Packet32SkinData;
import me.FurH.SkyHub.skins.Packet33SkinRemove;
import me.FurH.SkyHub.storage.Packet141FileUnlock;
import me.FurH.SkyHub.storage.Packet84OpenFile;
import me.FurH.SkyHub.storage.Packet85FileData;
import me.FurH.SkyHub.storage.Packet86SaveFile;
import me.FurH.SkyHub.teleport.Packet48TeleportReq;
import me.FurH.SkyHub.teleport.Packet49PlayerTo;
import me.FurH.SkyHub.yt.Packet69YoutubeList;

/*
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class BotConnection extends ServerConnection implements PacketListener {

    private final DiscordBot bot;

    public BotConnection(DiscordBot bot) throws IOException {
        
        super(Utils.getMasterIp(), 0 /*@REMOVED*/, true);
        
        this.bot = bot;
        
        super.enableCompression();
        this.enableWriteQueue();
        
        this.setPacketMap(PacketLoader.loadPackets(PacketListener.class));
    }
    
    @Override
    public BotConnection getPacketListener() {
        return this;
    }
    
    @Override
    public void connected() {
        
        super.connected();
        
        Packet1Handshake packet = new Packet1Handshake();

        packet.clientType = ClientType.DISCORD;
       
        packet.ip = bot.host;
        packet.port = bot.port;

        write(packet);
        
        ArrayList<Byte> listeners = new ArrayList<>();

        listeners.add(ListenerType.SAFEREBOOT.value);
        listeners.add(ListenerType.SETVALUESPP.value);
        listeners.add(ListenerType.FLUSHPERMS.value);

        Packet4ListenerAction tags = new Packet4ListenerAction();

        tags.action = ListenerAction.ADD;
        tags.tags = listeners.toArray(new Byte[ 0 ]);

        write(tags);
        
        // listen to bungee values
        
        Packet152ValueListener over = new Packet152ValueListener();

        over.serverName = "bungeecord";

        write(over);
        
        try {
            bot.connected();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Override
    public void handshake(Packet1Handshake packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void servername(Packet2ServerName packet) {
        System.out.println("Connected to hub, " + packet.serverName);
    }

    @Override
    public void handleTags(Packet4ListenerAction packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void serverStatus(Packet6ServerStatus packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mirrorDown(Packet7MirrorDown packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void synchronize(Packet8CounterSync packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void dropPlayer(Packet9DisconnectPlayer packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void broadcastMessage(Packet10BroadcastMessage packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void bankDeposit(Packet11BankDeposit packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void tryPurchase(Packet12TryPurchase packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void purchaseResult(Packet13PurchaseResult packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void userLogin(Packet14UserAuth packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void connectTo(Packet17ConnectTo packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void rewardPlayer(Packet21PlayerReward packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void playerJoin(Packet22PlayerJoin packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void punishCache(Packet23PunishCache packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void phpCommand(Packet24ActionPhP packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void unbanUser(Packet25UnbanUser packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void skinData(Packet32SkinData packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeSkin(Packet33SkinRemove packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void messageTo(Packet35MessageTo packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void dropServer(Packet37DropServer packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void userCreated(Packet38UserCreated packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void joinCmd(Packet39JoinCmd packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void plotLoad(Packet40PlotLoad packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void plotUnload(Packet41PlotUnload packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void tpaRequest(Packet48TeleportReq packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void teleportTo(Packet49PlayerTo packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void syncping(Packet61SyncPing packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void syncpong(Packet62SyncPong packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void asyncping(Packet63AsyncPing packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void asyncpong(Packet64AsyncPong packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void shutdown(Packet67Shutdown packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void giveExp(Packet68GiveExp packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void youtubeList(Packet69YoutubeList packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateRequest(Packet71UpdateRequest packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void partyCreate(Packet72PartyCreate packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void partyLeave(Packet73PartyLeave packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void partyInvite(Packet74PartyInvite packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void partyAccept(Packet75PartyAccept packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void partyKick(Packet76PartyKick packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void partyLeader(Packet77PartyLeader packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void partyTicket(Packet78PartyTicket packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void partyResult(Packet79PartyResult packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addConnection(Packet80AddConnection packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeConnection(Packet81RemoveConnection packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addPlayer(Packet82ServerAddPlayer packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removePlayer(Packet83ServerRemovePlayer packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void openFile(Packet84OpenFile packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void fileData(Packet85FileData packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void closeFile(Packet86SaveFile packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void askNextWorld(Packet88AskNextWorld packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void nextWorld(Packet89NextWorld packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void handleTeam(Packet91PlayerTags packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void vanishPlayer(Packet93VanishPlayer packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void flushWardobre(Packet94FlushWardobre packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void closeShield(Packet95CloseShield packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void openShield(Packet96OpenShield packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void askForShield(Packet97AskShield packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void shieldData(Packet98ShieldData packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void timeSync(Packet99TimeSync packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void playerShield(Packet100PlayerShield packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void topTags(Packet101TopStats packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void registerCmd(Packet102CommandRegister packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setGroup(Packet111SetGroup packet) {
        bot.setGroup(packet);
    }

    @Override
    public void flushGroup(Packet30FlushGroup packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void bankRegister(Packet112BankRegister packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void friendAction(Packet113FriendAction packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void checkSession(Packet114CheckUser packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void safeReboot(Packet115SafeReboot packet) {
        System.err.println("Safe reboot time");
        bot.shutdown();
    }

    @Override
    public void fileRead(Packet119FileRead packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void fileWrite(Packet120FileWrite packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void fileReadData(Packet121FileData packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void askMapList(Packet122AskMapList packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mapList(Packet123MapList packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void askBmTheme(Packet124AskBMTheme packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void themeReply(Packet125BMThemeReply packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void serverTps(Packet126ServerTps packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saveRejoin(Packet127SaveRejoin packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cancelRejoin(Packet128CancelRejoin packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean handleValue() {
        return false;
    }

    @Override
    public void setValue(Packet129SetValue packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void globalChat(Packet131GlobalChat packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void globalToggle(Packet132GlobalToggle packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void ranksUpdated(Packet133RanksUpdated packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void remoteQuery(Packet134RemoteQuery packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void proxyRead(Packet136ProxyRead packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void proxyReply(Packet137ProxyReply packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void proxyList(Packet138ProxyList packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void redirPacket(Packet139DestPacket packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void directMessage(Packet140DirectMessage packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void fileUnlock(Packet141FileUnlock packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void arenaStatus(Packet142ArenaStatus packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void changeMap(Packet143ChangeMap packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void lobbyStatus(Packet145LobbyStatus packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void balancerPing(Packet146BalancerPing packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void openRequest(Packet147OpenRequest packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void openResult(Packet148OpenResult packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void checkServer(Packet149CheckServer packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void selectArena(Packet150SelectArena packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void defenseMode(Packet151DefenseMode packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void valueListener(Packet152ValueListener packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BotConnection getOwner() {
        return this;
    }

    @Override
    public void profileCache(Packet153ProfileCache packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void openFaq(Packet154OpenFaq packet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}