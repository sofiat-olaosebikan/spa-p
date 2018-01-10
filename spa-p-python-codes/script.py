# -*- coding: utf-8 -*-
"""
Spyder Editor

This temporary script file is located here:
/home/maryam/.spyder2/.temp.py
"""

def clean(inputfile, outputfile):
    with open(outputfile, 'w') as O:
        with open(inputfile) as f:
            f = f.readlines()
            num_students = int(f[0].strip())
            num_projects = int(f[1].strip())
            num_lecturers = int(f[2].strip())
            
            # write first line into cleaned output
            O.write(str(num_students) + ' ' + str(num_projects) + ' ' + str(num_lecturers))
            O.write('\n')
            
            # next extract student ordered preference list and store in a dictionary..
            # we know there are num_students students, so the next 3 + num_students lines are useless
            
            start = 2 + num_students + 1
            student_info = {}
            student_index = 1
            for student_pref in f[start : start+num_students]:
                #print(student_pref)
                student_info[student_index] = [p for p in student_pref.strip().split()]
                student_index += 1
            
            #print(len(student_info))
            #print(student_info[75])
            
            # extract project names according to order of appearance and assign index starting from 1, 2, .., num_projects
            project_info = {}
            project_list = []
            start_project = start+num_students
            project_index = 1
            for projects in f[start_project : start_project+num_projects]:
                project_list.append(projects.strip())
                project_info[projects.strip()] = project_index
                
                # assign project index to each project, and point to anonymised name and capacity
                project_info[project_index] = [projects.strip(), 1]
                project_index += 1
            
            #print(len(project_info))
            #print(project_info['4708'])
            #print(project_info['4467'])
            lecturer_info = {}
            start_lecturer = start_project+ (3*num_projects)
            lecturer_index = 1
            for lecturer in f[start_lecturer : start_lecturer + num_lecturers]:
                supervisor_name = lecturer.strip()
                supervisor_name = supervisor_name.replace('Superviosr', 'Supervisor')
                lecturer_info[supervisor_name] = lecturer_index
                
                # assign index to each lecturer and point to anonymised name
                lecturer_info[lecturer_index] = [supervisor_name]
                lecturer_index += 1
                #print(lecturer.strip())
            
            lecturer_index = 1
            for lecturer_capacity in f[start_lecturer+num_lecturers  : start_lecturer+(2*num_lecturers)]:
                d_k = int(lecturer_capacity.strip())
                if d_k == 99:
                    lecturer_info[lecturer_index].append(0)
                else:
                    # append each lecturer capacity to their index
                    lecturer_info[lecturer_index].append(int(lecturer_capacity.strip()))
                lecturer_index += 1
            
            #print(lecturer_info[num_lecturers])
            
            for i in range(len(project_list)):

                lecturer = f[start_lecturer+(2*num_lecturers)+i].strip()
                lecturer_index = lecturer_info[lecturer]

                project = project_list[i]
                project_index = project_info[project]

                # append lecturer offering what project to each project index
                project_info[project_index].append(lecturer_index)
            # print(project_info)
            #print(student_info)

            # write student information
            n = 1
            while n <= num_students:
                projects = student_info[n]
                indexed_project = [project_info[j] for j in projects]
                O.write(str(n)+': ')
                O.writelines('%s ' % k for k in indexed_project)
                O.write('\n')
                n += 1

            # write project information
            m = 1
            while m <= num_projects:
                O.write(str(m)+': 1 '+str(project_info[m][-1]))
                O.write('\n')
                m += 1


            # write lecturer information
            k = 1
            while k <= num_lecturers:
                O.write(str(k) + ': 1 ' + str(lecturer_info[k][-1]))
                O.write('\n')
                k += 1


clean('anon/15.txt', 'cleaned/15.txt')


