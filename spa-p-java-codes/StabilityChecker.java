import java.util.ArrayList;

public class StabilityChecker {

	Algorithm algorithm;

	public StabilityChecker(Algorithm algorithm) {
		this.algorithm = algorithm;
	}

	protected void blockingCoalitionDetector(ArrayList<Student> assignedStudents) {
		Digraph digraph = new Digraph();
		for (Student s : assignedStudents) {
			digraph.add(s); // add all students as nodes
		}
		for (Student s : assignedStudents) { // for every student add edges to other students who have a preferable project
			for (int p = 0; p < s.preferenceList.indexOf(s.proj); p++) { // for every project they find acceptable
				for (Student x : assignedStudents) { // make digraph edge from current student to all currently assigned to preferred project
					if (x.proj == s.untouchedPreferenceList.get(p)) {
						digraph.add(s, x);
					}
				}
			}
		}

		// If a blocking coalition is found, print the instance and halt execution
		if (!digraph.doesNotContainCycle()) {
			algorithm.printDiagnosticInformation();
			System.out.println("Blocking coalition exists");
			System.exit(0);
		}
	}
	
	// Checks no assigned students form a blocking pair
	void checkAssignedStudentsForBlockingPairs(ArrayList<Student> assignedStudents) {

		Project currentProj;
		Project lecturersWorstNonEmptyProject = null;

		int rLT; // Tracks location of project currently assigned to student

		for (Student s : assignedStudents) {
			rLT = s.preferenceList.indexOf(s.proj);
			for (int p = 0; p < rLT; p++) {	// For every project s prefers
				currentProj = s.untouchedPreferenceList.get(p);
				// If currentProj is undercapacity and currentProj's lecturer is full
				if (currentProj.capacity != (currentProj.unpromoted.size() + currentProj.promoted.size())) {
					if (currentProj.lecturer.capacity == currentProj.lecturer.assigned) {
						lecturersWorstNonEmptyProject = currentProj.lecturer.worstNonEmptyProject(currentProj);
						// If currentProj's lecturer prefers currentProj to their worst non empty project
						if (currentProj.lecturer.projects.indexOf(currentProj) < currentProj.lecturer.projects.indexOf(lecturersWorstNonEmptyProject)) {
							System.out.println("ERROR: Assigned student with full teacher who prefers this project");
							algorithm.printDiagnosticInformation();
						}
					// If proj and lecturer are undersubscribed, a blocking pair has occured
					} else {
						System.out.println("ERROR: Assigned student with under capacity teacher");
						algorithm.printDiagnosticInformation();

					}
				}
			}
		}
	}
	
	// Very similar to method above, just checks unassigned students instead
	void checkUnassignedStudentsForBlockingPairs(ArrayList<Student> unassignedStudents) {
		Project currentProj;
		Project lecturersWorstNonEmptyProject = null;

		for (Student s : unassignedStudents) {
			for (int p = 0; p < s.untouchedPreferenceList.size(); p++) {	// For every project in students preference list
				currentProj = s.untouchedPreferenceList.get(p);
				if (currentProj.lecturer.capacity == currentProj.lecturer.assigned) {
					lecturersWorstNonEmptyProject = currentProj.lecturer.worstNonEmptyProject(currentProj);
					if (currentProj.lecturer.projects.indexOf(currentProj) < currentProj.lecturer.projects.indexOf(lecturersWorstNonEmptyProject)) {
						System.out.println("ERROR: unassigned student with full teacher who prefers this project");
						algorithm.printDiagnosticInformation();
					}
				} else {
					System.out.println("ERROR: unassigned student with under capacity teacher");
					algorithm.printDiagnosticInformation();
				}
			}
		}
	}

	void IProgrammingBlockingPairs(ArrayList<Student> students) {
		Project currentProj;

		for (Student s : students) {
			if (s.proj != algorithm.emptyProject) { // if student is not unassigned
				for (int p = 0; p < s.untouchedPreferenceList.indexOf(s.proj); p++) {
					currentProj = s.untouchedPreferenceList.get(p);
					if (currentProj.capacity > currentProj.unpromoted.size()) {
						if (s.proj.lecturer == currentProj.lecturer) { 
							// if the lecturer supervises both projects, and prefers one that the student also prefers
							if (s.proj.lecturer.projects.indexOf(s.proj) > currentProj.lecturer.projects.indexOf(currentProj)) {
								algorithm.printDiagnosticInformation();
								System.out.println("DOES NOT COMPUTE, blocking pair condition 3a"); // 3a fails
								System.exit(1);
							}
						} else {
							// blocking pair condition 3b
							if (currentProj.lecturer.assigned < currentProj.lecturer.capacity) {
								algorithm.printDiagnosticInformation();
								System.out.println("DOES NOT COMPUTE, blocking pair condition 3b"); // 3b fails
								System.exit(1);
							}
							// blocking pair condition 3c
							if (currentProj.lecturer.assigned == currentProj.lecturer.capacity) {
								if (currentProj.lecturer.projects.indexOf(currentProj) < s.proj.lecturer.projects.indexOf(currentProj.lecturer.worstNonEmptyProject(algorithm.emptyProject))) {
									algorithm.printDiagnosticInformation();
									System.out.println("DOES NOT COMPUTE, blocking pair condition 3c"); // 3c fails
									System.exit(1);
								}
							}
						}
					}
				}
			} else {
				for (int p = 0; p < s.untouchedPreferenceList.size(); p++) {
					currentProj = s.untouchedPreferenceList.get(p);
					if (currentProj.capacity > currentProj.unpromoted.size()) {	// if current proj is under subscribed
						if (currentProj.lecturer.capacity > currentProj.lecturer.assigned) { // if lecturer is under subscribed
							algorithm.printDiagnosticInformation();
							System.out.println("DOES NOT COMPUTE, blocking pair condition 3b -- empty student"); // 3b
							System.exit(1);
						} else {
							if (currentProj.lecturer.capacity == currentProj.lecturer.assigned
									&& currentProj.lecturer.projects.indexOf(currentProj.lecturer.worstNonEmptyProject(
											algorithm.emptyProject)) > currentProj.lecturer.projects.indexOf(currentProj)) { // need to check for if current proj is better than lecturers worst non empty project
								algorithm.printDiagnosticInformation();
								System.out.println("DOES NOT COMPUTE, blocking pair condition 3c here"); // 3c																											// fails
								System.exit(1);
							}
						}
					}
				}
			}
		}

	}
}