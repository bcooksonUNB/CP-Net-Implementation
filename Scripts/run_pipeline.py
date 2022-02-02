from pattern_mining_v3 import runMain as pat_mine
from conditional_pattern_counting import runMain as pat_count
from finalize_patterns import runMain as finalize
from generate_final_input import runMain as genCPNetInput
import sys
import os

UNCONDITIONAL_SUPPORT = 0.4
CONDITIONAL_SUPPORT = 0.4
MAX_PATTERN_LENGTH = 2

if len(sys.argv) < 2:
    raise Exception("Input a directory name for output")
dir_name = sys.argv[1]
os.mkdir("../Output/{0}".format(dir_name))

pat_mine(dir_name, UNCONDITIONAL_SUPPORT, MAX_PATTERN_LENGTH)
pat_count(dir_name, CONDITIONAL_SUPPORT)
finalize(dir_name)
genCPNetInput(dir_name)
