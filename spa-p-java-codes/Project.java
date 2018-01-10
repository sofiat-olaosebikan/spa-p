import java.util.ArrayList;

public class Project {

	Lecturer lecturer;
	String name;

	// List of unpromoted students currently assigned to project
	ArrayList<Student> unpromoted;
	
	// List of promoted students currently assigned to project
	ArrayList<Student> promoted;

	// Most students that can be assigned to this project
	int capacity;

	public Project(String name) {
		this.name = name;
		this.capacity = 1;
		unpromoted = new ArrayList<Student>();
		promoted = new ArrayList<Student>();
	}

	public Project(String name, int capacity) {
		this.name = name;
		this.capacity = capacity;
		unpromoted = new ArrayList<Student>();
		promoted = new ArrayList<Student>();
	}
}
