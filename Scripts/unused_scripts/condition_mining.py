import os
DEBUG = True

file_name_list = os.listdir("csv-consec_identical_calls_removed")

attribute_list = set()
event_list = {}


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

counter = 0
for attribute in attribute_list:
    final_map[attribute] = {y:[0,0,0,0,0,0] for y in attribute_list if y != attribute}
    for name in event_list:
        if attribute in event_list[name]:
            is_malicious = True
            if 'benign' in name: is_malicious = False
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
    f = open("conditional_output/{0}.csv".format(attribute.replace(":","")), "x")
    f.write(attribute + "\n")
    f.write(
        "Attribute_Name,Total_Unique_Count,Malicious_Unique_Count,Beneign_Unique_Count\n")
    for att in final_map[attribute]:
        write_string = "{0},{1},{2},{3}\n".format(att,final_map[attribute][att][1],
                                                              final_map[attribute][att][3],
                                                              final_map[attribute][att][5])
        f.write(write_string)
    counter += 1
    if DEBUG: print("{0} Attribute Writes remaining".format(len(list(attribute_list))-counter))
    f.close()