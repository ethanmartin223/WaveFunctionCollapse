import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;

public class Chunk {

    private ChunkGenerator parent;

    public int xWorldLocation;
    public int yWorldLocation;

    public int[][] dataLayer;
    public int[][] renderLayer;

    // Opening system
    public Set<Direction> openings;

    public enum Direction {
        TOP, BOTTOM, LEFT, RIGHT
    }

    public Chunk(int x, int y, ChunkGenerator chunkGenerator) {
        dataLayer = new int[ChunkGenerator.CHUNK_SIZE][ChunkGenerator.CHUNK_SIZE];
        renderLayer = new int[ChunkGenerator.CHUNK_SIZE][ChunkGenerator.CHUNK_SIZE];
        xWorldLocation = x;
        yWorldLocation = y;
        parent = chunkGenerator;
        openings = new HashSet<>();
        generateOpenings();
        if (y>3) {
            randomizeDataLayer();
        }
    }

    void generateOpenings() {
        Random rand = new Random();
        Direction[] allDirections = Direction.values();

        // Check adjacent chunks for required openings
        Chunk topChunk = parent.getChunkIfExists(xWorldLocation, yWorldLocation - 1);
        Chunk bottomChunk = parent.getChunkIfExists(xWorldLocation, yWorldLocation + 1);
        Chunk leftChunk = parent.getChunkIfExists(xWorldLocation - 1, yWorldLocation);
        Chunk rightChunk = parent.getChunkIfExists(xWorldLocation + 1, yWorldLocation);

        // Match openings with adjacent chunks
        if (topChunk != null && topChunk.openings.contains(Direction.BOTTOM)) {
            openings.add(Direction.TOP);
        }
        if (bottomChunk != null && bottomChunk.openings.contains(Direction.TOP)) {
            openings.add(Direction.BOTTOM);
        }
        if (leftChunk != null && leftChunk.openings.contains(Direction.RIGHT)) {
            openings.add(Direction.LEFT);
        }
        if (rightChunk != null && rightChunk.openings.contains(Direction.LEFT)) {
            openings.add(Direction.RIGHT);
        }

        // If no forced openings, randomly generate 1-4 openings
        if (openings.isEmpty()) {
            int numOpenings = rand.nextInt(4) + 1; // 1 to 4 openings
            ArrayList<Direction> availableDirections = new ArrayList<>(Arrays.asList(allDirections));

            for (int i = 0; i < numOpenings; i++) {
                if (!availableDirections.isEmpty()) {
                    int index = rand.nextInt(availableDirections.size());
                    openings.add(availableDirections.remove(index));
                }
            }
        } else {
            // Add additional random openings (optional)
            ArrayList<Direction> availableDirections = new ArrayList<>();
            for (Direction dir : allDirections) {
                if (!openings.contains(dir)) {
                    availableDirections.add(dir);
                }
            }

            // 50% chance to add each additional opening
            for (Direction dir : availableDirections) {
                if (rand.nextBoolean()) {
                    openings.add(dir);
                }
            }
        }
    }

    public void randomizeDataLayer() {
        Random rand = new Random(parent.hashCode() + xWorldLocation * 9187L + yWorldLocation * 4513L);
        int size = ChunkGenerator.CHUNK_SIZE;

        ArrayList<int[][]> examples = loadExamplesFromFile("src/examples.txt");

        if (examples.isEmpty()) {
            System.err.println("No examples found! Falling back to random terrain.");
            for (int y = 0; y < size; y++)
                for (int x = 0; x < size; x++)
                    dataLayer[y][x] = rand.nextInt(17);
            return;
        }

        WfcRules rules = new WfcRules(17);
        for (int[][] ex : examples) {
            rules.learnFromExample(ex);
        }

        // Get edge constraints from adjacent chunks
        EdgeConstraints constraints = getEdgeConstraints();

        WfcSolver solver = new WfcSolver(size, rules, rand, constraints);
        dataLayer = solver.solve();
    }

    private EdgeConstraints getEdgeConstraints() {
        EdgeConstraints constraints = new EdgeConstraints();
        int size = ChunkGenerator.CHUNK_SIZE;

        Chunk topChunk = parent.getChunkIfExists(xWorldLocation, yWorldLocation - 1);
        Chunk bottomChunk = parent.getChunkIfExists(xWorldLocation, yWorldLocation + 1);
        Chunk leftChunk = parent.getChunkIfExists(xWorldLocation - 1, yWorldLocation);
        Chunk rightChunk = parent.getChunkIfExists(xWorldLocation + 1, yWorldLocation);

        if (topChunk != null) {
            constraints.topEdge = new int[size];
            for (int x = 0; x < size; x++) {
                constraints.topEdge[x] = topChunk.dataLayer[size - 1][x];
            }
        }

        if (bottomChunk != null) {
            constraints.bottomEdge = new int[size];
            for (int x = 0; x < size; x++) {
                constraints.bottomEdge[x] = bottomChunk.dataLayer[0][x];
            }
        }

        if (leftChunk != null) {
            constraints.leftEdge = new int[size];
            for (int y = 0; y < size; y++) {
                constraints.leftEdge[y] = leftChunk.dataLayer[y][size - 1];
            }
        }

        if (rightChunk != null) {
            constraints.rightEdge = new int[size];
            for (int y = 0; y < size; y++) {
                constraints.rightEdge[y] = rightChunk.dataLayer[y][0];
            }
        }

        return constraints;
    }

    private ArrayList<int[][]> loadExamplesFromFile(String filename) {
        ArrayList<int[][]> examples = new ArrayList<>();

        try (Scanner scanner = new Scanner(new FileReader(filename))) {
            ArrayList<int[]> current = new ArrayList<>();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();

                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.equals("---")) {
                    if (!current.isEmpty()) {
                        int[][] example = new int[current.size()][];
                        for (int i = 0; i < current.size(); i++)
                            example[i] = current.get(i);
                        examples.add(example);
                        current.clear();
                    }
                    continue;
                }

                String[] parts = line.split("\\s+");
                int[] row = new int[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    try {
                        row[i] = Integer.parseInt(parts[i]);
                    } catch (NumberFormatException e) {
                        row[i] = 0; // default fallback
                    }
                }
                current.add(row);
            }

            if (!current.isEmpty()) {
                int[][] example = new int[current.size()][];
                for (int i = 0; i < current.size(); i++)
                    example[i] = current.get(i);
                examples.add(example);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return examples;
    }

    @Override
    public String toString() {
        return "Chunk(x=" + xWorldLocation + ",y=" + yWorldLocation + ", openings=" + openings + ")";
    }

    public int[][] getDataLayer() {
        return dataLayer;
    }

    public int[][] getRenderLayer() {
        return renderLayer;
    }

    public Set<Direction> getOpenings() {
        return openings;
    }
}

// Container for edge constraints
class EdgeConstraints {
    public int[] topEdge = null;
    public int[] bottomEdge = null;
    public int[] leftEdge = null;
    public int[] rightEdge = null;
}

class WfcRules {
    private final ArrayList<Set<Integer>> northRules = new ArrayList<>();
    private final ArrayList<Set<Integer>> southRules = new ArrayList<>();
    private final ArrayList<Set<Integer>> eastRules = new ArrayList<>();
    private final ArrayList<Set<Integer>> westRules = new ArrayList<>();
    public int numberOfRules;

    public WfcRules(int x) {
        numberOfRules = x;
        for (int i = 0; i <= x; i++) {
            northRules.add(new HashSet<>());
            southRules.add(new HashSet<>());
            eastRules.add(new HashSet<>());
            westRules.add(new HashSet<>());
        }
    }

    public void learnFromExample(int[][] example) {
        int h = example.length;
        int w = example[0].length;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int tile = example[y][x];

                if (y > 0)
                    northRules.get(tile).add(example[y - 1][x]);
                if (y < h - 1)
                    southRules.get(tile).add(example[y + 1][x]);
                if (x > 0)
                    westRules.get(tile).add(example[y][x - 1]);
                if (x < w - 1)
                    eastRules.get(tile).add(example[y][x + 1]);
            }
        }
    }

    public boolean isAllowed(int tile, int neighbor, int dx, int dy) {
        if (dx == 1) return eastRules.get(tile).contains(neighbor);
        if (dx == -1) return westRules.get(tile).contains(neighbor);
        if (dy == 1) return southRules.get(tile).contains(neighbor);
        if (dy == -1) return northRules.get(tile).contains(neighbor);
        return true;
    }

    public Set<Integer> getAllowedNeighbors(int tile, int dx, int dy) {
        if (dx == 1) return eastRules.get(tile);
        if (dx == -1) return westRules.get(tile);
        if (dy == 1) return southRules.get(tile);
        if (dy == -1) return northRules.get(tile);
        return new HashSet<>();
    }
}