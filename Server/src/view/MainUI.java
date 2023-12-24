package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import model.Client;

import javax.swing.JButton;
import javax.swing.JToggleButton;

public class MainUI {

	private static MainUI window = new MainUI();
	private static JFrame frame;
	private static JTree tree;
	private static String valuePath = "";
	private static List<String> listClientData = new ArrayList<String>();
	public static final int NUM_OF_THREAD = 4;
	public static Socket socketCurrent;
	private static JLabel currentIcon;
	private static JLabel currentName;
	private static JLabel currentIconSetPath;
	public final static int SERVER_PORT = 8080;
	private JScrollPane listClient;
	private static JPanel listClientPanel;
	private JButton btnSelectFolder;
	private static JScrollPane dashboard;
	private static JScrollPane scrollTree;
	public static JTextArea valueCurrent;
	public static ArrayList<Client> clientForManager = new ArrayList<Client>();
	private static JButton btnUpdatePath;
	private static JToggleButton btnPause;
	private static JPanel boxCurrent;
	public static boolean modeALL = true;
	private static JButton btnMode;

	private static Image imageTest = null;

	public static void addClient(Socket client) {

		clientForManager.add(new Client(client));

		ImageIcon clientIcon = new ImageIcon(
				new ImageIcon(imageTest).getImage().getScaledInstance(60, 60, java.awt.Image.SCALE_SMOOTH));

		FlowLayout flow = new FlowLayout();

		JPanel box = new JPanel();
		box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
		JLabel imagelabel = new JLabel(clientIcon);
		box.add(imagelabel);
		String text = client.getInetAddress().getHostAddress().toString() + "-" + client.getPort();
		JLabel name = new JLabel(text);
		imagelabel.setAlignmentY(Component.CENTER_ALIGNMENT);
		name.setForeground(new Color(255, 196, 79));
		box.add(imagelabel);
		box.add(name);
		imagelabel.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {

			}

			@Override
			public void mousePressed(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {

				socketCurrent = client;
				boxCurrent = box;
				Client current = clientForManager.stream().filter(cli -> cli.getSocket() == client).findFirst().get();

				if (current != null && current.getFile() != null) {
					try {

						if (!current.isSetedPath()) {

							int result = JOptionPane.showOptionDialog(null,
									"Chưa khởi tạo thư mục được" + " theo dõi !!!!", "Vui lòng chọn thư mục",
									JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);

							if (result == 0) {
								initTree(current.getFile());
								scrollTree.setVisible(true);
								dashboard.setVisible(false);
								currentIconSetPath = name;
								return;
							} else
								return;
						}

					} catch (ClassNotFoundException e1) {

						e1.printStackTrace();
					} catch (IOException e1) {

						e1.printStackTrace();
					}

				}
				if (currentIcon != null) {
					if (current.isSetedPath())
						currentIcon.setForeground(new Color(72, 168, 58));
					else
						currentIcon.setForeground(new Color(255, 196, 79));
					currentIcon = name;

					currentIcon.setForeground(new Color(29, 136, 165));
				} else {
					currentIcon = name;
					currentIcon.setForeground(new Color(29, 136, 165));
				}

				modeALL = false;
				btnMode.setText("Riêng một");
				String text = socketCurrent.getInetAddress().toString() + "-" + client.getPort();
				text = text.substring(1, text.length());
				valueCurrent.append("__________________ Theo dõi " + text + "___________________\n\n");

				if (!dashboard.isVisible()) {
					dashboard.setVisible(true);
					scrollTree.setVisible(false);
				}

			}
		});
		box.setAlignmentX(Component.CENTER_ALIGNMENT);
		listClientPanel.add(box);
		listClientPanel.repaint();
		listClientPanel.revalidate();

	}

	public static void reInit(Socket socket) {

		try {
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));

			bw.write("reInit");
			bw.newLine();
			bw.flush();

		} catch (Exception e) {
			System.out.println(e);

		}

	}

	public static void repPath(String path, Socket socket) throws IOException {

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));

		bw.write(path);
		bw.newLine();
		bw.flush();
		Client current = clientForManager.stream().filter(cli -> cli.getSocket() == socket).findFirst().get();
		current.setSetedPath(true);

	}

	public static void initTree(File folder) throws IOException, ClassNotFoundException {

		DefaultMutableTreeNode root = new DefaultMutableTreeNode(folder.getName());
		WorkerThread.createTree(root, folder);
		setTree(root);
	}

	public static void setTree(DefaultMutableTreeNode root) {
		tree.setModel(new javax.swing.tree.DefaultTreeModel(root));
	}

	public static void removeClient(Socket client) {

		if (client == socketCurrent) {

			if (modeALL)
				return;
			modeALL = true;
			btnMode.setText("Tất cả");
			socketCurrent = null;
			valueCurrent.append("__Theo dõi tất cả," + "client được theo dõi riêng vừa dời đi\n\n");

		}
		clientForManager.removeIf(cli -> cli.getSocket() == client);

		
		ImageIcon clientIcon = new ImageIcon(
				new ImageIcon(imageTest).getImage().getScaledInstance(60, 60, java.awt.Image.SCALE_SMOOTH));

		
		Image image = clientIcon.getImage(); // transform it
		Image newimg = ((Image) image).getScaledInstance(60, 60, java.awt.Image.SCALE_SMOOTH); // scale it the
																								// smooth // way
		clientIcon = new ImageIcon(newimg);
		FlowLayout flow = new FlowLayout();
		listClientPanel.removeAll();
		listClientPanel.repaint();
		listClientPanel.revalidate();
		for (Client cli : clientForManager) {
			JPanel box = new JPanel();
			box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
			JLabel imagelabel = new JLabel(clientIcon);
			box.add(imagelabel);
			String text = client.getInetAddress().getHostAddress().toString() + "-" + client.getPort();
			JLabel name = new JLabel(text);
			imagelabel.setAlignmentY(Component.CENTER_ALIGNMENT);
			box.add(imagelabel);
			box.add(name);
			if (cli.isSetedPath())
				name.setForeground(new Color(72, 168, 58));
			else
				name.setForeground(new Color(255, 196, 79));
			imagelabel.addMouseListener(new MouseListener() {

				@Override
				public void mouseReleased(MouseEvent e) {

				}

				@Override
				public void mousePressed(MouseEvent e) {

				}

				@Override
				public void mouseExited(MouseEvent e) {

				}

				@Override
				public void mouseEntered(MouseEvent e) {

				}

				@Override
				public void mouseClicked(MouseEvent e) {

					socketCurrent = client;
					boxCurrent = box;
					Client current = cli;

					if (current != null && current.getFile() != null) {
						try {

							if (!current.isSetedPath()) {

								int result = JOptionPane.showOptionDialog(null,
										"Chưa khởi tạo thư mục được" + " theo dõi !!!!", "Vui lòng chọn thư mục",
										JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
								if (result == 0) {
									initTree(current.getFile());
									scrollTree.setVisible(true);
									dashboard.setVisible(false);
									currentIconSetPath = name;
									return;
								} else
									return;
							}

						} catch (ClassNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

					}
					if (currentIcon != null) {

						if (current.isSetedPath())
							currentIcon.setForeground(new Color(72, 168, 58));
						else
							currentIcon.setForeground(new Color(255, 196, 79));
						currentIcon = name;

						currentIcon.setForeground(new Color(29, 136, 165));
					} else {
						currentIcon = name;
						currentIcon.setForeground(new Color(29, 136, 165));
					}
					// TODO Auto-generated method stub

					modeALL = false;
					btnMode.setText("Riêng một");
					String text = socketCurrent.getInetAddress().toString() + "-" + client.getPort();
					text = text.substring(1, text.length());
					valueCurrent.append("__________________ Theo dõi " + text + "___________________\n\n");

					if (!dashboard.isVisible()) {
						dashboard.setVisible(true);
						scrollTree.setVisible(false);
					}

				}
			});
			box.setAlignmentX(Component.CENTER_ALIGNMENT);
			try {
				listClientPanel.add(box);
				listClientPanel.repaint();
				listClientPanel.revalidate();
				System.out.println("done");
			} catch (Exception e) {
				System.out.println("sdsd");
			}

		}

	}

	/**
	 * @wbp.parser.entryPoint
	 */
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					window = new MainUI();
					window.frame.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

		window.initNetwork();
	}

	public void initNetwork() {
		ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_THREAD);
		ServerSocket serverSocket = null;
		try {
			System.out.println("Binding to port " + SERVER_PORT + ", please wait  ...");
			serverSocket = new ServerSocket(SERVER_PORT);
			System.out.println("Server started: " + serverSocket);
			System.out.println("Waiting for a client ...");
			while (true) {
				try {
					Socket socket = serverSocket.accept();
					System.out.println("Client accepted: " + socket);

					WorkerThread handler = new WorkerThread(socket, frame, tree, this);
					executor.execute(handler);
					addClient(socket);
				} catch (IOException e) {
					System.err.println(" Connection Error: " + e);
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Create the application.
	 * 
	 * @wbp.parser.entryPoint
	 */
	public MainUI() {
		initialize();

	}

	private void initialize() {

		frame = new JFrame();
		frame.setBounds(100, 100, 866, 587);
		frame.setTitle("Quản lí client");
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(0, 39, 852, 482);
		frame.getContentPane().add(tabbedPane);

		JPanel tab1 = new JPanel();
		tabbedPane.addTab("Trang chủ", null, tab1, null);
		tab1.setLayout(null);

		tree = new JTree();
		tree.setFont(new Font("Tahoma", Font.BOLD, 11));
		tree.setBackground(new Color(255, 255, 255));

		tree.addTreeSelectionListener(new TreeSelectionListener() {

			public void valueChanged(TreeSelectionEvent e) {
				TreePath treepath = e.getPath();
				
				valuePath = "";
				Object elements[] = treepath.getPath();

				for (int i = 0, n = elements.length; i < n; i++) {
					valuePath += elements[i] + "\\";
				}
			}
		});

		scrollTree = new JScrollPane(tree);
		scrollTree.setBounds(506, 93, 286, 350);
		scrollTree.setVisible(false);

		tab1.add(scrollTree);

		listClientPanel = new JPanel();
		listClientPanel.setBackground(new Color(255, 255, 255));
		listClientPanel.setForeground(new Color(128, 0, 128));
		listClientPanel.setLayout(new BoxLayout(listClientPanel, BoxLayout.Y_AXIS));
		listClient = new JScrollPane(listClientPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		listClient.setBounds(30, 93, 135, 296);
		tab1.add(listClient);

		btnSelectFolder = new JButton("Chọn thư mục");
		btnSelectFolder.setBounds(653, 11, 120, 23);
		tab1.add(btnSelectFolder);

		dashboard = new JScrollPane();
		dashboard.setBounds(227, 93, 586, 350);
		tab1.add(dashboard);
		valueCurrent = new JTextArea(5, 10);
		valueCurrent.setFont(new Font("Monospaced", Font.PLAIN, 15));
		valueCurrent.setLineWrap(true);
		dashboard.setViewportView(valueCurrent);

		btnPause = new JToggleButton("Tạm dừng");
		btnPause.setBounds(30, 63, 135, 23);
		btnPause.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (socketCurrent == null)
					return;
				Client current = clientForManager.stream().filter(cli -> cli.getSocket() == socketCurrent).findFirst()
						.get();
				if (!current.isPause()) {
					boxCurrent.setBackground(Color.red);
					current.setPause(true);
				} else {
					boxCurrent.setBackground(Color.white);
					current.setPause(false);
				}
			}
		});
		tab1.add(btnPause);

		btnUpdatePath = new JButton("Đặt lại thư mục");
		btnUpdatePath.setBounds(30, 37, 135, 23);
		btnUpdatePath.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {
//					reInit(client);
					if (modeALL) {
						int result = JOptionPane.showOptionDialog(null, "   Vui lòng chọn một client. ",
								" Chưa chọn client !", JOptionPane.CLOSED_OPTION, JOptionPane.PLAIN_MESSAGE, null, null,
								null);
						return;
					}
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socketCurrent.getOutputStream(),"UTF-8"));
					bw.write("reInit"); // Send the results to client
					bw.newLine();
					bw.flush();
					Client current = clientForManager.stream().filter(cli -> cli.getSocket() == socketCurrent)
							.findFirst().get();
					initTree(current.getFile());
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				scrollTree.setVisible(true);
				dashboard.setVisible(false);
			}
		});
		tab1.add(btnUpdatePath);

		try {
//			imageTest = ImageIO.read(this.getClass().getClassLoader().getResource("/exchange.png"));
			imageTest = ImageIO.read(this.getClass().getClassLoader().getResource("client.jpg"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		btnMode = new JButton("Tất cả");
		btnMode.setBounds(520, 11, 104, 23);
		btnMode.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (!dashboard.isVisible()) {
					dashboard.setVisible(true);
					scrollTree.setVisible(false);
				}
				if (modeALL)
					return;
				modeALL = true;
				btnMode.setText("Tất cả");
				currentIcon.setForeground(new Color(72, 168, 58));
				socketCurrent = null;
				valueCurrent.append("_______________________ Theo dõi tất cả" + "_____________________\n\n");
			}
		});
		tab1.add(btnMode);

		JLabel lblNewLabel = new JLabel("DANH SÁCH CÁC THÔNG BÁO ĐẾN");
		lblNewLabel.setForeground(new Color(0, 128, 0));
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblNewLabel.setBounds(378, 45, 320, 37);
		tab1.add(lblNewLabel);

		JLabel lblMode = new JLabel("Chế độ :");
		lblMode.setBounds(453, 13, 72, 18);
		tab1.add(lblMode);
		btnSelectFolder.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (socketCurrent == null)
					return;
				if (valuePath.equals("")) {
					JOptionPane.showOptionDialog(null, "Chưa chọn thư mục được theo dõi !!!!", "Vui lòng chọn thư mục",
							JOptionPane.CLOSED_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
					return;

				}
				
				if (valuePath.contains(".")) {
					 JOptionPane.showOptionDialog(null, "   Vui lòng chọn một thư mục ",
							" Không chọn file !", JOptionPane.CLOSED_OPTION, JOptionPane.PLAIN_MESSAGE, null, null,
							null);
					return ;
				}
				currentIconSetPath.setForeground(new Color(72, 168, 58));
				dashboard.setVisible(true);
				scrollTree.setVisible(false);
				// TODO Auto-generated method stub
				try {
					repPath(valuePath, socketCurrent);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});
	}
}
