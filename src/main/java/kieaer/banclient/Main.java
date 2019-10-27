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

import static io.anuke.mindustry.Vars.netServer;

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

                    bw.write("[]\n");
                    bw.flush();

                    InputStream is = socket.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                    BufferedReader br = new BufferedReader(isr);
                    String message = br.readLine();

                    is.close();
                    isr.close();
                    br.close();
                    socket.close();
                    JSONTokener ar = new JSONTokener(message);
                    JSONArray result = new JSONArray(ar);

                    boolean kick = false;

                    for (int i = 0; i < result.length(); i++) {
                        String[] array = result.getString(i).split("\\|", -1);
                        if (array[0].length() == 12) {
                            netServer.admins.banPlayerID(array[0]);
                            if (e.player.uuid.equals(array[0])) {
                                kick = true;
                            }
                            if (!array[1].equals("<unknown>") && array[1].length() <= 15) {
                                if (Vars.netServer.admins.getInfo(e.player.uuid).lastIP.equals(array[1])) {
                                    kick = true;
                                }
                            }
                        }
                        if (array[0].equals("<unknown>")) {
                            if (Vars.netServer.admins.getInfo(e.player.uuid).lastIP.equals(array[1])) {
                                kick = true;
                            }
                        }
                    }
                    if (kick) {
                        Call.onKick(e.player.con, "You're banned from the main server!");
                        Log.info(e.player.name + " player has been kicked due to him being banned from the main server.");
                    } else {
                        Log.info(e.player.name + " player isn't banned from the main server.");
                    }
                    result = null;
                    ar = null;
                    System.gc();
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
