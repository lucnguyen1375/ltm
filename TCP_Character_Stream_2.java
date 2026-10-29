import java.util.*;
import java.net.*;
import java.io.*;

public class TCP_Character_Stream_2 {
    public static void main(String[] args) {
        String host = "36.50.135.242";
        int port = 2208;

        String request = "B23DCCN516;rQIDNYNF";

        try(Socket socket = new Socket(host, port)){
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            writer.write(request);
            writer.newLine();
            writer.flush();

            String response = reader.readLine().trim();

            HashMap<Character, Integer> map = new HashMap<Character, Integer>();

            for(Character c : response.toCharArray()){
                if (c.equals(' ')) continue;
                if (map.containsKey(c)){
                    map.put(c, map.get(c)+1);
                } else{
                    map.put(c, 1);
                }
            }

            String res = "";

            for(Character c : response.toCharArray()){
                if (map.containsKey(c) && map.get(c) > 1){
                    res = res +  c + ":" + map.get(c) + ",";
                    map.remove(c);
                }
            }

            writer.write(res);
            writer.newLine();
            writer.flush();
            reader.close();
            reader.close();
            socket.close();
        } catch (Exception e){

        }
    }
}
