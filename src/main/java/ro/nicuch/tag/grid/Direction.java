package ro.nicuch.tag.grid;

public enum Direction {
    CENTER(0, 0, 0),
    NORTH(0, 0, -1),
    EAST(1, 0, 0),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    UP(0, 1, 0),
    DOWN(0, -1, 0),
    NORTH_EAST(NORTH, EAST), NORTH_WEST(NORTH, WEST),
    SOUTH_EAST(SOUTH, EAST), SOUTH_WEST(SOUTH, WEST),
    NORTH_UP(NORTH, UP), NORTH_DOWN(NORTH, DOWN),
    SOUTH_UP(SOUTH, UP), SOUTH_DOWN(SOUTH, DOWN),
    EAST_UP(EAST, UP), EAST_DOWN(EAST, DOWN),
    WEST_UP(WEST, UP), WEST_DOWN(WEST, DOWN),
    NORTH_EAST_UP(NORTH, EAST, UP), NORTH_EAST_DOWN(NORTH, EAST, DOWN),
    NORTH_WEST_UP(NORTH, WEST, UP), NORTH_WEST_DOWN(NORTH, WEST, DOWN),
    SOUTH_EAST_UP(SOUTH, EAST, UP), SOUTH_EAST_DOWN(SOUTH, EAST, DOWN),
    SOUTH_WEST_UP(SOUTH, WEST, UP), SOUTH_WEST_DOWN(SOUTH, WEST, DOWN);

    private final int x;
    private final int y;
    private final int z;

    Direction(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    Direction(Direction dir1, Direction dir2) {
        this.x = dir1.getModX() + dir2.getModX();
        this.y = dir1.getModY() + dir2.getModY();
        this.z = dir1.getModZ() + dir2.getModZ();
    }

    Direction(Direction dir1, Direction dir2, Direction dir3) {
        this.x = dir1.getModX() + dir2.getModX() + dir3.getModX();
        this.y = dir1.getModY() + dir2.getModY() + dir3.getModY();
        this.z = dir1.getModZ() + dir2.getModZ() + dir3.getModZ();
    }

    public Direction[] splitToSide() {
        return switch (this) {
            case CENTER, NORTH, EAST, SOUTH, WEST, UP, DOWN -> new Direction[]{this};
            case NORTH_EAST -> new Direction[]{NORTH, EAST};
            case NORTH_WEST -> new Direction[]{NORTH, WEST};
            case SOUTH_EAST -> new Direction[]{SOUTH, EAST};
            case SOUTH_WEST -> new Direction[]{SOUTH, WEST};
            case NORTH_UP -> new Direction[]{NORTH, UP};
            case NORTH_DOWN -> new Direction[]{NORTH, DOWN};
            case SOUTH_UP -> new Direction[]{SOUTH, UP};
            case SOUTH_DOWN -> new Direction[]{SOUTH, DOWN};
            case EAST_UP -> new Direction[]{EAST, UP};
            case EAST_DOWN -> new Direction[]{EAST, DOWN};
            case WEST_UP -> new Direction[]{WEST, UP};
            case WEST_DOWN -> new Direction[]{WEST, DOWN};
            case NORTH_EAST_UP -> new Direction[]{NORTH, EAST, UP};
            case NORTH_EAST_DOWN -> new Direction[]{NORTH, EAST, DOWN};
            case NORTH_WEST_UP -> new Direction[]{NORTH, WEST, UP};
            case NORTH_WEST_DOWN -> new Direction[]{NORTH, WEST, DOWN};
            case SOUTH_EAST_UP -> new Direction[]{SOUTH, EAST, UP};
            case SOUTH_EAST_DOWN -> new Direction[]{SOUTH, EAST, DOWN};
            case SOUTH_WEST_UP -> new Direction[]{SOUTH, WEST, UP};
            case SOUTH_WEST_DOWN -> new Direction[]{SOUTH, WEST, DOWN};
        };
    }

    public Direction[] splitToEdge() {
        return switch (this) {
            case CENTER, NORTH, EAST, SOUTH, WEST, UP, DOWN, NORTH_EAST,
                    NORTH_WEST, SOUTH_EAST, SOUTH_WEST, NORTH_UP, NORTH_DOWN,
                    SOUTH_UP, SOUTH_DOWN, EAST_UP, EAST_DOWN, WEST_UP, WEST_DOWN -> new Direction[]{this};
            case NORTH_EAST_UP -> new Direction[]{NORTH_EAST, EAST_UP, NORTH_UP};
            case NORTH_EAST_DOWN -> new Direction[]{NORTH_EAST, EAST_DOWN, NORTH_DOWN};
            case NORTH_WEST_UP -> new Direction[]{NORTH_WEST, WEST_UP, NORTH_UP};
            case NORTH_WEST_DOWN -> new Direction[]{NORTH_WEST, WEST_DOWN, NORTH_DOWN};
            case SOUTH_EAST_UP -> new Direction[]{SOUTH_EAST, EAST_UP, SOUTH_UP};
            case SOUTH_EAST_DOWN -> new Direction[]{SOUTH_EAST, EAST_DOWN, SOUTH_DOWN};
            case SOUTH_WEST_UP -> new Direction[]{SOUTH_WEST, WEST_UP, SOUTH_UP};
            case SOUTH_WEST_DOWN -> new Direction[]{SOUTH_WEST, WEST_DOWN, SOUTH_DOWN};
        };
    }

    public final int getModX() {
        return this.x;
    }

    public final int getModY() {
        return this.y;
    }

    public final int getModZ() {
        return this.z;
    }
}
