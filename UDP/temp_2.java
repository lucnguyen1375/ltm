package UDP;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

class Customer implements  Serializable{
    private static final long serialVersionUID = 20151107L;

    public String id, code, name, dayOfBirth, userName;

    public Customer(String id, String code, String name, String dob) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.dayOfBirth = dob;
    }
    
}

public class temp_2{
    public static void main(String[] args) {
        String host = "36.50.135.242";
        String msv = "B23DCCN516";
        String qcode = "4BB5K8UK";
        int port = 2209;
        try(DatagramSocket socket = new DatagramSocket()){
            InetAddress serverAddress = InetAddress.getByName(host);
            String request = ";" + msv + ";" + qcode;
            byte[] sendData = request.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(
                sendData,
                sendData.length,
                serverAddress,
                port
            );
            socket.send(sendPacket);
            System.out.println("da gui request");
            byte[] receiveData = new byte[65535];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            int length = receivePacket.getLength();

            String requestId = new String(
                receiveData, 0, 8, StandardCharsets.UTF_8
            );

            System.out.println("RequestID " + requestId);
            ByteArrayInputStream bis = new ByteArrayInputStream(receiveData, 8, length-8);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Customer customer = (Customer) ois.readObject();
            customer.userName = createAccount(customer.name);
            customer.name = normalizeName(customer.name);
            customer.dayOfBirth = normalizeDob(customer.dayOfBirth);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(customer);
            oos.flush();

            byte[] customerBytes = bos.toByteArray();

            byte[] requestIdBytes = requestId.getBytes(StandardCharsets.UTF_8);
            byte[] resultData = new byte[8+customerBytes.length];

            System.arraycopy(requestIdBytes, 0, resultData, 0, 8);
            System.arraycopy(customerBytes, 0, resultData, 8, customerBytes.length);

            DatagramPacket resultPacket = new DatagramPacket(
                resultData, resultData.length, serverAddress, port
            );
            socket.send(resultPacket);
            System.out.println("Da gui len server");

        } catch(Exception e){

        }
    }

    public static String normalizeName(String name){
        String[] parts = name.trim().split("\\s+");
        StringBuilder normalized = new StringBuilder();
        normalized.append(parts[parts.length-1].toUpperCase())
                        .append(", ");
        for(int i = 0; i < parts.length-1; i++){
            String word = parts[i].toLowerCase();
            String temp = Character.toUpperCase(word.charAt(0))
                        + word.substring(1);
            normalized.append(temp).append(" ");
        }

        return normalized.toString().trim();
    }

    public static String normalizeDob(String dob){
        return 
            dob.substring(3,5) + "/" + dob.substring(0,2) + "/" + dob.substring(6);
    }

    public static String createAccount(String name){
        String[] parts = name.trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < parts.length - 1; i++){
            result.append(parts[i].toLowerCase().charAt(0));
        } 
        result.append(parts[parts.length-1].toLowerCase());
        return result.toString().trim();
    }
}