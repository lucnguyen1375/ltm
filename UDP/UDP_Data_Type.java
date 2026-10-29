import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

public class UDP_Data_Type {
    public static void main(String[] args) {
        String serverHost = "36.50.135.242"; // đổi thành IP server nếu cần
        int serverPort = 2207;

        String studentCode = "B23DCCN516";
        String qCode = "7RxY9Lw6";

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(serverHost);

            // a. Gửi ;studentCode;qCode
            String request = ";" + studentCode + ";" + qCode;

            byte[] sendData = request.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(
                    sendData,
                    sendData.length,
                    serverAddress,
                    serverPort
            );

            socket.send(sendPacket);

            System.out.println("Sent: " + request);

            // b. Nhận requestId;n;A1,A2,...,Am
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

            // requestId;B1,B2,...,Bm
            String result = requestId + ";" + missingNumbers;

            byte[] resultData = result.getBytes();

            DatagramPacket resultPacket = new DatagramPacket(
                    resultData,
                    resultData.length,
                    serverAddress,
                    serverPort
            );

            socket.send(resultPacket);

            System.out.println("Sent result: " + result);

            // d. Socket tự đóng do try-with-resources
            System.out.println("Socket closed.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}