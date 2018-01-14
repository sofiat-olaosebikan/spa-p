===== The Python codes in this directory were implemented by me (Please feel free to get in touch if you need any clarification) =====
|
The directory contains an implementation of the 3/2-approximation algorithm and 2-approximation algorithm, for the problem of finding a maximum cardinality stable matching,
given an instance of the Student-Project Allocation problem with lecturer preferences over Projects (SPA-P).

ip-spa-p.ipynb --------------- a jupyter notebook, containing an implementation of the IP model
spa-p-instance-generator.py--- a random SPA-P instance generator. It takes as input from the command line the following, seperated by space:
------------------------------ python3 spa-p-instance-generator.py <number_of_students> <min_pref_list_length> <max_pref_list_length> <output_file.txt>
============================= Suppose n1 is the number of students provided, the following are obtained from n1 and can be amended within the code:
============================= number_of_project = 0.5 * n1
============================= number_of_lecturers = 0.2 * n1  
============================= total_project_capacity = 1.1 * n1 

