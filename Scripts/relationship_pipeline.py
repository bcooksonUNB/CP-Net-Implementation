from pattern_mining_v3 import runMain as pat_mine

from find_relationships import runMain as find_rels

from partition_data import runMain as partition_data

import os
#from pattern_mining_v4 import runMain as pat_mine
#from parse_into_patterns_v2 import runMain as patternParse

# UNCONDITIONAL_SUPPORT = 0.4
# CONDITIONAL_SUPPORT = 0.4
#UNCONDITIONAL_SUPPORT_LIST = [70,65,60,55,50,45,40,35,30,25,20,15,10,5]
#UNCONDITIONAL_SUPPORT_LIST = [70,60,50,40,30,20,10]
UNCONDITIONAL_SUPPORT_LIST = [70,60,50,40,30,20,10]
#CONDITIONAL_SUPPORT_LIST = [80,70,60,50,40,30,20,10]
CONDITIONAL_SUPPORT_LIST = [80,70,60,50,40,30,20,10]
MAX_PATTERN_LENGTH = 1

# if len(sys.argv) < 2:
#     raise Exception("Input a directory name for output")
#dir_name = sys.argv[1]

for s in UNCONDITIONAL_SUPPORT_LIST:
    pre_inputs = [[None,None] for x in range(5)]
    ratios = [0.824 for x in range(5)]
    for c in CONDITIONAL_SUPPORT_LIST:
        dir_name = "s" + str(s) + "c" + str(c) + "l" + str(MAX_PATTERN_LENGTH) + "_cycle1_method1"
        print("Starting " + dir_name)
        final_ratio_map = {}
        final_no_chi_map = {}
        final_total = 0
        for i in range(5):
            new_dir_name = dir_name + "_partition_" + str(i)
            os.mkdir("../Output/{0}".format(new_dir_name))
            partition_data(i)
            filePatternList, pre_patterns, ratio = pat_mine(new_dir_name, (float)(s)/100.0, MAX_PATTERN_LENGTH, DEBUG=False, pre_patterns=pre_inputs[i][1], pre_filePatternList=pre_inputs[i][0], pre_ratios=ratios[i])
            pre_inputs[i][0] = filePatternList
            pre_inputs[i][1] = pre_patterns
            ratios[i] = ratio
            map1, map2, total = find_rels(new_dir_name, (float)(c)/100.0, remove_zeros=True, DEBUG=False, filePatternList=filePatternList, ratio=ratios[i])
            final_total += total
            for rat in map1:
                final_ratio_map[rat] = final_ratio_map.get(rat,0) + map1[rat]
            for rat in map2:
                final_no_chi_map[rat] = final_no_chi_map.get(rat,0) + map2[rat]

        w = open("../Output/category_types_{0}_{1}.csv".format(s,c),"x")
        final_ratio_map = {x:final_ratio_map[x]/final_total for x in final_ratio_map}
        top_ten = [(x,final_ratio_map[x]) for x in sorted(final_ratio_map.keys(),key=lambda y: final_ratio_map[y], reverse=True)]
        for x in top_ten: 
            #print(x)
            w.write(str(x) + "\n")
        w.write("\n")
        final_no_chi_map = {x:final_no_chi_map[x]/final_total for x in final_no_chi_map}
        top_ten = [(x,final_no_chi_map[x]) for x in sorted(final_no_chi_map.keys(),key=lambda y: final_no_chi_map[y], reverse=True)]
        for x in top_ten: 
            #print(x)
            w.write(str(x) + "\n")
        w.close()
