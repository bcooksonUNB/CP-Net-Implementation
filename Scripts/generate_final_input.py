import sys
import os

DEBUG = True

def runMain(dir_name):
    file_name_list = os.listdir("../Output/{0}/conditional_statements".format(dir_name))
    w = open("../Output/{0}/cp_net_input.csv".format(dir_name),"x")
    for name in file_name_list:
        r = open("../Output/{0}/conditional_statements/{1}".format(dir_name,name))
        pat_details_string = r.readline().strip()
        r.readline()
        for line in r:
            line = [x.strip() for x in line.split(",")]
            pat_details_string += "," + line[0] + "," + line[1]
        w.write(pat_details_string + "\n")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        raise Exception("Input a directory name for output")
    dir_name = sys.argv[1]
    runMain(dir_name)