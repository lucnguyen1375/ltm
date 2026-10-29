import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

public class temp {
    public static void main(String[] args) {
        String host = "36.50.135.242";
        String msv = "B23DCCN516";
        String qcode = "7RxY9Lw6";
        String request = ";" + msv + ";" + qcode;
        int serverPort = 2207;
        try(DatagramSocket socket = new DatagramSocket()){
            InetAddress serverAddress = InetAddress.getByName(host);
            byte[] sendData = request.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(
                sendData,
                sendData.length,
                serverAddress,
                serverPort
            );

            socket.send(sendPacket);

            System.out.println("Sent: " + request);
            
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
            System.out.println("Received: " + response);

            // Tách dữ liệu
            String[] parts = response.split(";");

            String requestId = parts[0];
            int n = Integer.parseInt(parts[1]);

            String[] numberStrings = parts[2].split(",");

            // Lưu các số server đã gửi
            Set<Integer> existingNumbers = new HashSet<>();

            for (String number : numberStrings) {
                existingNumbers.add(Integer.parseInt(number.trim()));
            }

            // c. Tìm các số thiếu từ 1 -> n
            StringBuilder missingNumbers = new StringBuilder();

            for (int i = 1; i <= n; i++) {
                if (!existingNumbers.contains(i)) {
                    if (missingNumbers.length() > 0) {
                        missingNumbers.append(",");
                    }

                    missingNumbers.append(i);
                }
            }
            String result = requestId + ";" + missingNumbers;
            byte[] resultData = result.getBytes();
            DatagramPacket resultPackage = new DatagramPacket(
                resultData,
                resultData.length,
                serverAddress,
                serverPort
            );
            socket.send(resultPackage);
        }  catch(Exception e){

        }
    }
    
}
