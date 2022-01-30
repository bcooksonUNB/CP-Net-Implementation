import os
import math
import itertools


def sub_lists(l, pos, length, cur_list):
    print(length)
    if length == 0: return [cur_list]
    ret_list = []
    if len(l) - pos < length: return cur_list
    for i in range(pos+1,len(l)):
        ret_list += sub_lists(l, i, length-1,cur_list + [l[i]])
    return ret_list

def get_one_less_sublists(l):
    ret_list = []
    for i in range(len(l)):
        ret_list.append(tuple(l[x] for x in range(len(l)) if x != i))
    return ret_list


def gen_cands(itemset, n):
    cands = set()
    if len(itemset) == 0: return cands
    prodList = list(itertools.product(itemset,itemset))
    for p, q in prodList:
        flag = True
        for k in range(n-2):
            if p[k] != q[k]:
                flag = False
                break
        if flag:
            new_cand = p + (q[n-2],)
            cands.add(new_cand)
    remove_list = []
    for c in cands:
        subsets = get_one_less_sublists(c)
        for s in subsets:
            if s not in litemSet[n-1]:
                remove_list.append(c)
                break
    cands = set(x for x in cands if x not in remove_list)
    return cands


def is_sequence_in_file(seq, file):
    current = 0
    for f in file:
        if f == seq[current]: current += 1
        if current == len(seq): return True
    return False

DEBUG = True
MIN_SUPPORT = 0.8
MAX_K = 5

file_name_list = os.listdir("csv-consec_identical_calls_removed")

fileList = {}


itemCounts = {}
itemList = {}
litemSet = {1:set()}
pattern_list = {}

counter = 0
for name in file_name_list:
    f = open("csv-consec_identical_calls_removed/" + name)
    next_attribute = f.readline()
    l = []
    while next_attribute:
        next_attribute = next_attribute.strip("\n")
        l.append(next_attribute)
        next_attribute = f.readline()
    fileList[name] = l
    if DEBUG: print("Done Reading {0}".format(name))
    f.close()
    if counter == 400: break
    counter += 1

TOTAL = len(fileList)
MIN_COUNT = math.ceil(TOTAL*MIN_SUPPORT)
fileList = {x:fileList[x][1:] for x in fileList}

#litemsetphase

for f in fileList:
    foundSet = set()
    file = fileList[f]
    for item in file:
        if item not in foundSet:
            if item not in itemCounts:
                foundSet.add(item)
                itemCounts[item] = 1
                itemList[item] = [f]
            else:
                foundSet.add(item)
                itemCounts[item] += 1
                itemList[item].append(f)

for item in itemCounts:
    if itemCounts[item] > MIN_COUNT:
        litemSet[1].add( (item,) )
        pattern_list[str(item)] = [itemCounts[item],itemList[item]]

#transformation phase
for i in fileList:
    temp_list = [x for x in fileList[i] if (x,) in litemSet[1]]
    fileList[i] = temp_list

#generate sequences
k = 2
while True:
    cands = gen_cands(litemSet[k-1],k)
    cand_counts = {str(c): 0 for c in cands}
    cand_list = {str(c): [] for c in cands}
    for i in fileList:
        print("Done adding to file " + str(i) + " for level " + str(k))
        for c in cands:
            if is_sequence_in_file(c, fileList[i]):
                cand_counts[str(c)] += 1
                cand_list[str(c)].append(i)
    litemSet[k] = set(x for x in cands if cand_counts[str(x)] >= MIN_COUNT)
    for item in litemSet[k]:
        pattern_list[str(item)] = (cand_counts[str(item)], cand_list[str(item)])
    print(litemSet[k])
    if len(litemSet[k]) == 0 or k >= MAX_K:
        break
    k += 1

print(litemSet)
print({x:len(litemSet[x]) for x in litemSet})

f = open("patterns.csv","x")
f.write("Pattern_name,total_count,ben_counts,mal_counts\n")
for pattern in pattern_list:
    count = pattern_list[pattern][0]
    ben_count = 0
    mal_count = 0
    print(pattern_list[pattern][1])
    for name in pattern_list[pattern][1]:
        if 'benign' in name: ben_count += 1
        else: mal_count += 1
    write_string = "{0},{1},{2},{3}\n".format(pattern.replace(',',';'), count, ben_count, mal_count)
    f.write(write_string)
    if DEBUG: print("Done Writing {0}".format(pattern))
f.close()