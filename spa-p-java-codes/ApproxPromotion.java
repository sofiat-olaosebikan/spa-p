import java.util.ArrayList;
import java.util.Random;

public class ApproxPromotion extends Algorithm {

	public ApproxPromotion() {
		super();
	}

	// Deep copies instance variables from algorithm object
	public ApproxPromotion(Algorithm algorithm) {
		this.assignedStudents = new ArrayList<Student>(algorithm.assignedStudents);
		this.projects = new ArrayList<Project>(algorithm.projects);
		this.lecturers = new ArrayList<Lecturer>(algorithm.lecturers);
		this.unassigned = new ArrayList<Student>(algorithm.unassigned);
		this.emptyProject = new Project("empty");
		this.untouchedStudents = new ArrayList<Student>(algorithm.untouchedStudents);
		this.s = new StabilityChecker(this);
	}

  	// Performs SPA-P-APPROX-PROMOTION algorithm
	protected void assignProjectsToStudents() {

		// first projects lecturer
		Lecturer fPL; 
		
		 // random student chosen from list of unassigned students
		Student stud;
		
		// the random students first project
		Project firstProj; 
		
		// used to locate students favourite project
		int currentIndex; 

		boolean ignoreFunctionRunthrough; // boolean value determining whether or not the student is considered inelligible for any of project they find acceptable
		
		// the lecturers worst non empty project
		Project wNEP; 
		
		// used to chose a random student
		Random randomStudent = new Random(); 

		while (!unassigned.isEmpty()) {

			wNEP = emptyProject;
			
			// Get random student
			stud = unassigned.get(randomStudent.nextInt(unassigned.size()));

			// Reset variable for this iteration of while loop
			ignoreFunctionRunthrough = false;

			// Find students favourite project they haven't been rejected from
			stud.findNextFavouriteProject(this);

			/*
			 * if stud has empty preference list and is not promoted then
			 * promote them. Otherwise if student has empty preference list and
			 * is promoted, remove them 
			 */
			if (stud.rankingListTracker == -1) {
				if (!stud.promoted) {
					stud.promote();
				} else {
					unassigned.remove(stud);
					ignoreFunctionRunthrough = true;
				}
			}

			if (!ignoreFunctionRunthrough) { // used to ignore function runthrough if the student is considered inelligible for any of project they find acceptable

				// store the index of the students favourite "available" project
				currentIndex = stud.rankingListTracker;

				// get students favourite proj they haven't been rejected from
				firstProj = stud.preferenceList.get(currentIndex); 
				
				fPL = firstProj.lecturer;

				wNEP = fPL.worstNonEmptyProject(wNEP);

				// Checks to see if project is full OR lecturer is full and the favourite project is the lecturer's worst non empty project
				if (((firstProj.unpromoted.size() + firstProj.promoted.size()) == firstProj.capacity
						|| (fPL.isFull() && wNEP == firstProj))) {

					// if student is unpromoted or there is no unpromoted student assigned to firstProj
					if (!stud.promoted || firstProj.unpromoted.size() == 0) {
						// reject student and find their next favourite project
						stud.preferenceList.set(currentIndex, emptyProject);
						stud.findNextFavouriteProject(this);

					} else {
						// get random unpromoted student from the project's currently assigned students
						Student removeStudent = firstProj.unpromoted.get(randomStudent.nextInt(firstProj.unpromoted.size()));
						firstProj.unpromoted.remove(removeStudent);
						assignedStudents.remove(removeStudent);
						unassigned.add(removeStudent);
						fPL.assigned--;
						// set the project is the remove students list to be
						// essentially -1
						removeStudent.preferenceList.set(removeStudent.rankingListTracker, emptyProject);
						stud.findNextFavouriteProject(this);

						// add the student to the list of promoted students
						// assigned to this project
						assignStudentToProj(stud, firstProj, fPL, wNEP);
					}
				// Otherwise if the lecturer is full and they prefer their worstNonEmptyProject to this project
				} else if (fPL.isFull() && fPL.projects.indexOf(wNEP) < fPL.projects.indexOf(firstProj)) {
					stud.preferenceList.set(currentIndex, emptyProject);	// Reject student and find their next favourite project
					stud.findNextFavouriteProject(this);
				} else {
					// Abstracted out the task of assigning a student to a project for ease of readability
					assignStudentToProj(stud, firstProj, fPL, wNEP);
				}
			}
		}
	}

	// Function used to assign a student to a project
	void assignStudentToProj(Student stud, Project firstProj, Lecturer fPL, Project wNEP) {
		stud.proj = firstProj;
		if (!stud.promoted)
			firstProj.unpromoted.add(stud);
		else
			firstProj.promoted.add(stud);
		assignedStudents.add(stud);
		unassigned.remove(stud);
		fPL.assigned++;
		wNEP = fPL.worstNonEmptyProject(wNEP);
		// if lecturer is oversubscribed
		if (fPL.assigned > fPL.capacity) {
			// Abstracted out process of removing a student from the project list for ease of readability
			removeStudentFromArrayList(fPL, wNEP);
		}
	}
}
