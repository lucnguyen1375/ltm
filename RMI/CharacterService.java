package RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

interface CharacterService extends Remote {
    String requestCharacter(String studentCode, String qCode)
            throws RemoteException;

    void submitCharacter(String studentCode, String qCode, String data)
            throws RemoteException;
}