package view;

/*
 * Sam Christian and Brad Mitchell
 */

import java.util.Objects;

class Pos {
    public Pos(int row, int col) {
        this.row = row;
        this.col = col;
    }
    int row;
    int col;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pos pos = (Pos) o;
        return row == pos.row &&
                col == pos.col;
    }

    @Override
    public int hashCode() {

        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return "["+row+","+col+"]";
    }


}