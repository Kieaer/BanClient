import arc.Events;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.mod.Plugin;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.Base64;
import java.util.Objects;

public class Main extends Plugin {
    public Main(){
        Events.on(EventType.PlayerJoin.class, e -> {
            Thread t = new Thread(() -> {
                try {
                    InetAddress address = InetAddress.getByName("mindustry.kr");
                    KeyGenerator gen = KeyGenerator.getInstance("AES");
                    SecretKey key = gen.generateKey();
                    gen.init(256);
                    byte[] raw = key.getEncoded();
                    SecretKeySpec spec = new SecretKeySpec(raw, "AES");
                    Cipher cipher = Cipher.getInstance("AES");

                    try (Socket socket = new Socket(address, 25000);
                         BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                         DataOutputStream os = new DataOutputStream(socket.getOutputStream())) {
                        String ip = Vars.netServer.admins.getInfo(e.player.uuid()).lastIP;
                        JsonObject data = new JsonObject();
                        data.add("type", "CheckBan");
                        data.add("uuid", e.player.uuid());
                        data.add("ip", ip);

                        byte[] encrypted = encrypt(data.toString(), spec, cipher);

                        os.writeBytes(Base64.getEncoder().encodeToString(raw) + "\n");
                        os.writeBytes(Base64.getEncoder().encodeToString(encrypted) + "\n");
                        os.flush();

                        byte[] receive = Base64.getDecoder().decode(is.readLine());
                        byte[] result = decrypt(receive, spec, cipher);

                        if (JsonValue.readJSON(new String(result)).asObject().get("result").asBoolean()) {
                            Call.kick(e.player.con, "You're banned from the server!");
                            Log.info(e.player.name + " player has been kicked due to him being banned from the remote server.");
                        }
                    } catch (IOException ex){
                        ex.printStackTrace();
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

    public static byte[] encrypt(String data, SecretKeySpec spec, Cipher cipher) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        cipher.init(Cipher.ENCRYPT_MODE, spec);
        return cipher.doFinal(data.getBytes());
    }

    public static byte[] decrypt(byte[] data, SecretKeySpec spec, Cipher cipher) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        cipher.init(Cipher.DECRYPT_MODE, spec);
        return cipher.doFinal(data);
    }
}
