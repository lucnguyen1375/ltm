import java.io.*;
import java.net.*;

public class Q1_DataStream {
    public static void main(String[] args) {
        String host = "36.50.135.242";
        int port = 2207;

        String studentCode = "B23DCCN516";
        String qCode = "keOGhmWN";

        try (Socket socket = new Socket(host, port)) {

            socket.setSoTimeout(5000);

            DataInputStream in =
                    new DataInputStream(socket.getInputStream());

            DataOutputStream out =
                    new DataOutputStream(socket.getOutputStream());

            // a. Gửi mã sinh viên + qCode
            out.writeUTF(studentCode + ";" + qCode);
            out.flush();

            // b. Nhận số nguyên
            int n = in.readInt();

            System.out.println("Nhan: " + n);

            // c. Đổi sang binary và hex
            String binary = Integer.toBinaryString(n);
            String hex = Integer.toHexString(n).toUpperCase();

            String result = binary + ";" + hex;

            System.out.println("Gui: " + result);

            out.writeUTF(result);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}