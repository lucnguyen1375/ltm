import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class TCP_Data_Stream {
    public static void main(String[] args) {

        String host = "36.50.135.242";
        int port = 2207;

        String studentCode = "B23DCCN516";
        String qCode = "P5R6KAem"; // thay bằng mã câu hỏi thật

        try (Socket socket = new Socket(host, port)) {

            // Thời gian chờ tối đa 5 giây
            socket.setSoTimeout(5000);

            DataInputStream in =
                    new DataInputStream(socket.getInputStream());

            DataOutputStream out =
                    new DataOutputStream(socket.getOutputStream());

            // a. Gửi studentCode;qCode
            String request = studentCode + ";" + qCode;

            out.writeUTF(request);
            out.flush();

            System.out.println("Gui: " + request);

            // b. Nhận lần lượt 2 số nguyên a và b
            int a = in.readInt();
            int b = in.readInt();

            System.out.println("a = " + a);
            System.out.println("b = " + b);

            // c. Tính tổng và tích
            int sum = a + b;
            int product = a * b;

            System.out.println("Tong = " + sum);
            System.out.println("Tich = " + product);

            // Gửi LẦN LƯỢT tổng rồi tích
            out.writeInt(sum);
            out.writeInt(product);
            out.flush();

            // d. Socket tự đóng

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}