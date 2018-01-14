import matplotlib.pyplot as plt

def get_average_sm_size(merged_result, algorithm_type):
    # with respect to the algorithm run we are interested in, we stick to a single row in the merged results..
    if algorithm_type == '2-approx':
        index = 1
    elif algorithm_type == '3-2-approx':
        index = 3
    elif algorithm_type == 'ip':
        index = 5
    elif algorithm_type == '2-approx-100':
        index = 7
    elif algorithm_type == '3-2-approx-100':
        index = 9
    total_sm_size = 0
    total_solution_time = 0
    with open(merged_result) as f:
        f = f.readlines()
        for line in f:
            line = line.strip().split()
            total_sm_size += int(line[index])
            total_solution_time += float(line[index+1])

    # get the average size of the stable matchings produced and time to obtain the solution
    average_sm_size = int(total_sm_size/1000)
    average_solution_time = float(total_solution_time/1000)
    return average_sm_size, average_solution_time

def experiment1(path_to_file):

    ratio_2_approx = []
    ratio_3_2_approx = []
    ratio_2_approx_100 = []
    ratio_3_2_approx_100 = []

    solution_time_ip = []
    solution_time_2_approx = []
    solution_time_3_2_approx = []



    all_sm_sizes_ip = [] # get the average size of the stable matching obtained with respect to each instance size
    all_instance_size = []
    for instance_size in range(100, 2600, 100):
        all_instance_size.append(instance_size)
        merged_result = path_to_file + str(instance_size) + '/results/merged.txt'
        average_sm_size_ip, average_solution_time_ip = get_average_sm_size(merged_result, 'ip')
        all_sm_sizes_ip.append(average_sm_size_ip)
        solution_time_ip.append(average_solution_time_ip)

        # now use the optimal solution to find the ratio with respect to the size obtained from each approx runs..
        average_sm_size_2_approx, average_solution_time_2 = get_average_sm_size(merged_result, '2-approx')
        average_sm_size_3_2_approx, average_solution_time_3_2 = get_average_sm_size(merged_result, '3-2-approx')
        average_sm_size_2_approx_100, average_solution_time_2_approx = get_average_sm_size(merged_result, '2-approx-100')
        average_sm_size_3_2_approx_100, average_solution_time_3_2_approx = get_average_sm_size(merged_result, '3-2-approx-100')

        ratio_2_approx.append(float(average_sm_size_2_approx / average_sm_size_ip))
        ratio_3_2_approx.append(float(average_sm_size_3_2_approx / average_sm_size_ip))
        ratio_2_approx_100.append(float(average_sm_size_2_approx_100 / average_sm_size_ip))
        ratio_3_2_approx_100.append(float(average_sm_size_3_2_approx_100 / average_sm_size_ip))

        solution_time_2_approx.append(average_solution_time_2_approx)
        solution_time_3_2_approx.append(average_solution_time_3_2_approx)

    plt.figure(figsize=(6,6))
    plt.plot(all_instance_size, [1 for _ in range(len(all_sm_sizes_ip))], color='k', label='optimal solution')
    plt.plot(all_instance_size, ratio_2_approx, linestyle='--', marker='.', label='single-run-2-approx')
    plt.plot(all_instance_size, ratio_2_approx_100, linestyle='-.', marker='v', label='100-runs-2-approx')

    plt.plot(all_instance_size, ratio_3_2_approx, linestyle=':', marker='x', label='single-run-3-2-approx')
    plt.plot(all_instance_size, ratio_3_2_approx_100, marker='o', label='100-runs-3-2-approx')

    plt.xlim(100, 2500)
    plt.ylim(0.925, 1)
    plt.xlabel('Number of Students')
    plt.ylabel('Approximate Solution')

    plt.title('Ratio of the average size of a stable matching \n with respect to the optimal solution')
    plt.legend(loc='best')
    plt.savefig('experiment1_ratio.png')


    # time plot
    plt.figure(figsize=(6, 6))
    plt.plot(all_instance_size, solution_time_ip,  label="ip model")
    plt.plot(all_instance_size, solution_time_2_approx, linestyle='--', label="2-approx")
    plt.plot(all_instance_size, solution_time_3_2_approx, linestyle='-.', label="3-2-approx")
    plt.xlim(100, 2500)
    plt.xlabel('Number of Students')
    plt.ylabel('Time(s)')
    # plt.title('Average time to find a maximum cardinality matching \n')
    plt.legend(loc='best')
    plt.savefig('experiment1_time.png')






def experiment2(path_to_file, instance_size):

    ratio_2_approx = []
    ratio_3_2_approx = []
    ratio_2_approx_100 = []
    ratio_3_2_approx_100 = []

    solution_time_ip = []
    solution_time_2_approx = []
    solution_time_3_2_approx = []

    all_sm_sizes_ip = [] # get the average size of the stable matching obtained with respect to each instance size
    all_length = []
    for length in range(2, 11):
        all_length.append(length)
        merged_result = path_to_file + str(instance_size) + '/length'+ str(length) + '/results/merged.txt'
        average_sm_size_ip, average_solution_time_ip = get_average_sm_size(merged_result, 'ip')
        all_sm_sizes_ip.append(average_sm_size_ip)
        solution_time_ip.append(average_solution_time_ip)

        # now use the optimal solution to find the ratio with respect to the size obtained from each approx runs..
        average_sm_size_2_approx, average_solution_time_2 = get_average_sm_size(merged_result, '2-approx')
        average_sm_size_3_2_approx, average_solution_time_3_2 = get_average_sm_size(merged_result, '3-2-approx')
        average_sm_size_2_approx_100, average_solution_time_2_approx = get_average_sm_size(merged_result, '2-approx-100')
        average_sm_size_3_2_approx_100, average_solution_time_3_2_approx = get_average_sm_size(merged_result, '3-2-approx-100')


        ratio_2_approx.append(float(average_sm_size_2_approx / average_sm_size_ip))
        ratio_3_2_approx.append(float(average_sm_size_3_2_approx / average_sm_size_ip))
        ratio_2_approx_100.append(float(average_sm_size_2_approx_100 / average_sm_size_ip))
        ratio_3_2_approx_100.append(float(average_sm_size_3_2_approx_100 / average_sm_size_ip))

        solution_time_2_approx.append(average_solution_time_2_approx)
        solution_time_3_2_approx.append(average_solution_time_3_2_approx)

    plt.figure(figsize=(6,6))
    plt.plot(all_length, [1 for _ in range(len(all_sm_sizes_ip))], color='k', label='optimal solution')
    plt.plot(all_length, ratio_2_approx, linestyle='--', marker='.', label='single-run-2-approx')
    plt.plot(all_length, ratio_2_approx_100, linestyle='-.', marker='v', label='100-runs-2-approx')

    plt.plot(all_length, ratio_3_2_approx, linestyle=':', marker='x', label='single-run-3-2-approx')
    plt.plot(all_length, ratio_3_2_approx_100, marker='o', label='100-runs-3-2-approx')

    plt.xlim(2, 10)
    plt.ylim(0.925, 1)
    plt.xlabel('Preference List Length')
    plt.ylabel('Approximate Solution')

    plt.title('Ratio of the average size of a stable matching \n with respect to the optimal solution')
    plt.legend(loc='best')
    plt.savefig('experiment2_ratio.png')

    # time plot
    plt.figure(figsize=(6, 6))
    plt.plot(all_length, solution_time_ip, label="ip model")
    plt.plot(all_length, solution_time_2_approx, linestyle='--', label="2-approx")
    plt.plot(all_length, solution_time_3_2_approx, linestyle='-.', label="3-2-approx")
    plt.xlim(2, 10)
    plt.xlabel('Preference List Length')
    plt.ylabel('Time(s)')
    # plt.title('Average time to find a maximum cardinality matching \n ')
    plt.legend(loc='best')
    plt.savefig('experiment2_time.png')


path_to_file1 = '/home/sofiat/Documents/Glasgow/research/spa-p/spa-p-isco-2018/spa-p-experiments/experiment1/'
experiment1(path_to_file1)

path_to_file2 = '/home/sofiat/Documents/Glasgow/research/spa-p/spa-p-isco-2018/spa-p-experiments/experiment2/'
experiment2(path_to_file2, '1000')











