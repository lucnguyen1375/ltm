import java.io.*;
import java.net.*;
import java.util.*;

class TCP_Byte_Stream_2{
    public static void main(String[] args) {
        String host = "36.50.135.242";
        int port = 2206;

        String request = "B23DCCN516;nkvdrOJ8";

        try(Socket socket = new Socket(host, port)){
            socket.setSoTimeout(5000);

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            out.write(request.getBytes());
            out.flush();

            System.out.println("gui: " + request);

            byte[] buffer = new byte[1024];
            int bytesRead = in.read(buffer);
            String response = new String(buffer,0,bytesRead).trim();


            String[] list = response.split(",");
            int[] numbers = new int[list.length];

            for(int i = 0; i < list.length; i++){
                numbers[i] = Integer.parseInt(list[i].trim());
            }
            
            int maxx = 0;
            for(int i = 0; i < numbers.length; i++){
                if (maxx < numbers[i]) maxx = numbers[i];
            }

            int res = 0, pos = -1;
            for(int i = 0; i < numbers.length; i++){
                if (res < numbers[i] && numbers[i] < maxx){
                    res = numbers[i];
                    pos = i;
                }
            }

            String result = res + "," + pos;


            out.write(result.getBytes());
            out.flush();
        } catch(Exception e){

        }
    }
}