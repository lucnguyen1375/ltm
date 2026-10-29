package UDP;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Locale;

// ======================================================
// Class Student - tên đầy đủ vẫn là UDP.Student
// ======================================================
class Student implements Serializable {

    private static final long serialVersionUID = 20171107L;

    private String id;
    private String code;
    private String name;
    private String email;

    public Student(String id, String code, String name, String email) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.email = email;
    }

    public Student(String code) {
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}


// ======================================================
// UDP Client
// ======================================================
public class UDP_Object {

    public static void main(String[] args) {

        String serverHost = "36.50.135.242"; // đổi IP server
        int serverPort = 2209;

        String studentCode = "B23DCCN516";
        String qCode = "icVhYNOz";

        try (DatagramSocket socket = new DatagramSocket()) {

            InetAddress serverAddress =
                    InetAddress.getByName(serverHost);

            // ==========================================
            // a. Gửi ;studentCode;qCode
            // ==========================================
            String request =
                    ";" + studentCode + ";" + qCode;

            byte[] sendData =
                    request.getBytes(StandardCharsets.UTF_8);

            DatagramPacket sendPacket =
                    new DatagramPacket(
                            sendData,
                            sendData.length,
                            serverAddress,
                            serverPort
                    );

            socket.send(sendPacket);

            System.out.println("Sent: " + request);


            // ==========================================
            // b. Nhận dữ liệu
            // 8 byte đầu: requestId
            // còn lại: Student object
            // ==========================================
            byte[] receiveData = new byte[65535];

            DatagramPacket receivePacket = new DatagramPacket(
                            receiveData,
                            receiveData.length
                    );

            socket.receive(receivePacket);

            int length = receivePacket.getLength();


            // Lấy requestId
            String requestId =
                    new String(
                            receiveData,
                            0,
                            8,
                            StandardCharsets.UTF_8
                    );

            System.out.println("RequestId: " + requestId);


            // Deserialize Student
            ByteArrayInputStream bis = new ByteArrayInputStream(
                            receiveData,
                            8,
                            length - 8
                    );

            ObjectInputStream ois = new ObjectInputStream(bis);

            Student student =
                    (Student) ois.readObject();

            System.out.println(
                    "Name nhận được: " + student.getName()
            );


            // ==========================================
            // c1. Chuẩn hóa tên
            // ==========================================
            String normalizedName =
                    normalizeName(student.getName());

            student.setName(normalizedName);


            // ==========================================
            // c2. Tạo email
            // Nguyen Van Tuan Nam
            // -> namnvt@ptit.edu.vn
            // ==========================================
            String email =
                    createEmail(normalizedName);

            student.setEmail(email);

            System.out.println(
                    "Name chuẩn hóa: " + student.getName()
            );

            System.out.println(
                    "Email: " + student.getEmail()
            );


            // ==========================================
            // Serialize Student
            // ==========================================
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(student);
            oos.flush();

            byte[] studentBytes = bos.toByteArray();

            // ==========================================
            // Ghép:
            // [8 byte requestId][Student object]
            // ==========================================
            byte[] requestIdBytes =requestId.getBytes(StandardCharsets.UTF_8);
            byte[] resultData = new byte[8 + studentBytes.length];

            // copy requestId
            System.arraycopy(
                    requestIdBytes,
                    0,
                    resultData,
                    0,
                    8
            );

            // copy Student
            System.arraycopy(
                    studentBytes,
                    0,
                    resultData,
                    8,
                    studentBytes.length
            );


            // Gửi lại server
            DatagramPacket resultPacket =
                    new DatagramPacket(
                            resultData,
                            resultData.length,
                            serverAddress,
                            serverPort
                    );

            socket.send(resultPacket);

            System.out.println("Đã gửi kết quả lên server.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ======================================================
    // Chuẩn hóa tên:
    // nGUYEN vAN tUAN nAM
    // -> Nguyen Van Tuan Nam
    // ======================================================
    public static String normalizeName(String name) {

        String[] words =
                name.trim().split("\\s+");

        StringBuilder result =
                new StringBuilder();

        for (String word : words) {

            word =
                    word.toLowerCase(Locale.ROOT);

            String temp =
                    Character.toUpperCase(word.charAt(0))
                            + word.substring(1);

            if (result.length() > 0) {
                result.append(" ");
            }

            result.append(temp);
        }

        return result.toString();
    }


    // ======================================================
    // Nguyen Van Tuan Nam
    // -> namnvt@ptit.edu.vn
    // ======================================================
    public static String createEmail(String name) {

        // bỏ dấu tiếng Việt
        String noAccent =
                removeAccent(name);

        String[] words =
                noAccent
                        .toLowerCase(Locale.ROOT)
                        .trim()
                        .split("\\s+");

        StringBuilder email =
                new StringBuilder();

        // Lấy tên cuối
        email.append(
                words[words.length - 1]
        );

        // Lấy chữ cái đầu họ + tên đệm
        for (int i = 0;
             i < words.length - 1;
             i++) {

            email.append(
                    words[i].charAt(0)
            );
        }

        email.append("@ptit.edu.vn");

        return email.toString();
    }


    // ======================================================
    // Nguyễn Văn Nam
    // -> Nguyen Van Nam
    // ======================================================
    public static String removeAccent(String str) {

        String temp =Normalizer.normalize(str,Normalizer.Form.NFD);

        temp = temp.replaceAll("\\p{M}", "");

        temp = temp.replace("đ", "d")
                        .replace("Đ", "D");

        return temp;
    }
}