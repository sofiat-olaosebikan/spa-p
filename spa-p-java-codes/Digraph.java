import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class Digraph {

	// keys are the student with the project, values are the student that prefer this project to their own
	private Map<Student, ArrayList<Student>> preferThisStudentsProject = new HashMap<Student, ArrayList<Student>>();

	// adds vertices for each student
	public void add(Student student) {
		if (preferThisStudentsProject.containsKey(student))
			return;
		preferThisStudentsProject.put(student, new ArrayList<Student>());
	}

	// adds edges between students
	public void add(Student from, Student to) {
		// Adds the student if they haven't been added before
		this.add(to);
		preferThisStudentsProject.get(to).add(from);
	}

	// Sets the in degree for each vertex
	public Map<Student, Integer> inDegree() {
		Map<Student, Integer> inDegreeMap = new HashMap<Student, Integer>();
		for (Student s : preferThisStudentsProject.keySet())
			inDegreeMap.put(s, 0); // All in-degrees are 0 initially
		for (Student from : preferThisStudentsProject.keySet()) { // For every student
			for (Student to : preferThisStudentsProject.get(from)) { // Find out who prefers this project and increment their in degree by one
				inDegreeMap.put(to, inDegreeMap.get(to) + 1); // Increment in-degree
			}
		}
		return inDegreeMap;
	}

	public List<Student> topSort() {
		Map<Student, Integer> degree = inDegree();
		// Determine all vertices with zero in-degree and add them to stack
		Stack<Student> zeroVerts = new Stack<Student>(); 
		for (Student s : degree.keySet()) {
			if (degree.get(s) == 0)
				zeroVerts.push(s);
		}
		// Determine the topological order
		List<Student> result = new ArrayList<Student>(); // Result contains vertices that have been found to be a source in the top sort
		while (!zeroVerts.isEmpty()) {
			Student s = zeroVerts.pop(); // Choose a vertex with zero in-degree
			result.add(s); // Vertex v is next in topol order
			// "Remove" vertex v by updating its neighbors
			for (Student studentPrefersThisProject : preferThisStudentsProject.get(s)) {
				degree.put(studentPrefersThisProject, degree.get(studentPrefersThisProject) - 1);
				// push any vertices that now have zero in-degree to the stack
				if (degree.get(studentPrefersThisProject) == 0)
					zeroVerts.push(studentPrefersThisProject);
			}
		}
		// Check that we have used the entire graph (if not, there was a cycle)
		if (result.size() != preferThisStudentsProject.size()) {
			return null;
		}
		return result;
	}

	public boolean doesNotContainCycle() {
		return topSort() != null;
	}

}
