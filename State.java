package view;

import java.util.Objects;

class State implements Comparable<State> {

    public enum CellState {UNVISITED, OPEN, BLOCKED}

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

    int h;
    int f;
    int g;
    CellState cellState = CellState.UNVISITED;
    int priority = 0;
    Pos pos;
    Pos prevPos;

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
        return this.f - that.f;
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
                '}';
    }
}