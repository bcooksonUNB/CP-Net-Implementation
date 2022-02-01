from pattern_mining_v3 import runMain as pat_mine
from conditional_pattern_counting import runMain as pat_count
import sys
import os

UNCONDITIONAL_SUPPORT = 0.60
CONDITIONAL_SUPPORT = 0.60
MAX_PATTERN_LENGTH = 3

if len(sys.argv) < 2:
    raise Exception("Input a directory name for output")
dir_name = sys.argv[1]
os.mkdir("../Output/{0}".format(dir_name))

pat_mine(dir_name, UNCONDITIONAL_SUPPORT, MAX_PATTERN_LENGTH)
pat_count(dir_name, CONDITIONAL_SUPPORT)
