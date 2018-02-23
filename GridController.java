package view;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

public class GridController {

    Pos startPos;
    Pos targetPos;
    Map<Pos, State> closedMap = new HashMap<>();
    BinaryHeap<State> openList = new BinaryHeap<>();
    Stack<Pos> routeStack = new Stack<>();

    @FXML
    GridPane grid;
    private Random random = new Random();

    State[][] array;

    int totalCells = 0;
    int initCount  = 0;
    int gridSize = 0;

    public void makeGrid(int size){


        totalCells = size * size;
        array = new State[size][size];

        gridSize = size;
        initArray(size);

        setArrayValues(size);

        int closed = 0, open = 0;
        int min = 0, max = 9;
        int rowNum = size, colNum = size;
        //grid.getColumnConstraints().add(new ColumnConstraints(gridWidth));
        //grid.getRowConstraints().add(new RowConstraints(gridHeight));

        Random rand = new Random();
        Color[] colors = {Color.BLACK, Color.BLUE, Color.GREEN, Color.RED};

//        int n = rand.nextInt(4)+1;
        for (int row = 0; row < rowNum; row++) {
            for (int col = 0; col < colNum; col++) {
                int n = rand.nextInt(4);
                Rectangle rec = new Rectangle();
                rec.setWidth(30);
                rec.setHeight(30);

//                int val = random.nextInt(max - min + 1) + min;

                State s = array[row][col];

                if (s.cellState.equals(State.CellState.BLOCKED))
                 {
//                if (row % 10 == 0 || col % 2 == 0)
                    rec.setFill(Color.BLACK);
                    closed++;
                }
                else{
                        rec.setFill(Color.TRANSPARENT);
                        open++;
                    }

                rec.setOnMouseClicked(new EventHandler<MouseEvent>()
                {
                    @Override
                    public void handle(MouseEvent t) {

                        int cellRow = (int) rec.getX();
                        int cellCol = (int) rec.getY();
                        System.out.println("User clicked on row: " + cellRow + " col: " + cellCol);
                        State s = array[cellRow][cellCol];
                        if (startPos == null) {
                            startPos = new Pos(cellRow, cellCol);
                            rec.setFill(Color.RED);
                        }
                        else {
                            targetPos = new Pos(cellRow, cellCol);
                            rec.setFill(Color.BLUE);
                        }
                        System.out.println(s);
                    }
                });
                rec.setStroke(Color.BLACK);
                rec.setX(row);
                rec.setY(col);
                GridPane.setRowIndex(rec, row);
                GridPane.setColumnIndex(rec, col);
                grid.getChildren().addAll(rec);
            }
        }

        System.out.println("Open: " + open + " closed: " + closed);



    }

    private void displayMessage(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle(title);
        alert.setHeaderText("");
        alert.setContentText(text);
        alert.showAndWait();
    }
    @FXML
    public void doSearch() {
        System.out.println("Starting search");

        if (startPos == null || targetPos == null) {

            displayMessage("Missing Input", "Starting and ending position must be selected before searching" );

        }
        else {
            searchGrid();
        }
    }

    @FXML
    public void doRestart() {
        System.out.println("Doing restart");
        startPos = null;
        targetPos = null;
        routeStack.clear();
        closedMap.clear();
        openList = new BinaryHeap<>();
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                State s = array[row][col];
                s.prevPos = null;
                s.f = 0;
                s.g = Integer.MAX_VALUE;
                s.h = 0;
                s.priority = 0;
                Rectangle rec1 = (Rectangle) getNodeFromGridPane(row, col);
                if (rec1.getFill().equals(Color.GREEN)
                        || rec1.getFill().equals(Color.RED)
                        || rec1.getFill().equals(Color.BLUE))
                 rec1.setFill(Color.TRANSPARENT);
            }
        }
    }

    private void setArrayValues(int size) {

        // pick a random row and column
        int max = size - 1;
        int min = 0;
        int col = random.nextInt(max - min + 1) + min;
        int row = random.nextInt(max - min + 1) + min;

        Stack<State> cellStack = new Stack<>();
        State s = array[row][col];

        s.cellState = State.CellState.OPEN;

        initCount++;
        // pick a random neighbor

      while(initCount < totalCells) {
          setNeighbor(row, col, cellStack);
          col = random.nextInt(max - min + 1) + min;
          row = random.nextInt(max - min + 1) + min;
      }
    }

    private boolean setNeighbor(int row, int col, Stack<State> cellStack) {

        // pick a random neighbor

        Pos east = new Pos(row, col+1);
        Pos west = new Pos(row,col-1);
        Pos north =  new Pos(row+1, col);
        Pos south = new Pos(row -1, col);

        Pos[] neighbors = {east, west, north, south};

       State s = null;
        int tryCount = 0;
        Pos pos = null;

        /*
        Try all four neighbors in random fashion
         */
        while (tryCount < 4) {

            int rand = random.nextInt(3 - 0 + 1) + 0;

            pos = neighbors[rand];

            if (pos != null && isUnvisited(pos)) {
                s = array[pos.row][pos.col];
                break;
            }
            else if (pos != null){
                neighbors[tryCount] = null;
                tryCount++;
            }

        }
        if (s == null) {
           if (! cellStack.isEmpty()) {
               s = cellStack.pop();
               setNeighbor(s.pos.row, s.pos.col, cellStack);
           }

           else {
               System.out.println("Stack is empty " + initCount + " cells initialized");
               return false;
           }
        }
        else {
            s.cellState = getRandomCellState();
            initCount++;

            if (initCount >= totalCells) {
                System.out.println("All " + initCount + " cells initialized");

            }
            else {
                cellStack.push(s);
                setNeighbor(s.pos.row, s.pos.col, cellStack);
            }
        }
        return true;
    }

    private State.CellState getRandomCellState() {


        // 30% probability of cell being blocked
        if (Math.random() < .3) {
            return State.CellState.BLOCKED;
        }
        else {
            return State.CellState.OPEN;
    }

}

    private void initArray(int size) {

        int rowNum = size, colNum = size;

        // Initialize the array to unvisited

        for (int row = 0; row < rowNum; row++) {
            for (int col = 0; col < colNum; col++) {
                array[row][col] = new State(0, 0, State.CellState.UNVISITED, new Pos(row, col));
            }
        }

    }

    private boolean isUnvisited(Pos pos) {

        if (pos.row < 0) return false;
        if (pos.col < 0) return false;
        if (array.length <= pos.row) return false;
        if (array[pos.row].length <= pos.col) return false;


        return array[pos.row][pos.col].cellState.equals(State.CellState.UNVISITED);
    }

    private Node getNodeFromGridPane( int row, int col) {
        for (Node node : grid.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }
    public void searchGrid() {


        State start = array[startPos.row][startPos.col];

        State target = array[targetPos.row][targetPos.col];

        start.g = 0;
        target.g = Integer.MAX_VALUE;
        start.h = getDistance(targetPos, new Pos(startPos.row, startPos.col));
        start.f = start.g + start.h;

        openList.add(start);
        Pos currentPos = startPos;

        Pos pos = getNextState();

        int counter = 0;
        boolean listIsEmpty = openList.isEmpty();
        while (!listIsEmpty) {
            while (!pos.equals(targetPos)) {

                pos = getNextState();
                if (pos == null) {
                    System.out.println("Cannot find next position");
                    break;
                }
            }
            if (pos != null && pos.equals(targetPos)) {
                System.out.println("I reached the target");
                break;
            }
            if (!openList.isEmpty()) {
                State s = openList.peek();
                System.out.println("Peeked: " + s + " from openList");
                pos = s.pos;
            } else {
                listIsEmpty = true;
            }
        }

        if (openList.isEmpty()) {
            System.out.println("I cannot reach the target");
        displayMessage("Target not found", "Target cannot be reached");
        }
        else {
            // go back from goal to target

            State s = array[pos.row][pos.col];

            int pathCount = 0;
            while (s.prevPos != null) {
                System.out.println(s.pos);
                routeStack.push(s.pos);
                s = array[s.prevPos.row][s.prevPos.col];
                if (s.pos.equals(start.pos)) {
                    System.out.println(s.pos);
                    routeStack.push(s.pos);
                    break;
                }
                pathCount++;
                if (pathCount > totalCells) {
                    System.out.println("Problem displaying route");
                    break;
                }
            }

            System.out.println("\nRoute");
            while (! routeStack.empty()) {
                pos = routeStack.pop();
                Rectangle rec1 = (Rectangle) getNodeFromGridPane(pos.row, pos.col);

                if (! pos.equals(startPos) && ! pos.equals(targetPos))
                rec1.setFill(Color.GREEN);
                System.out.println(pos);
            }
        }
    }

    private Pos getNextState() {


        if (openList.isEmpty()) {   // should not happen
            return null;
        }

        State currentState = openList.remove();
        Pos currentPos = currentState.pos;
        System.out.println("Removed: " + currentState + " from openList");


        closedMap.put(currentPos, currentState);
        List<State> stateList = new ArrayList<State>();

        // get col distance
        int row = currentPos.row, col = currentPos.col;
        int east = currentPos.col + 1;
        int west = currentPos.col - 1;
        int north = currentPos.row - 1;
        int south = currentPos.row + 1;

        int eastDistance = Integer.MAX_VALUE, westDistance = Integer.MAX_VALUE,
                northDistance = Integer.MAX_VALUE, southDistance = Integer.MAX_VALUE;

        State eastState = null, westState = null, northState = null, southState = null;

        if (isOpen(row, east)) {
            eastState = array[row][east];
            eastDistance = getDistance(targetPos, new Pos(row, east));
            eastState.g = currentState.g + 1; //getDistance(startPos, new Pos(row, east));
            eastState.h = eastDistance;
            eastState.f = eastState.g + eastState.h;
//            eastState.open = true;

            if (targetPos.col > currentPos.col) {
                eastState.priority++;
            } else if (east < currentPos.col) {
                eastState.priority--;
            }
            stateList.add(eastState);

            openList.add(eastState);
//            System.out.println("Setting eastState.prePos to " + currentPos);
            eastState.prevPos = currentPos;
        }

        if (isOpen(row, west)) {
            westState = array[row][west];
            westDistance = getDistance(targetPos, new Pos(row, west));
            westState.g = currentState.g + 1; //getDistance(startPos, new Pos(row, west));
            westState.h = westDistance;
//            westState.open = true;
            westState.f = westState.g + westState.h;
            if (targetPos.col < currentPos.col) {
                westState.priority++;
            } else if (west < currentPos.col) {
                westState.priority--;
            }
            stateList.add(westState);
            openList.add(westState);
//            System.out.println("Setting westState.prePos to " + currentPos);
            westState.prevPos = currentPos;
        }

        if (isOpen(south, col)) {
            southState = array[south][col];
            southDistance = getDistance(targetPos, new Pos(south, col));
            southState.g = currentState.g + 1; //getDistance(startPos, new Pos(south, col));
            southState.h = southDistance;
//            southState.open = true;
            southState.f = southState.g + southState.h;
            if (targetPos.row > currentPos.row) {
                southState.priority++;
            } else if (south < currentPos.row) {
                southState.priority--;
            }
            stateList.add(southState);
            openList.add(southState);
//            System.out.println("Setting southState.prePos to " + currentPos);
            southState.prevPos = currentPos;
        }

        if (isOpen(north, col)) {
            northState = array[north][col];
            northDistance = getDistance(targetPos, new Pos(north, col));
            northState.g = currentState.g + 1; //getDistance(startPos, new Pos(north, col));
            northState.h = northDistance;
//            northState.open = true;
            northState.f = northState.h + northState.g;
            if (targetPos.row < currentPos.row) {
                northState.priority++;
            } else if (north > currentPos.col) {
                northState.priority--;
            }

            stateList.add(northState);
            openList.add(northState);
//            System.out.println("Setting northState.prePos to " + currentPos);
            northState.prevPos = currentPos;
        }


        // Sort on lowest f, then highest g, then highest priority

//        stateList.sort(Comparator.comparing((State s) -> s.f).thenComparing(s -> s.g).thenComparing(s -> s.priority).reversed());
        stateList = stateList.stream().sorted(comparing(State::getF).
                thenComparing(comparing(State::getG).reversed())
                .thenComparing((comparing(State::getPriority).reversed()))).collect(Collectors.toList());
        System.out.println(stateList);

        State nextState = null;
        if (!stateList.isEmpty()) {
            nextState = stateList.get(0);
            nextState.g = 0;

//            openList.add(nextState);

            System.out.println("next state " + nextState.pos + "  is open ? " + (nextState.cellState.equals(State.CellState.OPEN)));
//            System.out.println(openList.toString());
        }


        return (nextState != null) ? nextState.pos : null;
    }

    private boolean isOpen(int row, int col) {

        if (row < 0) return false;
        if (col < 0) return false;
        if (array.length <= row) return false;
        if (array[row].length <= col) return false;
        if (closedMap.containsKey(new Pos(row, col))) return false;

        return array[row][col].cellState.equals(State.CellState.OPEN);
    }

    private void printGrid(State[][] array) {
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 5; column++)
                System.out.print("[" + row + "," + column + "] = " + array[row][column].cellState.toString()+ " ");
            System.out.println("");
        }
    }


    int getDistance(Pos pos1, Pos pos2) {
        return Math.abs(pos1.row - pos2.row) + Math.abs(pos1.col - pos2.col);
    }
}
