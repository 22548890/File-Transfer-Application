import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerListener implements Runnable {
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private int[] seqNumsSent;

    public ServerListener(Socket socket, ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        this.socket = socket;
        this.oos = oos;
        this.ois = ois;
    }

    @Override
    public void run() {
        try {
            seqNumsSent = (int[]) ois.readObject();
        } catch (ClassNotFoundException e) {
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int[] getSeqNumsSent() {
        return seqNumsSent;
    }
}