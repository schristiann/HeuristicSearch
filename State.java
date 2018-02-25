package view;



import java.util.Objects;

class State implements Comparable<State> {

    int h;
    int f;
    int g;
    CellState cellState = CellState.UNVISITED;
    int priority = 0;
    Pos pos;
    Pos prevPos;
    int search = 0;

    public State(int g, int h, CellState cellState, Pos pos) {
        this.g = g;
        this.h = h;
        this.cellState = cellState;
        this.pos = pos;
    }

    public int getH() {
        return h;
    }

    public int getF() {
        return f;
    }

    public int getG() {
        return g;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return Objects.equals(pos, state.pos);
    }

    @Override
    public int hashCode() {

        return Objects.hash(pos);
    }

    @Override
    public int compareTo(State that) {

        int result = this.f - that.f;

        if (result == 0 )  {
            result = that.g - this.g;
        }
        return result;
    }

    @Override
    public String toString() {
        return "State{" +
                "h=" + h +
                ", f=" + f +
                ", g=" + g +
                ", cellState=" + cellState +
                ", priority=" + priority +
                ", pos=" + pos +
                ", prevPos=" + prevPos +
                ", search=" + search +
                '}';
    }

    public enum CellState {UNVISITED, OPEN, BLOCKED}
}
