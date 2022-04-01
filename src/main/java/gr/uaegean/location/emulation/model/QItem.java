package gr.uaegean.location.emulation.model;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

// QItem for current location and distance
// from source location
@Getter
@Setter
@ToString
public class QItem {
    int row;
    int col;
    int dist;
    String parent;

    public QItem(int row, int col, int dist){
        this.row = row;
        this.col = col;
        this.dist = dist;
    }

    public QItem(int row, int col, int dist, String parent){
        this.row = row;
        this.col = col;
        this.dist = dist;
        this.parent = parent;
    }
}