package onyx.game;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Coord {
    private static final String ERR = "Coordinates are incorrect";
    private Board board;
    private int x;
    private int y;
    private int z;

    public Coord(Board board, int x, int y, int z) throws Exception {
        if(!board.isInbound(x, y, z)) throw new Exception(ERR);
        this.board = board;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Coord(Board board, String position) throws Exception {
        this.board = board;
        Pattern pattern1 = Pattern.compile(",");
        String[] coords = pattern1.split(position);
        if(coords.length != 2) throw new Exception(ERR);

        Pattern pattern2 = Pattern.compile("-");
        String[] abs = pattern2.split(coords[0]);
        String[] ord = pattern2.split(coords[1]);

        int x, y, z;

        if(abs.length == 1 && ord.length == 1) {
            z = 0;
            x = abs[0].charAt(0) - 'A';
            y = Integer.parseInt(String.valueOf(ord[0])) -1;
        }
        else if(abs.length == 2 && ord.length == 2) {
            int y1 = Integer.parseInt(String.valueOf(ord[0])) -1;
            int x1 = abs[0].charAt(0) - 'A';

            if(y1 % 2 == 0 && x1 % 2 == 1) {
                z = 1;
                x = (x1 -1) / 2;
                y = y1 / 2;
            }
            else if(y1 % 2 == 1 && x1 % 2 == 0) {
                z = 2;
                x = x1 / 2;
                y = (y1 - 1) / 2;
            }
            else {
                throw new Exception(ERR);
            }
        }
        else {
            throw new Exception(ERR);
        }

        if(!this.board.isInbound(x, y, z)) throw new Exception(ERR);
        this.x = x;
        this.y = y;
        this.z = z;
    }
    


    public List<Coord> getNeighbors() {
        List<Coord> coords = new ArrayList<>();

        if(z == 0) {
            addToListIfInbound(coords, this.board, x, y - 1, 0);
            addToListIfInbound(coords, this.board, x + 1, y, 0);
            addToListIfInbound(coords, this.board, x, y + 1, 0);
            addToListIfInbound(coords, this.board, x - 1, y, 0);

            if(x % 2 == 0 && y % 2 == 0) {
                addToListIfInbound(coords, this.board, x / 2 - 1, y / 2, 1);
                addToListIfInbound(coords, this.board, x / 2, y / 2 - 1, 2);
                addToListIfInbound(coords, this.board, x + 1, y + 1, 0);
            }
            else if(x % 2 == 0 && y % 2 == 1) {
                addToListIfInbound(coords, this.board, x / 2 - 1, (y - 1) / 2, 1);
                addToListIfInbound(coords, this.board, x / 2, (y - 1) / 2, 2);
                addToListIfInbound(coords, this.board, x - 1, y + 1, 0);
            }
            else if(x % 2 == 1 && y % 2 == 0) {
                addToListIfInbound(coords, this.board,  (x - 1) / 2, y / 2, 1);
                addToListIfInbound(coords, this.board, (x - 1) / 2 , y / 2 - 1, 2);
                addToListIfInbound(coords, this.board, x + 1, y - 1, 0);
            }
            else if(x % 2 == 1 && y % 2 == 1) {
                addToListIfInbound(coords, this.board, (x - 1) / 2, (y - 1) / 2, 1);
                addToListIfInbound(coords, this.board, (x - 1) / 2, (y - 1) / 2, 2);
                addToListIfInbound(coords, this.board, x - 1, y - 1, 0);
            }
        }
        else if(z == 1) {
            addToListIfInbound(coords, this.board, 2 * x + 1, 2 * y, 0);
            addToListIfInbound(coords, this.board, 2 * x + 2, 2 * y, 0);
            addToListIfInbound(coords, this.board, 2 * x + 1, 2 * y + 1, 0);
            addToListIfInbound(coords, this.board, 2 * x + 2, 2 * y + 1, 0);
        }
        else if(z == 2) {
            addToListIfInbound(coords, this.board, 2 * x, 2 * y + 1, 0);
            addToListIfInbound(coords, this.board, 2 * x + 1, 2 * y + 1, 0);
            addToListIfInbound(coords, this.board, 2 * x, 2 * y + 2, 0);
            addToListIfInbound(coords, this.board, 2 * x + 1, 2 * y + 2, 0);
        }

        return coords;
    }

    private void addToListIfInbound(List<Coord> coords, Board board, int x, int y, int z) {
        try {
            coords.add(new Coord(board, x, y,z));
        } catch (Exception ignored) {
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Coord)) return false;
        Coord c2 = (Coord) obj;
        return this.x == c2.x && this.y == c2.y && this.z == c2.z;
    }

    @Override
    public String toString() {
        String x = "";
        String y = "";

        if(this.z == 0) {
            x = ((char) (this.x + 'A')) + "";
            y = (this.y + 1) + "";
        }
        else if(this.z == 1) {
            x = ((char) (this.x * 2 + 1 + 'A')) + "-";
            x += ((char) (this.x * 2 + 2 + 'A')) + "";
            y = (this.y * 2 + 1) + "-";
            y += (this.y * 2 + 2) + "";
        }
        else if(this.z == 2) {
            x = ((char)('A' + this.x * 2)) + "-";
            x += ((char)('A' + this.x * 2 + 1)) + "";
            y = (this.y * 2 + 2) + "-";
            y += (this.y * 2 + 3) + "";
        }
        return x + "," + y;
    }
}
