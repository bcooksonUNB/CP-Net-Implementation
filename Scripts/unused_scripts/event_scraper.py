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


#total,total_unique,mal,mal_unique,ben,ben_unique
final_attribute_list = {x:[0,0,0,0,0,0] for x in attribute_list}
for name in event_list:
    is_malicious = True
    if 'benign' in name: is_malicious = False
    for att in event_list[name]:
        final_attribute_list[att][0] += event_list[name][att]
        final_attribute_list[att][1] += 1
        if is_malicious:
            final_attribute_list[att][2] += event_list[name][att]
            final_attribute_list[att][3] += 1
        else:
            final_attribute_list[att][4] += event_list[name][att]
            final_attribute_list[att][5] += 1
    if DEBUG: print("Done Processing {0}".format(name))

f = open("output.csv","x")
f.write("Attribute_Name,Total_Count,Total_Unique_Count,Malicious_Count,Malicious_Unique_Count,Beneign_Count,Beneign_Unique_Count\n")
for att in final_attribute_list:
    write_string = "{0},{1},{2},{3},{4},{5},{6}\n".format(att,final_attribute_list[att][0],final_attribute_list[att][1],final_attribute_list[att][2],
                                                            final_attribute_list[att][3],final_attribute_list[att][4],final_attribute_list[att][5])
    f.write(write_string)
    if DEBUG: print("Done Writing {0}".format(att))

f.close()
