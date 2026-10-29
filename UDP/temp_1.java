import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class temp_1 {
    public static void main(String[] args) {
        String host = "36.50.135.242";
        String msv = "B23DCCN516";
        String qcode = "U7NnBLVm";
        String request = ";" + msv + ";" + qcode;
        int serverPort = 2208;
        try(DatagramSocket socket = new DatagramSocket()){
            InetAddress serverAddress = InetAddress.getByName(host);
            byte[] sendData = request.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(
                sendData,
                sendData.length,
                serverAddress ,
                serverPort
            );

            socket.send(sendPacket);

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(
                receiveData, 
                receiveData.length
            );
            socket.receive(receivePacket);
            String response = new String(
                receivePacket.getData(),
                0,
                receivePacket.getLength()
            ).trim();
            String[] parts = response.split(";");
            String requestId = parts[0];
            String text = parts[1];
            char maxChar =' ';
            int maxCount = 0;
            HashMap<Character, Integer> map = new HashMap<Character, Integer>();
            for(int i = 0; i < text.length(); i++){
                Character c = text.charAt(i);
                int count = map.getOrDefault(c, 0);
                count++;
                if (count > maxCount){
                    maxCount = count;
                    maxChar = c;
                }
                map.put(c,count);
            }
            StringBuilder position = new StringBuilder();
            for(int i =0; i < text.length(); i++){
                if (text.charAt(i) == maxChar){
                    position.append(i+1).append(",");
                }
            }

            String result = requestId + ";" + maxChar + ":" + position;

            byte[] resultData = result.getBytes();
            DatagramPacket resultPacket = new DatagramPacket(
                resultData, 
                resultData.length,
                serverAddress,
                serverPort
            );
            socket.send(resultPacket);
        }  catch(Exception e){

        }
    }
    
}
