package onyx.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Board {
    private int boardSize;
    private StoneColor[][][] grid;
    private StoneColor turn;
    private boolean finished;
    private StoneColor winner;


    public Board(int boardSize) {
        if(boardSize % 2 == 1) boardSize++;
        this.boardSize = boardSize;

        this.grid = new StoneColor[3][this.boardSize][this.boardSize];
        for(int z = 0; z < 3; z++) {
            for(int x = 0; x < this.boardSize; x++) {
                for(int y = 0; y < this.boardSize; y++) {
                    this.grid[z][x][y] = StoneColor.BLANK;
                }
            }
        }
        this.turn = StoneColor.BLACK;
        this.finished = false;
        this.winner = StoneColor.BLANK;

        /*this.grid = new Spot[3][][];
        this.grid = new Spot[0][12][12];
        this.grid = new Spot[1][5][6];
        this.grid = new Spot[2][6][5];*/
    }

    public List<Coord> addStone(Coord point, StoneColor stoneColor) throws Exception {
        if(stoneColor != this.turn) throw new Exception("It is not your turn");
        if(!isAvailable(point)) throw new Exception("Position not allowed:" + point.toString());
        if(this.finished) throw new Exception("The onyx.game is already finished");

        List<Coord> captured = getCaptured(point, stoneColor);

        for(Coord c : captured) {
            this.grid[c.getZ()][c.getX()][c.getY()] = StoneColor.BLANK;
        }

        this.grid[point.getZ()][point.getX()][point.getY()] = stoneColor;
        this.turn = this.turn == StoneColor.BLACK ? StoneColor.WHITE : StoneColor.BLACK;
        this.winner = checkWinner();
        if(this.winner != StoneColor.BLANK) this.finished = true;
        if(getAllAvailable().size() == 0) this.finished = true;

        return captured;
    }

    private List<Coord> getCaptured(Coord point, StoneColor stoneColor) throws Exception {
        List<Coord> captured = new ArrayList<>();

        if(point.getZ() == 0 && stoneColor != StoneColor.BLANK) {
            StoneColor opposite = stoneColor == StoneColor.WHITE ? StoneColor.BLACK : StoneColor.WHITE;
            boolean t = this.isInbound(point.getX(), point.getY() + 1, 0)
                    && this.grid[0][point.getX()][point.getY() + 1] == opposite;
            boolean b = this.isInbound(point.getX(), point.getY() - 1, 0)
                    && this.grid[0][point.getX()][point.getY() - 1] == opposite;
            boolean l = this.isInbound(point.getX() - 1, point.getY(), 0)
                    && this.grid[0][point.getX() - 1][point.getY()] == opposite;
            boolean r = this.isInbound(point.getX() + 1, point.getY(), 0)
                    && this.grid[0][point.getX() + 1][point.getY()] == opposite;

            List<Coord> neighbors = point.getNeighbors();
            Optional<Coord> s1 = neighbors.stream().filter(coord -> coord.getZ() == 1).findAny();
            Optional<Coord> s2 = neighbors.stream().filter(coord -> coord.getZ() == 2).findAny();
            boolean centreZ1 = !s1.isPresent() || this.grid[1][s1.get().getX()][s1.get().getY()] == StoneColor.BLANK;
            boolean centreZ2 = !s2.isPresent() || this.grid[2][s2.get().getX()][s2.get().getY()] == StoneColor.BLANK;

            if((point.getX() % 2 == 0 && point.getY() % 2 == 0) || point.getX() % 2 == 1 && point.getY() % 2 == 1) {
                boolean c1 = (point.getX() % 2 == 0 && point.getY() % 2 == 0) ? centreZ2 : centreZ1;
                boolean c2 = (point.getX() % 2 == 0 && point.getY() % 2 == 0) ? centreZ1 : centreZ2;

                if(c1 && r && b && this.grid[0][point.getX() + 1][point.getY() - 1] == stoneColor) {
                    captured.add(new Coord(this, point.getX() + 1, point.getY(), 0));
                    captured.add(new Coord(this, point.getX(), point.getY() - 1, 0));
                }
                if(c2 && l && t && this.grid[0][point.getX() - 1][point.getY() + 1] == stoneColor) {
                    captured.add(new Coord(this, point.getX() - 1, point.getY(), 0));
                    captured.add(new Coord(this, point.getX(), point.getY() + 1, 0));
                }
            }
            else if((point.getX() % 2 == 0 && point.getY() % 2 == 1) || (point.getX() % 2 == 1 && point.getY() % 2 == 0)) {
                boolean c1 = (point.getX() % 2 == 0 && point.getY() % 2 == 1) ? centreZ2 : centreZ1;
                boolean c2 = (point.getX() % 2 == 0 && point.getY() % 2 == 1) ? centreZ1 : centreZ2;

                if(c2 && l && b && this.grid[0][point.getX() - 1][point.getY() - 1] == stoneColor) {
                    captured.add(new Coord(this, point.getX() - 1, point.getY(), 0));
                    captured.add(new Coord(this, point.getX(), point.getY() - 1, 0));
                }
                if(c1 && t && r && this.grid[0][point.getX() + 1][point.getY() + 1] == stoneColor) {
                    captured.add(new Coord(this, point.getX(), point.getY() + 1, 0));
                    captured.add(new Coord(this, point.getX() + 1, point.getY(), 0));
                }
            }
        }

        return captured;
    }

    private boolean isAvailable(Coord point) {
        if(this.grid[point.getZ()][point.getX()][point.getY()] != StoneColor.BLANK) {
            return false;
        }

        if(point.getZ() != 0) {
            for(Coord p : point.getNeighbors()) {
                if(this.grid[p.getZ()][p.getX()][p.getY()] != StoneColor.BLANK) {
                    return false;
                }
            }
        }

        return true;
    }

    public StoneColor checkWinner() {
        List<Coord> visited = new ArrayList<>();

        for(int i = 0; i < this.boardSize; i++) {
            if(this.grid[0][0][i] == StoneColor.WHITE) {
                visited.clear();
                try {
                    if(checkWinnerRec(new Coord(this, 0, i, 0), visited, StoneColor.WHITE)) {
                        return StoneColor.WHITE;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        for(int i = 0; i < this.boardSize; i++) {
            if(this.grid[0][i][0] == StoneColor.BLACK) {
                visited.clear();
                try {
                    if(checkWinnerRec(new Coord(this, i, 0, 0), visited, StoneColor.BLACK)) {
                        return StoneColor.BLACK;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return StoneColor.BLANK;
    }

    private boolean checkWinnerRec(Coord point, List<Coord> visited, StoneColor goal) {
        if(goal == StoneColor.WHITE && this.grid[point.getZ()][point.getX()][point.getY()] == StoneColor.WHITE && point.getX() == (this.boardSize - 1)) {
            return true;
        }
        else if(goal == StoneColor.BLACK && this.grid[point.getZ()][point.getX()][point.getY()] == StoneColor.BLACK && point.getY() == (this.boardSize - 1)) {
            return true;
        }

        visited.add(point);
        List<Coord> next = new ArrayList<>();
        for(Coord coord : point.getNeighbors()) {
            if(this.grid[coord.getZ()][coord.getX()][coord.getY()] == goal && !visited.contains(coord)) {
                next.add(coord);
            }
        }

        if(next.isEmpty()) {
            return false;
        }
        else {
            boolean b  = false;
            for(Coord n : next) {
                b = b || checkWinnerRec(n, visited, goal);
            }
            return b;
        }
    }

    public List<Coord> getAllAvailable() {
        List<Coord> available = new ArrayList<>();

        for(int z = 0; z < 3; z++) {
            for(int x = 0; x < this.boardSize; x++) {
                for (int y = 0; y < this.boardSize; y++) {
                    try {
                        Coord c = new Coord(this, x, y, z);
                        if(isAvailable(c)) {
                            available.add(c);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        return available;
    }

    public StoneColor getTurn() {
        return turn;
    }

    public boolean isFinished() {
        return finished;
    }

    public StoneColor getWinner() {
        return winner;
    }

    public boolean isInbound(int x, int y, int z) {
        //System.out.println("x = " + x + ", y = " + y + ", z = " + z);

        if(z == 0) {
            return x >= 0 && x < this.boardSize && y >= 0 && y < this.boardSize;
        }
        else if(z == 1) {
            return x >= 0 && x < (this.boardSize / 2 -1 ) && y >= 0 && y < (this.boardSize / 2);
        }
        else if(z == 2) {
            return x >= 0 && x < (this.boardSize / 2) && y >= 0 && y < (this.boardSize / 2 -1 );
        }
        else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder board = new StringBuilder();
        for(int z = 0; z < 3; z++) {
            for(int x = 0; x < this.boardSize; x++) {
                for (int y = 0; y < this.boardSize; y++) {
                    if(this.grid[z][x][y] != StoneColor.BLANK) {
                        try {
                            Coord c = new Coord(this, x, y, z);
                            board
                                    .append(c.toString()).append(" : ")
                                    .append(this.grid[z][x][y])
                                    .append("\t[")
                                    .append(z)
                                    .append("][")
                                    .append(x)
                                    .append("][")
                                    .append(y)
                                    .append("]\n");
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
        return board.toString();
    }
}
