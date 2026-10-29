import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDP_Data_Type_1 {
    public static void main(String[] args) {
        String serverHost = "36.50.135.242"; // đổi thành IP server nếu cần
        int serverPort = 2207;

        String studentCode = "B23DCCN516";
        String qCode = "eGloeotK";

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

            // b. Nhận requestId;a1,a2,...,a50
            byte[] receiveData = new byte[2048];

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

            // Tách requestId và dãy số
            String[] parts = response.split(";", 2);

            String requestId = parts[0];
            String data = parts[1];

            // Tách 50 số
            String[] numbers = data.split(",");

            int max = Integer.MIN_VALUE;
            int min = Integer.MAX_VALUE;

            // c. Tìm max, min
            for (String number : numbers) {
                int value = Integer.parseInt(number.trim());

                if (value > max) {
                    max = value;
                }

                if (value < min) {
                    min = value;
                }
            }

            System.out.println("Max = " + max);
            System.out.println("Min = " + min);

            // Format: requestId;max,min
            String result = requestId + ";" + max + "," + min;

            byte[] resultData = result.getBytes();

            DatagramPacket resultPacket = new DatagramPacket(
                    resultData,
                    resultData.length,
                    serverAddress,
                    serverPort
            );

            socket.send(resultPacket);

            System.out.println("Sent result: " + result);

            // d. socket tự đóng do try-with-resources

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}