import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.awt.Font;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class File_Server {
    ArrayList<MyFile> myFiles = new ArrayList<>();
    DatagramSocket dsocket;
    private ServerSocket serverSocket;
    int fileId = 0;
    JFrame jFrame;
    JPanel jPanel;

    public File_Server() {
        jFrame = new JFrame("File Transfer Server");
        jFrame.setSize(450, 450);
        jFrame.setLayout(new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS));
        jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

        JScrollPane jScrollPane = new JScrollPane(jPanel);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JLabel jTitle = new JLabel("File Transfer Receiver");
        jTitle.setFont(new Font("Arial", Font.BOLD, 25));
        jTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        jTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        jFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dsocket.close();
                System.exit(0);
            }
        });

        jFrame.add(jTitle);
        jFrame.add(jScrollPane);
        jFrame.setVisible(true);
    }

    private MouseListener getMyMouseListener() {
        return new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub
                JPanel jPanel = (JPanel) e.getSource();
                int fileId = Integer.parseInt(jPanel.getName());

                for (MyFile myFile : myFiles) {
                    JFrame jfPreview = createFrame(myFile.getName(), myFile.getData(), myFile.getFileExtension());
                    jfPreview.setVisible(true);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}

        };
    }

    public JFrame createFrame(String fileName, byte[] fileData, String fileExtension) {

        JFrame jFrame = new JFrame("File Preview Downloader");
        jFrame.setSize(450, 450);

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

        JLabel jTitle = new JLabel("File Preview Downloader");
        jTitle.setFont(new Font("Arial", Font.BOLD, 25));
        jTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        jTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel jlPrompt = new JLabel("Confirm to download ->" + fileName);
        jlPrompt.setFont(new Font("Arial", Font.BOLD, 18));
        jlPrompt.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton jbDownload = new JButton("Download");
        jbDownload.setPreferredSize(new Dimension(150, 75));
        jbDownload.setFont(new Font("Arial", Font.BOLD, 18));

        JButton jbCancel = new JButton("Cancel");
        jbCancel.setPreferredSize(new Dimension(150, 75));
        jbCancel.setFont(new Font("Arial", Font.BOLD, 18));

        JLabel jlFileContent = new JLabel();
        jlFileContent.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel jpButtons = new JPanel();
        jpButtons.setBorder(new EmptyBorder(20, 0, 10, 0));

        jpButtons.add(jbDownload);
        jpButtons.add(jbCancel);

        if (fileExtension.equalsIgnoreCase("txt")) {
            jlFileContent.setText("<html>" + new String(fileData) + "</html>");
        } else {
            jlFileContent.setIcon(new ImageIcon(fileData));
        }

        jbDownload.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                File fileToDownload = new File(fileName);

                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(fileToDownload);
                    fileOutputStream.write(fileData);
                    fileOutputStream.close();

                    jFrame.dispose();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });


        jbCancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                jFrame.dispose();
            }
        });

        jPanel.add(jlPrompt);
        jPanel.add(jlFileContent);
        jPanel.add(jpButtons);
        jPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        jPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        jFrame.add(jTitle);
        jFrame.add(jPanel);
        jFrame.setVisible(true);
        return jFrame;
    }

    public String getFileExtension(String fileName) {
        // not work for tar.gz or multiple '.' characters
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }
        return fileName.substring(dotIndex + 1);
    }

    public void receive() throws IOException, ClassNotFoundException {
        int udpport = 1345;

        // setting up socket for tcp communications
        serverSocket = new ServerSocket(1234);
        Socket tcpsocket = serverSocket.accept();
        ObjectOutputStream oos = new ObjectOutputStream(tcpsocket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(tcpsocket.getInputStream());

        // receiving filename and size over tcp
        String filename = (String) ois.readObject();
        int filesize = ois.readInt();

        // setting up socket for udp communications
        dsocket = new DatagramSocket(udpport);

        byte[] fileBytes = new byte[filesize];
        ArrayList<byte[]> blastPacketsBytes = new ArrayList<byte[]>();
        boolean eof = false;
        int i = 0;
        int count = 0;
        while (!eof) {
            byte[] packetBytes = new byte[1024];
            DatagramPacket packet = new DatagramPacket(packetBytes, packetBytes.length);
            try {
                dsocket.setSoTimeout(50);
                dsocket.receive(packet);
                blastPacketsBytes.add(packetBytes);
                count++;

                if (count%42 == 0) {
                    int[] seqNums = (int[]) ois.readObject();
                    for (int n : seqNums) {
                        System.out.println(n);
                    }
                    System.out.println();
                }
                for (byte[] b : blastPacketsBytes) {
                    System.arraycopy(b, 3, fileBytes, i, 1021);
                    i += 1021;    
                }
                blastPacketsBytes = new ArrayList<byte[]>();
            } catch (SocketTimeoutException e) {
                System.out.println("TIMEOUT");
                int[] seqNums = (int[]) ois.readObject();
                for (int n : seqNums) {
                    System.out.println(n);
                }
                System.out.println();

                for (byte[] b : blastPacketsBytes) {
                    System.arraycopy(b, 3, fileBytes, i, filesize-i);
                    i += 1021;    
                }
                eof = true;

                // for (byte[] b : blastPacketsBytes) {
                //     eof = (b[2] & 0xff) == 1;
                //     if (eof) {
                //         System.arraycopy(b, 3, fileBytes, i, filesize-i);
                //     } else {
                //         System.arraycopy(b, 3, fileBytes, i, 1021);
                //         i += 1021;
                //     }
                // }
            }
            
        }

        // testing receiving all packets 
        // --------------------------------------------------------------------------
        // byte[] fileBytes = new byte[filesize];
        // for (int i = 0; i < filesize; i += 1021) {
        //     byte[] packetBytes = new byte[1024];
        //     DatagramPacket packet = new DatagramPacket(packetBytes, packetBytes.length);
        //     dsocket.receive(packet);

        //     if ((packetBytes[2] & 0xff) == 1) { // end of file reached
        //         System.arraycopy(packetBytes, 3, fileBytes, i, filesize - i);
        //     } else {
        //         System.arraycopy(packetBytes, 3, fileBytes, i, 1021);
        //     }
        // }
        JPanel jpFileRow = new JPanel();
        jpFileRow.setLayout(new BoxLayout(jpFileRow, BoxLayout.Y_AXIS));

        JLabel jlFileName = new JLabel(filename);
        jlFileName.setFont(new Font("Arial", Font.BOLD, 15));
        jlFileName.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        jlFileName.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (getFileExtension(filename).equalsIgnoreCase("txt")) {
            jpFileRow.setName(String.valueOf(fileId));
            jpFileRow.addMouseListener(getMyMouseListener());

            jpFileRow.add(jlFileName);
            jPanel.add(jpFileRow);
            jFrame.validate();
        } else {
            jpFileRow.setName(String.valueOf(fileId));
            jpFileRow.addMouseListener(getMyMouseListener());

            jpFileRow.add(jlFileName);
            jPanel.add(jpFileRow);
            jFrame.validate();

        }

        myFiles.add(new MyFile(fileId, filename, fileBytes, getFileExtension(filename)));
        fileId++;
        // --------------------------------------------------------------------------
    }

    public void confSend(int lastSeqNum, InetAddress senderAddress, int senderPort) throws IOException {
        byte[] confBytes = new byte[2];
        confBytes[0] = (byte) (lastSeqNum >> 8);
        confBytes[1] = (byte) (lastSeqNum);

        DatagramPacket confPacket = new DatagramPacket(confBytes, confBytes.length, senderAddress, senderPort);
        dsocket.send(confPacket);
        System.out.println("Sent confirmation, sequence number: " + lastSeqNum);
    }

    public void udpReceive(int port) {
        try {
            serverSocket = new ServerSocket(port);
            Socket tcpsocket = serverSocket.accept();
            ObjectOutputStream oos = new ObjectOutputStream(tcpsocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(tcpsocket.getInputStream());
            // serverSocket = new ServerSocket(port);
            // Socket socket = serverSocket.accept();
            // ServerListener listener = new ServerListener(socket);

            // Thread thread = new Thread(listener);
            // thread.start();

            dsocket = new DatagramSocket(1233);
            byte[] receiveFilenameBytes = new byte[1024];
            DatagramPacket receiveFilenamePacket = new DatagramPacket(receiveFilenameBytes, receiveFilenameBytes.length);
            dsocket.receive(receiveFilenamePacket);

            byte[] filenameBytes = receiveFilenamePacket.getData();
            String filename = new String(filenameBytes, 0, receiveFilenamePacket.getLength());

            

            // connect tcp socket

            int filesize = ois.readInt();

            int[] seqNumsRec = new int[42];
            // ArrayList<byte[]> finalBytes = new ArrayList<byte[]>();
            byte[] finalBytes = new byte[filesize];
            boolean eof = false;
            int count = 0;
            while (!eof) {
                // ArrayList<DatagramPacket> packets = new ArrayList<DatagramPacket>();
                ArrayList<byte[]> blastBytes = new ArrayList<byte[]>();
                for (int i = 0; i < 42; i++) {
                    byte[] msg = new byte[1024];
                    byte[] packetBytes = new byte[1021];
                    DatagramPacket packetRec = new DatagramPacket(msg, msg.length);
                    try {
                        dsocket.setSoTimeout(100);
                        dsocket.receive(packetRec);
                    } catch (SocketTimeoutException e) {
                        // TODO - missing packets
                        System.out.println("missing packet. Amount sent " + i);
                    }
                    // packets.add(packetRec);

                    msg = packetRec.getData();
                    int seqNum = ((msg[0] & 0xff) << 8) + (msg[1] & 0xff);
                    eof = (msg[2] & 0xff) == 1;
                    
                    seqNumsRec[i] = seqNum;
                    System.arraycopy(msg, 3, packetBytes, 0, 1021);
                    blastBytes.add(packetBytes);
                    if (eof) break;
                }
                int[] seqNumsSent = (int[]) ois.readObject();
                int prev = 0;
                for (int n : seqNumsSent) {
                    if (n < prev) {
                        // TODO reorder
                    }
                }  
                for (byte[] b : blastBytes) {
                    for (int i = 0; i < 1021; i++) {
                        finalBytes[count++] = b[i];
                    }
                }       
                // InetAddress senderAddress = packetRec.getAddress();
                // int senderPort = packetRec.getPort();
            }
            JPanel jpFileRow = new JPanel();
            jpFileRow.setLayout(new BoxLayout(jpFileRow, BoxLayout.Y_AXIS));

            JLabel jlFileName = new JLabel(filename);
            jlFileName.setFont(new Font("Arial", Font.BOLD, 15));
            jlFileName.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            jlFileName.setAlignmentX(Component.CENTER_ALIGNMENT);

            if (getFileExtension(filename).equalsIgnoreCase("txt")) {
                jpFileRow.setName(String.valueOf(fileId));
                jpFileRow.addMouseListener(getMyMouseListener());

                jpFileRow.add(jlFileName);
                jPanel.add(jpFileRow);
                jFrame.validate();
            } else {
                jpFileRow.setName(String.valueOf(fileId));
                jpFileRow.addMouseListener(getMyMouseListener());

                jpFileRow.add(jlFileName);
                jPanel.add(jpFileRow);
                jFrame.validate();

            }

            myFiles.add(new MyFile(fileId, filename, finalBytes, getFileExtension(filename)));
            fileId++;
            // make one bytes array
        } catch (IOException e) {

        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

            // try {
            //     dsocket = new DatagramSocket(port);
            //     byte[] receiveFilenameBytes = new byte[1024];
            //     DatagramPacket receiveFilenamePacket = new DatagramPacket(receiveFilenameBytes, receiveFilenameBytes.length);
            //     dsocket.receive(receiveFilenamePacket);

            //     byte[] filenameBytes = receiveFilenamePacket.getData();
            //     String filename = new String(filenameBytes, 0, receiveFilenamePacket.getLength());

            //     boolean eof;
            //     int seqNum = 0;
            //     int lastSeqNum = 0;

                
            //     while (true) {
            //         byte[] msg = new byte[1024];
            //         byte[] fileBytes = new byte[1021];

            //         DatagramPacket packetRec = new DatagramPacket(msg, msg.length);
            //         dsocket.receive(packetRec);
            //         msg = packetRec.getData();

            //         InetAddress senderAddress = packetRec.getAddress();
            //         int senderPort = packetRec.getPort();

            //         seqNum = ((msg[0] & 0xff) << 8) + (msg[1] & 0xff);
            //         eof = (msg[2] & 0xff) == 1;
                    
            //         System.out.println("seqNum = " + seqNum + " lastSeqNum = " + lastSeqNum);

            //         if (seqNum == lastSeqNum + 1) {
            //             lastSeqNum = seqNum;

            //             System.arraycopy(msg, 3, fileBytes, 0, 1021);


            //             confSend(lastSeqNum, senderAddress, senderPort);
            //         } else {
            //             confSend(lastSeqNum, senderAddress, senderPort);
            //         }

            //         if (eof) {
            //             JPanel jpFileRow = new JPanel();
            //             jpFileRow.setLayout(new BoxLayout(jpFileRow, BoxLayout.Y_AXIS));

            //             JLabel jlFileName = new JLabel(filename);
            //             jlFileName.setFont(new Font("Arial", Font.BOLD, 15));
            //             jlFileName.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            //             jlFileName.setAlignmentX(Component.CENTER_ALIGNMENT);

            //             if (getFileExtension(filename).equalsIgnoreCase("txt")) {
            //                 jpFileRow.setName(String.valueOf(fileId));
            //                 jpFileRow.addMouseListener(getMyMouseListener());

            //                 jpFileRow.add(jlFileName);
            //                 jPanel.add(jpFileRow);
            //                 jFrame.validate();
            //             } else {
            //                 jpFileRow.setName(String.valueOf(fileId));
            //                 jpFileRow.addMouseListener(getMyMouseListener());

            //                 jpFileRow.add(jlFileName);
            //                 jPanel.add(jpFileRow);
            //                 jFrame.validate();

            //             }

            //             myFiles.add(new MyFile(fileId, filename, fileBytes, getFileExtension(filename)));
            //             fileId++;
            //             break;
            //         }
            //     }   
            // } catch (IOException e) {
            //     // TODO Auto-generated catch block
            //     e.printStackTrace();
            //     System.exit(0);
            // }
            // dsocket.close();
        
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        File_Server receiver = new File_Server();

        receiver.receive();

        // ServerSocket serverSocket;
        // serverSocket = new ServerSocket(1234);

        // while (true) {
        //     try {
                // Socket socket = serverSocket.accept();
                // DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                // int fileNameLength = dataInputStream.readInt();

                // if (fileNameLength > 0) {
                //     byte[] bytes = new byte[fileNameLength];
                //     dataInputStream.readFully(bytes, 0, bytes.length);
                //     String fileName = new String(bytes);

                //     int fileSize = dataInputStream.readInt();

                //     if (fileSize > 0) {
                //         byte[] fileBytes = new byte[fileSize];
                //         dataInputStream.readFully(fileBytes, 0, fileBytes.length);

                //         JPanel jpFileRow = new JPanel();
                //         jpFileRow.setLayout(new BoxLayout(jpFileRow, BoxLayout.Y_AXIS));

                //         JLabel jlFileName = new JLabel(fileName);
                //         jlFileName.setFont(new Font("Arial", Font.BOLD, 15));
                //         jlFileName.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
                //         jlFileName.setAlignmentX(Component.CENTER_ALIGNMENT);

                //         if (getFileExtension(fileName).equalsIgnoreCase("txt")) {
                //             jpFileRow.setName(String.valueOf(fileId));
                //             jpFileRow.addMouseListener(getMyMouseListener());

                //             jpFileRow.add(jlFileName);
                //             jPanel.add(jpFileRow);
                //             jFrame.validate();
                //         } else {
                //             jpFileRow.setName(String.valueOf(fileId));
                //             jpFileRow.addMouseListener(getMyMouseListener());

                //             jpFileRow.add(jlFileName);
                //             jPanel.add(jpFileRow);
                //             jFrame.validate();

                //         }
                //         myFiles.add(new MyFile(fileId, fileName, fileBytes, getFileExtension(fileName)));
                //         fileId++;

                //         // serverSocket.close();
                //     }
                // }
            // } catch (IOException e) {
            //     // TODO Auto-generated catch block
            //     e.printStackTrace();
            // }

        // }

    }
}
