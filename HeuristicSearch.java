


public class HeuristicSearch {
	
	public enum CellState{BLOCKED, OPEN, UNVISITED};
	
	class CellData{
		int h;
		int g;
		int f;
		CellState state;
		Cell pos;
		
		
		
		public CellData(int h, int g, CellState state, Cell pos) {
			this.h=h;
			this.g=g;
			this.state=state;
			this.pos=pos;
		}



		@Override
		public String toString() {
			return "CellData [h=" + h + ", g=" + g + ", f=" + f + ", state=" + state + ", pos=" + pos + "]";
		}
		
		
	}
	
	public class Cell{
		int row;
		int col;
		 
		public Cell(int x, int y) {
			this.row=x;
			this.col=y;
		}
		
		@Override
		public String toString() {
			return("["+row+","+col+"]");
		}
	}
	
	CellData [][] grid=new CellData [5][5];
	
	
	public static void main(String[] args) {
		HeuristicSearch heuristicSearch=new HeuristicSearch();
		heuristicSearch.loadGrid();
		heuristicSearch.printGrid();
		
	
	
		
	}
 
	public void loadGrid() {
		
		for(int row=0;row<grid.length;row++) {
			for(int col=0;col<grid[row].length;col++) {
				CellState cellState=CellState.OPEN;
				if(row==0&&col==3) {
					cellState=CellState.BLOCKED;
				}
				if(row==1&&(col==2||col==3)) {
					cellState=CellState.BLOCKED;
				}
				if(row==2&&(col==2||col==3)) {
					cellState=CellState.BLOCKED;
				}
				if(row==3&&col==2) {
					cellState=CellState.BLOCKED;
				}
				grid[row][col]=new CellData(Integer.MAX_VALUE, Integer.MAX_VALUE, cellState, new Cell (row,col));
			}	
		}
		
	
	}

	
public int geth(CellData current, CellData end) {
			return Math.abs(current.pos.row-end.pos.row)+Math.abs(current.pos.col-end.pos.col);
		}


public void printGrid() {
	for(int row=0;row<grid.length;row++) {
		for(int col=0;col<grid[row].length;col++) {
			System.out.println(grid[row][col]);
		}
	}
}
}
