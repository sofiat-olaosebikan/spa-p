# READ SPA-P INSTANCE FROM A TEXT FILE
class readSPAPInstance():
    def __init__(self):
        self.students = dict()
        self.projects = dict()
        self.lecturers = dict()

    def read(self, filename):
        """ reads an SPA-P instance from a text file, structured as follows

                n1 n2 n3
                next n1 lines lists the students<space>their ranked projects separated by space
                next n2 lines lists the projects<space>its capacity[space]the lecturer offering it
                last n3 lines lists the lecturers<space>their capacity[space]their ranked projects separated by space


                 where,
                    n1 --- number_of_students
                    n2 --- number_of_projects
                    n3 --- number_of_lecturers

            sample text file is ../test_instances/instance1.txt

            students =  {'s2': [['p1', 'p2'], {'p2': 0, 'p1': 0}], 's1': [['p3', 'p2', 'p1'], {'p1': 0, 'p2': 0, 'p3': 0}], 's3': [['p3'], {'p3': 0}]}
            projects = {'p2': [1, 'l1'], 'p1': [1, 'l1'], 'p3': [1, 'l2']}
            lecturers = {'l1': [2, ['p2', 'p1'], []], 'l2': [1, ['p3'], []]}
        """
        with open(filename) as I:
            I = I.readlines()
            object_count = I[0].strip().split()
            student_size, project_size, lecturer_size = object_count
            # ..reading the student's preference list..
            for l in I[1:int(student_size) + 1]:                
                line = l.strip().split()
                # .. key is student --> value is the ranked project, ordered using a list..
                self.students['s' + line[0]] = [['p' + i for i in line[1:]]]
                self.students['s' + line[0]].append({'p' + i: 0 for i in line[1:]})
                # .. reading the project's information ..
            for l in I[int(student_size) + 1:int(student_size) + int(project_size) + 1]:
                line = l.strip().split()
                # .. key is project --> value is [project_capacity, lecturer_offering_project]
                self.projects['p' + line[0]] = [int(line[1]), 'l' + line[2]]
                # to keep track of students assigned to a project from the matching obtained
                self.projects['p' + line[0]].append(0)
            # .. reading the lecturer's preference list
            for l in I[int(student_size) + int(project_size) + 1:]:
                line = l.strip().split()
                self.lecturers['l' + line[0]] = [int(line[1]), ['p' + i for i in line[2:]]]
                # to keep track of lecturer's non-empty project from the maximum matching obtained
                self.lecturers['l' + line[0]].append(0)
                self.lecturers['l' + line[0]].append(None)
# r = readSPAPInstance()         
# r.read("instance2.txt")
# print(r.students)
# print(r.projects)
# print(r.lecturers)
# ---------------- OUTPUT ----------------------- 
# {'s1': [['p1', 'p2', 'p3'], {'p2': 0, 'p3': 0, 'p1': 0}], 's4': [['p3', 'p1'], {'p3': 0, 'p1': 0}], 's3': [['p4', 'p3'], {'p3': 0, 'p4': 0}], 's2': [['p2', 'p1'], {'p1': 0, 'p2': 0}], 's5': [['p3', 'p5', 'p2'], {'p3': 0, 'p2': 0, 'p5': 0}], 's6': [['p5', 'p4'], {'p5': 0, 'p4': 0}]}
# {'p2': [1, 'l1', 0], 'p3': [1, 'l1', 0], 'p1': [2, 'l1', 0], 'p5': [1, 'l2', 0], 'p4': [1, 'l2', 0]}
# {'l2': [2, ['p4', 'p5'], 0, None], 'l1': [4, ['p1', 'p2', 'p3'], 0, None]}

#=============================================================================================================#
# ================================== IP MODEL IMPLEMENTATION STARTS HERE  ==================================  #
#=============================================================================================================#
import sys
from gurobipy import *
from time import *
         
class GurobiSPAP():
    def __init__(self, filename):
        self.filename = filename
        r = readSPAPInstance()
        r.read(self.filename)
        self.studentdict = r.students
        self.projectdict = r.projects
        self.lecturerdict = r.lecturers
        # Create a new Gurobi Model
        self.J = Model("SPAP")
        
    def assignmentConstraints(self):
        # Create variables
        #=============================================================================================================#
        # =============================================== CONSTRAINT 6 ===============================================#
        # ...for each acceptable (student, project) pair, we create the binary variable xij and impose constraint 6   #
        #=============================================================================================================#
        for student in self.studentdict:                        
            sumstudentvariables = LinExpr()
            for project in self.studentdict[student][0]:                
                # addVar(lb, ub, obj, vtype, name, column)
                xij = self.J.addVar(lb=0.0, ub=1.0, obj=0.0, vtype=GRB.BINARY, name=student + " is assigned " + project)            
                self.studentdict[student][1][project] = xij            
                sumstudentvariables += xij 
            # .. add constraint that a student can be assigned to at most one project
            # addConstr(lhs, sense, rhs, name)
            self.J.addConstr(sumstudentvariables <= 1, "Constraint for "+ student)
        #=============================================================================================================#


            
        #=============================================================================================================#
        # =============================================== CONSTRAINT 7 ===============================================#
        # we loop through each project and each student that finds this project acceptable
        # then increment the corresponding student-project variable (xij)
        #=============================================================================================================#
        for project in self.projectdict:
            totalprojectcapacity = LinExpr()
            for student in self.studentdict:
                if project in self.studentdict[student][0]:
                    totalprojectcapacity += self.studentdict[student][1][project]        
                    
            # .. next we create the constraint that ensures each project does not exceed its capacity
            ## ------------- debugging --------------------
            #print(totalprojectcapacity)
            #print(self.projectdict[project][0])
            self.J.addConstr(totalprojectcapacity <= self.projectdict[project][0], "Total capacity constraint for "+ project)
        #=============================================================================================================#
        
                
        
        #=============================================================================================================#
        # =============================================== CONSTRAINT 8 ===============================================#
        # loop through each lecturer and each acceptable (student,project) pairs
        # if for an acceptable pair, the project is offered by lecturer, then increment
        # the totallecturercapacity with the corresponding (student,project) variable (xij)
        #=============================================================================================================#
        for lecturer in self.lecturerdict:
            totallecturercapacity = LinExpr()
            for student in self.studentdict:
                for project in self.studentdict[student][0]:
                    if lecturer == self.projectdict[project][1]:
                        totallecturercapacity += self.studentdict[student][1][project]
            self.J.addConstr(totallecturercapacity <= self.lecturerdict[lecturer][0], "Total capacity constraint for "+ lecturer) 
        #=============================================================================================================#
        

        
        
    #|--------------------------------------------------------------------------------------------------------------------------|#
    #|                                                                                                                          |#
    #| For an arbitrary acceptable pair (s_i, p_j), we define all the relevant terms to ensure (s_i, p_j) does not block M..    |#
    #|                                                                                                                          |#
    #|--------------------------------------------------------------------------------------------------------------------------|#
    
    #=============================================================================================================#
    # we define thetaij :::::: if thetaij = 1, s_i is either unmatched in M or prefers p_j to M(s_i)
    #=============================================================================================================#
    def theta(self, student, project):
        thetaij = LinExpr()
        sumSij = LinExpr()
        studentPreference = self.studentdict[student][0]
        indexproject = studentPreference.index(project) # get the rank of project on student's preference list
        # now we loop through each project (p_j') that student likes as much as project (p_j), p_j inclusive
        for pjprime in studentPreference[:indexproject+1]:            
            sumSij += self.studentdict[student][1][pjprime]
        # sumSij = quicksum(self.studentdict[student][1][pjprime] for pjprime in studentPreference[:indexproject+1])        
        # thetaij += 1 - sumSij        
        thetaij.addConstant(1.0)
        thetaij.add(sumSij, -1)
        # print(G.theta('s1', 'p3')) --- to verify the validity of quicksum on sumSij ----- working.        
        # <gurobi.LinExpr: 1.0 + -1.0 s1 is assigned p3>
        return thetaij      
    #=============================================================================================================#
    
    
    #=============================================================================================================#
    # =============================================== CONSTRAINT 9 ===============================================#
    # we define alpha_j to be a binary variable that corresponds to the occupancy of p_j in M
    # if p_j is undersubscribed in M then we enforce alpha_j to be 1
    #=============================================================================================================#    
    def alpha(self, project):        
        alphaj = self.J.addVar(lb=0.0, ub=1.0, obj=0.0, vtype=GRB.BINARY, name=project+" is undersubscribed")                
        capacity = self.projectdict[project][0]           # c_j
        projectoccupancy = LinExpr()
        for student in self.studentdict:
            if project in self.studentdict[student][0]:
                projectoccupancy += self.studentdict[student][1][project]
        #projectoccupancy = quicksum(self.studentdict[student][1][project] for student in self.studentdict if project in self.studentdict[student][0])
        self.J.addConstr(capacity*alphaj >= (capacity - projectoccupancy), "constraint 9")
        return alphaj
    #=============================================================================================================#    
    
    
    #=============================================================================================================#    
    # For an acceptable pair (s_i, p_j) wrt l_k, we define gamma_{ijk} such that (s_i, p_j') \in M, and
    # s_i and l_k prefers p_j to p_j' 
    #=============================================================================================================#    
    def gamma(self, student, project):
        studentPreference = self.studentdict[student][0]                    # A_i
        lecturer = self.projectdict[project][1]                             # l_k
        lecturerPreference = self.lecturerdict[lecturer][1]                 # P_k
        # s_i prefers p_j to every other project in T_k,j
        indexpreferredproject = lecturerPreference.index(project)           # rank(l_k, p_j)
        Tkj = lecturerPreference[indexpreferredproject+1:]                  # {p_j' \in P_k: rank(l_k, p_j) < rank(l_k, p_j')}
        intersection = set(studentPreference).intersection(set(Tkj))        # projects that s_i has in common with l_k
        gammaijk = LinExpr()
        gammaijk = quicksum(self.studentdict[student][1][pjprime] for pjprime in intersection)
        return gammaijk
    #=============================================================================================================#    
    
    
    #=============================================================================================================#    
    # if s_i is assigned to a project offered by l_k, then betaik = 1
    #=============================================================================================================#    
    def beta(self,student,project):
        studentPreference = self.studentdict[student][0]                             # A_i
        lecturer = self.projectdict[project][1]                                      # l_k
        lecturerPreference = self.lecturerdict[lecturer][1]                          # P_k
        intersection = set(studentPreference).intersection(set(lecturerPreference))  
        betaik = LinExpr()        
        betaik = quicksum(self.studentdict[student][1][pjprime] for pjprime in intersection)
        return betaik
    #=============================================================================================================#    
    
    
    #=============================================================================================================#
    # =============================================== CONSTRAINT 11 ==============================================#
    # we create a binary variable deltak that corresponds to the occupancy of l_k in M. 
    # If l_k is undersubscribed in M, we enforce deltak = 1
    #=============================================================================================================#
    def delta(self,student,project):        
        lecturer = self.projectdict[project][1]              # l_k
        lecturercapacity = self.lecturerdict[lecturer][0]    # d_k
        lecturerPreference = self.lecturerdict[lecturer][1]  # P_k
        deltak = self.J.addVar(lb=0.0, ub=1.0, obj=0.0, vtype=GRB.BINARY, name= lecturer + " is undersubscribed")
        lectureroccupancy = LinExpr()
        for pjprime in lecturerPreference:
            for student in self.studentdict:
                if pjprime in self.studentdict[student][0]:
                    lectureroccupancy += self.studentdict[student][1][pjprime]
        #lectureroccupancy = quicksum(self.studentdict[student][1][pjprime] for student in self.studentdict for pjprime in lecturerPreference if pjprime in self.studentdict[student][0])                
        self.J.addConstr((lecturercapacity*deltak) >= (lecturercapacity - lectureroccupancy), "constraint 11")             
        return deltak
    #=============================================================================================================#
    
    
    #=============================================================================================================#
    # =============================================== CONSTRAINT 13 ==============================================#
    # For an acceptable pair (s_i, p_j), we create a binary variable etajk. 
    # If l_k is full in M and prefers p_j to its worst non-empty project, we enforce etajk = 1
    #=============================================================================================================#
    def eta(self,student,project):        
        lecturer = self.projectdict[project][1]              # l_k
        lecturercapacity = self.lecturerdict[lecturer][0]    # d_k
        lecturerPreference = self.lecturerdict[lecturer][1]  # P_k
        indexproject = lecturerPreference.index(project)     # rank(l_k, project) - 1 ::::: 0-based
        Dkj = lecturerPreference[:indexproject+1]            # {p_j' \in P_k: rank(l_k, p_j') <= rank(l_k, p_j)} -------- p_j inclusive
        etajk = self.J.addVar(lb=0.0, ub=1.0, obj=0.0, vtype=GRB.BINARY, name= lecturer+ " prefers " + project + "to his worst non-empty project")
        lectureroccupancy = LinExpr()        
        # lectureroccupancy = quicksum(self.studentdict[student][1][pjprime] for student in self.studentdict for pjprime in Dkj if pjprime in self.studentdict[student][0])                
        for pjprime in Dkj:
            for student in self.studentdict:                                
                if pjprime in self.studentdict[student][0]:
                    lectureroccupancy += self.studentdict[student][1][pjprime]
        self.J.addConstr((lecturercapacity*etajk) >= (lecturercapacity - lectureroccupancy), "constraint 13")             
        return etajk
    #=============================================================================================================#
    
    
    
    #=============================================================================================================#
    # =============================================== CONSTRAINT 10, 12 AND 14 ===================================#
    # we enforce constraints to avoid blocking pair of type 3a, 3b and 3c for each acceptable (student, project) pairs
    #=============================================================================================================#
    def avoidblockingpair(self):        
        # self.J.update()
        # for all acceptable (student, project) pairs
        # for better complexity ::: is there a way to only check those pairs that could block the final matching?
        for student in self.studentdict:             
            for project in self.studentdict[student][0]:
                thetaij = self.theta(student, project)
                alphaj = self.alpha(project)
                gammaijk = self.gamma(student, project)
                betaik = self.beta(student, project)
                deltak = self.delta(student, project)
                etajk = self.eta(student, project)
                ## ---- blocking pair 3a -----
                self.J.addConstr(thetaij + alphaj + gammaijk <= 2, "constraint 10 - avoid blocking pair 3a")
                ## ----- blocking pair 3b -----
                self.J.addConstr(thetaij + alphaj + (1 - betaik) + deltak <= 3, "constraint 12 - avoid blocking pair 3b")              
                ## ----- blocking pair 3c -----
                self.J.addConstr(thetaij + alphaj + (1 - betaik) + etajk <= 3, "constraint 14 - avoid blocking pair 3c")              
    #=============================================================================================================#
               
    
    
    #=============================================================================================================#
    # =============================================== CONSTRAINT 15 and 16 =======================================#
    # we enforce constraints to avoid a coalition, for each acceptable (student, project) pair
    #=============================================================================================================#
    def avoidcoalition(self):        
        for student in self.studentdict:
            label = self.J.addVar(lb=1.0, ub=len(self.studentdict), obj=0.0, vtype=GRB.INTEGER, name= "vertex label for " + student)
            self.studentdict[student].append(label)  # entries for vertex label ::: position 2
            
        for student1 in self.studentdict:
            self.studentdict[student1].append(dict())          # entries for envy arc values ::: position 3
            for student2 in self.studentdict:
                if student1 != student2:                    
                    # addVar(lb, ub, obj, vtype, name, column) -- we add variables for all e_i,i'                
                    # here we construct the envy variable                    
                    e12 = self.J.addVar(lb=0.0, ub=1.0, obj=0.0, vtype=GRB.BINARY, name= student1 + " envies " + student2)                    
                    self.studentdict[student1][3][student2] = e12
                    # now for every pjprime student1 prefers to pj such that student2 finds pjprime acceptable
                    student1Preference = self.studentdict[student1][0]  # Ai
                    student2Preference = self.studentdict[student2][0]  # Ai'                    
                    for pj in student1Preference:
                        indexpj = student1Preference.index(pj)
                        acceptableprojects = student1Preference[:indexpj]
                        intersection = set(acceptableprojects).intersection(set(student2Preference))
                        # add an envy arc ::: e12 + 1 >= x_ij + x_i'j'
                        for pjprime in intersection:                                                        
                            self.J.addConstr(self.studentdict[student1][3][student2] + 1 >= (self.studentdict[student1][1][pj]+self.studentdict[student2][1][pjprime]), "constraint 15 - construct the envy arc")  
                      
                    topologicalorderingLHS = LinExpr()       
                    topologicalorderingLHS.add(self.studentdict[student1][2], 1.0)  #### v_1
                    topologicalorderingRHS = LinExpr()      #### v_2                        
                    topologicalorderingRHS.add(self.studentdict[student2][2], 1.0)   # v_2 + n1(1 - e12)
                    self.J.addConstr(topologicalorderingLHS+1 <= topologicalorderingRHS+(len(self.studentdict)*(1-self.studentdict[student1][3][student2])), "constraint 16 - avoid coalition")
                    #supposed to be self.J.addConstr(topologicalorderingLHS < topologicalorderingRHS, "constraint 16")
                    #but because Gurobi can only handle constraints of type "==", ">=" and "<=" 
                    #source ----------- (https://groups.google.com/forum/#!topic/gurobi/sXM6WEciljk)
                    #I added 1 to the LHS and made use of less or equal sign.
                    
    #=============================================================================================================#
    # =============================================== CONSTRAINT 17 ==============================================#
    # we maximize the objective function
    #=============================================================================================================#
    def objfunctionConstraints(self):        
        # finally we add the objective function to maximise the number of matched student-project pairs
        Totalxijbinaryvariables = LinExpr()
        for student in self.studentdict:
            studentPreference = self.studentdict[student][0]
            for project in studentPreference:
                Totalxijbinaryvariables += self.studentdict[student][1][project]
        #Totalxijbinaryvariables = quicksum(self.studentdict[student][1][project] for student in self.studentdict for project in self.studentdict[student][1])
        #setObjective(expression, sense=None)
        self.J.setObjective(Totalxijbinaryvariables, GRB.MAXIMIZE) 

filename = sys.argv[1]        
G = GurobiSPAP(filename)
try:
    start_time = time()
    G.assignmentConstraints()    
    G.avoidblockingpair()    
    #G.avoidcoalition()
    G.objfunctionConstraints()     
    G.J.optimize()
    end_time = time() - start_time 
    v = int(G.J.objVal) # get objective value, which is exactly the size of a maximum stable matching
    print(filename, end=' ')
    print(str(end_time), end=' ')
    print(str(v))
    
except:
    print('Error reported')
