package dam.sockets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ClienteChat extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    private JTextArea textArea;
    private JTextField textField;
    private JButton sendButton;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private JScrollPane scrollPane;
    private JButton exitButton;
   

    public ClienteChat(String nickname, String host, int port) {
        setTitle("Chat - " + nickname);
        getContentPane().setLayout(new BorderLayout());

        textArea = new JTextArea(20, 50);
        textArea.setEditable(false);
        scrollPane = new JScrollPane(textArea);
        scrollPane.setSize(400, 600);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        
        exitButton = new JButton("Salir");
        exitButton.addActionListener(this);
        inputPanel.add(exitButton, BorderLayout.WEST);

        textField = new JTextField();
        textField.setSize(100, 100);
        inputPanel.add(textField, BorderLayout.CENTER);

        sendButton = new JButton("Enviar");
        sendButton.addActionListener(this);
        inputPanel.add(sendButton, BorderLayout.EAST);

        getContentPane().add(inputPanel, BorderLayout.SOUTH);

        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        try {
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(nickname + "\n");
            writer.flush();
        } catch (ConnectException ce) {
            ce.printStackTrace();
        	JOptionPane.showMessageDialog(null,
                    "SERVIDOR NO CONECTADO\n" + ce.getMessage(),
                    "<<MENSAJE DE ERROR :1>>",
                    JOptionPane.ERROR_MESSAGE);
        	System.exit(0);
        } catch (IOException e) {
			e.printStackTrace();
		}

        new Thread(new Runnable() {
            @Override
            public void run() {
            	boolean seguir = true;
                while (seguir) {
                    try {
                        String message = reader.readLine();
                        if (message == null) {
                            break;
                        }
                        textArea.append(message + "\n");
                    } catch (SocketException se) {
                        se.printStackTrace();
                    	JOptionPane.showMessageDialog(null,
                                "SERVIDOR INTERRUMPIDO\n" + se.getMessage(),
                                "<<MENSAJE DE ERROR :1>>",
                                JOptionPane.ERROR_MESSAGE);
                    	seguir = false;
                    	System.exit(0);
                    }catch (IOException e) {
                    	e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            writer.write(textField.getText() + "\n");
            writer.flush();
            textField.setText("");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        if (e.getSource() instanceof JButton) { 
            if (e.getActionCommand().equals("Salir")) { 
            	int resp = JOptionPane.showConfirmDialog(this, "Se va a cerrrar la aplicacion, Desea cerrarla?",
                        "Confirmar salida", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (resp == JOptionPane.YES_NO_OPTION) {
                    System.exit(0);
                }
            }
        }
    }

    public static void main(String[] args) {
        String nickname = JOptionPane.showInputDialog(null, "Introduce tu nickname:");

        new ClienteChat(nickname, "localhost", 1234);
    }
}