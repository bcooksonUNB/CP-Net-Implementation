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

def runMain(dir_name):
    file_name_list = os.listdir("../Input/testing")

    patterns = []
    pattern_file = open("../Output/{0}/final_pattern_list.csv".format(dir_name))

    file_attribute_list = {}
    file_pattern_list = {x:[] for x in file_name_list}

    for name in file_name_list:
        f = open("../Input/testing/" + name)
        next_attribute = f.readline()
        file_attribute_list[name] = []
        while next_attribute: 
            next_attribute = next_attribute.strip()
            file_attribute_list[name].append(next_attribute)
            next_attribute = f.readline()
        if DEBUG: print("Done Reading {0}".format(name))
        f.close()

    pattern_list = []
    f = open("../Output/{0}/pattern_list.csv".format(dir_name))
    next_pattern = f.readline()
    while next_pattern:
        next_pattern = next_pattern.strip()
        next_pattern = next_pattern.split(",")
        pattern_tuple = tuple(x.strip() for x in next_pattern)
        pattern_list.append(pattern_tuple)
        next_pattern = f.readline()
    f.close()

    for file in file_name_list:
        file_pattern_list[file] = get_sequences_in_file(pattern_list,file_attribute_list[file])
        if DEBUG: print("Done finding patterns for " + file)

    os.mkdir("../Output/{0}/testingdata".format(dir_name))
    for file in file_name_list:
        f = open("../Output/{0}/testingdata/{1}".format(dir_name,file),"x")
        for pattern in pattern_list:
            flag = False
            for pat in file_pattern_list[file]:
                if str(pat) == str(pattern):
                    flag = True
            f.write(str(pattern).replace(",",";") + "," + str(flag) + "\n")
        f.close()
