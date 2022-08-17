import java.awt.Font;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * File_Client
 */
public class File_Client {

    DatagramSocket dsocket;
    InetAddress address;
    File file;
    String host;
    int port;

    // public void udpStart() {
    //     try {
    //         dsocket = new DatagramSocket();
    //         address = InetAddress.getByName("localhost");

    //         String filename = file.getName();
    //         byte[] filenameBytes = filename.getBytes();
    //         DatagramPacket filenamePacket = new DatagramPacket(filenameBytes, filenameBytes.length, address, port);
    //         dsocket.send(filenamePacket);

    //         DatagramPacket fileSizePacket = new DatagramPacket()

    //         FileInputStream fis = null;
    //         byte[] fileBytes = new byte[(int) file.length()];
    //         try {
    //             fis = new FileInputStream(file);
    //             fis.read(fileBytes);
    //             fis.close();
    //         } catch (IOException e) {
    //             e.printStackTrace();
    //         }
    //         udpSend(fileBytes);
    //     } catch (Exception e) {
    //         // TODO Auto-generated catch block
    //         e.printStackTrace();
    //         System.exit(0);
    //     }
    // }

    public void send() throws UnknownHostException, IOException {
        String tcphost = "localhost";
        int tcpport = 1234; 
        String udphost = "localhost";
        int udpport = 1345;

        // setting up socket for tcp communications
        Socket tcpsocket = new Socket(tcphost, tcpport);
        ObjectOutputStream oos = new ObjectOutputStream(tcpsocket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(tcpsocket.getInputStream());

        // sending filename and size over tcp
        String filename = file.getName();
        oos.writeObject(filename);
        oos.flush();
        int filesize = (int) file.length();
        oos.writeInt(filesize);
        oos.flush();

        // setting up socket for udp communications
        dsocket = new DatagramSocket();
        address = InetAddress.getByName(udphost);

        // convert file into byte array
        FileInputStream fis = null;
        byte[] fileBytes = new byte[filesize];
        try {
            fis = new FileInputStream(file);
            fis.read(fileBytes);
            fis.close();
        } catch (IOException e) {
            System.out.println("Error when converting file to byte array");
            System.exit(0);
        }

        ArrayList<DatagramPacket> packets = new ArrayList<DatagramPacket>();
        int[] seqNums = new int[filesize/1021 + 1];
        int seqNum = 0;
        // read file into datagram packets
        for (int i = 0; i < filesize; i += 1021) {
            seqNum++;
            seqNums[seqNum-1] = seqNum;
            byte[] packetBytes = new byte[1024];

            // add unqiue sequence number (starting at 1)
            packetBytes[0] = (byte) (seqNum >> 8);
            packetBytes[1] = (byte) (seqNum);

            if (i + 1021 >= filesize) { // if end of file is reached
                packetBytes[2] = (byte) (1);
                System.arraycopy(fileBytes, i, packetBytes, 3, filesize - i);
            } else {
                packetBytes[2] = (byte) (0);
                System.arraycopy(fileBytes, i, packetBytes, 3, 1021);
            }

            DatagramPacket packet = new DatagramPacket(packetBytes, packetBytes.length, address, udpport);
            packets.add(packet); 
        }

        System.out.println(packets.size());
        for (int i = 1; i <= packets.size(); i++) {
            dsocket.send(packets.get(i-1));
            if (i%42 == 0) { // every multiple of 42
                int[] toSend = new int[42];
                System.arraycopy(seqNums, i-42, toSend, 0, 42);
                oos.writeObject(toSend);
                oos.flush();
            } else if (i == packets.size()) { // final bunch of packets
                System.out.println("HAhahahah");
                int[] toSend = new int[packets.size()%42];
                System.arraycopy(seqNums, i-(i%42), toSend, 0, i%42);
                oos.writeObject(toSend);
                oos.flush();
            }
        }

       
        // testing sending all packets
        // for (DatagramPacket packet : packets) {
        //     dsocket.send(packet);
        // }

    }

    public void udpSend() throws IOException {
        // connect tcp socket
        Socket tcpsocket = new Socket("localhost", 1234);
        ObjectOutputStream oos = new ObjectOutputStream(tcpsocket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(tcpsocket.getInputStream());

        byte[] fileBytes = null;
        try {
            dsocket = new DatagramSocket();
            address = InetAddress.getByName("localhost");

            String filename = file.getName();
            byte[] filenameBytes = filename.getBytes();
            DatagramPacket filenamePacket = new DatagramPacket(filenameBytes, filenameBytes.length, address, 1233);
            dsocket.send(filenamePacket);

            int filesize = (int) file.length();
            oos.writeObject(filesize);

            FileInputStream fis = null;
            fileBytes = new byte[(int) file.length()];
            try {
                fis = new FileInputStream(file);
                fis.read(fileBytes);
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(0);
        }

        ArrayList<DatagramPacket> packets = new ArrayList<DatagramPacket>();
        int seqNum = 0;
        boolean eof;
        int confSeqNum = 0;

        // reads the file into datagram packets
        for (int i = 0; i < fileBytes.length; i += 1021) {
            seqNum++;

            byte[] msg = new byte[1024];
            // add unqiue sequence number
            msg[0] = (byte) (seqNum >> 8);
            msg[1] = (byte) (seqNum);

            if (i + 1021 >= fileBytes.length) {
                msg[2] = (byte) (1);
                eof = true;

                System.arraycopy(fileBytes, i, msg, 3, fileBytes.length - i);
            } else {
                msg[2] = (byte) (0);

                System.arraycopy(fileBytes, i, msg, 3, 1021);
            }

            DatagramPacket packet = new DatagramPacket(msg, msg.length, address, 1233);
            packets.add(packet); 
        }

        // send packets
        for (int i = 0; i < packets.size(); i+=42) {
            int[] seqNumsSent = new int[42];
            for (int j = 0; j < 42 && i + j < packets.size(); j++) {
                dsocket.send(packets.get(i+j));
                seqNumsSent[j] = i + j;
            }
            // send list of sequence numbers over tcp
            oos.writeObject(seqNumsSent);
        }

        oos.close();
        tcpsocket.close();

        // for (int i = 0; i < fileBytes.length; i += 1021) {
        //     seqNum += 1;

        //     byte[] msg = new byte[1024];
        //     msg[0] = (byte) (seqNum >> 8);
        //     msg[1] = (byte) (seqNum);

        //     if (i + 1021 >= fileBytes.length) {
        //         eof = true;
        //         msg[2] = (byte) (1);
        //     } else {
        //         eof = false;
        //         msg[2] = (byte) (0);
        //     }

        //     if (eof) {
        //         System.arraycopy(fileBytes, i, msg, 3, fileBytes.length - i);
        //     } else {
        //         System.arraycopy(fileBytes, i, msg, 3, 1021);
        //     }

        //     DatagramPacket packetSend = new DatagramPacket(msg, msg.length, address, port); 
        //     dsocket.send(packetSend);

        //     boolean confRec;

        //     while (true) {
        //         byte[] confBytes = new byte[2];
        //         DatagramPacket confPacket = new DatagramPacket(confBytes, confBytes.length);
        //         try {
        //             dsocket.setSoTimeout(100);
        //             dsocket.receive(confPacket);
        //             confSeqNum = ((confBytes[0] & 0xff) << 8) + (confBytes[1] & 0xff);
        //             confRec = true;
        //         } catch (SocketTimeoutException e) {
        //             System.out.println("Socket timed waiting for confirmation");
        //             confRec = false;
        //         }

        //         if (confSeqNum == seqNum && confRec) {
        //             System.out.println("Received sequence number " + seqNum);
        //             break;
        //         } else {
        //             dsocket.send(packetSend);
        //             System.out.println("Resending sequence number " + seqNum);
        //         }
        //     }
        // }
    }

    public static void main(String[] args) {
        // final File[] fileToSend = new File[1];

        JFrame jframe = new JFrame("File Transfer File_Client");

        jframe.setSize(450, 450);
        jframe.setLayout(new BoxLayout(jframe.getContentPane(), BoxLayout.Y_AXIS));
        jframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        JLabel jTitle = new JLabel("File Transfer Sender");
        jTitle.setFont(new Font("Arial", Font.BOLD, 25));
        jTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        jTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel JFileName = new JLabel("Choose a file to send");
        JFileName.setFont(new Font("Arial", Font.BOLD, 20));
        JFileName.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
        JFileName.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JPanel jpButton = new JPanel();
        jpButton.setBorder(BorderFactory.createEmptyBorder(75, 0, 10, 0));

        JButton btnSend = new JButton("Send File");
        btnSend.setPreferredSize(new Dimension(150, 75));

        JButton btnChooseFile = new JButton("Choose File");
        btnChooseFile.setPreferredSize(new Dimension(150, 75));

        jpButton.add(btnSend);
        jpButton.add(btnChooseFile);

        File_Client sender = new File_Client();

        btnChooseFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setDialogTitle("Choose file to send");

                if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    sender.file = jFileChooser.getSelectedFile();
                    JFileName.setText("The file you want to send it: " + sender.file.getName());
                }
            }

        });

        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sender.file == null) {
                    JFileName.setText("Please choose a file first.");
                } else {
                    try {
                        // FileInputStream fileInputStream = new FileInputStream(fileToSend[0].getAbsolutePath());
                        // Socket socket = new Socket("localhost", 1234);

                        // DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                        // String fileName = fileToSend[0].getName();
                        // byte[] fileNameBytes = fileName.getBytes();

                        // byte[] fileContentBytes = new byte[(int) fileToSend[0].length()];
                        // fileInputStream.read(fileContentBytes);

                        // dataOutputStream.writeInt(fileNameBytes.length);
                        // dataOutputStream.write(fileNameBytes);

                        // dataOutputStream.writeInt(fileContentBytes.length);
                        // dataOutputStream.write(fileContentBytes);


                        sender.send();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            }

        });

        jframe.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                sender.dsocket.close();
                System.exit(0);
            }
        });

        jframe.add(jTitle);
        jframe.add(JFileName);
        jframe.add(jpButton);
        jframe.setVisible(true);

    }

}