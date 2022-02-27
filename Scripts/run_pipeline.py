from pattern_mining_v3 import runMain as pat_mine
#from conditional_pattern_counting import runMain as pat_count
from conditional_pattern_counting_v2 import runMain as pat_count
#from Scripts.conditional_pattern_counting_v3 import runMain as pat_count
from finalize_patterns import runMain as finalize
from generate_final_input import runMain as genCPNetInput
from data_randomizer import runMain as data_randomize
from partition_data import runMain as partition_data
from parse_into_patterns import runMain as patternParse
import sys
import os

# UNCONDITIONAL_SUPPORT = 0.4
# CONDITIONAL_SUPPORT = 0.4
#UNCONDITIONAL_SUPPORT_LIST = [70,65,60,55,50,45,40,35,30,25,20,15,10,5]
UNCONDITIONAL_SUPPORT_LIST = [35,30,25,20,15,10,5]
CONDITIONAL_SUPPORT_LIST = [80,75,70,65,60,55,50,45,40,35,30,25,20,15,10,5]
MAX_PATTERN_LENGTH = 1

# if len(sys.argv) < 2:
#     raise Exception("Input a directory name for output")
#dir_name = sys.argv[1]

for s in UNCONDITIONAL_SUPPORT_LIST:
    for c in CONDITIONAL_SUPPORT_LIST:
        dir_name = "s" + str(s) + "c" + str(c) + "l" + str(MAX_PATTERN_LENGTH)
        print("Starting " + dir_name)
        for i in range(5):
            new_dir_name = dir_name + "_partition_" + str(i)
            os.mkdir("../Output/{0}".format(new_dir_name))
            partition_data(i)
            pat_mine(new_dir_name, (float)(s)/100.0, MAX_PATTERN_LENGTH, DEBUG=False)
            pat_count(new_dir_name, (float)(c)/100.0, remove_zeros=True, DEBUG=False)
            finalize(new_dir_name)
            genCPNetInput(new_dir_name)
            patternParse(new_dir_name)
            print("Complete Partition " + str(i))
