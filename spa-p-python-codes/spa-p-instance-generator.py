import os
import sys
import random
import math



class SPAPIG:
    def __init__(self, students, lower_bound, upper_bound):

        self.students = students
        self.projects = int(math.ceil(0.5*self.students))
        self.lecturers = int(math.ceil(0.2*self.students))  # assume number of lecturers <= number of projects
        self.tpc = int(math.ceil(1.1*self.students))  # assume total project capacity >= number of projects #
        #self.tlc = int(math.ceil(1.2*self.students))  # assume total lecturer capacity >= number of lecturers
        self.li = lower_bound  # lower bound of the student's preference list
        self.lj = upper_bound  # int(sys.argv[3])  # upper bound of the student's preference list

        self.sp = {}
        self.plc = {}
        self.lp = {}

    def spa_p_random_instance_generator(self):
        """
        A program that generates a random instance for the student project allocation problem with preferences over projects.
        :return: a random instance of SPA-P

        This program writes a random instance of the Student-Project Allocation problem with preferences over projects to a text file.
        It takes argument as follows:
            number of students
            lower bound of the student's preference list
            upper bound of the student's preference list
            #the file name to write the instance with a .txt extension.            
        """
        # -----------------------------------------------------------------------------------------------------------------------------------------
        # ---------------------------------------        ====== PROJECTS =======                    -----------------------------------------------
        # -----------------------------------------------------------------------------------------------------------------------------------------
        # projects have [at least capacity 1, empty string to assign lecturer]
        self.plc = {'p'+str(i): [1, ''] for i in range(1, self.projects+1)}
        project_list = list(self.plc.keys())
        # randomly assign the remaining project capacities
        for i in range(self.tpc - self.projects):  # range(9 - 8) = range(1) = 1 iteration. Okay!
            self.plc[random.choice(project_list)][0] += 1
        # -----------------------------------------------------------------------------------------------------------------------------------------
        # ---------------------------------------        ====== STUDENTS =======                    -----------------------------------------------
        # -----------------------------------------------------------------------------------------------------------------------------------------
        self.sp = {'s' + str(i): [] for i in range(1, self.students + 1)}  # stores randomly selected projects
        for student in self.sp:
            length = random.randint(self.li, self.lj)  # randomly decide the length of each student's preference list
            #  based on the length of their preference list, we provide projects at random
            projects_copy = project_list[:]
            for i in range(length):
                p = random.choice(projects_copy)
                projects_copy.remove(p)  # I did this to avoid picking the same project 2x. This could also be achieved by shuffling and popping?
                self.sp[student].append(p)
                

        # -----------------------------------------------------------------------------------------------------------------------------------------
        # ---------------------------------------        ====== LECTURERS =======                    ----------------------------------------------
        # -----------------------------------------------------------------------------------------------------------------------------------------
        # lecturers have [capacity set to 0, empty list to store projects, max c_j: p_j \in P_K, \sum_{p_j \in P_k} c_j]
        self.lp = {'l' + str(i): [0, [], 0, 0] for i in range(1, self.lecturers + 1)}  # we assign l1:[p1], l2:[p2], ..., l30:[p30]
        lecturer_list = list(self.lp.keys())
        upper_bound = math.floor(self.projects / self.lecturers)
        projects_copy = project_list[:]  # deep copy all the projects
        for lecturer in self.lp:
            # the number of projects a lecturer can offer is firstly bounded below by 1 and above by ceil(total_projects/total_lecturers)
            # to ensure projects are evenly distributed among lecturers
            number_of_projects = random.randint(1, upper_bound)
            for i in range(number_of_projects):
                random.shuffle(projects_copy) # I did this to skew the index of the project at the tail of the list                
                p = projects_copy.pop()  # before actually removing the project from end of the list
                self.plc[p][1] = lecturer  # take note of the lecturer who is offering the project
                self.lp[lecturer][1].append(p)
                #self.lp[lecturer][2].extend(self.plc[p][2])  # keep track of students who have chosen this project for the lecturer
                self.lp[lecturer][3] += self.plc[p][0]  # increment the total project capacity for each lecturer
                if self.plc[p][0] > self.lp[lecturer][2]:  # keep track of the project with the highest capacity
                    self.lp[lecturer][2] = self.plc[p][0]
        # -----------------------------------------------------------------------------------------------------------------------------------------
        # if at this point some projects are still yet to be assigned to a lecturer
        while projects_copy:
            p = projects_copy.pop()  # remove a project from end of the list
            lecturer = random.choice(lecturer_list)  # pick a lecturer at random
            self.plc[p][1] = lecturer  # take note of the lecturer who is offering the project
            self.lp[lecturer][1].append(p)
            #self.lp[lecturer][2].extend(self.plc[p][2])  # keep track of students who have chosen this project for the lecturer
            self.lp[lecturer][3] += self.plc[p][0]  # increment the total project capacity for each lecturer
            if self.plc[p][0] > self.lp[lecturer][2]:
                self.lp[lecturer][2] = self.plc[p][0]
        # -----------------------------------------------------------------------------------------------------------------------------------------
        #  Now we decide the ordered preference for each lecturer. 
        # capacity for each lecturer can also be decided here..
        for lecturer in self.lp:
            #self.lp[lecturer][2] = list(set(self.lp[lecturer][2]))
            random.shuffle(self.lp[lecturer][1])  # this line shuffles the projects offered by each lecturer. Hence strictly ordered according to preference.
            self.lp[lecturer][0] = random.randint(self.lp[lecturer][2], self.lp[lecturer][3])  # capacity for each lecturer

        # -----------------------------------------------------------------------------------------------------------------------------------------

  
    def write_instance(self, filename):  # writes the SPA-P instance to a txt file

        if __name__ == '__main__':
            with open(filename, 'w') as I:

                # ---------------------------------------------------------------------------------------------------------------------------------------
                #  ...write number of student (n) number of projects (m) number of lecturers (k) ---- for convenience, they are all separated by space
                I.write(str(self.students) + ' ' + str(self.projects) + ' ' + str(self.lecturers) + '\n')
                # ---------------------------------------------------------------------------------------------------------------------------------------

                # ---------------------------------------------------------------------------------------------------------------------------------------
                # .. write the students index and their corresponding preferences ---- 1 2 3 1 7
                for n in range(1, self.students + 1):
                    preference = self.sp['s'+str(n)]
                    sliced = [p[1:] for p in preference]
                    I.write(str(n) + ' ')
                    I.writelines('%s ' % p for p in sliced)
                    I.write('\n')
                # ---------------------------------------------------------------------------------------------------------------------------------------

                # ---------------------------------------------------------------------------------------------------------------------------------------
                #  ..write the projects index, its capacity and the lecturer who proposed it ------- 1 5 1
                for m in range(1, self.projects + 1):
                    project = 'p'+str(m)
                    capacity = self.plc[project][0]
                    lecturer = self.plc[project][1][1:]
                    I.write(str(m) + ' ' + str(capacity) + ' ' + str(lecturer))
                    I.write('\n')
                # ---------------------------------------------------------------------------------------------------------------------------------------

                # ---------------------------------------------------------------------------------------------------------------------------------------
                # .. write the lecturers index, their capacity and their corresponding preferences ---- 1 2 3 1 7
                for k in range(1, self.lecturers + 1):
                    lecturer = 'l'+str(k)
                    capacity = self.lp[lecturer][0]
                    preference = self.lp[lecturer][1]
                    sliced = [p[1:] for p in preference]
                    I.write(str(k) + ' ' + str(capacity) + ' ')
                    I.writelines('%s ' % p for p in sliced)
                    I.write('\n')
                # ---------------------------------------------------------------------------------------------------------------------------------------
                I.close()

   
students = int(sys.argv[1])
lower_bound = int(sys.argv[2])
upper_bound = int(sys.argv[3])
output_file = sys.argv[4]  # for example, instance.txt
S = SPAPIG(students, lower_bound, upper_bound)
S.spa_p_random_instance_generator()
S.write_instance(output_file)
