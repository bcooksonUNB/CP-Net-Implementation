import os
import shutil
import math
import random
import sys
import time

def runMain():
    for i in os.listdir("../Input/training"):
        os.remove("../Input/training/"+i)

    for i in os.listdir("../Input/testing"):
        os.remove("../Input/testing/"+i)

    file_name_list = os.listdir("../Input/alldata")

    split = 0.8

    mal_list = [x for x in file_name_list if "malicious" in x]
    ben_list = [x for x in file_name_list if "benign" in x]

    mal_training = []
    mal_testing = []
    ben_training = []
    ben_testing = []

    lim = math.ceil(len(mal_list)*split)
    for i in range(lim):
        r = random.randint(0,len(mal_list)-1)
        mal_training.append(mal_list[r])
        del[mal_list[r]]
    mal_testing = [x for x in mal_list]

    lim = math.ceil(len(ben_list)*split)
    for i in range(lim):
        r = random.randint(0,len(ben_list)-1)
        ben_training.append(ben_list[r])
        del[ben_list[r]]
    ben_testing = [x for x in ben_list]

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