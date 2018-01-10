import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import gurobi.GRBException;

public class Algorithm {

	// stores projects, lecturers and students (depending on their current state)
	protected ArrayList<Project> projects;
	protected ArrayList<Lecturer> lecturers;
	protected ArrayList<Student> assignedStudents;
	protected ArrayList<Student> unassigned;

	// Creates stability checker object for algorithm
	protected StabilityChecker s = new StabilityChecker(this);

	// Used to assigned projects in students preference list to be "-1"
	protected Project emptyProject;

	// Used for printing purposes after the execution of the program
	protected ArrayList<Student> untouchedStudents;

	// Initialises all instance variables
	public Algorithm() {
		projects = new ArrayList<Project>();
		lecturers = new ArrayList<Lecturer>();
		assignedStudents = new ArrayList<Student>();
		unassigned = new ArrayList<Student>();
		emptyProject = new Project("empty");
		untouchedStudents = new ArrayList<Student>();
	}
	
	// Writes instance to file, this can then be used for evaluation or running
	// the program again with the same instance
	public void writeToFile(String filename) {
		try {
			PrintWriter writer = new PrintWriter(filename, "UTF-8");
			writer.println(unassigned.size() + " " + projects.size() + " " + lecturers.size());
			for (Student s : unassigned) {
				String prefListString = "";
				for (Project p : s.preferenceList) {
					prefListString += p.name + " ";
				}
				writer.println(s.name + " " + prefListString);
			}
			for (Project p : projects) {
				writer.println(p.name + " " + p.capacity + " " + p.lecturer.name);
			}
			for (Lecturer l : lecturers) {
				String projectListString = "";
				for (Project p : l.projects) {
					projectListString += p.name + " ";
				}
				writer.println(l.name + " " + l.capacity + " " + projectListString);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO error");
		}
	}

	// methods to print information about the instance and matching

	public void printDiagnosticInformation() {

		this.printProjects();

		this.printStudents();

		this.printLecturers();

		this.printMatching();
	}

	void printProjects() {
		System.out.println("PRINTING PROJECTS");
		System.out.println();
		ArrayList<Project> toPrint = projects;
		for (Project p : toPrint) {
			System.out.println(p.name + " " + p.capacity);
		}
		System.out.println();
	}

	void printStudents() {
		System.out.println("PRINTING STUDENTS");
		for (Student st : untouchedStudents) {
			System.out.print(st.name + " : ");
			for (Project p : st.preferenceList) {
				System.out.print(p.name + " ");
			}
			System.out.println("");
		}
	}

	void printLecturers() {
		System.out.println("PRINTING LECTURERS");
		ArrayList<Lecturer> toPrint = lecturers;
		for (Lecturer l : toPrint) {
			System.out.print(l.name + " : " + l.capacity + " : ");
			for (Project p : l.projects) {
				System.out.print(p.name + " ");
			}
			System.out.println();
		}
		System.out.println();
	}

	void printMatching() {
		System.out.println("PRINTING MATCHING");
		for (Student s : assignedStudents) {
			System.out.println(s.name + " " + s.proj.name);
		}
		System.out.println(assignedStudents.size() + " students were assigned a project");

	}

	// Takes a lecturer and their worstNonEmptyProject and removes a student from said project
	void removeStudentFromArrayList(Lecturer firstProjectsLecturer, Project worstNonEmptyProject) {
		Random random = new Random();
		Student removeStudent;
		if (worstNonEmptyProject.unpromoted.size() > 0) {

			int removeInt = random.nextInt((worstNonEmptyProject.unpromoted.size()));
			if (removeInt != 0) {
				removeInt--; // allows access to each student
			}
			// remove a random student from the lecturersWorstNonEmptyProject
			removeStudent = worstNonEmptyProject.unpromoted.get(removeInt);
			worstNonEmptyProject.unpromoted.remove(removeStudent);
		} else {
			int removeInt = random.nextInt((worstNonEmptyProject.promoted.size()));
			if (removeInt != 0) {
				removeInt--; // allows access to each student
			}
			// remove a random student from the lecturersWorstNonEmptyProject
			removeStudent = worstNonEmptyProject.promoted.get(removeInt);
			worstNonEmptyProject.promoted.remove(removeStudent);
		}

		removeStudent.proj = null;

		removeStudent.preferenceList.set(removeStudent.preferenceList.indexOf(worstNonEmptyProject), emptyProject);

		removeStudent.findNextFavouriteProject(this);

		if (removeStudent.rankingListTracker != -1) { // if they don't only have rejected projects
			unassigned.add(removeStudent);
		}

		assignedStudents.remove(removeStudent);
		firstProjectsLecturer.assigned--;
	}

	protected void assignProjectsToStudents() {
	}

	// Used to compare performance of three methods for finding matchings
	protected void printMatchingOutput(int avg, int max, int min) {
		System.out.println("Average matching size was " + avg);
		System.out.println("Maximum matching size was " + max);
		System.out.println("Minimum matching size was " + min);
	}

	public void assignConstraints(Algorithm a) throws GRBException {

	}

}
