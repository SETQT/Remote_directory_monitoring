package model;

import java.io.File;
import java.net.Socket;

public class Client {
	private String ip;
	private Socket socket;
	public boolean isSetedPath() {
		return setedPath;
	}
	public void setSetedPath(boolean setedPath) {
		this.setedPath = setedPath;
	}
	public boolean isPause() {
		return isPause;
	}
	public void setPause(boolean isPause) {
		this.isPause = isPause;
	}
	private File file;
	private boolean setedPath;
	private boolean isPause;
	public Client(Socket socket) {
		super();
		this.isPause=false;
		this.setedPath=false;
		this.socket = socket;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
}
