import java.util.*;

import gurobi.GRBException;

import java.io.BufferedReader;
import java.io.FileReader;

public class Main {

	static Algorithm algorithm;

	public static void main(String[] args) {
		Algorithm algorithm3 = fileReader(args[0]);
		GurobiModel gurobiModel = new GurobiModel(algorithm3);
		try {
			//this runs ip solver
			Date startDateForIP = new java.util.Date();
			gurobiModel.assignConstraints(gurobiModel);
			Date endDateForIP = new java.util.Date();
			gurobiModel.s.IProgrammingBlockingPairs(gurobiModel.assignedStudents);
			double timeInSeconds = (endDateForIP.getTime() - startDateForIP.getTime())/1000.0;
			
			//initialise algos to run once
			Algorithm algorithm1 = fileReader(args[0]);
			Algorithm algorithm2 = fileReader(args[0]);
			Approx spaPApproxOnce = new Approx(algorithm1);
			ApproxPromotion spaPApproxPromotionOnce = new ApproxPromotion(algorithm2);
			
			//this runs approx once
			Date startDateForApprox = new java.util.Date();
			spaPApproxOnce.assignProjectsToStudents();
			Date endDateForApprox = new java.util.Date();
			double timeForApproxOnce = (endDateForApprox.getTime()-startDateForApprox.getTime())/1000.0;
			int spaPApproxOnceSize = spaPApproxOnce.assignedStudents.size();
			spaPApproxOnce.s.blockingCoalitionDetector(spaPApproxOnce.assignedStudents);
			
			// this runs spaPApproxPromotion once
			Date startDateForApproxPromotion = new java.util.Date();
			spaPApproxPromotionOnce.assignProjectsToStudents();
			Date endDateForApproxPromotion = new java.util.Date();
			double timeForApproxOncePromotion = (endDateForApproxPromotion.getTime()-startDateForApproxPromotion.getTime())/1000.0;
			int spaPApproxOncePromotionSize = spaPApproxPromotionOnce.assignedStudents.size();
			spaPApproxPromotionOnce.s.blockingCoalitionDetector(spaPApproxPromotionOnce.assignedStudents);
			
			//this runs spapapprox 100 times
			int currentMaxApprox = 0;
			int currentMaxPromotion = 0;
			Date endDateForApproxOneHundred = new java.util.Date();
			Date startDateForApproxOneHundred = new java.util.Date();
			for (int k = 0; k < 100; k++) { //iterate for approx alg
				Algorithm algorithm4 = fileReader(args[0]);
				Approx approxOneHundred = new Approx(algorithm4);
				approxOneHundred.assignProjectsToStudents();
				if (approxOneHundred.assignedStudents.size() > currentMaxApprox) {
					// update currently held max and track time that we found it at 
					endDateForApproxOneHundred = new java.util.Date();
					currentMaxApprox = approxOneHundred.assignedStudents.size();
				}
			}					
			double approxOneHundredTime = (endDateForApproxOneHundred.getTime() - startDateForApproxOneHundred.getTime())/1000.0;

			// this runs approxpromotion one hundred times
			Date startDateForPromotionOneHundred = new java.util.Date();
			Date endDateForPromotionOneHundred = new java.util.Date();
			for (int k = 0; k < 100; k++) { //iterate for promotion alg
				Algorithm algorithm5 = fileReader(args[0]);
				ApproxPromotion approxPromotionOneHundred = new ApproxPromotion(algorithm5);
				approxPromotionOneHundred.assignProjectsToStudents();
				if (approxPromotionOneHundred.assignedStudents.size() > currentMaxPromotion) {
					// update currently held max and track time that we found it at 
					endDateForPromotionOneHundred = new java.util.Date();
					currentMaxPromotion = approxPromotionOneHundred.assignedStudents.size();
				}
			}
			double approxPromotionOneHundredTime = (endDateForPromotionOneHundred.getTime() - startDateForPromotionOneHundred.getTime())/1000.0;

			// No need for GUI anymore
			//MyGUI.main(args);
			
			// <filename> <spapapproxrunoncetime> <spapapproxrunoncesize> <spapapproxpromotionrunoncetime> <spapapproxpromotionrunoncesize> 
			// <iptime> <ipsize> <spapapproxrun100time> <spapapproxrun100size> <spapapproxpromotionrun100time> <spapapproxpromotionrun100size>
			System.out.println(args[0] + " " + spaPApproxOnceSize + " " + timeForApproxOnce + " " + spaPApproxOncePromotionSize + " " + timeForApproxOncePromotion + " " + gurobiModel.sizeOfMatching() + " " + timeInSeconds + " " + currentMaxApprox + " " + approxOneHundredTime + " " + currentMaxPromotion + " " + approxPromotionOneHundredTime);
			
		} catch (GRBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	static // Reads instance of SPA from file
	Algorithm fileReader(String fileName) {
		Algorithm algorithm1 = new Algorithm();
		try {

			BufferedReader br = new BufferedReader(new FileReader(fileName));

			String[] splitted = br.readLine().split(" ");


			int numStudents = Integer.parseInt(splitted[0]);

			int numProjects = Integer.parseInt(splitted[1]);
			
			int numLecturers = Integer.parseInt(splitted[2]);

			Student currentStudent;

			Student untouchedStudent;

			// Do projects first, track their lecturer and assign at the end
			Project currentProject;


			// Now create students
			for (int i = 0; i < numStudents; i++) {

				splitted = br.readLine().split(" "); // get each student
				currentStudent = new Student(splitted[0].substring(0, splitted[0].length()));
				algorithm1.unassigned.add(currentStudent); // creates student with new name
				untouchedStudent = new Student(splitted[0].substring(0, splitted[0].length()));
				algorithm1.untouchedStudents.add(untouchedStudent); // creates student with new name
				currentStudent.prefList = splitted;
				currentStudent.untouchedStudent = untouchedStudent;
			}
			
			// Create projects
			for (int i = 0; i < numProjects; i++) {
				splitted = br.readLine().split(" ");
				currentProject = new Project(splitted[0], Integer.parseInt(splitted[1]));
				algorithm1.projects.add(currentProject);
			}
			
			for (Student s:algorithm1.unassigned){
				for (int j = 1; j < s.prefList.length; j++) {
					s.preferenceList.add(algorithm1.projects.get(Integer.parseInt(s.prefList[j])-1));
					s.untouchedStudent.preferenceList.add(algorithm1.projects.get(Integer.parseInt(s.prefList[j])-1));
				}
				s.untouchedPreferenceList = new ArrayList<Project>(s.preferenceList);
				s.untouchedStudent.untouchedPreferenceList = new ArrayList<Project>(s.untouchedStudent.preferenceList);
			}

			Lecturer currentLecturer;
			// Now create lecturers

			for (int i = 0; i < numLecturers; i++) {

				splitted = br.readLine().split(" ");
				currentLecturer = new Lecturer(splitted[0].substring(0, splitted[0].length()),
						Integer.parseInt(splitted[1].substring(0, splitted[1].length())));
				algorithm1.lecturers.add(currentLecturer);
				for (int j = 2; j < splitted.length; j++) {
					currentLecturer.projects.add(algorithm1.projects.get(Integer.parseInt(splitted[j])-1));
					algorithm1.projects.get(Integer.parseInt(splitted[j])-1).lecturer = currentLecturer;
				}
			}

			String line;

			// add any matchings
			ArrayList<Student> toBeRemoved = new ArrayList<Student>();
			Project matchingProject;
			int projectNumber;

			while ((line = br.readLine()) != null) {
				splitted = line.split(",");
				currentStudent = algorithm1.unassigned
						.get(Integer.parseInt(splitted[0].substring(1, splitted[0].length())));
				toBeRemoved.add(currentStudent);
				projectNumber = Integer.parseInt(splitted[1].substring(0, splitted[1].length()));
				matchingProject = algorithm1.projects.get(projectNumber);
				currentStudent.proj = matchingProject;
				currentStudent.rankingListTracker = projectNumber; // for
																	// stability
																	// checking
																	// purposes
				matchingProject.unpromoted.add(currentStudent);
				matchingProject.lecturer.assigned++;
			}

			br.close();

			algorithm1.assignedStudents.addAll(toBeRemoved);
			algorithm1.unassigned.removeAll(toBeRemoved);
			algorithm1.assignedStudents.size();
			algorithm1.unassigned.size();

		} catch (Exception e) {
			System.out.println("Type of error: " + e.getClass().getName() + " Error message: " + e.getMessage());
		}
		algorithm = algorithm1;
		return algorithm;
	}


	static void assignCapacity(int lecturerCapacity, int projectCapacity) {

		Random random = new Random();

		// Randomly assigns additional capacity to projects and lecturers
		for (int i = 0; i < projectCapacity; i++) {
			algorithm.projects.get(random.nextInt(algorithm.projects.size())).capacity++;
		}

		for (int i = 0; i < lecturerCapacity; i++) {
			algorithm.lecturers.get(random.nextInt(algorithm.lecturers.size())).capacity++;
		}
	}

	static void populate(int[] args) {
		populateProjects(args[0]);
		populateStudents(args[1]);
		populateLecturers(args[2]);
	}

	static void populateProjects(int numberOfProjects) {
		for (int i = 1; i < numberOfProjects+1; i++) {
			algorithm.projects.add(new Project(Integer.toString(i)));
		}
	}

	static void populateStudents(int numberOfStudents) {

		for (int i = 1; i < numberOfStudents+1; i++) {
			algorithm.unassigned.add(new Student(Integer.toString(i)));
			algorithm.untouchedStudents.add(new Student(Integer.toString(i)));
		}

		// populates student preference lists
		double random;
		Random randomProjectIndex = new Random();
		
		ArrayList<Project> duplicateList;
		int rPI;
		// need to re-add projects after a student has been assigned all his projects
		for (int j = 0; j < algorithm.unassigned.size(); j++) { // for each student
			duplicateList = new ArrayList<Project>(algorithm.projects);
			random = Math.random() * 2;

			// need to ensure we havent removed last item from duplicate list
			for (int i = 0; i < (random + 1) && !duplicateList.isEmpty(); i++) {
				rPI = randomProjectIndex.nextInt(duplicateList.size());
				algorithm.unassigned.get(j).preferenceList.add(duplicateList.get(rPI));
				algorithm.untouchedStudents.get(j).preferenceList.add(duplicateList.get(rPI));
				algorithm.unassigned.get(j).untouchedPreferenceList.add(duplicateList.get(rPI));
				algorithm.untouchedStudents.get(j).untouchedPreferenceList.add(duplicateList.get(rPI));
				duplicateList.remove(rPI);
			}
		}
	}

	static void populateLecturers(int numberOfLecturers) {
		for (int i = 1; i < numberOfLecturers+1; i++) {
			algorithm.lecturers.add(new Lecturer(Integer.toString(i)));
		}
	}

	// for each project: assign random lecturer to project and assign project to lecturer
	static void assignProjectsToLecturers() {
		ArrayList<Project> proj = new ArrayList<Project>(algorithm.projects);

		// first assign each lecturer one project
		Random randomProjectIndex = new Random();
		for (int i = 0; i < algorithm.lecturers.size() && proj.size() > 0; i++) {
			int randomInt = randomProjectIndex.nextInt(proj.size());
			algorithm.lecturers.get(i).projects.add(proj.get(randomInt));
			proj.get(randomInt).lecturer = algorithm.lecturers.get(i);
			proj.remove(randomInt);
		}

		// Hand out remaining projects randomly
		Project chosenProject;
		Lecturer chosenLecturer;
		while (proj.size() > 0) {
			int randomProjInt = randomProjectIndex.nextInt(proj.size());
			int randomLectInt = randomProjectIndex.nextInt(algorithm.lecturers.size());
			chosenProject = proj.get(randomProjInt);
			chosenLecturer = algorithm.lecturers.get(randomLectInt);
			chosenLecturer.projects.add(chosenProject);
			chosenProject.lecturer = chosenLecturer;
			proj.remove(randomProjInt);
		}

	}

}
