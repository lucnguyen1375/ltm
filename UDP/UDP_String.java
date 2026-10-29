import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class UDP_String {
    public static void main(String[] args) {
        String serverHost = "36.50.135.242"; // Đổi thành IP server nếu đề cung cấp
        int serverPort = 2208;

        String studentCode = "B23DCCN516";
        String qCode = "U7NnBLVm";

        try (DatagramSocket socket = new DatagramSocket()) {

            InetAddress serverAddress = InetAddress.getByName(serverHost);

            // =========================
            // a. Gửi ;studentCode;qCode
            // =========================
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

            // =========================
            // b. Nhận requestId;data
            // =========================
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

            // split thành 2 phần thôi
            String[] parts = response.split(";", 2);

            String requestId = parts[0];
            String data = parts[1];

            // =========================
            // c. Tìm ký tự xuất hiện nhiều nhất
            // =========================
            Map<Character, Integer> frequency = new HashMap<>();

            for (char c : data.toCharArray()) {
                frequency.put(c, frequency.getOrDefault(c, 0) + 1);
            }

            char maxChar = data.charAt(0);
            int maxCount = 0;

            for (char c : data.toCharArray()) {
                int count = frequency.get(c);

                if (count > maxCount) {
                    maxCount = count;
                    maxChar = c;
                }
            }

            // Tìm các vị trí xuất hiện
            // Đề sử dụng vị trí bắt đầu từ 1
            StringBuilder positions = new StringBuilder();

            for (int i = 0; i < data.length(); i++) {
                if (data.charAt(i) == maxChar) {
                    positions.append(i + 1).append(",");
                }
            }

            // requestId;ký_tự:vị_trí,
            String result = requestId + ";" + maxChar + ":" + positions;

            System.out.println("Result: " + result);

            // Gửi kết quả lại server
            byte[] resultData = result.getBytes();

            DatagramPacket resultPacket = new DatagramPacket(
                    resultData,
                    resultData.length,
                    serverAddress,
                    serverPort
            );

            socket.send(resultPacket);

            System.out.println("Sent result: " + result);

            // d. socket tự đóng nhờ try-with-resources

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
