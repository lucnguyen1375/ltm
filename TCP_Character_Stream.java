/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author ASUS
 */
import java.io.*;
import java.net.*;
import java.util.*;
public class TCP_Character_Stream {
    public static void main(String[] args){
        try{
            Socket socket = new Socket("36.50.135.242", 2208);
            socket.setSoTimeout(5000);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader (new InputStreamReader(socket.getInputStream()));
            // a
            String request = "B23DCCN516;zKKKBgtE";
            writer.write(request);
            writer.newLine(); // gửi kí tự xuống dòng
            writer.flush(); // đẩy dữ liệu trong buffer ra socket ngay lập tức
            // b
            String response = reader.readLine();
            // c
            String[] domains = response.split(",");
            List<String> eduDomains = new ArrayList<>();
            for(String domain: domains){
                domain = domain.trim();
                if(domain.endsWith(".edu"))
                    eduDomains.add(domain);
            }
            String result = String.join(", ", eduDomains);
            writer.write(result);
            writer.newLine();
            writer.flush();
            reader.close();
            writer.close();
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
          
        
    }
}
