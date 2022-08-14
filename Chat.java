//import com.sun.security.ntlm.Client;

//import sun.plugin2.message.Message;

Chat Program -- CET350 Technical Computing Using Java

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioSystem;


public class Chat implements Runnable, ActionListener, WindowListener {
    private BufferedReader br;  //declare buffered reader
    private PrintWriter pw; //declare print writer
    private static final long serialVersionUID = 19L; // serial version ID
    protected final static boolean auto_flush = true;   // for readability
    //Create Buttons
    private Button ChangePortButton = new Button("Change Port");
    private Button SendButton = new Button("Send");
    private Button ServerButton = new Button("Start Server");
    private Button ClientButton = new Button("Connect");
    private Button DisconnectButton = new Button("Disconnect");
    private Button ChangeHostButton = new Button("Change Host");
    //Create Labels
    private Label PortLabel = new Label("Port: ");
    private Label HostLabel = new Label("Host: ");
    //Create Text Firlds
    private TextField ChatText = new TextField(70);
    private TextField PortText = new TextField(60);
    private TextField HostText = new TextField(60);
    private Frame DisplayFrame; //create frame
    private Thread TheThread;   //create thread
    //Create Text Area
    private TextArea DialogScreen = new TextArea("", 15, 80);
    private TextArea MessageScreen = new TextArea("", 3, 80);
    //Create Sockets
    private Socket client;
    private Socket server;
    private ServerSocket listen_socket;// Create Listener socket
    String host = "";//internet host name
    private final int DEFAULT_PORT = 44004; //integer for network port
    private int port = DEFAULT_PORT;
    private int service = 0;    //integer to identify what state the machine is in
    private static int timeout = 1000;  //integer to specify wait time for connection
    private int timeoutMult = 30;
    private boolean more = true;    //coltrol the rocess loop for the program when it is either server or client
    Point FrameSize = new Point(740, 500);  //set the frame size
    private GridBagLayout gbl = new GridBagLayout();//apply gridbag out
    GridBagConstraints con = new GridBagConstraints();//use gridbag constraints
    //Chat constructor
    Chat(int timeout) { //accepts integer used as a base time for the sicket waits

        initComponents();//create the frame and components
        service = 0;    //set initial state to 0 indicating waiting
        more = true;    //set more flag to true
    }

    public static void main(String[] args) {    //main accepts a paramater from the keyboard
        if (args.length == 1) {//checks to see if the input from keyboard is an interger
            try {
                timeout = Integer.parseInt(args[0]);     //this integer will then be used as the timeout value
            } catch (NumberFormatException e) { //if no integer is given it will use defult timeout vale of 1000
            }
        }
        new Chat(timeout);  //instantiate an instance of Chat
    }
    //start method created a thread and then starts it
    public void start() {
        if (TheThread == null) {
            TheThread = new Thread(this);
            TheThread.start();
        }
    }

    public void run() {
        TheThread.setPriority(Thread.MAX_PRIORITY); //set thread priority to max
        while (more)
        {
            try //try to read line from print buffer
            {
                String input = br.readLine();
                if (input != null) {//if there is a line of text
                    DialogScreen.append("in: " + input + "\n");//add it to the Dialog screen
                } else {// if the socket is closed
                    more = false;   //change the more flag
                    //display status messages
                    if (service == 1) {
                        messageDisplay("The client disconnected");
                        messageDisplay("Disconnected");
                    }
                    if (service == 2) {
                        messageDisplay("The server disconnected");
                        messageDisplay("Disconnected");
                    }

                }
            }
            catch (IOException e) //catch the IO exceptions
            {

            }
        }
        close(); //call the close method
    }

    public void close() {
        try {
            if (server != null) { // does the server socket exist?
                if (pw != null) { // does the printwriter exist?
                    pw.print(""); // send null to other device
                    pw.close(); // close the printwriter
                }
                if (br != null) // does the bufferedreader exist?
                    br.close(); // close the bufferedreader
                server.close(); // close the socket
                server = null; // null the socket
            }
        } catch (IOException e) {
        }
        try {
            if (client != null) { // does the client socket exist?
                if (pw != null) { // does the printwriter exist?
                    pw.print(""); // send null to other device
                    pw.close(); // close the printwriter
                }
                if (br != null) // does the bufferedreader exist?
                    br.close(); // close the bufferedreader
                client.close(); // close the socket
                client = null; // null the socket
                if (listen_socket != null) { // does the listen socket exist?
                    listen_socket.close(); // close the socket
                    listen_socket = null; // null the socket
                }
            }
        } catch (IOException e) {

        }
        // set program back to default state
        ChatText.setEnabled(false); // set ChatText to false
        if (!HostText.getText().isEmpty()) { // if HostText has an entry
            ClientButton.setEnabled(true); // set ClientButton to true
        } else {
            ClientButton.setEnabled(false); // otherwise it is false
        }
        ServerButton.setEnabled(true); // set ServerButton to true
        MessageScreen.setEnabled(false); // set MessageScreen to false
        PortText.setText(String.valueOf(port)); // set PortText to the string value of whatever the port integer is
        DisplayFrame.setTitle("Group 1 - Chat Program"); // reset to title to initial title
        service = 0; // set service to 0 (neither client nor server)
        TheThread = null; // null the thread
    }

    public void initComponents() {
        // initialize the frame, add action listener's and set program to intial state
        DisplayFrame = new Frame("Group 1 - Chat Program");
        DisplayFrame.setLayout(gbl);
        DisplayFrame.setBounds(0, 0, FrameSize.x, FrameSize.y);
        DisplayFrame.setSize(FrameSize.x, FrameSize.y);
        ChangePortButton.addActionListener(this);
        ChatText.addActionListener(this);
        HostText.addActionListener(this);
        PortText.addActionListener(this);
        SendButton.addActionListener(this);
        ServerButton.addActionListener(this);
        ClientButton.addActionListener(this);
        DisconnectButton.addActionListener(this);
        ChangeHostButton.addActionListener(this);
        DisplayFrame.addWindowListener(this);
        ChatText.setEnabled(false);
        ClientButton.setEnabled(false);
        MessageScreen.setEnabled(false);
        PortText.setText(String.valueOf(port));

        // setup the GridBagLayout and it's constraints
        con.gridx = 0;
        con.gridy = 0;
        con.gridwidth = GridBagConstraints.REMAINDER;
        con.anchor = GridBagConstraints.LINE_START;
        con.fill = GridBagConstraints.HORIZONTAL;
        gbl.setConstraints(DialogScreen, con);
        DisplayFrame.add(DialogScreen);
        con.ipadx = 120;
        con.fill = GridBagConstraints.NONE;
        con.gridy = 1;
        gbl.setConstraints(ChatText, con);
        DisplayFrame.add(ChatText);
        con.gridx = 1;
        con.anchor = GridBagConstraints.LINE_END;
        con.ipadx = 36;
        gbl.setConstraints(SendButton, con);
        DisplayFrame.add(SendButton);
        con.ipadx = 0;
        con.gridwidth = 1;
        con.gridx = 0;
        con.gridy = 12;
        con.weightx = 0.5;
        con.weighty = 0.5;
        con.anchor = GridBagConstraints.LINE_START;
        con.insets = new Insets(0, 52, 165, 0);
        gbl.setConstraints(HostLabel, con);
        DisplayFrame.add(HostLabel);
        con.anchor = GridBagConstraints.LINE_END;
        con.gridx = GridBagConstraints.RELATIVE;
        con.insets = new Insets(0, 0, 165, 10);
        gbl.setConstraints(HostText, con);
        DisplayFrame.add(HostText);
        con.insets = new Insets(0, 0, 165, 0);
        gbl.setConstraints(ChangeHostButton, con);
        DisplayFrame.add(ChangeHostButton);
        con.ipadx = 2;
        gbl.setConstraints(ServerButton, con);
        DisplayFrame.add(ServerButton);
        con.ipadx = 0;
        con.gridx = 0;
        con.gridy = 13;
        con.insets = new Insets(-310, 55, 0, 0);
        con.anchor = GridBagConstraints.LINE_START;
        gbl.setConstraints(PortLabel, con);
        DisplayFrame.add(PortLabel);
        con.insets = new Insets(-310, 0, 0, 10);
        con.gridx = GridBagConstraints.RELATIVE;
        con.anchor = GridBagConstraints.LINE_END;
        gbl.setConstraints(PortText, con);
        DisplayFrame.add(PortText);
        con.insets = new Insets(-310, 0, 0, 0);
        con.ipadx = 4;
        gbl.setConstraints(ChangePortButton, con);
        DisplayFrame.add(ChangePortButton);
        con.ipadx = 19;
        gbl.setConstraints(ClientButton, con);
        DisplayFrame.add(ClientButton);
        con.ipadx = 0;
        con.gridx = 0;
        con.gridwidth = GridBagConstraints.REMAINDER;
        con.gridy = 14;
        con.insets = new Insets(-265, 0, 0, 0);
        con.ipadx = 3;
        gbl.setConstraints(DisconnectButton, con);
        DisplayFrame.add(DisconnectButton);
        con.ipadx = 0;
        con.ipady = 57;
        con.gridx = 0;
        con.gridy = 15;
        con.gridwidth = GridBagConstraints.REMAINDER;
        con.anchor = GridBagConstraints.LAST_LINE_END;
        con.fill = GridBagConstraints.HORIZONTAL;
        gbl.setConstraints(MessageScreen, con);
        DisplayFrame.add(MessageScreen);
        DisplayFrame.setVisible(true); // make the layout visible
        DisplayFrame.validate(); // validate the layout
    }

    public void messageDisplay(String message) { // this method determines if the machine stat is server or client and appends the approrpriate state to the status message TextArea
        if (service == 1) {
            MessageScreen.append("Server: " + message + "\n");
        }
        if (service == 2) {
            MessageScreen.append("Client: " + message + "\n");
        } else {
            MessageScreen.append(message + "\n");
        }
        ChatText.requestFocus();    //set the focus back to the Chat TextField
    }

    public void playSound(URL soundName) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(soundName.getPath()).getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {

        }
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == ChatText || source == SendButton) { // check if chat text or send button
            if (ChatText.isEnabled()) { //if chattext
                playSound(getClass().getResource("imsend.wav")); //play a custom sound effect
                DialogScreen.append("out: " + ChatText.getText() + "\n");   // append the message to the Dialog Screen text area
                pw.println(ChatText.getText()); //send the message out through socket's PrintWriter
                ChatText.setText(""); // Clear the Chat textfield
            }
        }

        if (source == ServerButton) {
            // if server button send the message to the status message text area
            service = 1;
            messageDisplay("Listening on port " + port);
            playSound(getClass().getResource("dooropen.wav"));  //play sound effect
            try {
                ServerButton.setEnabled(false); //disable the server button
                ClientButton.setEnabled(false);//disable the client button
                if (listen_socket != null) {    //check if the socket exists
                    listen_socket.close();  //close the socket
                    listen_socket = null;   //null the socket
                }
                messageDisplay("Timeout set to " + timeoutMult * timeout + " ms");  //send updated message to status message text area
                listen_socket = new ServerSocket(port);     //set the ServerSocket port
                listen_socket.setSoTimeout(timeoutMult * timeout);  //set the timeout for the Server to wait for connection
                if (client != null) {   //check to see if client socket exists
                    client.close();     //if it does exist close it
                    client = null;  //and null it
                }
                try {
                    messageDisplay("Waiting for a request on port " + port); //send an uptedated message to status message textarea
                    client = listen_socket.accept();    //listen for socket request
                    DisplayFrame.setTitle("Server: Connection from " + client.getInetAddress());    //set the title of frame to server
                    messageDisplay("Connection from " + client.getInetAddress());   //send updated status message that connection has been made
                    try {
                        br = new BufferedReader(new InputStreamReader(client.getInputStream()));    //create a buffered reader
                        pw = new PrintWriter(client.getOutputStream(), auto_flush); //create a print writer
                        ChatText.setEnabled(true);  //enable chat text field
                        more = true;    //set while loop flate true
                        start();    //start the thread
                        messageDisplay("Chat is running");
                        ServerButton.setEnabled(false); //disale server button
                    } catch (IOException ex) {  //catch IOExceptsion
                        messageDisplay("I/O error");
                        close();
                    }
                } catch (SocketTimeoutException s) {    //catch socket timeout exceptions
                    messageDisplay("Request timed out");
                    close();
                }
            } catch (IOException ex) {      //catch ioexceptions
                messageDisplay("I/O error");
                close();
            }
        }

        if (source == ClientButton) {
            service = 2;
            try {
                ServerButton.setEnabled(false); //disable server button
                ClientButton.setEnabled(false); //disable server button
                if (server != null) {   //if server exists
                    server.close(); //close it
                    server = null;  //clear it
                }
                server = new Socket();  //create a server sicket
                server.setSoTimeout(timeout);   //set the timeout
                try {
                    messageDisplay("Connecting to " + HostText.getText() + ":" + PortText.getText()); //send a message to status text area
                    server.connect(new InetSocketAddress(host, port));  //send connection request to server
                    DisplayFrame.setTitle("Client: Connected to " + server.getInetAddress() + " at port " + port);//update title
                    messageDisplay("Connected to " + server.getInetAddress() + " at port " + port); //send updated status message that a connection has been made to..
                    try
                    {
                        br = new BufferedReader(new InputStreamReader(server.getInputStream()));    //create buffered reader
                        pw = new PrintWriter(server.getOutputStream(), auto_flush); //create printwriter
                        ChatText.setEnabled(true);; //enable chat textfield
                        more = true;    //set more flag true
                        playSound(getClass().getResource("dooropen.wav"));  //play custome sound
                        start(); //start the thread
                        messageDisplay("Chat is running");//update display message
                        ServerButton.setEnabled(false);//disable server button
                        ClientButton.setEnabled(false);//disable client button
                    } catch (IOException ex) {  //catch IOException
                        messageDisplay("I/O error");
                        close();
                    }
                } catch (SocketTimeoutException s) {    //catch socket timeout exception
                    messageDisplay("Request timed out");
                    close();
                }
            } catch (IOException ex) {  //catch ioexception
                messageDisplay("I/O error");
                close();
            }
        }

        if (source == DisconnectButton) {
            if (TheThread != null) { // if the thread exists
                playSound(getClass().getResource("doorslam.wav"));//play custom sound
                messageDisplay("Disconnected"); //send message to status display text area
                pw.println("");// send a null to the connection
                TheThread.interrupt();//interrupt the thread
                close(); //close method
            }
        }

        if (source == HostText || source == ChangeHostButton) {
            if (!HostText.getText().isEmpty()) {    //if there is a host string
                ClientButton.setEnabled(true);  //set client button to enable
            }
        }

        if (source == PortText || source == ChangePortButton) {
            String thePort = null;
            int portInt;
            if (!PortText.getText().isEmpty()) { //of there is port text
                thePort = PortText.getText(); //get the text from port text field
            }
            try {
                portInt = Integer.parseInt(thePort);    //try to convert the string to the integer
                port = portInt; //store integer in port
                if (!host.isEmpty()) {  //if there is a host string
                    ClientButton.setEnabled(true);  //set the client button to enable
                }
            } catch (NumberFormatException ex) {    //catch number format exception
                messageDisplay("Not a valid port");
                messageDisplay("Reverting to default port");
                PortText.setText(String.valueOf(DEFAULT_PORT));
            }
        }
        ChatText.requestFocus(); //set focus back to chat text field
    }

    public void windowOpened(WindowEvent e) {
        ChatText.requestFocus();
    }

    public void windowClosing(WindowEvent e) {
        messageDisplay("Terminating program"); //update status message
        if (TheThread != null) { //if there is a thread
            pw.println(""); //send a null
            TheThread.interrupt();  //interrupt the thread
        }
        stop(); //call stop method
    }

    public void windowClosed(WindowEvent e) {
        ChatText.requestFocus();
    }

    public void windowIconified(WindowEvent e) {
        ChatText.requestFocus();
    }

    public void windowDeiconified(WindowEvent e) {
        ChatText.requestFocus();
    }

    public void windowActivated(WindowEvent e) {
        ChatText.requestFocus();
    }

    public void windowDeactivated(WindowEvent e) {
        ChatText.requestFocus();
    }

    public void stop() {
        if (TheThread != null) {    //if thread priority exists
            TheThread.setPriority(Thread.MIN_PRIORITY);//set thread priority to min
        }
        //remove listeners
        ChangePortButton.removeActionListener(this);
        ChatText.removeActionListener(this);
        HostText.removeActionListener(this);
        PortText.removeActionListener(this);
        SendButton.removeActionListener(this);
        ServerButton.removeActionListener(this);
        ClientButton.removeActionListener(this);
        DisconnectButton.removeActionListener(this);
        ChangeHostButton.removeActionListener(this);
        DisplayFrame.removeWindowListener(this);

        DisplayFrame.dispose(); //dispose the Frame
        System.exit(0); //exit tp to the system
    }
}
