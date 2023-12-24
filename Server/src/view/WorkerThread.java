package view;

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
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import model.Client;

public class WorkerThread extends Thread {
	private Socket socket;
	private JFrame frame;
	private JTree tree;
	private MainUI ui;
	private String nameClient;
	private Client currentClient;

	public WorkerThread(Socket socket, JFrame frame, JTree tree, MainUI ui) {
		this.socket = socket;
		this.frame = frame;
		this.tree = tree;
		this.ui = ui;
		String ip = socket.getInetAddress().toString();
		ip = ip.replace("/", "");
		this.nameClient = ip + "-" + Integer.toString(socket.getPort());

	}

	public void run() {
		System.out.println("Processing: " + socket);
		try {
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
	

			bw.write("init"); // Send the init to client
			bw.newLine();
			bw.flush();
			System.out.println("-----");
			initTree();
			String message;
			while (true) {

				message = br.readLine();
				if (message != null) {

					if (ui.modeALL == false) {
						String ip = ui.socketCurrent.getInetAddress().toString();
						ip = ip.replace("/", "");
						ip = ip + "-" + Integer.toString(ui.socketCurrent.getPort());

						if (!(ip.equals(nameClient)) || currentClient.isPause())
							continue;

					}
					if (currentClient.isPause())
						continue;
					ui.valueCurrent.append(this.nameClient + ": " + message + "\n");
					
				}
	}
		} catch (IOException |	ClassNotFoundException e) {
		

			MainUI.removeClient(socket);
		}
		System.out.println("Complete processing: " + socket);
	}


	public void initTree() throws IOException, ClassNotFoundException {

		System.out.println("*****");
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

		Client current = ui.clientForManager.stream().filter(cli -> cli.getSocket() == socket).findFirst().get();
		this.currentClient = current;
		File folder = (File) in.readObject();
		current.setFile(folder);
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(folder.getName());
		createTree(root, folder);
		setTree(root);
	}

	public void setTree(DefaultMutableTreeNode root) {
		tree.setModel(new javax.swing.tree.DefaultTreeModel(root));
	}

	public static void createTree(DefaultMutableTreeNode parent, File file) {
		if (!file.isDirectory()) {
			return;
		}

		for (File subFile : file.listFiles()) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(subFile.getName());
			parent.add(node);
			createTree(node, subFile);
		}
	}
}