import os
import sys
import math
import random
#DEBUG = True

unconditional_chi_square_value =  0.01579077
conditional_chi_square_value =  0.01579077

def strongconnect(v, node_nums,index, S,parents_list,cycles):
    node_nums[v]["index"] = index
    node_nums[v]["lowlink"] = index
    index += 1
    S.append(v)
    node_nums[v]["onstack"] = False

    for w in parents_list[v]:
        if node_nums[w]["index"] == None:
            index = strongconnect(w,node_nums,index,S,parents_list,cycles)
            node_nums[v]["lowlink"] == min(node_nums[v]["lowlink"],node_nums[w]["lowlink"])
        elif node_nums[w]["onstack"]:
            node_nums[v]["lowlink"] = min(node_nums[v]["lowlink"],node_nums[w]["index"])
    if node_nums[v]["lowlink"] == node_nums[v]["index"] and len(S) > 0:
        new_cycle = []
        w = S[0]
        del S[0]
        node_nums[w]["onstack"] = False
        new_cycle.append(w)
        while w != v and len(S) > 0:
            w = S[0]
            del S[0]
            node_nums[w]["onstack"] = False
            new_cycle.append(w)
        cycles.append(new_cycle)
    return index
        

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

def unconditional_chi_test(attribute, total_counts,ratio):
    expected_ratio = ratio
    expected_mal = expected_ratio*total_counts[attribute][0]
    expected_ben = (1 - expected_ratio)*total_counts[attribute][0]
    actual_mal = total_counts[attribute][1]
    actual_ben = total_counts[attribute][2]
    if expected_mal == 0: chi_val = ((actual_ben - expected_ben) ** 2) / expected_ben
    elif expected_ben == 0: chi_val = ((actual_mal - expected_mal) ** 2) / expected_mal
    else: chi_val = ((actual_mal - expected_mal) ** 2) / expected_mal + ((actual_ben - expected_ben) ** 2) / expected_ben
    if chi_val >= unconditional_chi_square_value: return True, chi_val
    return False, chi_val

def runMain(dir_name, CONDITIONAL_SUPPORT, remove_zeros=False, DEBUG=True, filePatternList=None, ratio=None):
    file_name_list = os.listdir("../Input/training")

    if ratio==None:
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

    # for x in file_pattern_list:
    #     if(set(file_pattern_list[x]) != set(file_pattern_list2[x])):
    #         print(x)
    #         print(file_pattern_list[x])
    #         print(file_pattern_list2[x])

    final_P_C_map = {}
    final_nP_C_map = {}
    final_P_nC_map = {}
    final_nP_nC_map = {}
    total_counts = {str(x):[0,0,0] for x in pattern_list}
    total_not_counts = {str(x):[0,0,0] for x in pattern_list}
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
        if not unconditional_chi_test(str(pat),total_counts,ratio)[0]:
            remove_list.append(pat)
        # else:
        #     mal_check = True if float(total_counts[str(pat)][1])/total_counts[str(pat)][0] >= 0.8247 else False
        #     not_check = True if float(320-total_counts[str(pat)][1])/(388-total_counts[str(pat)][0]) >= 0.8247 else False
        #     if mal_check == not_check:
        #         remove_list.append(pat)
        counter += 1
        if DEBUG: print("{0} Attributes remaining for Unconditional Checks".format(len(list(pattern_list))-counter))

    all_pattern_counts = {str(x):total_counts[str(x)] for x in total_counts}
    all_pattern_not_counts = {str(x):total_not_counts[str(x)] for x in total_not_counts}
    for pat in remove_list:
        del total_counts[str(pat)]
        del total_not_counts[str(pat)]

    remove_list = []
    if remove_zeros:
        for pat in total_counts:
            if total_counts[pat][1] == 0 or total_counts[pat][2] == 0:
                remove_list.append(pat)
            if total_not_counts[pat][1] == 0 or total_not_counts[pat][2] == 0:
                remove_list.append(pat)
    
        for pat in remove_list:
            del total_counts[str(pat)]
            del total_not_counts[str(pat)]
            del all_pattern_counts[str(pat)]
            del all_pattern_not_counts[str(pat)]

    only_conditionals = [x for x in all_pattern_counts if x not in total_counts]


    for pat in all_pattern_counts:
        final_P_C_map[str(pat)] = {str(y):[0,0,0] for y in all_pattern_counts if str(y) != str(pat)}
        final_nP_C_map[str(pat)] = {str(y):[0,0,0] for y in all_pattern_counts if str(y) != str(pat)}
        final_P_nC_map[str(pat)] = {str(y):[0,0,0] for y in all_pattern_counts if str(y) != str(pat)}
        final_nP_nC_map[str(pat)] = {str(y):[0,0,0] for y in all_pattern_counts if str(y) != str(pat)}
        for pat2 in all_pattern_counts:
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
            if final_P_C_map[pat][pat2][0]/all_pattern_counts[pat][0] < CONDITIONAL_SUPPORT:
                remove_list.append(pat2)
            elif final_P_C_map[pat][pat2][0] == 0 or final_P_nC_map[pat][pat2][0] == 0 or final_nP_C_map[pat][pat2][0] == 0 or final_nP_nC_map[pat][pat2][0] == 0:
                remove_list.append(pat2)
        for pat2 in remove_list:
            del final_P_C_map[pat][pat2]

    # counter = 0
    # for pat in pattern_list:
    #     if str(pat) in all_pattern_counts:
    #         remove_list = []
    #         final_P_C_map[str(pat)] = {str(y):[0,0,0] for y in total_counts if str(y) != str(pat)}
    #         final_not_map[str(pat)] = {str(y):[0,0,0] for y in total_counts if str(y) != str(pat)}
    #         for name in file_name_list:
    #             is_malicious = True
    #             if 'benign' in name: is_malicious = False
    #             if pat in file_pattern_list[name]:
    #                 for pat2 in file_pattern_list[name]:
    #                     if str(pat2) in total_counts and str(pat2) != str(pat):
    #                         final_map[str(pat)][str(pat2)][0] += 1
    #                         if is_malicious:
    #                             final_map[str(pat)][str(pat2)][1] += 1
    #                         else:
    #                             final_map[str(pat)][str(pat2)][2] += 1
    #             else:
    #                 for pat2 in file_pattern_list[name]:
    #                     if str(pat2) in total_counts and str(pat2) != str(pat):
    #                         final_not_map[str(pat)][str(pat2)][0] += 1
    #                         if is_malicious:
    #                             final_not_map[str(pat)][str(pat2)][1] += 1
    #                         else:
    #                             final_not_map[str(pat)][str(pat2)][2] += 1
    #         for pat2 in final_map[str(pat)]:
    #             if float(final_map[str(pat)][str(pat2)][0])/float(all_pattern_counts[str(pat)][0]) < CONDITIONAL_SUPPORT or \
    #                 float(final_not_map[str(pat)][str(pat2)][0])/float(all_pattern_not_counts[str(pat)][0]) < CONDITIONAL_SUPPORT:
    #                 remove_list.append(pat2)
    #         for p in remove_list:
    #             del final_map[str(pat)][str(p)]
    #             del final_not_map[str(pat)][str(p)]   
    #         counter += 1
    #         if DEBUG: print("{0} Attributes remaining for Conditional Checks".format(len(list(all_pattern_counts.keys()))-counter))

    for pat in all_pattern_counts:
        del_list = []
        for pat2 in final_P_C_map[pat]:
            P_C_val = True if final_P_C_map[pat][pat2][1]/final_P_C_map[pat][pat2][0] > ratio else False
            P_nC_val = True if final_P_nC_map[pat][pat2][1]/final_P_nC_map[pat][pat2][0] > ratio else False
            nP_C_val = True if final_nP_C_map[pat][pat2][1]/final_nP_C_map[pat][pat2][0] > ratio else False
            nP_nC_val = True if final_nP_nC_map[pat][pat2][1]/final_nP_nC_map[pat][pat2][0] > ratio else False
            if P_C_val == nP_C_val or P_nC_val == nP_nC_val or P_C_val == nP_C_val or P_nC_val == nP_nC_val:
                del_list.append(pat2)
            elif not unconditional_chi_test("pat",{"pat":final_P_C_map[pat][pat2]},ratio)[0] or \
                 not unconditional_chi_test("pat",{"pat":final_P_nC_map[pat][pat2]},ratio)[0] or \
                 not unconditional_chi_test("pat",{"pat":final_nP_C_map[pat][pat2]},ratio)[0] or \
                 not unconditional_chi_test("pat",{"pat":final_nP_nC_map[pat][pat2]},ratio)[0]:
                del_list.append(pat2)
        for p in del_list:
            del final_P_C_map[pat][p]


    remove_list = []
    for pat in only_conditionals:
        if len(list(final_P_C_map[pat].keys())) == 0:
            remove_list.append(pat)

    for p in remove_list:
        for pat in final_P_C_map:
            if p in final_P_C_map[pat].keys():
                del final_P_C_map[pat][p]
        del final_P_C_map[p]
        del only_conditionals[only_conditionals.index(p)]


    #get cycles
    parents_list = {str(x):[] for x in final_P_C_map}
    for pat in final_P_C_map:
        for pat2 in final_P_C_map:
            if str(pat) in final_P_C_map and str(pat2) in final_P_C_map[str(pat)]:
                parents_list[str(pat2)].append(str(pat))


    #chi squared cycle method
    # for pat in final_P_C_map:
    #     for pat2 in final_P_C_map:
    #         if pat2 in parents_list[pat] and pat in parents_list[pat2]:
    #             if min(unconditional_chi_test("pat",{"pat":final_P_C_map[pat][pat2]},ratio)[1] + unconditional_chi_test("pat",{"pat":final_P_nC_map[pat][pat2]},ratio)[1], unconditional_chi_test("pat",{"pat":final_nP_C_map[pat][pat2]},ratio)[1] + unconditional_chi_test("pat",{"pat":final_nP_nC_map[pat][pat2]},ratio)[1]) < \
    #             min(unconditional_chi_test("pat",{"pat":final_P_C_map[pat2][pat]},ratio)[1] + unconditional_chi_test("pat",{"pat":final_P_nC_map[pat2][pat]},ratio)[1], unconditional_chi_test("pat",{"pat":final_nP_C_map[pat2][pat]},ratio)[1] + unconditional_chi_test("pat",{"pat":final_nP_nC_map[pat2][pat]},ratio)[1]):
    #                 del final_P_C_map[pat][pat2]
    #                 parents_list[pat2].remove(pat)
    #             else:
    #                 del final_P_C_map[pat2][pat]
    #                 parents_list[pat].remove(pat2)

    #many parents method
    # for pat in final_P_C_map:
    #     for pat2 in final_P_C_map:
    #         if pat2 in parents_list[pat] and pat in parents_list[pat2]:
    #             if len(final_P_C_map[pat2]) < len(final_P_C_map[pat]):
    #                 del final_P_C_map[pat][pat2]
    #                 parents_list[pat2].remove(pat)
    #             else:
    #                 del final_P_C_map[pat2][pat]
    #                 parents_list[pat].remove(pat2)

    #many children method
    for pat in final_P_C_map:
        for pat2 in final_P_C_map:
            if pat2 in parents_list[pat] and pat in parents_list[pat2]:
                if len(parents_list[pat]) < len(parents_list[pat2]):
                    del final_P_C_map[pat][pat2]
                    parents_list[pat2].remove(pat)
                else:
                    del final_P_C_map[pat2][pat]
                    parents_list[pat].remove(pat2)

    #Random Method
    # for pat in final_P_C_map:
    #     for pat2 in final_P_C_map:
    #         if pat2 in parents_list[pat] and pat in parents_list[pat2]:
    #             r = random.randint(1,2)
    #             if r == 1:
    #                 del final_P_C_map[pat][pat2]
    #                 parents_list[pat2].remove(pat)
    #             else:
    #                 del final_P_C_map[pat2][pat]
    #                 parents_list[pat].remove(pat2)


    index = 0
    temp_stack = []
    node_nums = {x:{"index":None,"lowlink":None,"onstack":False} for x in parents_list}
    cycles = []
    for v in parents_list:
        if node_nums[v]["index"] == None:
            index = strongconnect(v,node_nums,index,temp_stack,parents_list,cycles)

    def dfs(node, parents_list, cur_stack,strongly_connected):
        #print("parent list: " + str((node, parents_list[node])))
        #print("strong list: " + str([x for x in parents_list[node] if x in strongly_connected]))
        for p in [x for x in parents_list[node]]:
            if p in cur_stack:
                if DEBUG:
                    print("FOUND CYCLE" + str(cur_stack[cur_stack.index(p):] + [p]))
                return cur_stack[cur_stack.index(p):] + [p]
            cur_stack.append(p)
            l = dfs(p,parents_list,cur_stack,strongly_connected)
            if len(l) > 0: return l
            del cur_stack[-1]
        return []


    cycles = [x for x in cycles if len(x) > 1]
    #print(cycles)
    final_cycles = []
    for c in cycles:
        for i in range(len(c)):
            n = c[i]
            flag = False
            while not flag:
                cur_stack = [n]
                l = dfs(n,parents_list,cur_stack,c)
                if len(l) == 0:
                    flag = True
                else:
                    final_cycles.append(l)
                    #chi squared method
                    # max = float("inf")
                    # maxNode = -1
                    # for node in range(len(l)-1):
                    #     chi = min(unconditional_chi_test("pat",{"pat":final_P_C_map[str(l[node+1])][str(l[node])]},ratio)[1] + unconditional_chi_test("pat",{"pat":final_P_nC_map[str(l[node+1])][str(l[node])]},ratio)[1], unconditional_chi_test("pat",{"pat":final_nP_C_map[str(l[node+1])][str(l[node])]},ratio)[1] + unconditional_chi_test("pat",{"pat":final_nP_nC_map[str(l[node+1])][str(l[node])]},ratio)[1])
                    #     if chi < max:
                    #         max = chi
                    #         maxNode = node
                    # del final_P_C_map[str(l[maxNode+1])][str(l[maxNode])]
                    # parents_list[l[maxNode]].remove(l[maxNode+1])
                    #many parents method
                    # max = float("-inf")
                    # minNode = -1
                    # for node in range(1,len(l)):
                    #     parent_count = len(final_P_C_map[l[node]])
                    #     if parent_count > max:
                    #         max = parent_count
                    #         maxNode = node
                    # #print(l[maxNode],l[maxNode+1])
                    # del final_P_C_map[str(l[maxNode])][str(l[maxNode-1])]
                    # parents_list[l[maxNode-1]].remove(l[maxNode])
                    #many children method
                    max = float("-inf")
                    minNode = -1
                    for node in range(len(l)-1):
                        child_count = len(parents_list[l[node]])
                        if child_count > max:
                            max = child_count
                            maxNode = node
                    #print(l[maxNode],l[maxNode+1])
                    del final_P_C_map[str(l[maxNode+1])][str(l[maxNode])]
                    parents_list[l[maxNode]].remove(l[maxNode+1])
                    #Random method
                    # maxNode = random.randint(0,len(l)-2)
                    # del final_P_C_map[str(l[maxNode+1])][str(l[maxNode])]
                    # parents_list[l[maxNode]].remove(l[maxNode+1])
    #print(final_cycles)
        
    remove_list = []
    for pat in only_conditionals:
        if len(list(final_P_C_map[pat].keys())) == 0:
            remove_list.append(pat)

    for p in remove_list:
        for pat in final_P_C_map:
            if p in final_P_C_map[pat].keys():
                del final_P_C_map[pat][p]
        del final_P_C_map[p]
        del only_conditionals[only_conditionals.index(p)]
        

    f = open("../Output/{0}/unconditional_pattern_count.csv".format(dir_name),"x")
    f.write("Pattern_Name,Total_Unique_Count,Malicious_Unique_Count,Beneign_Unique_Count\n")
    for pat in total_counts:
        pat = str(pat)
        write_string = "{0},{1},{2},{3}\n".format(pat.replace(",",";"),total_counts[pat][0],total_counts[pat][1],
                                                                    total_counts[pat][2])
        f.write(write_string)
        if DEBUG: print("Done Writing {0}".format(pat))

    f.close()

    f = open("../Output/{0}/all_pattern_count.csv".format(dir_name),"x")
    f.write("Condition,Pattern_Name,Total_Unique_Count,Malicious_Unique_Count,Beneign_Unique_Count\n")
    for pat in all_pattern_counts:
        pat = str(pat)
        conditional = "Conditional" if pat in only_conditionals else "Unconditional"
        write_string = "{0},{1},{2},{3},{4}\n".format(conditional,pat.replace(",",";"),all_pattern_counts[pat][0],all_pattern_counts[pat][1],
                                                                    all_pattern_counts[pat][2])
        f.write(write_string)
        if DEBUG: print("Done Writing {0}".format(pat))

    f.close()

    os.mkdir("../Output/{0}/conditional_pattern_output".format(dir_name))

    counter = 0
    for pattern in final_P_C_map:
        f = open("../Output/{0}/conditional_pattern_output/pattern{1}.csv".format(dir_name,counter), "x")
        #f.write(str(pattern).replace("(","").replace(")","").replace("'","").replace("\"","") + "\n")
        f.write("{0},{1},{2},{3},{4},{5},{6}\n".format(str(pattern).replace(",",";"),all_pattern_counts[str(pattern)][0]
                                                        ,all_pattern_counts[str(pattern)][1],all_pattern_counts[str(pattern)][2],
                                            len(file_name_list)-all_pattern_counts[str(pattern)][0],
                                            len([x for x in file_name_list if "mal" in x])-all_pattern_counts[str(pattern)][1],
                                            len([x for x in file_name_list if "mal" not in x])-all_pattern_counts[str(pattern)][2]  ))
        f.write("Pattern_Name,Total_Unique_Count,Malicious_Unique_Count,Beneign_Unique_Count,Total_Unique_Count_Missing,Malicious_Unique_Count_Missing,Benign_Unique_Count_Missing\n")
        for pat in final_P_C_map[str(pattern)]:
            pat = str(pat)
            write_string = "{0},{1},{2},{3},{4},{5},{6}\n".format(pat.replace(",",";"),final_P_C_map[str(pattern)][str(pat)][0],
                                                                final_P_C_map[str(pattern)][str(pat)][1],
                                                                final_P_C_map[str(pattern)][str(pat)][2],final_P_nC_map[str(pattern)][str(pat)][0],
                                                                final_P_nC_map[str(pattern)][str(pat)][1],
                                                                final_P_nC_map[str(pattern)][str(pat)][2])
            f.write(write_string)
        counter += 1
        if DEBUG: print("{0} Attribute Writes remaining".format(len(list(pattern_list))-counter))
        f.close()

    f = open("../Output/{0}/final_pattern_list.csv".format(dir_name,counter), "x")
    for pattern in pattern_list:
        if str(pattern) in final_P_C_map:
            pattern = str(pattern).strip()
            pattern = str(pattern).replace("(","").replace(")","").replace("'","").replace("\"","")
            if str(pattern)[-1] == ',':
                pattern = str(pattern)[:-1]
            f.write(pattern + "\n")
    f.close()

if __name__ == "__main__":
    if len(sys.argv) < 2:
        raise Exception("Input a directory name for output")
    dir_name = sys.argv[1]
    runMain(dir_name)