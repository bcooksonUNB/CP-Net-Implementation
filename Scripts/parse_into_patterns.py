import os
DEBUG = False

def get_one_less_sublists(l):
    if(len(l) <= 1): return []
    ret_list = []
    for i in range(len(l)):
        ret_list.append(tuple(l[x] for x in range(len(l)) if x != i))
    return ret_list

def is_sequence_in_file(seq, file):
    current = 0
    for f in file:
        if f == seq[current]: current += 1
        if current == len(seq): return True
    return False

def get_sequences_in_file(seqs,file):
    temp_seqs = [x for x in seqs]
    ret_list = []
    while(len(temp_seqs) > 0):
        cur_pattern = temp_seqs[0]
        sublists = get_one_less_sublists(cur_pattern)
        flag = True
        for l in sublists:
            if l not in ret_list:
                flag = False
                break
        if flag and is_sequence_in_file(cur_pattern,file):
            ret_list.append(cur_pattern)
        temp_seqs.remove(cur_pattern)
    return ret_list

#dir_name = "support30length1"

def runMain(dir_name, filePatternList=None):
    testing_name_list = os.listdir("../Input/testing")
    training_name_list = os.listdir("../Input/training")

    testing_attribute_list = {}
    testing_pattern_list = {x:[] for x in testing_name_list}

    training_attribute_list = {}
    training_pattern_list = {x:[] for x in testing_name_list}

    for name in testing_name_list:
        f = open("../Input/testing/" + name)
        next_attribute = f.readline()
        testing_attribute_list[name] = []
        while next_attribute: 
            next_attribute = next_attribute.strip()
            testing_attribute_list[name].append(next_attribute)
            next_attribute = f.readline()
        if DEBUG: print("Done Reading {0}".format(name))
        f.close()

    for name in training_name_list:
        f = open("../Input/training/" + name)
        next_attribute = f.readline()
        training_attribute_list[name] = []
        while next_attribute: 
            next_attribute = next_attribute.strip()
            training_attribute_list[name].append(next_attribute)
            next_attribute = f.readline()
        if DEBUG: print("Done Reading {0}".format(name))
        f.close()

    pattern_list = []
    f = open("../Output/{0}/final_pattern_list.csv".format(dir_name))
    next_pattern = f.readline()
    while next_pattern:
        next_pattern = next_pattern.strip()
        next_pattern = next_pattern.split(",")
        pattern_tuple = tuple(x.strip() for x in next_pattern)
        pattern_list.append(pattern_tuple)
        next_pattern = f.readline()
    f.close()
    

    for file in testing_name_list:
        testing_pattern_list[file] = get_sequences_in_file(pattern_list,testing_attribute_list[file])
        if DEBUG: print("Done finding patterns for " + file)

    if not filePatternList:
        for file in training_name_list:
            training_pattern_list[file] = get_sequences_in_file(pattern_list,training_attribute_list[file])
            if DEBUG: print("Done finding patterns for " + file)
    else:
        training_pattern_list = {x:filePatternList[x] for x in filePatternList}

    os.mkdir("../Output/{0}/testingdata".format(dir_name))
    os.mkdir("../Output/{0}/testingdata/training".format(dir_name))
    os.mkdir("../Output/{0}/testingdata/testing".format(dir_name))
    for file in testing_name_list:
        f = open("../Output/{0}/testingdata/testing/{1}".format(dir_name,file),"x")
        for pattern in pattern_list:
            flag = False
            for pat in testing_pattern_list[file]:
                if str(pat) == str(pattern):
                    flag = True
            f.write(str(pattern).replace(",",";") + "," + str(flag) + "\n")
        f.close()

    for file in training_name_list:
        f = open("../Output/{0}/testingdata/training/{1}".format(dir_name,file),"x")
        for pattern in pattern_list:
            flag = False
            for pat in training_pattern_list[file]:
                if str(pat) == str(pattern):
                    flag = True
            f.write(str(pattern).replace(",",";") + "," + str(flag) + "\n")
        f.close()
