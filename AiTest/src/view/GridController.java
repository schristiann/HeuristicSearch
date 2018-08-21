package view;

/*
 * Sam Christian and Brad Mitchell
 */

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

public class GridController {
	public enum SortType{FORWARD, BACKWARD, ADAPTIVE}
	
	int newPath=0;
	
	State start=null;
	
	SortType sortType=null;
	
    Pos startPos;
    Pos targetPos;
    Map<Pos, State> closedMap = new HashMap<>();
    BinaryHeap<State> openList = new BinaryHeap<>();
    List<Pos> routeList = new ArrayList<>();
    private Map<Pos, State> blockedMap = new HashMap<>();
    

//
//    String states = "0,0,0:0,1,0:0,2,0:0,3,0:0,4,0:"
//            + "1,0,0:1,1,0:1,2,1:1,3,0:1,4,0:"
//            + "2,0,0:2,1,0:2,2,1:2,3,1:2,4,0:"
//            + "3,0,0:3,1,0:3,2,1:3,3,1:3,4,0:"
//            + "4,0,0:4,1,0:4,2,0:4,3,1:4,4,0";

    @FXML
    GridPane grid;
    private Random random = new Random();

    State[][] array;

    int totalCells = 0;
    int initCount  = 0;
    int gridSize = 0;


    public void loadGridFromFile(File file) {

        try {
            byte[] data = Files.readAllBytes(file.toPath());
            if (data.length == 0) {
                throw new IOException("Input file is empty");
            }
            String gridInfo = new String(data);


            String[] cellStates = gridInfo.split(":");

            gridSize = (int) Math.sqrt(cellStates.length);
            totalCells = gridSize * gridSize;
            System.out.println("Creating grid of size " + gridSize + "x" + gridSize);
            array = new State[gridSize][gridSize];

            for (String cellState: cellStates) {
                String[] values = cellState.split(",");
                int r = Integer.valueOf(values[0]);
                int c = Integer.valueOf(values[1]);

                int s = Integer.valueOf(values[2]);
                State state = new State(0,0, (s ==0 ) ? State.CellState.OPEN : State.CellState.BLOCKED, new Pos(r,c));

                array[r][c] = state;
//            state.cellState = (s ==0 ) ? State.CellState.OPEN : State.CellState.BLOCKED;
//
//            state.pos = new Pos(r,c);
            }
        }
        catch (IOException ioe) {
            displayMessage("Error", "Cannot open file " + file.getAbsolutePath());
            System.exit(1);
        }

    }

    public void makeGrid(int defaultSize){

        File file = new File("./gridinfo.txt");

        if ( file.exists() && file.length() > 0) {
            loadGridFromFile(file);
        }

        else {
            totalCells = defaultSize * defaultSize;
            array = new State[defaultSize][defaultSize];

            gridSize = defaultSize;
        initArray(defaultSize);

        setArrayValues(defaultSize);


        }

        int recSize = 30;

        if (array.length > 5)
            recSize = 15;
        if (array.length > 30)
            recSize = 10;

        if (array.length > 50)
            recSize = 8;
        if (array.length > 70)
            recSize = 6;

        int closed = 0, open = 0;
        int min = 0, max = 9;
        int rowNum = array.length, colNum = array.length;
        //grid.getColumnConstraints().add(new ColumnConstraints(gridWidth));
        //grid.getRowConstraints().add(new RowConstraints(gridHeight));

        Random rand = new Random();
        Color[] colors = {Color.BLACK, Color.BLUE, Color.GREEN, Color.RED};

//        int n = rand.nextInt(4)+1;
        for (int row = 0; row < rowNum; row++) {
            for (int col = 0; col < colNum; col++) {
                int n = rand.nextInt(4);
                Rectangle rec = new Rectangle();
                rec.setWidth(recSize);
                rec.setHeight(recSize);

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
    public void doForwardSearch() {
        System.out.println("Starting forward search");
        
        sortType=SortType.FORWARD;

        if (startPos == null || targetPos == null) {

            displayMessage("Missing Input", "Starting and ending position must be selected before searching" );

        }
        else {
        	long startTime=System.currentTimeMillis();
            searchGrid();
        System.out.println("search time is: "+ (System.currentTimeMillis()-startTime+"milliseconds"));
        }
    }

    @FXML
    public void doBackwardSearch() {
        System.out.println("Starting backwards search");

        sortType=SortType.BACKWARD;
        
        if (startPos == null || targetPos == null) {

            displayMessage("Missing Input", "Starting and ending position must be selected before searching" );
            
        }  
        else {
        	long startTime=System.currentTimeMillis();
            searchGrid();
        System.out.println("search time is: "+ (System.currentTimeMillis()-startTime+"milliseconds"));
        }
    }
    
    @FXML
    public void doAdaptiveSearch() {
        System.out.println("Starting adaptive search");
        
        sortType=SortType.ADAPTIVE;

        if (startPos == null || targetPos == null) {

            displayMessage("Missing Input", "Starting and ending position must be selected before searching" );

        }
        else {
        	long startTime=System.currentTimeMillis();
            searchGrid();
        System.out.println("search time is: "+ (System.currentTimeMillis()-startTime+"milliseconds"));
        }
    }
    
    
    @FXML
    public void doRestart() {
        System.out.println("Doing restart");
        startPos = null;
        targetPos = null;
        routeList.clear();
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
        initHvalues();

        start = array[startPos.row][startPos.col];
        State target = array[targetPos.row][targetPos.col];

        
        if(sortType.equals(SortType.FORWARD)) {
        start.g = 0;

        start.h = getDistance(targetPos, new Pos(startPos.row, startPos.col));
        start.f = start.g + start.h;
        }
        
        if(sortType.equals(SortType.BACKWARD)) {
            target.g = 0;

            target.h = getDistance(targetPos, new Pos(startPos.row, startPos.col));
            target.f = target.g + target.h;
            }


        int counter = 0;

        while (! start.equals(target)) {

            // add blocked neighbors to map

            addBlockedNeighbors(start);

            counter++;
            
            if(sortType.equals(SortType.ADAPTIVE)) {
            	initializeState(start, counter);
            	initializeState(target,counter);
            }
            start.g=0;
            if(sortType.equals(SortType.FORWARD)) {
            	 start.g = 0;
            	 target.g = Integer.MAX_VALUE;
            }
            if(sortType.equals(SortType.BACKWARD)) {
           	 target.g = 0;
           	 start.g = Integer.MAX_VALUE;
           }
            start.search = counter;
            target.search = counter;
            

            closedMap.clear();
            routeList.clear();
            openList = new BinaryHeap<>();
            if(sortType.equals(sortType.BACKWARD)) {
            	openList.add(target);
            }
            else {
            	openList.add(start);
            }

            computePath(counter);

            if (openList.isEmpty()) {
                displayMessage("No target", "Cannot find target");
                break;
            }

            State s=null;
            
            if(sortType.equals(SortType.BACKWARD)) {
                s = array[start.pos.row][start.pos.col];
                }
            
            else{
            s = array[targetPos.row][targetPos.col];
            }

            int pathCount = 0;
            
            while (s.prevPos != null) {
                System.out.println(s.pos);
                routeList.add(s.pos);
                s = array[s.prevPos.row][s.prevPos.col];
                if (s.pos.equals(start.pos)) {
                    System.out.println(s.pos);
                    routeList.add(s.pos);
                    break;
                }
                pathCount++;
                if (pathCount > totalCells) {
                    System.out.println("Problem displaying route");
                    break;
                }
            }
            if(sortType.equals(SortType.BACKWARD)) {
            	routeList.add(s.pos);
            }
            System.out.println("\nRoute");
           
           
           if(sortType.equals(SortType.BACKWARD)) {
        	   for(int i=0;i<routeList.size();i++) {
                   Pos pos = routeList.get(i);

                   if (array[pos.row][pos.col].cellState.equals(State.CellState.BLOCKED))
                   {
                       break;
                   }
                   System.out.println(pos);
                   start = array[pos.row][pos.col];
                   Rectangle rec1 = (Rectangle) getNodeFromGridPane(pos.row, pos.col);
                       rec1.setFill(Color.GREEN);
               }
        	   
           }
           
           else {
            for(int i=routeList.size()-1;i>=0;i--) {
                Pos pos = routeList.get(i);

                if (array[pos.row][pos.col].cellState.equals(State.CellState.BLOCKED))
                {
                    break;
                }
                System.out.println(pos);
                start = array[pos.row][pos.col];
                Rectangle rec1 = (Rectangle) getNodeFromGridPane(pos.row, pos.col);
                    rec1.setFill(Color.GREEN);
            }
           }
           
           
        }

        System.out.println("I hit the target");

    }

    private void addBlockedNeighbors(State start) {

        Pos currentPos = start.pos;

        int east = currentPos.col + 1;
        int west = currentPos.col - 1;
        int north = currentPos.row - 1;
        int south = currentPos.row + 1;

        if (isBLockedCell(currentPos.row, east)) {
            blockedMap.put(new Pos(currentPos.row, east), array[currentPos.row] [east]);
        }

        if (isBLockedCell(currentPos.row, west)) {
            blockedMap.put(new Pos(currentPos.row, west), array[currentPos.row] [west]);
        }

        if (isBLockedCell(south, currentPos.col)) {
            blockedMap.put(new Pos(south, currentPos.col), array[south] [currentPos.col]);
        }

        if (isBLockedCell(north, currentPos.col)) {
            blockedMap.put(new Pos(north, currentPos.col), array[north] [currentPos.col]);
        }
    }

    private boolean isBLockedCell(int row, int col) {
        if (row < 0) return false;
        if (col < 0) return false;
        if (array.length <= row) return false;
        if (array[row].length <= col) return false;

        return array[row][col].cellState.equals(State.CellState.BLOCKED);
    }




    private void initHvalues() {
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c< gridSize; c++) {
                State s = array[r][c];
                s.h = getDistance(targetPos, new Pos(r,c));
                s.search = 0;
            }
        }
    }

    private void computePath(int counter) {
    	State targetState=null;
    	
    	if(sortType.equals(SortType.BACKWARD)) {
    			targetState = array[start.pos.row][start.pos.col];
    	}
        
    	else  {
			targetState = array[targetPos.row][targetPos.col];
	}
    		State currentState=null;
        while (targetState.g > openList.peek().g) {


            currentState = openList.remove();
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

            if (isValidAction(row, east)) {
                eastState = array[row][east];
                if(sortType.equals(SortType.ADAPTIVE)) {
                	initializeState(eastState,counter);
                }
                else if (eastState.search < counter) {
                    eastState.g = Integer.MAX_VALUE;
                    eastState.search = counter;
                }
                if (eastState.g > currentState.g + 1) {
                    eastState.g = currentState.g + 1;
                    eastState.prevPos = currentPos;

                    if (! openList.isEmpty() && openList.peek().equals(eastState)) {
                        State temp = openList.remove();
                        System.out.println("removing east: " + temp);

                    }
                    eastDistance = getDistance(targetPos, new Pos(row, east));

                    eastState.h = eastDistance;
                    eastState.f = eastState.g + eastState.h;
                    eastState.prevPos = currentPos;
                    openList.add(eastState);

                }


                if (targetPos.col > currentPos.col) {
                    eastState.priority++;
                } else if (east < currentPos.col) {
                    eastState.priority--;
                }
                stateList.add(eastState);

            }

            if (isValidAction(row, west)) {
                westState = array[row][west];
                if(sortType.equals(SortType.ADAPTIVE)) {
                	initializeState(westState,counter);
                }
                
                else if (westState.search < counter) {
                    westState.g = Integer.MAX_VALUE;
                    westState.search = counter;
                }

                if (westState.g > currentState.g + 1) {
                    westState.g = currentState.g + 1;
                    westState.prevPos = currentPos;

                    if (! openList.isEmpty() && openList.peek().equals(westState)) {
                        State temp = openList.remove();
                        System.out.println("removing west: " + temp );

                    }

                    westDistance = getDistance(targetPos, new Pos(row, west));

                    westState.h = westDistance;
                    westState.f = westState.g + westState.h;
                    openList.add(westState);
                    westState.prevPos = currentPos;
                }


                if (targetPos.col < currentPos.col) {
                    westState.priority++;
                } else if (west < currentPos.col) {
                    westState.priority--;
                }
                stateList.add(westState);


            }

            if (isValidAction(south, col)) {
                southState = array[south][col];
                if(sortType.equals(SortType.ADAPTIVE)) {
                	initializeState(southState,counter);
                }
                
                else if (southState.search < counter) {
                    southState.g = Integer.MAX_VALUE;
                    southState.search = counter;
                }

                if (southState.g > currentState.g + 1) {
                    southState.g = currentState.g + 1;
                    southState.search = counter;

                    if (! openList.isEmpty() && openList.peek().equals(southState)) {
                        State temp = openList.remove();
                        System.out.println("removing south: " + temp);

                    }

                    southDistance = getDistance(targetPos, new Pos(south, col));

                    southState.h = southDistance;
                    southState.f = southState.g + southState.h;
                    openList.add(southState);
                    southState.prevPos = currentPos;
                }


                if (targetPos.row > currentPos.row) {
                    southState.priority++;
                } else if (south < currentPos.row) {
                    southState.priority--;
                }
                stateList.add(southState);

            }

            if (isValidAction(north, col)) {
                northState = array[north][col];
                if(sortType.equals(SortType.ADAPTIVE)) {
                	initializeState(northState,counter);
                }
                else if (northState.search < counter) {
                    northState.g = Integer.MAX_VALUE;
                    northState.search = counter;
                }

                if (northState.g > currentState.g + 1) {
                    northState.g = currentState.g + 1;
                    northState.search = counter;


                    if (! openList.isEmpty() && openList.peek().equals(northState)) {
                        State temp = openList.remove();
                        System.out.println("removing north: " + temp);

                    }
                    northDistance = getDistance(targetPos, new Pos(north, col));

                    northState.h = northDistance;
                    northState.f = northState.h + northState.g;

                    openList.add(northState);
                    northState.prevPos = currentPos;
                }


                if (targetPos.row < currentPos.row) {
                    northState.priority++;
                } else if (north > currentPos.col) {
                    northState.priority--;
                }

                stateList.add(northState);

            }

            if (openList.isEmpty()) {
                System.out.println("Openlist is empty in compute path, breaking loop");
                break;
            }
        }
        if(currentState!=null) {
        newPath=currentState.g;
        }
    }

    private void initializeState(State s, int counter) {

        State target = array[targetPos.row][targetPos.col];
        if (s.search != counter && s.search > 0) {
            
            if (s.g + s.h <  newPath) {
                s.h = newPath - s.g;
            }
            s.g = Integer.MAX_VALUE;
        }
        else if (s.search == 0) {
            s.g = Integer.MAX_VALUE;
            s.h = getDistance(s.pos, target.pos);
        }
        
        s.search = counter;
    }
    
    
    private boolean isValidAction(int row, int col) {
        if (row < 0) return false;
        if (col < 0) return false;
        if (array.length <= row) return false;
        if (blockedMap.containsKey(new Pos(row, col))) return false;
        if (closedMap.containsKey(new Pos(row, col))) return false;
        if (array[row].length <= col) return false;
        return true;
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

    public void saveGrid() {


        File file = new File("./gridinfo.txt");

        StringBuilder sb = new StringBuilder();
        if ( !file.exists() || file.length() == 0) {
            for (int row = 0; row < gridSize; row++) {

                for (int col = 0; col < gridSize; col++) {
                    State s = array[row][col];

                    if (sb.length() > 0) {
                        sb.append(":");
                    }
                    sb.append(String.valueOf(row)).append(",")
                            .append(String.valueOf(col)).append(",")
                            .append((s.cellState.equals(State.CellState.OPEN)) ? "0" : "1");

                }

            }
            try (PrintStream fileStream = new PrintStream(file)) {
                fileStream.print(sb.toString());
            }
            catch (FileNotFoundException fne) {
                System.out.println("Failed to save gridinfo file");
            }
        }
    }
}