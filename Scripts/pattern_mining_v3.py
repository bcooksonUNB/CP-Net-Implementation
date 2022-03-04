import os
import math
import itertools
import sys

#DEBUG = True
#MIN_SUPPORT = 0.4
#MAX_K = 2

def get_one_less_sublists(l):
    ret_list = []
    for i in range(len(l)):
        ret_list.append(tuple(l[x] for x in range(len(l)) if x != i))
    return ret_list


def gen_cands(itemset, n, litemSet):
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

def runMain(dir_name, MIN_SUPPORT, MAX_K, DEBUG=True, pre_patterns=None, pre_filePatternList=None, pre_ratios=None):
    if pre_patterns and pre_filePatternList:
        pattern_list = pre_patterns
        f = open("../Output/{0}/pattern_list.csv".format(dir_name),"x")
        for i in pattern_list:
            f.write(i.replace("(","").replace(")","").replace("'","").replace("\"","") + "\n")
        f.close()
        return pre_filePatternList, pre_patterns, pre_ratios

    file_name_list = os.listdir("../Input/training")
    fileList = {}

    benItemCounts = {}
    malItemCounts = {}
    itemList = {}
    litemSet = {1:set()}
    pattern_list = {}

    for name in file_name_list:
        f = open("../Input/training/" + name)
        next_attribute = f.readline()
        l = []
        while next_attribute:
            next_attribute = next_attribute.strip("\n")
            l.append(next_attribute)
            next_attribute = f.readline()
        fileList[name] = l
        if DEBUG: print("Done Reading {0}".format(name))
        f.close()

    filePatternList = {x:[] for x in fileList}
    TOTAL = len(fileList)
    MIN_COUNT = math.ceil(TOTAL*MIN_SUPPORT)
    fileList = {x:fileList[x][1:] for x in fileList}
    ben_files = {x:fileList[x] for x in fileList if 'benign' in x}
    mal_files = {x:fileList[x] for x in fileList if 'benign' not in x}

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
                else:
                    foundSet.add(item)
                    benItemCounts[item] += 1
                itemList[item] = itemList.get(item,[]) + [f]

    for item in benItemCounts:
        if benItemCounts[item] > BEN_MIN_COUNT:
            litemSet[1].add( (item,) )
            pattern_list[str(item)] = [benItemCounts[item],itemList[item]]

    for f in mal_files:
        #print(f)
        foundSet = set()
        file = fileList[f]
        for item in file:
            if item not in foundSet:
                if item not in malItemCounts:
                    foundSet.add(item)
                    malItemCounts[item] = 1
                else:
                    foundSet.add(item)
                    malItemCounts[item] += 1
                itemList[item] = itemList.get(item,[]) + [f]

    for item in malItemCounts:
        #print(item, malItemCounts[item])
        if malItemCounts[item] > MAL_MIN_COUNT :
            #print(True,MAL_MIN_COUNT,len(mal_files))
            litemSet[1].add( (item,) )
            pattern_list[str(item)] = [malItemCounts[item],itemList[item]]
    
    for item in [x for x in itemList if (x,) in litemSet[1]]:
        for f in itemList[item]:
                filePatternList[f].append( (item,) )
    #raise Exception()

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
        cands = gen_cands(litemSet[k-1],k,litemSet)
        cand_counts = {str(c): [0,0] for c in cands}
        cand_list = {str(c): [] for c in cands}
        for i in fileList:
            if DEBUG: print("Done adding to file " + str(i) + " for level " + str(k))
            for c in cands:
                if is_sequence_in_file(c, fileList[i]):
                    if "mal" in fileList[i]:
                        cand_counts[str(c)][0] += 1
                    else:
                        cand_counts[str(c)][1] += 1
                    cand_list[str(c)].append(i)
        litemSet[k] = set(x for x in cands if cand_counts[str(x)][0] >= MAL_MIN_COUNT or cand_counts[str(x)][1] >= BEN_MIN_COUNT)
        #litemSet[k] = set(x for x in cands)
        for item in litemSet[k]:
            pattern_list[str(item)] = (cand_counts[str(item)], cand_list[str(item)])
            for f in cand_list[str(item)]:
                filePatternList[f].append(item)
        if len(litemSet[k]) == 0:
            break
        k += 1

    f = open("../Output/{0}/pattern_list.csv".format(dir_name),"x")
    for i in pattern_list:
        f.write(i.replace("(","").replace(")","").replace("'","").replace("\"","") + "\n")
    f.close()

    ratio = float(len(mal_files))/( float(len(mal_files)) + float(len(ben_files)) )

    return filePatternList, pattern_list, ratio

if __name__ == "__main__":
    if len(sys.argv) < 2:
        raise Exception("Input a directory name for output")
    dir_name = sys.argv[1]
    runMain(dir_name)