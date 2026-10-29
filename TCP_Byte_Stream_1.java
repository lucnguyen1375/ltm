import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

public class TCP_Byte_Stream_1 {
    public static void main(String[] args) {
        String host = "36.50.135.242";
        int port = 2206;

        String request = "B23DCCN516;CHBSFiDF";

        try (Socket socket = new Socket(host, port)) {

            socket.setSoTimeout(5000);

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // a. Gửi studentCode;qCode
            out.write(request.getBytes());
            out.flush();

            System.out.println("Gui: " + request);

            // b. Nhận chuỗi số từ server
            byte[] buffer = new byte[1024];

            int bytesRead = in.read(buffer);

            String response = new String(buffer, 0, bytesRead).trim();

            System.out.println("Nhan: " + response);

            // Chuyển chuỗi thành mảng số nguyên
            String[] parts = response.split(",");
            int[] numbers = new int[parts.length];

            for (int i = 0; i < parts.length; i++) {
                numbers[i] = Integer.parseInt(parts[i].trim());
            }

            // c. Tìm khoảng cách nhỏ nhất
            Arrays.sort(numbers);

            int minDistance = Integer.MAX_VALUE;
            int first = 0;
            int second = 0;

            for (int i = 1; i < numbers.length; i++) {

                int distance = numbers[i] - numbers[i - 1];

                if (distance < minDistance) {
                    minDistance = distance;
                    first = numbers[i - 1];
                    second = numbers[i];
                }
                // Nếu có nhiều cặp cùng khoảng cách nhỏ nhất,
                // lấy cặp có giá trị lớn hơn
                else if (distance == minDistance) {
                    if (numbers[i] > second) {
                        first = numbers[i - 1];
                        second = numbers[i];
                    }
                }
            }

            String result = minDistance + "," + first + "," + second;

            System.out.println("Ket qua: " + result);

            // Gửi kết quả về server
            out.write(result.getBytes());
            out.flush();

            // d. Socket tự đóng do try-with-resources

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}