import os
DEBUG = True

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

file_name_list = os.listdir("../Input")

#set of all attributes
attribute_list = set()

#ordered list of the attributes in each file, indexed by file name
file_attribute_list = {}

#list of patterns that appear in each file, indexed by file name
file_pattern_list = {x:[] for x in file_name_list}

for name in file_name_list:
    f = open("../Input/" + name)
    next_attribute = f.readline()
    file_attribute_list[name] = []
    while next_attribute: 
        next_attribute = next_attribute.strip()
        attribute_list.add(next_attribute)
        file_attribute_list[name].append(next_attribute)
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

#total_unique,mal_unique,ben_unique
final_pattern_list = {str(x):[0,0,0] for x in pattern_list}
for name in file_name_list:
    is_malicious = True
    if 'benign' in name: is_malicious = False
    for pat in file_pattern_list[name]:
        pat = str(pat)
        final_pattern_list[pat][0] += 1
        if is_malicious:
            final_pattern_list[pat][1] += 1
        else:
            final_pattern_list[pat][2] += 1
    if DEBUG: print("Done Processing {0}".format(name))

f = open("../Output/pattern_count.csv","x")
f.write("Pattern_Name,Total_Unique_Count,Malicious_Unique_Count,Beneign_Unique_Count\n")
for pat in final_pattern_list:
    pat = str(pat)
    write_string = "{0},{1},{2},{3}\n".format(pat.replace(",",";"),final_pattern_list[pat][0],final_pattern_list[pat][1],
                                                                final_pattern_list[pat][2])
    f.write(write_string)
    if DEBUG: print("Done Writing {0}".format(pat))

f.close()

