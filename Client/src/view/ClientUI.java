package view;

import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JTextField;

import model.Event;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.Font;
import javax.swing.JPanel;
import java.awt.Color;

public class ClientUI {

	private JFrame frame;
	private static String pathFolder;
	private JButton btnConnect;
	private static ExecutorService executor;
	private JButton btnExit;
	private JTextField hostAddress;
	private JTextField textPort;
	private Thread connect;
	private static Socket socket;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientUI window = new ClientUI();
					window.frame.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	/**
	 * Create the application.
	 */
	public ClientUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 268);
		frame.setTitle("Chương trình client");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		hostAddress = new JTextField();
		hostAddress.setFont(new Font("Tahoma", Font.PLAIN, 14));
		hostAddress.setBounds(145, 38, 179, 29);
		hostAddress.setText("localhost");
		frame.getContentPane().add(hostAddress);
		hostAddress.setColumns(10);

		btnConnect = new JButton("Kết nối");
		btnConnect.setForeground(new Color(0, 128, 64));
		btnConnect.setFont(new Font("Tahoma", Font.BOLD, 12));
		btnConnect.setBounds(144, 164, 133, 29);
		btnConnect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String server = hostAddress.getText().toString();
				String text = textPort.getText().toString();
				if (text.equals("") || server.equals("")) {
					JOptionPane.showOptionDialog(null, "Vui lòng nhập đủ thông tin", "Lỗi kết nối !",
							JOptionPane.CLOSED_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
					return;
				}
				int port = 0;
				;
				try {
					port = Integer.parseInt(text);
//					 System.out.println(port);
				} catch (Exception ex) {
					System.out.println(ex);
					JOptionPane.showOptionDialog(null, "Vui lòng nhập đúng port", "Lỗi kết nối !",
							JOptionPane.CLOSED_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
					return;
					// TODO: handle exception
				}
				final Integer numPort = new Integer(port);
				connect = new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						runClient(server, numPort);

					}
				});
				if (socket ==null)
				connect.start();

			}
		});
		frame.getContentPane().add(btnConnect);


		JLabel lblNewLabel = new JLabel("Địa chỉ host :");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblNewLabel.setBounds(33, 35, 102, 35);
		frame.getContentPane().add(lblNewLabel);

		JLabel lblPort = new JLabel("Port :");
		lblPort.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblPort.setBounds(33, 103, 66, 26);
		frame.getContentPane().add(lblPort);

		textPort = new JTextField();
		textPort.setFont(new Font("Tahoma", Font.PLAIN, 14));
		textPort.setColumns(10);
		textPort.setBounds(145, 104, 179, 29);
		textPort.setText("8080");
		frame.getContentPane().add(textPort);
		
		JLabel labelExAddress = new JLabel("Ví dụ: localhost, 192.168.1.1,...");
		labelExAddress.setForeground(new Color(128, 64, 0));
		labelExAddress.setBounds(145, 70, 181, 23);
		frame.getContentPane().add(labelExAddress);
	}

	public static void track(BufferedWriter bw) throws IOException, InterruptedException {
	
		while (true) {

			String path = System.getProperty("user.dir");
			if (pathFolder.equals(""))
				return;
			
			String pathSystem =System.getProperty("user.dir");
			int lastSeparatorIndex = pathSystem.lastIndexOf(System.getProperty("file.separator"));
	        String newPath = pathSystem.substring(0, lastSeparatorIndex);
			Path folderToWatch = Paths.get(newPath +"\\"+ pathFolder);
			WatchService watchService = folderToWatch.getFileSystem().newWatchService();
			folderToWatch.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

			WatchKey key = watchService.take();
			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind<?> kind = event.kind();

				Event fileEvent = new Event(kind.name(), event.context().toString(),
						folderToWatch.resolve((Path) event.context()).toString());
				
				bw.write(fileEvent.toString());
				bw.newLine();
				bw.flush();

			}
			key.reset();
		}
	}

	public void runClient(String hostname, int port) {

		try   {
			socket = new Socket(hostname, port);
			OutputStream output = socket.getOutputStream();

			InputStream input = socket.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(input,"UTF-8"));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(output,"UTF-8"));

			JOptionPane.showOptionDialog(null, "Kết nối thành công !", "Trạng thái",
					JOptionPane.CLOSED_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);

			String line = "";
			Thread reporter = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						track(bw);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			executor = Executors.newFixedThreadPool(2);
			while (true) {
				System.out.println("start");
				line = br.readLine();
				System.out.println(line);

				switch (line) {
				case "init": {

					ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

					init(oos);

					String path = br.readLine();

					System.out.println(line);
					if (path != "") {
						pathFolder = path;

						executor.execute(reporter);

					}
					break;
				}
				case "reInit": {
					executor.shutdownNow();

					String path = br.readLine();
					System.out.println(path);
					if (path != "") {
						pathFolder = path;

						executor = Executors.newFixedThreadPool(2);
						executor.execute(reporter);
					}

					break;
				}

				}

			}

		} catch (UnknownHostException ex) {


			int result = JOptionPane.showOptionDialog(null, "Server not found: " + ex.getMessage(), "Lỗi kết nối !",
					JOptionPane.CLOSED_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);

		} catch (IOException ex) {

			System.out.println( ex.getMessage());
			int result = JOptionPane.showOptionDialog(null, "Server not found: " + ex.getMessage(), "Lỗi kết nối !",
					JOptionPane.CLOSED_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
			this.socket=null;
		}
	}

	public static void init(ObjectOutputStream output) throws IOException {
		
		String path =System.getProperty("user.dir");
		System.out.println(path);
		File folder = new File(path);

		output.writeObject(folder);

		output.flush();


	}
}
