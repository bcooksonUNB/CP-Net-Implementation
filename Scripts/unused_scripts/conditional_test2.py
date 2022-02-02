import os
DEBUG = True

LIMIT = 0.2

file_name_list = os.listdir("conditional_output")

event_list = {}

f = open("output.csv")
next_attribute = f.readline()
next_attribute = f.readline()
while next_attribute:
    next_attribute = next_attribute.strip().split(",")
    event_list[next_attribute[0]] = float(next_attribute[4])/float(next_attribute[2])
    next_attribute = f.readline()
f.close()

conditional_map = {}

for name in file_name_list:
    mal_flag = True
    f = open("conditional_output/" + name)
    event_name = f.readline().strip()
    if event_list[event_name] < 0.8247:
        mal_flag = False

    conditional_map[event_name] = []
    next_line = f.readline()
    next_line = f.readline()
    while next_line:
        next_line = next_line.strip().split(",")
        if mal_flag:
            if float(next_line[1]) != 0 and (float(next_line[2])/float(next_line[1])) < 0.8247:
                conditional_map[event_name].append( (next_line[0],(float(next_line[2])/float(next_line[1]))))
        else:
            if float(next_line[1]) != 0 and (float(next_line[2])/float(next_line[1])) >= 0.8247:
                conditional_map[event_name].append( (next_line[0],(float(next_line[2])/float(next_line[1]))))
        next_line = f.readline()

f = open("cond_output6.csv","x")
for name in conditional_map:
    temp_string = name + "," + str(event_list[name])
    for c in conditional_map[name]:
        temp_string += "," + c[0] + "," + str(c[1])
    f.write(temp_string + "\n")
f.close()
