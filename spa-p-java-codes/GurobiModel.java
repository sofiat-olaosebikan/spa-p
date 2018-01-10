import java.util.ArrayList;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class GurobiModel extends Algorithm {

	// The gurobi environment.
	 
	GRBEnv env;

	// The gurobi model.
	GRBModel grbmodel;

	boolean feasible = true;
	
	// Three ArrayLists used to ensure blocking pairs are not permitted
	ArrayList<GRBVar> underCapacityProjects = new ArrayList<GRBVar>();
	ArrayList<GRBVar> underCapacityLecturers = new ArrayList<GRBVar>();
	ArrayList<GRBVar> gammas = new ArrayList<GRBVar>();

	public GurobiModel() {
		super();
		try {
			env = new GRBEnv();
			grbmodel = new GRBModel(env);
			grbmodel.set(GRB.IntParam.Threads, 3);
		} catch (GRBException e) {
			e.printStackTrace();
		}
	}

	public GurobiModel(Algorithm algorithm) {
		this.projects = new ArrayList<Project>(algorithm.projects);
		this.lecturers = new ArrayList<Lecturer>(algorithm.lecturers);
		this.emptyProject = new Project("empty");
		this.assignedStudents = new ArrayList<Student>(algorithm.unassigned);
		try {
			env = new GRBEnv();
			grbmodel = new GRBModel(env);
		} catch (GRBException e) {
			e.printStackTrace();
		}
	}

	public int sizeOfMatching() {
		int countOfMatched = 0;
		for (Student s : assignedStudents) {
			if (s.proj != null) {
				if (s.proj != emptyProject) {
					countOfMatched++;
				}
			}
		}
		return countOfMatched;
	}

	public void assignConstraints(Algorithm a) throws GRBException {

		grbmodel.getEnv().set(GRB.IntParam.OutputFlag, 0);

		// capacity constraints are first assigned, then blocking coalition and blocking pair constraints
		upperLowerConstraints(a);

		addMaxSizeConstraint(a);

		// blockingCoalitionConstraints(a);

		assign3aConstraints(a);

		assign3bConstraints(a);

		assign3cConstraints(a);

		// optimise after adding all constraints

		grbmodel.optimize();

		int status = grbmodel.get(GRB.IntAttr.Status);

		if (status != GRB.Status.OPTIMAL) {
			feasible = false;
			System.out.println("no solution found in the following instance:");
			a.printDiagnosticInformation();
		} else {
			setStudentAssignments(a);
			// can print matching here with printMatching();
		}

		grbmodel.dispose();
		env.dispose();
	}

	// Adds blocking pair condition 3a as defined in formulation of IP model
	private void assign3aConstraints(Algorithm a) throws GRBException {

		// Adds variable for each project denoting whether or not project is under capacity
		for (Project p : projects) {
			GRBVar v = grbmodel.addVar(0.0, 1.0, 0.0, GRB.BINARY, p.name + " under capacity");
			underCapacityProjects.add(v); // Ei,i'
		}

		for (Student s : assignedStudents) {

			int i = 0; // tracks current project location in pref list

			for (Project p : s.preferenceList) { // for every project s finds acceptable

				// Aijk is 1 if the student is assigned a worse project than p, or is unassigned
				GRBLinExpr Aijk = new GRBLinExpr();

				Aijk.addConstant(1.0);

				GRBLinExpr sumOf = new GRBLinExpr();

				// sumOf is 1 if a student has a worse project than p or student is unassigned
				for (int j = 0; j < i; j++) {
					sumOf.addTerm(1.0, s.stuProPairs.get(j));
				}

				Aijk.multAdd(-1.0, sumOf);

				GRBLinExpr BijkRHS = new GRBLinExpr();

				GRBLinExpr BijkLHS = new GRBLinExpr();

				BijkLHS.addTerm(p.capacity, underCapacityProjects.get(a.projects.indexOf(p)));

				BijkRHS.addConstant(p.capacity);

				// 1- (students assigned to projects/capacity of project)
				GRBLinExpr Eijk = new GRBLinExpr();

				// Eijk is used to count how many students are subscribed to p
				for (Student t : assignedStudents) {
					if (t.preferenceList.contains(p)) {
						Eijk.addTerm(1.0, t.stuProPairs.get(t.preferenceList.indexOf(p)));
					}
				}

				BijkRHS.multAdd(-1.0, Eijk);

				// projects' under capacity variable is set to 1 if there are less students assigned to the project than the projects capacity
				grbmodel.addConstr(BijkLHS, GRB.GREATER_EQUAL, BijkRHS, "constraintname");

				// Cijk is one if student is matched to another project offered by p's lecturer
				GRBLinExpr Cijk = new GRBLinExpr();

				for (Project q : p.lecturer.projects) {
					if (s.preferenceList.contains(q) && p != q) {
						Cijk.addTerm(1.0, s.stuProPairs.get(s.preferenceList.indexOf(q)));
					}
				}

				// Dijk will be set to 1 if the lecturer is not assigned c students on projects they prefer to p. C = lecturer capacity
				GRBLinExpr Dijk = new GRBLinExpr();

				for (int j = p.lecturer.projects.indexOf(p) + 1; j < p.lecturer.projects.size(); j++) {
					Project curr = p.lecturer.projects.get(j);
					if (s.preferenceList.contains(curr)) {
						Dijk.addTerm(1.0, s.stuProPairs.get(s.preferenceList.indexOf(curr)));
					}
				}

				// The following linear expression ensures not all four conditions can be true
				GRBLinExpr threeA = new GRBLinExpr();

				threeA.multAdd(1.0, Aijk);

				threeA.addTerm(1.0, underCapacityProjects.get(a.projects.indexOf(p)));

				threeA.multAdd(1.0, Cijk);

				threeA.multAdd(1.0, Dijk);

				grbmodel.addConstr(threeA, GRB.LESS_EQUAL, 3, "constraint 3a");
				i++;
			}
		}
	}

	// Adds blocking pair condition 3b as defined in formulation of IP model
	private void assign3bConstraints(Algorithm a) throws GRBException {

		// Adds variable for each lecturer denoting whether or not lecturer is under capacity
		for (Lecturer l : a.lecturers) {
			GRBVar v = grbmodel.addVar(0.0, 1.0, 0.0, GRB.BINARY, l.name + " under capacity");
			underCapacityLecturers.add(v); // Ei,i'
		}

		for (Student s : assignedStudents) {

			int i = 0; // tracks current project location in pref list

			for (Project p : s.preferenceList) {
				GRBLinExpr Aijk = new GRBLinExpr();

				Aijk.addConstant(1.0);

				GRBLinExpr sumOf = new GRBLinExpr();

				// Aijk is 1 if a student has a worse project than p or student is unassigned
				for (int j = 0; j < i; j++) {
					sumOf.addTerm(1.0, s.stuProPairs.get(j));
				}

				Aijk.multAdd(-1.0, sumOf);

				GRBLinExpr BijkRHS = new GRBLinExpr();

				GRBLinExpr BijkLHS = new GRBLinExpr();

				BijkLHS.addTerm(p.capacity, underCapacityProjects.get(a.projects.indexOf(p)));

				BijkRHS.addConstant(p.capacity);

				// 1- (students assigned to projects/capacity of project)
				GRBLinExpr Eijk = new GRBLinExpr();

				// Eijk is used to count how many students are subscribed to p
				for (Student t : assignedStudents) {
					if (t.preferenceList.contains(p)) {
						Eijk.addTerm(1.0, t.stuProPairs.get(t.preferenceList.indexOf(p)));
					}
				}

				BijkRHS.multAdd(-1.0, Eijk);

				// projects' under capacity variable is set to 1 if there are less students assigned to the project than the projects capacity
				grbmodel.addConstr(BijkLHS, GRB.GREATER_EQUAL, BijkRHS, "constraintname");

				// Cijk is 1 if s is not already matched to a project offered by lk
				GRBLinExpr Cijk = new GRBLinExpr();
				GRBLinExpr sumXij = new GRBLinExpr();

				for (Project q : p.lecturer.projects) {
					if (s.preferenceList.contains(q)) {
						sumXij.addTerm(1.0, s.stuProPairs.get(s.preferenceList.indexOf(q)));
					}
				}

				Cijk.addConstant(1.0);

				// 1 - 1 if student is assigned to proj offered by l otherwise 1-0
				Cijk.multAdd(-1.0, sumXij);

				// determines whether or not the lecturer is undersubscribed
				GRBLinExpr DijkLHS = new GRBLinExpr();

				// lecturers "under capacity" variable is 1 if lecturer is undersubscribed
				DijkLHS.addTerm(p.lecturer.capacity, underCapacityLecturers.get(lecturers.indexOf(p.lecturer)));

				GRBLinExpr DijkRHS = new GRBLinExpr();

				DijkRHS.addConstant(p.lecturer.capacity);

				GRBLinExpr bracketedExpression = new GRBLinExpr();

				for (Student t : assignedStudents) {
					for (Project q : t.preferenceList) {
						if (q.lecturer == p.lecturer) {
							bracketedExpression.addTerm(1.0, t.stuProPairs.get(t.preferenceList.indexOf(q)));
						}
					}
				}

				DijkRHS.multAdd(-1.0, bracketedExpression);

				grbmodel.addConstr(DijkLHS, GRB.GREATER_EQUAL, DijkRHS, "constraintname");

				// The following linear expression ensures not all four conditions can be true
				GRBLinExpr threeB = new GRBLinExpr();

				threeB.multAdd(1.0, Aijk);

				threeB.addTerm(1.0, underCapacityProjects.get(a.projects.indexOf(p)));

				threeB.multAdd(1.0, Cijk);

				threeB.addTerm(1.0, underCapacityLecturers.get(lecturers.indexOf(p.lecturer)));

				grbmodel.addConstr(threeB, GRB.LESS_EQUAL, 3, "constraint 3b");

				i++;
			}
		}
	}

	// Adds blocking pair condition 3c as defined in formulation of IP model
	private void assign3cConstraints(Algorithm a) throws GRBException {

		GRBVar currentGamma;
		for (Lecturer l : a.lecturers) {
			currentGamma = grbmodel.addVar(0.0, 1.0, 0.0, GRB.BINARY, l.name + " gamma");
			gammas.add(currentGamma);
		}

		for (Student s : assignedStudents) {

			int i = 0; // tracks current project location in pref list

			for (Project p : s.preferenceList) {

				// Aijk is 1 if the student is assigned a worse project than p, or is unassigned
				GRBLinExpr Aijk = new GRBLinExpr();

				Aijk.addConstant(1.0);

				GRBLinExpr sumOf = new GRBLinExpr();

				// sumOf is 1 if a student has a worse project than p or student is unassigned
				for (int j = 0; j < i; j++) {
					sumOf.addTerm(1.0, s.stuProPairs.get(j));
				}

				Aijk.multAdd(-1.0, sumOf);

				GRBLinExpr BijkRHS = new GRBLinExpr();

				GRBLinExpr BijkLHS = new GRBLinExpr();

				BijkLHS.addTerm(p.capacity, underCapacityProjects.get(a.projects.indexOf(p)));

				BijkRHS.addConstant(p.capacity); // instead of dividing Eijk by capacity of project, times everything else by capacity of project

				// 1- (students assigned to projects/capacity of project)
				GRBLinExpr bracketRHS = new GRBLinExpr();

				// Eijk is used to count how many students are subscribed to p
				for (Student t : assignedStudents) {
					if (t.preferenceList.contains(p))
						bracketRHS.addTerm(1.0, t.stuProPairs.get(t.preferenceList.indexOf(p)));
				}

				BijkRHS.multAdd(-1.0, bracketRHS);

				grbmodel.addConstr(BijkLHS, GRB.GREATER_EQUAL, BijkRHS, "constraintname");

				// same as constraint 3b
				GRBLinExpr Cijk = new GRBLinExpr();
				GRBLinExpr sumXij = new GRBLinExpr();

				for (Project q : p.lecturer.projects) {
					if (s.preferenceList.contains(q)) {
						sumXij.addTerm(1.0, s.stuProPairs.get(s.preferenceList.indexOf(q)));
					}
				}

				Cijk.addConstant(1.0);

				Cijk.multAdd(-1.0, sumXij);

				GRBLinExpr DijkRHS = new GRBLinExpr();

				GRBLinExpr sumOfRHS = new GRBLinExpr();
				// checks to see if lecturer prefers this project to their worst non empty project
				for (int j = 0; j <= p.lecturer.projects.indexOf(p); j++) {
					for (Student t : assignedStudents) {
						if (t.preferenceList.contains(p.lecturer.projects.get(j))) {
							sumOfRHS.addTerm(1.0,
									t.stuProPairs.get(t.preferenceList.indexOf(p.lecturer.projects.get(j))));
						}
					}
				}

				DijkRHS.multAdd(-1.0, sumOfRHS);

				DijkRHS.addConstant(p.lecturer.capacity);

				GRBLinExpr DijkLHS = new GRBLinExpr();

				GRBVar delta = grbmodel.addVar(0.0, 1.0, 0.0, GRB.BINARY, "delta");
				DijkLHS.addTerm(p.lecturer.capacity, delta);

				grbmodel.addConstr(DijkLHS, GRB.GREATER_EQUAL, DijkRHS, "lk if full");

				GRBLinExpr EijkRHS = new GRBLinExpr();

				GRBLinExpr EijkBracketed = new GRBLinExpr();

				for (int j = 0; j < p.lecturer.projects.size(); j++) { // how many students does the lecturer have
					for (Student t : assignedStudents) {
						if (t.preferenceList.contains(p.lecturer.projects.get(j))) {
							EijkBracketed.addTerm(1.0,
									t.stuProPairs.get(t.preferenceList.indexOf(p.lecturer.projects.get(j)))); // add student project pair variable to linear expression
						}
					}
				}

				EijkRHS.multAdd(1.0, EijkBracketed);

				EijkRHS.addConstant(1.0);

				EijkRHS.addConstant(-p.lecturer.capacity);

				// if RHS of equation is equal to one (meaning lecturer is full), the gamma pertaining to the lecturer is also set to 1
				grbmodel.addConstr(gammas.get(lecturers.indexOf(p.lecturer)), GRB.GREATER_EQUAL, EijkRHS,
						"gamma constraint");

				// The following linear expression ensures not all five conditions can be true
				GRBLinExpr threeC = new GRBLinExpr();

				threeC.multAdd(1.0, Aijk);

				threeC.addTerm(1.0, underCapacityProjects.get(a.projects.indexOf(p)));

				threeC.multAdd(1.0, Cijk);

				threeC.addTerm(1.0, delta);

				threeC.addTerm(1.0, gammas.get(lecturers.indexOf(p.lecturer)));

				grbmodel.addConstr(threeC, GRB.LESS_EQUAL, 4, "constraint 3c");

				i++;
			}
		}
	}

	/**
	 * <p>
	 * Adds upper and lower quota constraints to projects and lecturers, and
	 * student upper quota.
	 * </p>
	 */
	private void upperLowerConstraints(Algorithm a) throws GRBException {

		// ----------------------------------------------------------------------------------------
		// each student is matched to 1 or less projects
		for (Student s : a.assignedStudents) {
			GRBLinExpr sumVarsForStudent = new GRBLinExpr(); // create linear expression for every student
			for (Project p : s.preferenceList) { // add student project pair variable for each project found acceptable by student
				GRBVar v = grbmodel.addVar(0.0, 1.0, 0.0, GRB.BINARY, "pref" + p.name);
				s.stuProPairs.add(v);
				sumVarsForStudent.addTerm(1, v);
			}
			// The number of projects a student has must be less than or equal to one
			grbmodel.addConstr(sumVarsForStudent, GRB.LESS_EQUAL, 1.0, "ConstraintStudent " + s.name);
		}

		// ----------------------------------------------------------------------------------------
		// each project is assigned a number of students between 0 and their
		// capacity
		for (Project p : a.projects) {
			GRBLinExpr numStudentsForProj = new GRBLinExpr();
			for (Student s : a.assignedStudents) { // add every student project pair for every student that finds project acceptable
				if (s.preferenceList.contains(p)) {
					numStudentsForProj.addTerm(1, s.stuProPairs.get(s.preferenceList.indexOf(p)));
				}
			}
			// The number of students a project has must be less than or equal to the projects max capacity
			grbmodel.addConstr(numStudentsForProj, GRB.LESS_EQUAL, (double) p.capacity, "ConstraintProjectUQ" + p.name);
		}

		// ----------------------------------------------------------------------------------------
		// each lecturer is assigned a number of students between 0 and their
		// capacity
		int x = 0;
		for (Lecturer l : a.lecturers) {
			GRBLinExpr numStudentsForLect = new GRBLinExpr();
			for (Student s : a.assignedStudents) { // for every student that finds a project offered by lecturer acceptable, add student project pair variable to gurobi linear expression
				for (Project p : s.preferenceList) {
					if (l.projects.contains(p)) {
						numStudentsForLect.addTerm(1, s.stuProPairs.get(x));
					}
					x++;
				}
				x = 0;
			}
			// The number of students a lecturer has must be less than or equal to the lecturers max capacity
			grbmodel.addConstr(numStudentsForLect, GRB.LESS_EQUAL, (double) l.capacity,
					"ConstraintLecturerUQ" + l.name);
		}
	}


	 // Adds objective to optimise on max sum of student pair variables
	 public void addMaxSizeConstraint(Algorithm a) throws GRBException {

		GRBLinExpr sumAllVariables = new GRBLinExpr();
		// for every student, add student project pair variables for every project they find acceptable to linear expression
		for (Student s : a.assignedStudents) {
			for (GRBVar var : s.stuProPairs)
				sumAllVariables.addTerm(1, var);
		}
		// attempt to maximise on the highest value possible for the sum of all student project pairs#
		grbmodel.setObjective(sumAllVariables, GRB.MAXIMIZE);
	}

	// adds constraint preventing blocking coalitions from occuring
	private void blockingCoalitionConstraints(Algorithm a) throws GRBException {

		// Begin by creating an envy graph
		for (Student i1 : a.assignedStudents) {
			for (Student i2 : a.assignedStudents) {
				if (i1 != i2) {
					// create envy variable which determines whether or not i1 envies i2.
					GRBVar v = grbmodel.addVar(0.0, 1.0, 0.0, GRB.BINARY,
							i1 + " envies " + i2.name + " if this is 1.0");
					i1.envyList.add(v);
					for (Project j1 : i1.preferenceList) {
						// for every project i1 prefers to j1
						for (int x = 0; x < i1.preferenceList.indexOf(j1); x++) {
							Project j2 = i1.preferenceList.get(x);
							if (i2.preferenceList.contains(j2)) { // if i2 finds j2 acceptable
								// creating envy graph is done by ensuring ei1,i2 + 1 >= xi1,j1 + xi2,j2. ei1,i2 is 1 if i1 is envious of i2, 0 otherwise. xi1,j1 means if i1 is assigned to j1, xi2,j2 means if student i2 is assigned j2)
								GRBLinExpr lhs = new GRBLinExpr();
								GRBLinExpr rhs = new GRBLinExpr();
								lhs.addConstant(1.0);
								lhs.addTerm(1, v);
								rhs.addTerm(1, i1.stuProPairs.get(i1.preferenceList.indexOf(j1)));
								rhs.addTerm(1, i2.stuProPairs.get(i2.preferenceList.indexOf(j2)));
								grbmodel.addConstr(lhs, GRB.GREATER_EQUAL, rhs, "creates envygraph");
							}
						}
					}
				}
			}
		}

		ArrayList<GRBVar> vertexLabels = new ArrayList<GRBVar>();

		// Every student is then assigned a vertex label integer variable
		for (int i = 0; i < a.assignedStudents.size(); i++) {
			GRBVar v = grbmodel.addVar(0.0, assignedStudents.size() - 1, 0.0, GRB.INTEGER, "vertex label for " + i);
			vertexLabels.add(v);
		}

		int i1Index = 0;
		int i2Index = 0; // used to locate envy list variables

		// ensures a topological ordering exists in the envy graph
		for (Student i1 : a.assignedStudents) {
			for (Student i2 : a.assignedStudents) {
				if (i1 != i2) {

					GRBLinExpr lhs = new GRBLinExpr();
					GRBLinExpr rhs = new GRBLinExpr();

					GRBLinExpr bracketedExpression = new GRBLinExpr();
					// bracketed expression contains 1 - ei1,i2
					bracketedExpression.addTerm(-1, i1.envyList.get(i2Index));
					bracketedExpression.addConstant(1.0);
					// get vertex label for i2
					rhs.addTerm(1.0, vertexLabels.get(a.assignedStudents.indexOf(i2)));
					rhs.multAdd(a.assignedStudents.size(), bracketedExpression);
					// get vertex label for i1
					lhs.addTerm(1.0, vertexLabels.get(i1Index));
					lhs.addConstant(1.0);
					grbmodel.addConstr(lhs, GRB.LESS_EQUAL, rhs, "myconstraint2");
					i2Index++;
				}
			}
			i2Index = 0;
			i1Index++;
		}
	}

	// The assignments have already been chosen by calling grbmodel.optimize(),
	// this sets the assignments them in such a way that the matching can be
	// assessed for stability as well as being printable
	public void setStudentAssignments(Algorithm a) throws GRBException {

		// set the student assignments
		for (int x = 0; x < a.assignedStudents.size(); x++) {
			Student s = a.assignedStudents.get(x);
			int prefLength = s.preferenceList.size();
			boolean matched = false;
			// for every preference of current student
			for (int projInd = 0; projInd < prefLength; projInd++) {
				double resultPref = s.stuProPairs.get(projInd).get(GRB.DoubleAttr.X);
				if (resultPref > 0.5) { // if a student project pair variable is one, the student has been assignedthat project
					s.proj = s.preferenceList.get(projInd);
					matched = true;
					s.proj.unpromoted.add(s);
					s.proj.lecturer.assigned++;
				}
			}
			if (!matched) {
				s.proj = emptyProject;
			}
		}
	}

	// uses polymorphism to printMatching in a customised way for the ip model.
	@Override
	public void printMatching() {

		System.out.println("PRINTING CONSTRAINT MATCHING");

		for (Student s : assignedStudents) {
			if (s.proj != emptyProject) {
				System.out.println(s.name + " " + s.proj.name);
			}
		}

		System.out.println(sizeOfMatching() + " students were matched");
	}
}
