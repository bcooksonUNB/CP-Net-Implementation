from pattern_mining_v3 import runMain as pat_mine
#from conditional_pattern_counting import runMain as pat_count
from conditional_pattern_counting_v2 import runMain as pat_count
from finalize_patterns import runMain as finalize
from generate_final_input import runMain as genCPNetInput
from data_randomizer import runMain as data_randomize
from parse_into_patterns import runMain as patternParse
import sys
import os

UNCONDITIONAL_SUPPORT = 0.75
CONDITIONAL_SUPPORT = 0.75
MAX_PATTERN_LENGTH = 1

if len(sys.argv) < 2:
    raise Exception("Input a directory name for output")
dir_name = sys.argv[1]
os.mkdir("../Output/{0}".format(dir_name))

#data_randomize()
pat_mine(dir_name, UNCONDITIONAL_SUPPORT, MAX_PATTERN_LENGTH)
pat_count(dir_name, CONDITIONAL_SUPPORT, remove_zeros=False)
finalize(dir_name)
genCPNetInput(dir_name)
patternParse(dir_name)
