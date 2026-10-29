import java.util.*;
import java.net.*;
import java.io.*;

public class TCP_Data_Stream_1 {
    public static void main(String[] args) {
        String host = "36.50.135.242";
        int port = 2207;

        String request = "B23DCCN516;QQRCT8vT";

        try (Socket socket = new Socket(host, port)) {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            out.writeUTF(request);
            out.flush();

            String s = in.readUTF().trim();
            int c = in.readInt();
            c = c % 26;
            System.out.println(s);
            System.out.println(c);
            
            StringBuilder res = new StringBuilder();
            for(char character : s.toCharArray()) {
                if (character >= 'A' && character <= 'Z') {
                    char newChar = (char) ((character - 'A' - c + 26) % 26 + 'A');
                    res.append(newChar);
                }
                else res.append(character);
            }
            System.out.println(res);
            out.writeUTF(res.toString());
            out.flush();
        } catch (Exception e){

        }
    }
}
