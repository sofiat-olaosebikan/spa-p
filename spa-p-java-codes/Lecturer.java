import java.util.ArrayList;

public class Lecturer {

	String name;

	// List of lecturers projects
	ArrayList<Project> projects = new ArrayList<Project>();

	// Max number of students lecturer is willing to advise
	int capacity;

	// Current number of students lecturer is advising
	int assigned;

	public Lecturer(String name) {
		this.name = name;
		this.projects = new ArrayList<Project>();
		this.capacity = 1;
		this.assigned = 0;
	}

	public Lecturer(String name, int capacity) {
		this.name = name;
		this.projects = new ArrayList<Project>();
		this.capacity = capacity;
		this.assigned = 0;
	}

	public boolean isFull() {
		return capacity == assigned;
	}

	// Returns lecturersWorstNonEmptyProject
	public Project worstNonEmptyProject(Project lecturersWorstNonEmptyProject) {
		boolean foundNonEmpty = false;
		// iterate from the end as the last entry will contain the worst project
		for (int i = projects.size() - 1; i > -1; i--) {

			// if project is not empty
			if (projects.get(i).unpromoted.size() + projects.get(i).promoted.size() > 0) {
				lecturersWorstNonEmptyProject = projects.get(i);
				i = -1;
				foundNonEmpty = true;
			}
		}

		if (foundNonEmpty == true) {
			return lecturersWorstNonEmptyProject;
		} else {
			return projects.get(projects.size() - 1); // we return their worst if no worst non empty was found
		}
	}

}
