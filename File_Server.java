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
    static ArrayList<MyFile> myFiles = new ArrayList<>();
    private static Socket socket;
    private static ServerSocket serverSocket;
    private DatagramSocket dsocket;
    private int fileId = 0;
    private JFrame jFrame;
    private JPanel jPanel;
    private static String port;

    public static void main(String[] args) throws IOException {

        int fileId = 0;
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        JFrame jFrame = new JFrame("File Transfer Server");
        jFrame.setSize(450, 450);
        jFrame.setLayout(new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS));
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

        JScrollPane jScrollPane = new JScrollPane(jPanel);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JLabel jTitle = new JLabel("File Transfer Receiver");
        jTitle.setFont(new Font("Serif", Font.BOLD, 25));
        jTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        jTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnChange = new JButton("Change Protocol");
        btnChange.setPreferredSize(new Dimension(100, 50));
        btnChange.setFont(new Font("Serif", Font.BOLD, 14));
        // btnChange.setBorder(BorderFactory.createEmptyBorder(20, 50, 0, 0));
        btnChange.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnChange.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                JOptionPane.showMessageDialog(null, "To change you have to restart application");

            }
        });

        jFrame.add(jTitle);
        jFrame.add(btnChange);
        jFrame.add(jScrollPane);
        jFrame.setVisible(true);

        port = JOptionPane.showInputDialog("Enter the port number: ", "1234");
        int ans = JOptionPane.showConfirmDialog(null, "Are you receiving through TCP?", "Protocol Type",
                JOptionPane.YES_NO_OPTION);

        if (ans == JOptionPane.YES_OPTION) {
            serverSocket = new ServerSocket(Integer.parseInt(port));

            while (true) {
                try {
                    socket = serverSocket.accept();
                    DataInputStream dIn = new DataInputStream(socket.getInputStream());

                    int fileNameLength = dIn.readInt();

                    if (fileNameLength > 0) {
                        byte[] bytes = new byte[fileNameLength];
                        dIn.readFully(bytes, 0, bytes.length);
                        String fileName = new String(bytes);
                        int fileSize = dIn.readInt();

                        if (fileSize > 0) {
                            byte[] fileBytes = new byte[fileSize];
                            dIn.readFully(fileBytes, 0, fileBytes.length);

                            JPanel jpFileRow = new JPanel();
                            jpFileRow.setLayout(new BoxLayout(jpFileRow, BoxLayout.Y_AXIS));

                            JLabel jlFileName = new JLabel(fileName);
                            jlFileName.setFont(new Font("Arial", Font.PLAIN, 15));
                            jlFileName.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
                            jlFileName.setAlignmentX(Component.CENTER_ALIGNMENT);

                            if (getFileExtension(fileName).equalsIgnoreCase("txt")) {
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
                            myFiles.add(new MyFile(fileId, fileName, fileBytes, getFileExtension(fileName)));
                            fileId++;

                            // serverSocket.close();
                        }
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        } else {
            File_Server receiver = new File_Server();

            try {
                receiver.receive();
            } catch (ClassNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        // String port = JOptionPane.showInputDialog("Enter the port number: ", "1234");

    }

    // private static int close() throws IOException {
    // socket.close();
    // serverSocket.close();
    // System.exit(0);
    // return 0;
    // }

    private static MouseListener getMyMouseListener() {
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
            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub

            }

        };
    }

    public static JFrame createFrame(String fileName, byte[] fileData, String fileExtension) {

        JFrame jFrame = new JFrame("File Preview Downloader");
        jFrame.setSize(600, 200);

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

        JLabel jTitle = new JLabel("File Preview Downloader");
        jTitle.setFont(new Font("Serif", Font.BOLD, 25));
        jTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        jTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel jlPrompt = new JLabel("Confirm to download ->" + fileName);
        jlPrompt.setFont(new Font("Serif", Font.PLAIN, 16));
        jlPrompt.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton jbDownload = new JButton("Download");
        jbDownload.setPreferredSize(new Dimension(100, 50));
        jbDownload.setFont(new Font("Serif", Font.BOLD, 14));

        JButton jbCancel = new JButton("Cancel");
        jbCancel.setPreferredSize(new Dimension(100, 50));
        jbCancel.setFont(new Font("Serif", Font.BOLD, 14));

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
                    FileOutputStream fileOut = new FileOutputStream(fileToDownload);
                    fileOut.write(fileData);
                    fileOut.close();

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

    public static String getFileExtension(String fileName) {
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

        ServerListener listener = new ServerListener(tcpsocket, oos, ois);
        Thread thread = new Thread(listener);
        thread.start();
        byte[] fileBytes = new byte[filesize];
        for (int i = 0; i < filesize; i += 1021) {
            byte[] packetBytes = new byte[1024];
            DatagramPacket packet = new DatagramPacket(packetBytes, packetBytes.length);
            dsocket.receive(packet);

            if ((packetBytes[2] & 0xff) == 1) { // end of file reached
                System.arraycopy(packetBytes, 3, fileBytes, i, filesize - i);
            } else {
                System.arraycopy(packetBytes, 3, fileBytes, i, 1021);
            }
        }

        int[] seqNUmsSent = listener.getSeqNumsSent();

        // byte[] fileBytes = new byte[filesize];
        // ArrayList<byte[]> blastPacketsBytes = new ArrayList<byte[]>();
        // int i = 0;
        // int count = 0;
        // while (true) {
        //     try {
        //         byte[] packetBytes = new byte[1024];
        //         DatagramPacket packet = new DatagramPacket(packetBytes, packetBytes.length);
        //         dsocket.setSoTimeout(50);
        //         dsocket.receive(packet);
                
        //         if ((packetBytes[2] & 0xff) == 0) {
        //             blastPacketsBytes.add(packetBytes);
        //         }
        //         count++;

        //         if (count%42 == 0) {
        //             int[] seqNums = (int[]) ois.readObject();
        //             for (byte[] b : blastPacketsBytes) { 
        //                 System.out.println(((b[0] & 0xff) << 8) + (b[1] & 0xff));
        //                 System.arraycopy(b, 3, fileBytes, i, 1021);  
        //                 i += 1021;
        //             }
        //             System.out.println();
        //             blastPacketsBytes = new ArrayList<byte[]>();
        //         }
        //     } catch (SocketTimeoutException e) {
        //         System.out.println("TIMEOUT");
        //         System.out.println("i = " + i + " size = " + (filesize - filesize%1021));
        //         int[] seqNums = (int[]) ois.readObject();
                
        //         for (byte[] b : blastPacketsBytes) {
        //             System.out.println(((b[0] & 0xff) << 8) + (b[1] & 0xff));
        //             System.arraycopy(b, 3, fileBytes, i, 1021);  
        //             i += 1021;
                    
        //         }
        //         System.out.println();
        //         break;
        //     }
        // }
        // byte[] packetBytes = new byte[1024];
        // DatagramPacket packet = new DatagramPacket(packetBytes, packetBytes.length);
        // dsocket.receive(packet);
        // System.arraycopy(packetBytes, 3, fileBytes, i, filesize%42);


        
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
    }

}
