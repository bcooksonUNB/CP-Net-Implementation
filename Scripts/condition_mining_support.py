import os
DEBUG = True

file_name_list = os.listdir("csv-consec_identical_calls_removed")

attribute_list = set()
event_list = {}

LIMIT = 0.4

def unconditional_chi_test(attribute):
    expected_ratio = 0.8247
    expected_mal = expected_ratio*total_counts[attribute][0]
    expected_ben = (1 - expected_ratio)*total_counts[attribute][0]
    actual_mal = total_counts[attribute][1]
    actual_ben = total_counts[attribute][2]
    if expected_mal == 0: chi_val = ((actual_ben - expected_ben) ** 2) / expected_ben
    elif expected_ben == 0: chi_val = ((actual_mal - expected_mal) ** 2) / expected_mal
    else: chi_val = ((actual_mal - expected_mal) ** 2) / expected_mal + ((actual_ben - expected_ben) ** 2) / expected_ben
    if chi_val >= 3.84: return True, chi_val
    return False, chi_val

def conditional_chi_test(att, attribute):
    expected_ratio = float(total_counts[attribute][1])/total_counts[attribute][0]
    expected_mal = final_map[attribute][att][1]*expected_ratio
    expected_ben = final_map[attribute][att][1]*(1-expected_ratio)
    actual_mal = final_map[attribute][att][3]
    actual_ben = final_map[attribute][att][5]
    if expected_mal == 0: chi_val = ((actual_ben - expected_ben)**2)/expected_ben
    elif expected_ben == 0: chi_val = ((actual_mal - expected_mal)**2)/expected_mal
    else: chi_val = ((actual_mal - expected_mal)**2)/expected_mal + ((actual_ben - expected_ben)**2)/expected_ben
    if chi_val >= 3.84: return True, chi_val
    return False, chi_val

for name in file_name_list:
    f = open("csv-consec_identical_calls_removed/" + name)
    next_attribute = f.readline()
    event_list[name] = {}
    while next_attribute:
        next_attribute = next_attribute.strip("\n")
        attribute_list.add(next_attribute)
        if(next_attribute in event_list[name]): event_list[name][next_attribute] += 1
        else: event_list[name][next_attribute] = 1
        next_attribute = f.readline()
    if DEBUG: print("Done Reading {0}".format(name))
    f.close()

final_map = {}
total_counts = {x:[0,0,0] for x in attribute_list}
chi_tests = {x:False for x in attribute_list}

counter = 0
for attribute in attribute_list:
    final_map[attribute] = {y:[0,0,0,0,0,0] for y in attribute_list if y != attribute}
    for name in event_list:
        if attribute in event_list[name]:
            total_counts[attribute][0] += 1
            is_malicious = True
            if 'benign' in name: 
                is_malicious = False
                total_counts[attribute][2] += 1
            else:
                total_counts[attribute][1] += 1
            for att in event_list[name]:
                if att != attribute:
                    final_map[attribute][att][0] += event_list[name][att]
                    final_map[attribute][att][1] += 1
                    if is_malicious:
                        final_map[attribute][att][2] += event_list[name][att]
                        final_map[attribute][att][3] += 1
                    else:
                        final_map[attribute][att][4] += event_list[name][att]
                        final_map[attribute][att][5] += 1
    counter += 1
    if DEBUG: print("{0} Attributes remaining".format(len(list(attribute_list))-counter))

counter = 0
for attribute in attribute_list:
    if total_counts[attribute][1] >= 5 and total_counts[attribute][2] >= 5 and unconditional_chi_test(attribute)[0]:
        f = open("conditional_output/{0}.csv".format(attribute.replace(":","")), "x")
        f.write(attribute + "\n")
        f.write(
            "Attribute_Name,Total_Unique_Count,Malicious_Unique_Count,Beneign_Unique_Count\n")
        for att in final_map[attribute]:
            if total_counts[att][1] >= 5 and total_counts[att][2] >= 5 and unconditional_chi_test(att)[0] and \
                    float(final_map[attribute][att][1])/total_counts[attribute][0] > LIMIT and conditional_chi_test(att, attribute)[0]:
                write_string = "{0},{1},{2},{3}\n".format(att,final_map[attribute][att][1],
                                                                  final_map[attribute][att][3],
                                                                  final_map[attribute][att][5])
                f.write(write_string)
        counter += 1
        if DEBUG: print("{0} Attribute Writes remaining".format(len(list(attribute_list))-counter))
        f.close()