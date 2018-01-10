import java.util.ArrayList;
import gurobi.*;

public class Student {

	ArrayList<Project> preferenceList;

	// untouchedPreferenceList is used to print out instances after the execution of the program has completed.
	// This is useful for testing, as we may only want to print out instances 
	// that perform in certain ways, for example one which produces a blocking coalition.
	// The printed out instance can then be used for debugging purposes.
	ArrayList<Project> untouchedPreferenceList;

	// The students currently assigned project
	Project proj;

	// A list of Gurobi variables, one is added for each project in the students
	// preference list.
	// In the IP programming model, these are used to calculate the optimal
	// solution, if a gurobi variable belonging to a student is one, they are
	// assigned the related project.
	ArrayList<GRBVar> stuProPairs; // student project pairs

	// create envy list. This is a list pertaining to other students and whether or not this student envies them.
	ArrayList<GRBVar> envyList;

	// tracks current best project student has not been rejected from
	int rankingListTracker;

	String[] prefList;
	
	Student untouchedStudent;
	
	// The name of the student
	String name;

	// applicable to Spa-P-Approx-Promotion only
	boolean promoted;

	public Student(String name) {
		// Every student initially prefers the first project in their preference list.
		rankingListTracker = 0;
		this.name = name;
		promoted = false;
		preferenceList = new ArrayList<Project>();
		untouchedPreferenceList = new ArrayList<Project>();
		stuProPairs = new ArrayList<GRBVar>();
		envyList = new ArrayList<GRBVar>();
	}

	public void promote() {
		// Effectively "promotes a student", allowing them to attempt to apply
		// to each project in their preference list again. Only relevant for
		// SPA-P-APPROX-PROMOTION
		promoted = true;
		preferenceList = new ArrayList<Project>(untouchedPreferenceList);
		rankingListTracker = 0;
	}

	protected void findNextFavouriteProject(Algorithm a) {
		int max = -1;
		boolean found = false;
		// iterates over students full ranking list
		for (int k = 0; k < preferenceList.size() && found == false; k++) {
			// found potential next favourite project
			if (preferenceList.get(k) != a.emptyProject) {
				found = true;
				max = k;
			}
		}

		// Will be set to -1 if no next project was found
		rankingListTracker = max;
	}

}
