import java.util.ArrayList;
import java.util.Random;

public class Approx extends Algorithm{

	public Approx() {
		super();
	}

	// Deep copies instance variables from algorithm object
  	public Approx(Algorithm algorithm) {
  		this.assignedStudents = new ArrayList<Student>(algorithm.assignedStudents);
  		this.projects = new ArrayList<Project>(algorithm.projects);
  		this.lecturers = new ArrayList<Lecturer>(algorithm.lecturers);
  		this.unassigned = new ArrayList<Student>(algorithm.unassigned);
  		this.emptyProject = new Project("empty");
  		this.untouchedStudents = new ArrayList<Student>(algorithm.untouchedStudents);
  		this.s = new StabilityChecker(this);
	}
  	
  	// Performs SPA-P-APPROX algorithm
	protected void assignProjectsToStudents() {

		//could use random value to randomise which student in unassigned we use
		Project studentsFirstProject;

		// First projects lecturer
	    Lecturer fPL;
	
	    // Tracks fPL's worst non empty project
	    Project wNEP;
	
	    // Current student
	    Student student;
	
	    Project redundantProject;
	
	    int currentIndex; // used to locate students favourite 	project
	
	    // Used to find a random student to give an application opportunity to.
	    Random randomStudent = new Random();
	
		while (!unassigned.isEmpty()) {	// While there is still a student who has not been rejected from all projects and is not currently assigned
			
			// Choose random student
			student = unassigned.get(randomStudent.nextInt(unassigned.size()));
			
			// Finds current location of students favourite project that they haven't been rejected from
			currentIndex = student.rankingListTracker;
			if (currentIndex == -1) {	// if they have been rejected from all projects, remove them from unassigned list 
				unassigned.remove(student);
			} else {
				
				// Find project student wants to apply to
				studentsFirstProject = student.preferenceList.get(currentIndex);
				
				// Lecturer of project being applied to
				fPL = studentsFirstProject.lecturer;
	
				
				wNEP = fPL.projects.get(fPL.projects.size() - 1); //initially set it to worst project
	
				if (fPL.assigned != 0) {
					//iterate over all lecturers projects backwards to find worst nonEmptyProject
					wNEP = fPL.worstNonEmptyProject(wNEP);
				}
	
				// if project is full || lecturer is full and this is lecturers worst project
				if (studentsFirstProject.unpromoted.size() == studentsFirstProject.capacity || (fPL.isFull() && wNEP == studentsFirstProject)) {
					// Reject student and find their next favourite project
					student.preferenceList.set(currentIndex, emptyProject);
					student.findNextFavouriteProject(this);
				} else {
					// Set project as students assigned project
					
					student.proj = studentsFirstProject;
					studentsFirstProject.unpromoted.add(student);
					assignedStudents.add(student);
					unassigned.remove(student);
					fPL.assigned++;
	
					wNEP = fPL.worstNonEmptyProject(wNEP);	// Refind lecturers worst non empty project
					
					if (fPL.assigned > fPL.capacity) { // if lecturer is over subscribed
						
						// Chose random student to remove from lecturers worst non empty project
						Random random = new Random();
						int removeInt = random.nextInt((wNEP.unpromoted.size()));

						if (removeInt != 0) {
							removeInt--; // allows access to each student
						}
						
						// Remove randomly selected student from the lecturersWorstNonEmptyProject
						Student removeStudent = wNEP.unpromoted.get(removeInt);
						wNEP.unpromoted.remove(removeStudent);
						removeStudent.proj = null;
						removeStudent.preferenceList.set(removeStudent.preferenceList.indexOf(wNEP), emptyProject);
	
						removeStudent.findNextFavouriteProject(this);	// Sets students ranking list tracker
	
						if (removeStudent.rankingListTracker != -1){	//if they dont only have rejected projects
							unassigned.add(removeStudent);
						}
	
						assignedStudents.remove(removeStudent);
						fPL.assigned--;
					}
					// if lecturer is full
					if (fPL.isFull()) {
						// Find every project lecturer prefers their worst non empty project to and remove it from all students project list
						for (int i = (fPL.projects.indexOf(wNEP)+1); i < fPL.projects.size(); i++){
							redundantProject = fPL.projects.get(i);
							// for each student remove from their preferenceList if they have it
							for (Student s:unassigned) {
	
								// causing concurrent modification access error
								// so have to track location of redundant project and remove it after
								int location = -1;
		
								for (int j = 0; j < s.preferenceList.size(); j++) {
									if(s.preferenceList.get(j)==redundantProject)
										location = j;
										j=s.preferenceList.size();	// break out of loop
								}
	
								if (location!= -1) {
									s.preferenceList.set(location, emptyProject);
									s.findNextFavouriteProject(this);
								}
							}
							
							// Also remove project from assigned students project lists
							for (Student s:assignedStudents) {
		
								// causing concurrent modification access error
								// so have to track location of redundant project and remove it after
								int location = -1;
		
								for (int j = 0; j < s.preferenceList.size(); j++) {
									if(s.preferenceList.get(j)==redundantProject)
										location = j;
										j = s.preferenceList.size();
								}
		
								if (location!= -1) {
									if (s.preferenceList.get(location)!= s.proj){
										s.preferenceList.set(location, emptyProject);
										s.findNextFavouriteProject(this);
									}
								}
						}
					}
				}
			}
			}
		}
	}
}
