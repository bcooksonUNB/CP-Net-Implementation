#support_list = [0.7,0.65,0.6,0.55,0.5,0.45,0.4,0.35,0.3,0.25,0.2,0.15,0.1,0.05]
support_list = [0.7,0.6,0.5,0.4,0.3,0.2,0.1]
conditional_support_list = [0.8,0.7,0.6,0.5,0.4,0.3,0.2,0.1]
get = "Accuracy"
output_name = "AccGridMethod4Chi10Cycle1Test"
output = open("../Output/_Spreadsheets/{0}.csv".format(output_name),"w")
output.write(',' + ','.join([str(x) for x in conditional_support_list]) + "\n")

for s in support_list:
    output.write(str(s) + ",")
    for c in conditional_support_list:
        fName = "s{0}c{1}l1_cycle3_method4_chi10_output.txt".format(str(int(s*100)),str(int(c*100)))
        f = open("../Output/{0}".format(fName))
        names = f.readline()
        names = [x.strip() for x in names.split(",")]
        # print(s,c)
        # print(names)
        index = names.index(get)
        ave = 0
        for l in f:
            l = l.split(',')
            ave += float(l[index])
        output.write(str(ave/5) + ",")
        f.close()
    output.write("\n")
output.close()