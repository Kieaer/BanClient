package kieaer.banclient;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import io.anuke.arc.Events;
import io.anuke.arc.util.Log;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.game.EventType.PlayerJoin;
import io.anuke.mindustry.gen.Call;
import io.anuke.mindustry.plugin.Plugin;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
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

                    KeyGenerator gen = KeyGenerator.getInstance("AES");
                    SecretKey key = gen.generateKey();
                    gen.init(256);
                    byte[] raw = key.getEncoded();
                    SecretKeySpec spec = new SecretKeySpec(raw,"AES");
                    Cipher cipher = Cipher.getInstance("AES");
                    BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                    String ip = Vars.netServer.admins.getInfo(e.player.uuid).lastIP;
                    byte[] encrypted = encrypt("checkban"+e.player.uuid+"/"+ip+"\n",spec,cipher);

                    os.writeBytes(Base64.encode(encrypted)+"\n");
                    os.writeBytes(Base64.encode(raw)+"\n");
                    os.flush();

                    byte[] receive = Base64.decode(is.readLine());
                    byte[] result = decrypt(receive,spec,cipher);

                    is.close();
                    socket.close();
                    boolean kick = Boolean.parseBoolean(new String(result));

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

    public static byte[] encrypt(String data, SecretKeySpec spec, Cipher cipher) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, spec);
        return cipher.doFinal(data.getBytes());
    }

    public static byte[] decrypt(byte[] data, SecretKeySpec spec, Cipher cipher) throws Exception {
        cipher.init(Cipher.DECRYPT_MODE, spec);
        return cipher.doFinal(data);
    }
}
