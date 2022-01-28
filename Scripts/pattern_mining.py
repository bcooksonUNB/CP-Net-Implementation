import os
import math
DEBUG = True
MIN_SUPPORT = 0.99

file_name_list = os.listdir("csv-consec_identical_calls_removed")

fileList = []
triedList = []
successList = []

def get_combos(file):
    return_list = []
    if len(file) == 0:
        return []
    return_list = [[file[0]]]
    for i in range(1,len(file)):
        temp_list = []
        for l in return_list:
            temp_list.append(l)
        for l in temp_list:
            n = l + [file[i]]
            return_list.append(n)
    return return_list

def is_sequence_in_file(seq, file):
    current = 0
    for f in file:
        if f == seq[current]: current += 1
        if current == len(seq): return True
    return False


for name in file_name_list:
    f = open("csv-consec_identical_calls_removed/" + name)
    next_attribute = f.readline()
    l = []
    while next_attribute:
        next_attribute = next_attribute.strip("\n")
        l.append(next_attribute)
        next_attribute = f.readline()
    fileList.append(l)
    if DEBUG: print("Done Reading {0}".format(name))
    f.close()

TOTAL = len(fileList)
MIN_COUNT = math.ceil(TOTAL*MIN_SUPPORT)

for i in range(len(fileList)):
    file = fileList[i]
    l = []
    c = 0
    for att in file:
        print(c)
        temp_list = []
        for item in l:
            n = item + [att]
            if n not in triedList:
                temp_list.append(n)
        if [att] not in triedList:
            l.append([att])
        for item in temp_list:
            if item not in triedList:
                counter = 1
                for j in range(len(fileList)):
                    if j != i:
                        if is_sequence_in_file(item,fileList[j]): counter += 1
                if counter >= MIN_COUNT:
                    successList.append( (item,counter/TOTAL) )
                    l.append(item)
                triedList.append(item)
        c += 1
    print(successList)





