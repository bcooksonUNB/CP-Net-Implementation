public class Util {

    public static double average(double... vars){
        double total = 0;
        for(double d : vars){
            total += d;
        }
        return total/vars.length;
    }


}
