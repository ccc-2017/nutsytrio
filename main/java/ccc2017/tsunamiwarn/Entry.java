package ccc2017.tsunamiwarn;

/**
 * Created by Herman on 6/21/2017.
 */

public class Entry {
     final String event;
     final String time;

     Entry(String event, String time) {
        this.time = IsoToTime(time);
        this.event = event;
     }

     private String IsoToTime(String input){
         return input.substring(11,16)+" HST";
     }
}
