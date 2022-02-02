import sys
import os

DEBUG = True

def runMain(dir_name):
    file_name_list = os.listdir("../Output/{0}/conditional_pattern_output".format(dir_name))
    os.mkdir("../Output/{0}/conditional_statements".format(dir_name))
    
    for name in file_name_list:
        r = open("../Output/{0}/conditional_pattern_output/{1}".format(dir_name,name),"r")
        file_base_probs = r.readline()
        file_base_probs = [x.strip() for x in file_base_probs.split(",")]
        file_base_probs[1],file_base_probs[2],file_base_probs[3] = \
                            int(file_base_probs[1]),int(file_base_probs[2]),int(file_base_probs[3])

        w = open("../Output/{0}/conditional_statements/{1}".format(dir_name,name),"x")
        w.write("{0},{1}\n".format(file_base_probs[0], "malicious" if float(file_base_probs[2])/file_base_probs[1]>=0.8247 else "benign"))
        w.write("Pattern,Present,Absent\n")
        r.readline()
        for line in r:
            line = line.split(",")
            line = [x.strip() for x in line]
            line[1],line[2],line[3] = int(line[1]),int(line[2]),int(line[3])
            yes_val = True if float(line[2])/line[1] >= 0.8247 else False
            yes_list = ["malicious" if yes_val else "benign","malicious" if not yes_val else "benign"]
            w.write("{0},{1},{2}\n".format(line[0],yes_list[0],yes_list[1]))
        r.close()
        w.close()

if __name__ == "__main__":
    if len(sys.argv) < 2:
        raise Exception("Input a directory name for output")
    dir_name = sys.argv[1]
    runMain(dir_name)
