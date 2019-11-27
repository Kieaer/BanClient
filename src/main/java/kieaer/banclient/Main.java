package kieaer.banclient;

import io.anuke.arc.Events;
import io.anuke.arc.util.CommandHandler;
import io.anuke.arc.util.Log;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.game.EventType.PlayerJoin;
import io.anuke.mindustry.gen.Call;
import io.anuke.mindustry.plugin.Plugin;
import org.json.JSONArray;
import org.json.JSONTokener;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Main extends Plugin{
    public Main(){
        Events.on(PlayerJoin.class, e -> {
            Thread t = new Thread(() -> {
                try {
                    InetAddress address = InetAddress.getByName("mindustry.kr");
                    Socket socket = new Socket(address, 25000);
                    OutputStream os = socket.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                    BufferedWriter bw = new BufferedWriter(osw);

                    String ip = Vars.netServer.admins.getInfo(e.player.uuid).lastIP;

                    bw.write("checkban"+e.player.uuid+"/"+ip+"\n");
                    bw.flush();

                    InputStream is = socket.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                    BufferedReader br = new BufferedReader(isr);
                    String message = br.readLine();

                    is.close();
                    isr.close();
                    br.close();
                    socket.close();
                    boolean kick = Boolean.parseBoolean(message);

                    if (kick) {
                        Call.onKick(e.player.con, "You're banned from the main server!");
                        Log.info(e.player.name + " player has been kicked due to him being banned from the main server.");
                    }
                } catch (Exception ex) {
                    if(Objects.equals(ex.getMessage(), "Connection refused.")){
                        Log.err("Can't connect to the main server!");
                    } else {
                        ex.printStackTrace();
                    }
                }
            });
            t.start();
        });
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
    }
}
