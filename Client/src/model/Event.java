package model;

import java.io.Serializable;

public class Event implements Serializable {
    private static final long serialVersionUID = 1L;
	 private String context;

	private String name;
	 private String path;
	
	@Override
	public String toString() {
		String result="Thực hiện " + context + ",file " + name + ",đường dẫn " + path ;
		result=result.replace("ENTRY_MODIFY","thay đổi");
		result=result.replace("ENTRY_DELETE","xóa");
		result=result.replace("ENTRY_CREATE","tạo mới");
	if (!name.contains(".")) result=result.replace("file","folder");
		return result;
	}
	public Event() {
		
	}
	public Event(String context, String name, String path) {
		super();
		this.context = context;
		this.name = name;
		this.path = path;
	}
}
