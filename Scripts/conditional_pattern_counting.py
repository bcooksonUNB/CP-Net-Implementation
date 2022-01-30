import os
DEBUG = True

file_name_list = os.listdir("../Input")

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

#set of all attributes
attribute_list = set()

#ordered list of the attributes in each file, indexed by file name
file_attribute_list = {}

#list of patterns that appear in each file, indexed by file name
file_pattern_list = {x:[] for x in file_name_list}

LIMIT = 0.4

for name in file_name_list:
    f = open("../Input/" + name)
    next_attribute = f.readline()
    file_attribute_list[name] = {}
    while next_attribute:
        next_attribute = next_attribute.strip("\n")
        attribute_list.add(next_attribute)
        if(next_attribute in file_attribute_list[name]): file_attribute_list[name][next_attribute] += 1
        else: file_attribute_list[name][next_attribute] = 1
        next_attribute = f.readline()
    if DEBUG: print("Done Reading {0}".format(name))
    f.close()

pattern_list = []
f = open("../Output/pattern_list.csv")
next_pattern = f.readline()
while next_pattern:
    next_pattern = next_pattern.strip()
    next_pattern = next_pattern.split(",")
    pattern_tuple = tuple(x.strip() for x in next_pattern)
    pattern_list.append(pattern_tuple)
    next_pattern = f.readline()
f.close()

for file in file_name_list:
    if DEBUG: print("Done finding patterns for " + file)
    file_pattern_list[file] = get_sequences_in_file(pattern_list,file_attribute_list[file])

final_map = {}
total_counts = {str(x):[0,0,0] for x in pattern_list}
#chi_tests = {x:False for x in attribute_list}

counter = 0
for pat in pattern_list:
    final_map[str(pat)] = {str(y):[0,0,0] for y in pattern_list if y != pat}
    for name in file_name_list:
        if pat in file_pattern_list[name]:
            total_counts[str(pat)][0] += 1
            is_malicious = True
            if 'benign' in name: 
                is_malicious = False
                total_counts[str(pat)][2] += 1
            else:
                total_counts[str(pat)][1] += 1
            for pat2 in file_pattern_list[name]:
                if pat2 != pat:
                    final_map[str(pat)][str(pat2)][0] += 1
                    if is_malicious:
                        final_map[str(pat)][str(pat2)][1] += 1
                    else:
                        final_map[str(pat)][str(pat2)][2] += 1
    counter += 1
    if DEBUG: print("{0} Attributes remaining".format(len(list(pattern_list))-counter))

counter = 0
for pattern in pattern_list:
    f = open("../Output/conditional_pattern_output/pattern{0}.csv".format(counter), "x")
    f.write(str(pattern).replace("(","").replace(")","").replace("'","").replace("\"","") + "\n")
    f.write(
        "Pattern_Name,Total_Unique_Count,Malicious_Unique_Count,Beneign_Unique_Count\n")
    for pat in final_map[str(pattern)]:
        pat = str(pat)
        write_string = "{0},{1},{2},{3}\n".format(pat.replace(",",";"),final_map[str(pattern)][pat][0],
                                                              final_map[str(pattern)][pat][1],
                                                              final_map[str(pattern)][pat][2])
        f.write(write_string)
    counter += 1
    if DEBUG: print("{0} Attribute Writes remaining".format(len(list(pattern_list))-counter))
    f.close()