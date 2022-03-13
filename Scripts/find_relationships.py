import os

chi_square_value = 3.84

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

def unconditional_chi_test(attribute,total_counts,ratio):
    expected_ratio = ratio
    expected_mal = expected_ratio*total_counts[attribute][0]
    expected_ben = (1 - expected_ratio)*total_counts[attribute][0]
    actual_mal = total_counts[attribute][1]
    actual_ben = total_counts[attribute][2]
    if expected_mal == 0: chi_val = ((actual_ben - expected_ben) ** 2) / expected_ben
    elif expected_ben == 0: chi_val = ((actual_mal - expected_mal) ** 2) / expected_mal 
    else: chi_val = ((actual_mal - expected_mal) ** 2) / expected_mal + ((actual_ben - expected_ben) ** 2) / expected_ben
    if chi_val >= chi_square_value: return True, chi_val
    return False, chi_val

def conditional_chi_test(pat, pat2, total_counts, final_map):
    expected_ratio = float(total_counts[pat][1])/total_counts[pat][0]
    expected_mal = final_map[pat][pat2][0]*expected_ratio
    expected_ben = final_map[pat][pat2][0]*(1-expected_ratio)
    actual_mal = final_map[pat][pat2][1]
    actual_ben = final_map[pat][pat2][2]
    if expected_mal == 0 and expected_ben == 0:
        print(pat)
        print(pat2)
        print(total_counts[pat])
        print(final_map[pat])
    if expected_mal == 0: chi_val = ((actual_ben - expected_ben)**2)/expected_ben
    elif expected_ben == 0: chi_val = ((actual_mal - expected_mal)**2)/expected_mal
    else: chi_val = ((actual_mal - expected_mal)**2)/expected_mal + ((actual_ben - expected_ben)**2)/expected_ben
    if chi_val >= chi_square_value: return True, chi_val
    return False, chi_val

def runMain(dir_name, CONDITIONAL_SUPPORT, DEBUG=True, remove_zeros=True, filePatternList=None, ratio=None):
    file_name_list = os.listdir("../Input/training")

    if ratio == None:
        ratio = (float)(len([x for x in file_name_list if "mal" in x]))/len(file_name_list)

    #set of all attributes
    attribute_list = set()

    #ordered list of the attributes in each file, indexed by file name
    file_attribute_list = {}

    #list of patterns that appear in each file, indexed by file name
    file_pattern_list = {x:[] for x in file_name_list}

    for name in file_name_list:
        f = open("../Input/training/" + name)
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

    if not filePatternList:
        for file in file_name_list:
            file_pattern_list[file] = get_sequences_in_file(pattern_list,file_attribute_list[file])
            if DEBUG: print("Done finding patterns for " + file)
    else:
        file_pattern_list = {x:filePatternList[x] for x in filePatternList}

    final_P_C_map = {}
    final_nP_C_map = {}
    final_P_nC_map = {}
    final_nP_nC_map = {}
    total_counts = {str(x):[0,0,0] for x in pattern_list}
    total_not_counts = {str(x):[0,0,0] for x in pattern_list}
    pref_list = {}
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
            else:
                total_not_counts[str(pat)][0] += 1
                is_malicious = True
                if 'benign' in name: 
                    is_malicious = False
                    total_not_counts[str(pat)][2] += 1
                else:
                    total_not_counts[str(pat)][1] += 1

    if remove_zeros:
        for pat in total_counts:
            if total_counts[pat][1] == 0 or total_counts[pat][2] == 0:
                remove_list.append(pat)
    
        for pat in remove_list:
            del total_counts[str(pat)]

    for pat in total_counts:
        if unconditional_chi_test(pat,total_counts,ratio)[0]:
            if total_counts[pat][1]/total_counts[pat][0] > ratio:
                pref_list[pat] = 'mal'
            else:
                pref_list[pat] = 'ben'
        else:
            pref_list[pat] = 'neutral'

    for pat in total_counts:
        final_P_C_map[str(pat)] = {str(y):[0,0,0] for y in total_counts if str(y) != str(pat)}
        final_nP_C_map[str(pat)] = {str(y):[0,0,0] for y in total_counts if str(y) != str(pat)}
        final_P_nC_map[str(pat)] = {str(y):[0,0,0] for y in total_counts if str(y) != str(pat)}
        final_nP_nC_map[str(pat)] = {str(y):[0,0,0] for y in total_counts if str(y) != str(pat)}
        for pat2 in total_counts:
            if pat != pat2:
                for file in file_name_list:
                    is_mal = False
                    if "mal" in file: is_mal = True 
                    file_str_list = [str(x) for x in file_pattern_list[file]]
                    if pat in file_str_list and pat2 in file_str_list:
                        final_P_C_map[pat][pat2][0] += 1
                        if is_mal:
                            final_P_C_map[pat][pat2][1] += 1
                        else:
                            final_P_C_map[pat][pat2][2] += 1
                    elif pat in file_str_list and pat2 not in file_str_list:
                        final_nP_C_map[pat][pat2][0] += 1
                        if is_mal:
                            final_nP_C_map[pat][pat2][1] += 1
                        else:
                            final_nP_C_map[pat][pat2][2] += 1
                    elif pat not in file_str_list and pat2 in file_str_list:
                        final_P_nC_map[pat][pat2][0] += 1
                        if is_mal:
                            final_P_nC_map[pat][pat2][1] += 1
                        else:
                            final_P_nC_map[pat][pat2][2] += 1
                    elif pat not in file_str_list and pat2 not in file_str_list:
                        final_nP_nC_map[pat][pat2][0] += 1
                        if is_mal:
                            final_nP_nC_map[pat][pat2][1] += 1
                        else:
                            final_nP_nC_map[pat][pat2][2] += 1
        remove_list = []
        for pat2 in final_P_C_map[pat]:
            if final_P_C_map[pat][pat2][0] < total_counts[pat][0]*CONDITIONAL_SUPPORT:
                remove_list.append(pat2)
        for pat2 in remove_list:
            del final_P_C_map[pat][pat2]

    final_category_map = {}
    final_category_map_non_chi = {}
    for pat in total_counts:
        remove_list = []
        final_category_map[str(pat)] = {str(y):{} for y in final_P_C_map if str(y) != str(pat)}
        final_category_map_non_chi[str(pat)] = {str(y):{} for y in final_P_C_map if str(y) != str(pat)}
        for pat2 in final_P_C_map[pat]:
            if final_P_C_map[pat][pat2][0] != 0 and unconditional_chi_test("pat",{"pat":final_P_C_map[pat][pat2]},ratio)[0]:
                if final_P_C_map[pat][pat2][1]/final_P_C_map[pat][pat2][0] > ratio:
                    final_category_map[pat][pat2]['PC'] = "mal"
                    final_category_map_non_chi[pat][pat2]['PC'] = "mal"
                else:
                    final_category_map[pat][pat2]['PC'] = "ben"
                    final_category_map_non_chi[pat][pat2]['PC'] = "ben"
            else:
                final_category_map[pat][pat2]['PC'] = "neutral"
                if final_P_C_map[pat][pat2][0] != 0 and final_P_C_map[pat][pat2][1]/final_P_C_map[pat][pat2][0] > ratio:
                    final_category_map_non_chi[pat][pat2]['PC'] = "mal"
                elif final_P_C_map[pat][pat2][0] != 0 and final_P_C_map[pat][pat2][1]/final_P_C_map[pat][pat2][0] < ratio:
                    final_category_map_non_chi[pat][pat2]['PC'] = "ben"
                else:
                    final_category_map_non_chi[pat][pat2]['PC'] = "neutral"
            
            if final_nP_C_map[pat][pat2][0] != 0 and unconditional_chi_test("pat",{"pat":final_nP_C_map[pat][pat2]},ratio)[0]:
                if final_nP_C_map[pat][pat2][1]/final_nP_C_map[pat][pat2][0] > ratio:
                    final_category_map[pat][pat2]['nPC'] = "mal"
                    final_category_map_non_chi[pat][pat2]['nPC'] = "mal"
                else:
                    final_category_map[pat][pat2]['nPC'] = "ben"
                    final_category_map_non_chi[pat][pat2]['nPC'] = "mal"
            else:
                final_category_map[pat][pat2]['nPC'] = "neutral"
                if final_nP_C_map[pat][pat2][0] != 0 and final_nP_C_map[pat][pat2][1]/final_nP_C_map[pat][pat2][0] > ratio:
                    final_category_map_non_chi[pat][pat2]['nPC'] = "mal"
                elif final_nP_C_map[pat][pat2][0] != 0 and final_nP_C_map[pat][pat2][1]/final_nP_C_map[pat][pat2][0] < ratio:
                    final_category_map_non_chi[pat][pat2]['nPC'] = "ben"
                else:
                    final_category_map_non_chi[pat][pat2]['nPC'] = "neutral"

            if final_P_nC_map[pat][pat2][0] != 0 and unconditional_chi_test("pat",{"pat":final_P_nC_map[pat][pat2]},ratio)[0]:
                if final_P_nC_map[pat][pat2][1]/final_P_nC_map[pat][pat2][0] > ratio:
                    final_category_map[pat][pat2]['PnC'] = "mal"
                    final_category_map_non_chi[pat][pat2]['PnC'] = "mal"
                else:
                    final_category_map[pat][pat2]['PnC'] = "ben"
                    final_category_map_non_chi[pat][pat2]['PnC'] = "ben"
            else:
                final_category_map[pat][pat2]['PnC'] = "neutral"
                if final_P_nC_map[pat][pat2][0] != 0 and final_P_nC_map[pat][pat2][1]/final_P_nC_map[pat][pat2][0] > ratio:
                    final_category_map_non_chi[pat][pat2]['PnC'] = "mal"
                elif final_P_nC_map[pat][pat2][0] != 0 and final_P_nC_map[pat][pat2][1]/final_P_nC_map[pat][pat2][0] < ratio:
                    final_category_map_non_chi[pat][pat2]['PnC'] = "ben"
                else:
                    final_category_map_non_chi[pat][pat2]['PnC'] = "neutral"

            if final_nP_nC_map[pat][pat2][0] != 0 and unconditional_chi_test("pat",{"pat":final_nP_nC_map[pat][pat2]},ratio)[0]:
                if final_nP_nC_map[pat][pat2][1]/final_nP_nC_map[pat][pat2][0] > ratio:
                    final_category_map[pat][pat2]['nPnC'] = "mal"
                    final_category_map_non_chi[pat][pat2]['nPnC'] = "mal"
                else:
                    final_category_map[pat][pat2]['nPnC'] = "ben"
                    final_category_map_non_chi[pat][pat2]['nPnC'] = "ben"
            else:
                final_category_map[pat][pat2]['nPnC'] = "neutral"
                if final_nP_nC_map[pat][pat2][0] != 0 and final_nP_nC_map[pat][pat2][1]/final_nP_nC_map[pat][pat2][0] > ratio:
                    final_category_map_non_chi[pat][pat2]['nPnC'] = "mal"
                elif final_nP_nC_map[pat][pat2][0] != 0 and final_nP_nC_map[pat][pat2][1]/final_nP_nC_map[pat][pat2][0] < ratio:
                    final_category_map_non_chi[pat][pat2]['nPnC'] = "ben"
                else:
                    final_category_map_non_chi[pat][pat2]['nPnC'] = "neutral"

    # for pat in total_counts:
    #     print("-------------------" + pat + "-------------------")
    #     for pat2 in final_P_C_map[pat]:
    #         print("Parent: " + str(pat2) + " Child: " + str(pat))
    #         print("PC: " + final_category_map[pat][pat2]["PC"] + "(" + ("NA" if final_P_C_map[pat][pat2][0]==0 else str(final_P_C_map[pat][pat2][1]/final_P_C_map[pat][pat2][0])) + ")")
    #         print("PnC: " + final_category_map[pat][pat2]["PnC"] + "(" + ("NA" if final_P_nC_map[pat][pat2][0]==0 else str(final_P_nC_map[pat][pat2][1]/final_P_nC_map[pat][pat2][0])) + ")")
    #         print("nPC: " + final_category_map[pat][pat2]["nPC"] + "(" + ("NA" if final_nP_C_map[pat][pat2][0]==0 else str(final_nP_C_map[pat][pat2][1]/final_nP_C_map[pat][pat2][0])) + ")")
    #         print("nPnC: " + final_category_map[pat][pat2]["nPnC"] + "(" + ("NA" if final_nP_nC_map[pat][pat2][0]==0 else str(final_nP_nC_map[pat][pat2][1]/final_nP_nC_map[pat][pat2][0])) + ")")
    #         print()
    #     print("--------------------------------------")

    w = open("../Output/{0}/category_types.csv".format(dir_name),"x")

    ratio_map = {}
    total = 0
    for pat in total_counts:
        for pat2 in final_P_C_map[pat]:
            cat_str = ""
            cat_str += ("m" if final_category_map[pat][pat2]["PC"] == "mal" else ("b" if final_category_map[pat][pat2]["PC"] == "ben" else "n"))
            cat_str += ("m" if final_category_map[pat][pat2]["PnC"] == "mal" else ("b" if final_category_map[pat][pat2]["PnC"] == "ben" else "n"))
            cat_str += ("m" if final_category_map[pat][pat2]["nPC"] == "mal" else ("b" if final_category_map[pat][pat2]["nPC"] == "ben" else "n"))
            cat_str += ("m" if final_category_map[pat][pat2]["nPnC"] == "mal" else ("b" if final_category_map[pat][pat2]["nPnC"] == "ben" else "n"))
            ratio_map[cat_str] = ratio_map.get(cat_str,0) + 1
            total += 1
    
    map1 = {x:ratio_map[x] for x in ratio_map}

    ratio_map = {x:ratio_map[x]/total for x in ratio_map}
    top_ten = [(x,ratio_map[x]) for x in sorted(ratio_map.keys(),key=lambda y: ratio_map[y], reverse=True)]
    for x in top_ten: 
        #print(x)
        w.write(str(x) + "\n")

    #print()
    w.write("\n")

    ratio_map = {}
    total = 0
    for pat in total_counts:
        for pat2 in final_P_C_map[pat]:
            cat_str = ""
            cat_str += ("m" if final_category_map_non_chi[pat][pat2]["PC"] == "mal" else ("b" if final_category_map_non_chi[pat][pat2]["PC"] == "ben" else "z"))
            cat_str += ("m" if final_category_map_non_chi[pat][pat2]["PnC"] == "mal" else ("b" if final_category_map_non_chi[pat][pat2]["PnC"] == "ben" else "z"))
            cat_str += ("m" if final_category_map_non_chi[pat][pat2]["nPC"] == "mal" else ("b" if final_category_map_non_chi[pat][pat2]["nPC"] == "ben" else "z"))
            cat_str += ("m" if final_category_map_non_chi[pat][pat2]["nPnC"] == "mal" else ("b" if final_category_map_non_chi[pat][pat2]["nPnC"] == "ben" else "z"))
            ratio_map[cat_str] = ratio_map.get(cat_str,0) + 1
            total += 1

    map2 = {x:ratio_map[x] for x in ratio_map}

    ratio_map = {x:ratio_map[x]/total for x in ratio_map}
    top_ten = [(x,ratio_map[x]) for x in sorted(ratio_map.keys(),key=lambda y: ratio_map[y], reverse=True)]
    for x in top_ten: 
        #print(x)
        w.write(str(x) + "\n")

    w.write("\n")
    w.close()
    return map1, map2, total
