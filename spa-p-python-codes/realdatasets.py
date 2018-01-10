import random

def rewrite(input, lecturer_preference):
    lecturer_information = {}
    project_information = {}
    with open(input) as f:
        f = f.readlines()
        variables = f[0].strip().split()
        students, projects, lecturers = variables[0], variables[1], variables[2]
        #print(students, projects, lecturers)
        # write new representation to another file
        output = input[:2]+lecturer_preference+'.txt'
        with open(output, 'w') as O:
            # write first line -- #students, #project, #lecturers
            O.writelines('%s ' % p for p in variables)
            O.write('\n')
            for line in f[1:int(students)+1]:
                line = line.replace(':', '').split()
                # we want to know the number of students who finds each project acceptable
                for project in line[1:]:
                    project_information[project] = project_information.get(project, 0) + 1
                O.writelines('%s ' % p for p in line)
                O.write('\n')

            # we deal with rewriting the project information - ensuring project that has no lecturer is assigned to a random lecturer (could be any lecturer)
            for line in f[int(students)+1: int(students)+int(projects)+1]:
                line = line.replace(':', '').split()

                # if no student finds the project acceptable, set its count to 0
                if line[0] not in project_information:
                    project_information[line[0]] = 0


                # # if the lecturer offering the project is -1, randomly assign the project to any lecturer
                # if line[-1] == '-1':
                #     line[-1] = random.randint(1, int(lecturers))


                if line[-1] not in lecturer_information:
                    lecturer_information[line[-1]] = []
                # wrt each lecturer, keep track of projects and number of students that finds the project acceptable
                lecturer_information[line[-1]].append((project_information[line[0]], line[0]))
                O.writelines('%s ' % p for p in line)
                O.write('\n')

            # now we deal with lecturer information - this is the manufactured bit
            for line in f[int(projects) + int(students) + 1: int(students) + int(projects) + int(lecturers) + 1]:
                line = line.replace(':', '').split()

                # if no student finds the lecturer acceptable, set its list to empty
                if line[0] not in lecturer_information:
                    lecturer_information[line[0]] = []
                #print(line)
                lecturer = line[0]
                O.write(lecturer + ' ' + line[-1] + ' ')

                information = lecturer_information[lecturer][::]

                information.sort()
                least_preferred = information
                most_preferred = least_preferred[::-1]

                l = lecturer_information[lecturer][::]
                random.shuffle(l)

                if lecturer_preference == 'most_preferred':
                    O.writelines('%s ' % p[1] for p in most_preferred)
                    O.write('\n')
                elif lecturer_preference == 'least_preferred':
                    O.writelines('%s ' % p[1] for p in least_preferred)
                    O.write('\n')
                else:
                    O.writelines('%s ' % p[1] for p in l)
                    O.write('\n')




# in1 = '14old.txt'
in2 = '15old.txt'
# in3 = '16old.txt'
# in4 = '17old.txt'

# rewrite(in1, 'most_preferred')
# rewrite(in1, 'least_preferred')
# rewrite(in1, 'randomised')

rewrite(in2, 'most_preferred')
rewrite(in2, 'least_preferred')
rewrite(in2, 'randomised')

# rewrite(in3, 'most_preferred')
# rewrite(in3, 'least_preferred')
# rewrite(in3, 'randomised')
#
# rewrite(in4, 'most_preferred')
# rewrite(in4, 'least_preferred')
# rewrite(in4, 'randomised')
#

