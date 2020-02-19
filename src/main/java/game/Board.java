package game;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private StoneColor[][][] grid;
    private StoneColor turn;
    private boolean finished;
    private StoneColor winner;


    public Board() {
        this.grid = new StoneColor[3][12][12];
        for(int z = 0; z < 3; z++) {
            for(int x = 0; x < 12; x++) {
                for(int y = 0; y < 12; y++) {
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
        if(!isAvailable(point)) throw new Exception("Position not allowed");
        if(this.finished) throw new Exception("The game is already finished");

        List<Coord> captured = new ArrayList<>();

        if(point.getZ() == 0 && stoneColor != StoneColor.BLANK) {
            StoneColor opposite = stoneColor == StoneColor.WHITE ? StoneColor.BLACK : StoneColor.WHITE;
            boolean t = Coord.isInbound(point.getX(), point.getY() + 1, 0)
                    && this.grid[0][point.getX()][point.getY() + 1] == opposite;
            boolean b = Coord.isInbound(point.getX(), point.getY() - 1, 0)
                    && this.grid[0][point.getX()][point.getY() - 1] == opposite;
            boolean l = Coord.isInbound(point.getX() - 1, point.getY(), 0)
                    && this.grid[0][point.getX() - 1][point.getY()] == opposite;
            boolean r = Coord.isInbound(point.getX() + 1, point.getY(), 0)
                    && this.grid[0][point.getX() + 1][point.getY()] == opposite;

            if((point.getX() % 2 == 0 && point.getY() % 2 == 0) || point.getX() % 2 == 1 && point.getY() % 2 == 1) {
                if(r && b && this.grid[0][point.getX() + 1][point.getY() - 1] == stoneColor) {
                    captured.add(new Coord(point.getX() + 1, point.getY(), 0));
                    captured.add(new Coord(point.getX(), point.getY() - 1, 0));
                }
                if(l && t && this.grid[0][point.getX() - 1][point.getY() + 1] == stoneColor) {
                    captured.add(new Coord(point.getX() - 1, point.getY(), 0));
                    captured.add(new Coord(point.getX(), point.getY() + 1, 0));
                }
            }
            else if((point.getX() % 2 == 0 && point.getY() % 2 == 1) || (point.getX() % 2 == 1 && point.getY() % 2 == 0)) {
                if(l && b && this.grid[0][point.getX() - 1][point.getY() - 1] == stoneColor) {
                    captured.add(new Coord(point.getX() - 1, point.getY(), 0));
                    captured.add(new Coord(point.getX(), point.getY() - 1, 0));
                }
                if(t && r && this.grid[0][point.getX() + 1][point.getY() + 1] == stoneColor) {
                    captured.add(new Coord(point.getX(), point.getY() + 1, 0));
                    captured.add(new Coord(point.getX() + 1, point.getY(), 0));
                }
            }
        }

        for(Coord c : captured) {
            this.grid[c.getZ()][c.getX()][c.getY()] = StoneColor.BLANK;
        }

        this.grid[point.getZ()][point.getX()][point.getY()] = stoneColor;
        this.turn = this.turn == StoneColor.BLACK ? StoneColor.WHITE : StoneColor.BLACK;
        this.winner = checkWinner();
        if(this.winner != StoneColor.BLANK) this.finished = true;

        return captured;
    }

    private boolean isAvailable(Coord point) {
        if(this.grid[point.getZ()][point.getX()][point.getY()] != StoneColor.BLANK)
            return false;

        if(point.getZ() != 0) {
            for(Coord p : point.getNeighbors()) {
                if(this.grid[p.getZ()][p.getX()][p.getY()] != StoneColor.BLANK)
                    return false;
            }
        }

        return true;
    }

    public StoneColor checkWinner() {
        List<Coord> visited = new ArrayList<>();

        for(int i = 0; i < 12; i++) {
            if(this.grid[0][0][i] == StoneColor.WHITE) {
                visited.clear();
                try {
                    if(checkWinnerRec(new Coord(0, i, 0), visited, StoneColor.WHITE)) {
                        return StoneColor.WHITE;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        for(int i = 0; i < 12; i++) {
            if(this.grid[0][i][0] == StoneColor.BLACK) {
                visited.clear();
                try {
                    if(checkWinnerRec(new Coord(i, 0, 0), visited, StoneColor.BLACK)) {
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
        if(this.grid[point.getZ()][point.getX()][point.getZ()] == StoneColor.WHITE && point.getX() == 11) {
            return true;
        }
        else if(this.grid[point.getZ()][point.getX()][point.getZ()] == StoneColor.BLACK && point.getY() == 11) {
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

        for(int x = 0; x < 12; x++) {
            for(int y = 0; y < 12; y++) {
                try {
                    Coord c = new Coord(x, y, 0);
                    if(isAvailable(c)) {
                        available.add(c);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        for(int x = 0; x < 5; x++) {
            for(int y = 0; y < 6; y++) {
                try {
                    Coord c = new Coord(x, y, 1);
                    if(isAvailable(c)) {
                        available.add(c);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        for(int x = 0; x < 6; x++) {
            for(int y = 0; y < 5; y++) {
                try {
                    Coord c = new Coord(x, y, 2);
                    if(isAvailable(c)) {
                        available.add(c);
                    }
                } catch (Exception ignored) {
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
}
