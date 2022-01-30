import os
import math
import itertools


DEBUG = True
MIN_SUPPORT = 0.4
MAX_K = 2

file_name_list = os.listdir("../Input")

fileList = {}


benItemCounts = {}
malItemCounts = {}
itemList = {}
litemSet = {1:set()}
pattern_list = {}


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


counter = 0
for name in file_name_list:
    f = open("../Input/" + name)
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
ben_files = {x:fileList[x] for x in fileList if 'benign' in name}
mal_files = {x:fileList[x] for x in fileList if 'benign' not in name}

BEN_TOTAL = len(ben_files)
BEN_MIN_COUNT = math.ceil(BEN_TOTAL*MIN_SUPPORT)

MAL_TOTAL = len(mal_files)
MAL_MIN_COUNT = math.ceil(MAL_TOTAL*MIN_SUPPORT)

#litemsetphase

for f in ben_files:
    foundSet = set()
    file = fileList[f]
    for item in file:
        if item not in foundSet:
            if item not in benItemCounts:
                foundSet.add(item)
                benItemCounts[item] = 1
                itemList[item] = [f]
            else:
                foundSet.add(item)
                benItemCounts[item] += 1
                itemList[item].append(f)

for item in benItemCounts:
    if benItemCounts[item] > BEN_MIN_COUNT:
        litemSet[1].add( (item,) )
        pattern_list[str(item)] = [benItemCounts[item],itemList[item]]

for f in mal_files:
    foundSet = set()
    file = fileList[f]
    for item in file:
        if item not in foundSet:
            if item not in malItemCounts:
                foundSet.add(item)
                malItemCounts[item] = 1
                itemList[item] = [f]
            else:
                foundSet.add(item)
                malItemCounts[item] += 1
                itemList[item].append(f)

for item in malItemCounts:
    if malItemCounts[item] > MAL_MIN_COUNT :
        litemSet[1].add( (item,) )
        pattern_list[str(item)] = [malItemCounts[item],itemList[item]]

#transformation phase
for i in ben_files:
    temp_list = [x for x in ben_files[i] if (x,) in litemSet[1]]
    ben_files[i] = temp_list

for i in mal_files:
    temp_list = [x for x in mal_files[i] if (x,) in litemSet[1]]
    mal_files[i] = temp_list

#generate sequences
k = 2
while True:
	if k > MAX_K:
		break
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
	if len(litemSet[k]) == 0:
		break
	k += 1

f = open("../Output/pattern_list.csv","x")
for i in pattern_list:
	f.write(i.replace("(","").replace(")","").replace("'","").replace("\"","") + "\n")
f.close()