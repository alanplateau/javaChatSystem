package client;

public class PartitionString {
    String sum[];
    int index;
    //按splittarget分割target字符串
    public PartitionString(String target,String splittarget){
        sum=target.split(splittarget);
        index=0;
    }
    public String nextpart(){
        index++;
        return sum[index-1];
    }
}
