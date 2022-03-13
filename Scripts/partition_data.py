import os
import shutil
import math
import random
import sys
import time

def runMain(runNum):
    for i in os.listdir("../Input/training"):
        os.remove("../Input/training/"+i)

    for i in os.listdir("../Input/testing"):
        os.remove("../Input/testing/"+i)

    file_name_list = os.listdir("../Input/alldata")

    split = 0.2

    mal_list = [x for x in file_name_list if "malicious" in x]
    ben_list = [x for x in file_name_list if "benign" in x]

    mal_training = []
    mal_testing = []
    ben_training = []
    ben_testing = []

    lim = math.ceil(len(mal_list)*split)
    for i in range(lim*runNum,lim*runNum+lim):
        if i >= len(mal_list): break
        mal_testing.append(mal_list[i])
    mal_training = [x for x in mal_list if x not in mal_testing]

    lim = math.ceil(len(ben_list)*split)
    for i in range(lim*runNum,lim*runNum+lim):
        if i >= len(ben_list): break 
        ben_testing.append(ben_list[i])
    ben_training = [x for x in ben_list if x not in ben_testing]

    for f in mal_training:
        shutil.copy("../Input/alldata/"+f,"../Input/training")

    for f in ben_training:
        shutil.copy("../Input/alldata/"+f,"../Input/training")

    for f in mal_testing:
        shutil.copy("../Input/alldata/"+f,"../Input/testing")

    for f in ben_testing:
        shutil.copy("../Input/alldata/"+f,"../Input/testing")

if __name__ == "__main__":
    runMain()