package ie.ul.fitbook.ui.home;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivitiesModel extends Model implements Comparable<Model>{
double distance;
double elevation;
String timeStamp;
String activityId;

public ActivitiesModel(double distance, double elevation, String timeStamp, String activityId){
    this.distance = distance;
    this.elevation = elevation;
    this.timeStamp = timeStamp;
    this.activityId = activityId;
}

public double getDistance(){
    return distance;
}
public double getElevation(){
    return elevation;
}
public String getTimeStamp(){
    return timeStamp;
}
public String getActivityId(){
    return activityId;
}
public String getId(){
    return activityId;
}
public String getTime() throws ParseException {


    String newTimeStamp = timeStamp.replace("T", " ");

    String myDate = "2014/10/29 18:10:45";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");
    Date date = sdf.parse(newTimeStamp);
    long millis = date.getTime();


    return String.valueOf(millis);
}





















}
