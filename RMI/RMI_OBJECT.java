
package RMI;

import java.rmi.*;
import java.rmi.registry.*;
import java.io.Serializable;

// ========== 1. INTERFACE RMI ==========

interface ObjectService extends Remote {

    Serializable requestObject(String studentCode, String qCode)
            throws RemoteException;

    void submitObject(String studentCode, String qCode,
                      Serializable object)
            throws RemoteException;
}

// ========== 2. LOP DU LIEU ==========

class TicketSla implements Serializable {

    private static final long serialVersionUID = 20260517L;

    private String ticketId;
    private String priority;
    private int openedHoursAgo;
    private boolean breached;
    private String action;

    public TicketSla() {}

    public TicketSla(String ticketId, String priority, int openedHoursAgo) {
        this.ticketId = ticketId;
        this.priority = priority;
        this.openedHoursAgo = openedHoursAgo;
    }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public int getOpenedHoursAgo() { return openedHoursAgo; }
    public void setOpenedHoursAgo(int hours) { this.openedHoursAgo = hours; }

    public boolean isBreached() { return breached; }
    public void setBreached(boolean breached) { this.breached = breached; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    @Override
    public String toString() {
        return ticketId + " | " + priority +
               " | hours=" + openedHoursAgo +
               " | breached=" + breached +
               " | action=" + action;
    }
}

// ========== 3. MAIN ==========

public class RMI_OBJECT {

    static final String HOST = "36.50.135.242";
    static final int PORT = 1099;

    static final String STUDENT_CODE = "B23DCCN516";
    static final String Q_CODE = "hoOvK4NN";

    // Ket noi RMI
    static ObjectService connect() throws Exception {
        Registry registry = LocateRegistry.getRegistry(HOST, PORT);
        return (ObjectService) registry.lookup("RMIObjectService");
    }

    // Xu ly logic
    static void processData(TicketSla ticket) {

        String priority = ticket.getPriority();
        int hours = ticket.getOpenedHoursAgo();

        boolean breached;

        switch (priority) {
            case "CRITICAL":
                breached = hours > 2;
                break;
            case "HIGH":
                breached = hours > 8;
                break;
            case "MEDIUM":
                breached = hours > 24;
                break;
            case "LOW":
                breached = hours > 72;
                break;
            default:
                throw new IllegalArgumentException("Priority khong hop le");
        }

        ticket.setBreached(breached);

        if (!breached) {
            ticket.setAction("MONITOR");
        } else if (priority.equals("CRITICAL") || hours > 96) {
            ticket.setAction("ESCALATE_L2");
        } else {
            ticket.setAction("ESCALATE_L1");
        }
    }

    public static void main(String[] args) {
        try {
            // Ket noi
            ObjectService service = connect();

            // Nhan doi tuong
            TicketSla ticket = (TicketSla) service.requestObject(
                    STUDENT_CODE, Q_CODE
            );

            System.out.println("Data: " + ticket);

            // Xu ly
            processData(ticket);

            System.out.println("Result: " + ticket);

            // Gui ket qua
            service.submitObject(STUDENT_CODE, Q_CODE, ticket);

            System.out.println("Da gui ket qua!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
