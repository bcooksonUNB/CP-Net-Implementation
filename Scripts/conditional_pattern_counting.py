import os
import sys
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

def unconditional_chi_test(attribute, total_counts):
    expected_ratio = 0.8247
    expected_mal = expected_ratio*total_counts[attribute][0]
    expected_ben = (1 - expected_ratio)*total_counts[attribute][0]
    actual_mal = total_counts[attribute][1]
    actual_ben = total_counts[attribute][2]
    if expected_mal == 0: chi_val = ((actual_ben - expected_ben) ** 2) / expected_ben
    elif expected_ben == 0: chi_val = ((actual_mal - expected_mal) ** 2) / expected_mal
    else: chi_val = ((actual_mal - expected_mal) ** 2) / expected_mal + ((actual_ben - expected_ben) ** 2) / expected_ben
    print(chi_val)
    if chi_val >= 3.84: return True, chi_val
    return False, chi_val

def conditional_chi_test(pat, pat2, total_counts, final_map):
    expected_ratio = float(total_counts[pat][1])/total_counts[pat][0]
    expected_mal = final_map[pat][pat2][0]*expected_ratio
    expected_ben = final_map[pat][pat2][0]*(1-expected_ratio)
    actual_mal = final_map[pat][pat2][1]
    actual_ben = final_map[pat][pat2][2]
    if expected_mal == 0: chi_val = ((actual_ben - expected_ben)**2)/expected_ben
    elif expected_ben == 0: chi_val = ((actual_mal - expected_mal)**2)/expected_mal
    else: chi_val = ((actual_mal - expected_mal)**2)/expected_mal + ((actual_ben - expected_ben)**2)/expected_ben
    if chi_val >= 3.84: return True, chi_val
    return False, chi_val

def runMain(dir_name, CONDITIONAL_SUPPORT):
    file_name_list = os.listdir("../Input")

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
        file_attribute_list[name] = []
        while next_attribute: 
            next_attribute = next_attribute.strip()
            attribute_list.add(next_attribute)
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

    final_map = {}
    total_counts = {str(x):[0,0,0] for x in pattern_list}
    #chi_tests = {x:False for x in attribute_list}
    remove_list = []

    counter = 0
    for pat in pattern_list:
        for name in file_name_list:
            if pat in file_pattern_list[name]:
                total_counts[str(pat)][0] += 1
                is_malicious = True
                if 'benign' in name: 
                    is_malicious = False
                    total_counts[str(pat)][2] += 1
                else:
                    total_counts[str(pat)][1] += 1
        if not unconditional_chi_test(str(pat),total_counts)[0]:
            remove_list.append(pat)
        counter += 1
        if DEBUG: print("{0} Attributes remaining for Unconditional Checks".format(len(list(pattern_list))-counter))

    for pat in remove_list:
        del total_counts[str(pat)]

    counter = 0
    for pat in pattern_list:
        if str(pat) in total_counts:
            remove_list = []
            final_map[str(pat)] = {str(y):[0,0,0] for y in total_counts if str(y) != str(pat)}
            for name in file_name_list:
                is_malicious = True
                if 'benign' in name: is_malicious = False
                if pat in file_pattern_list[name]:
                    for pat2 in file_pattern_list[name]:
                        if str(pat2) in total_counts and str(pat2) != str(pat):
                            final_map[str(pat)][str(pat2)][0] += 1
                            if is_malicious:
                                final_map[str(pat)][str(pat2)][1] += 1
                            else:
                                final_map[str(pat)][str(pat2)][2] += 1
            for pat2 in final_map[str(pat)]:
                if not conditional_chi_test(str(pat),str(pat2),total_counts,final_map)[0] \
                        or float(final_map[str(pat)][str(pat2)][0])/float(total_counts[str(pat)][0]) < CONDITIONAL_SUPPORT:
                    remove_list.append(pat2)
            for p in remove_list:
                del final_map[str(pat)][str(p)]
            counter += 1
            if DEBUG: print("{0} Attributes remaining for Conditional Checks".format(len(list(total_counts.keys()))-counter))

    f = open("../Output/{0}/pattern_count.csv".format(dir_name),"x")
    f.write("Pattern_Name,Total_Unique_Count,Malicious_Unique_Count,Beneign_Unique_Count\n")
    for pat in total_counts:
        pat = str(pat)
        write_string = "{0},{1},{2},{3}\n".format(pat.replace(",",";"),total_counts[pat][0],total_counts[pat][1],
                                                                    total_counts[pat][2])
        f.write(write_string)
        if DEBUG: print("Done Writing {0}".format(pat))

    f.close()

    os.mkdir("../Output/{0}/conditional_pattern_output".format(dir_name))

    counter = 0
    for pattern in final_map:
        f = open("../Output/{0}/conditional_pattern_output/pattern{1}.csv".format(dir_name,counter), "x")
        f.write(str(pattern).replace("(","").replace(")","").replace("'","").replace("\"","") + "\n")
        f.write(
            "Pattern_Name,Total_Unique_Count,Malicious_Unique_Count,Beneign_Unique_Count\n")
        for pat in final_map[str(pattern)]:
            pat = str(pat)
            write_string = "{0},{1},{2},{3}\n".format(pat.replace(",",";"),final_map[str(pattern)][str(pat)][0],
                                                                final_map[str(pattern)][str(pat)][1],
                                                                final_map[str(pattern)][str(pat)][2])
            f.write(write_string)
        counter += 1
        if DEBUG: print("{0} Attribute Writes remaining".format(len(list(pattern_list))-counter))
        f.close()

    print(remove_list)
    print(len(remove_list))

if __name__ == "__main__":
    if len(sys.argv) < 2:
        raise Exception("Input a directory name for output")
    dir_name = sys.argv[1]
    runMain(dir_name)