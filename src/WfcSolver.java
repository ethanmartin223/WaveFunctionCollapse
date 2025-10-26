import java.util.*;

class WfcSolver {
    private int size;
    private WfcRules rules;
    private Random rand;
    private int[][] grid;
    private List<Integer> allTiles;
    private EdgeConstraints constraints;

    public WfcSolver(int size, WfcRules rules, Random rand, EdgeConstraints constraints) {
        this.size = size;
        this.rules = rules;
        this.rand = rand;
        this.constraints = constraints;
        this.grid = new int[size][size];
        this.allTiles = new ArrayList<>();
        for (int i = 0; i <= rules.numberOfRules; i++) allTiles.add(i);
    }

    public int[][] solve() {
        ArrayList<Set<Integer>> wave = new ArrayList<>();
        for (int i = 0; i < size * size; i++)
            wave.add(new HashSet<>(allTiles));

        applyEdgeConstraints(wave);
        propagateAll(wave);

        while (true) {
            int idx = findLowestE(wave);
            if (idx == -1) break;

            Set<Integer> options = wave.get(idx);
            if (options.isEmpty()) {
                break;
            }

            int choice = pickRandom(options);
            wave.get(idx).clear();
            wave.get(idx).add(choice);

            propagate(wave, idx);
        }

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int idx = y * size + x;
                if (!wave.get(idx).isEmpty()) {
                    grid[y][x] = wave.get(idx).iterator().next();
                } else {
                    grid[y][x] = 0;
                }
            }
        }

        return grid;
    }

    private void applyEdgeConstraints(ArrayList<Set<Integer>> wave) {
        if (constraints.topEdge != null) {
            for (int x = 0; x < size; x++) {
                int requiredTile = constraints.topEdge[x];
                int idx = 0 * size + x;

                Set<Integer> validTiles = new HashSet<>();
                for (int tile : allTiles) {
                    if (rules.isAllowed(requiredTile, tile, 0, 1)) {
                        validTiles.add(tile);
                    }
                }

                wave.get(idx).retainAll(validTiles);
            }
        }

        if (constraints.bottomEdge != null) {
            for (int x = 0; x < size; x++) {
                int requiredTile = constraints.bottomEdge[x];
                int idx = (size - 1) * size + x;

                Set<Integer> validTiles = new HashSet<>();
                for (int tile : allTiles) {
                    if (rules.isAllowed(requiredTile, tile, 0, -1)) {
                        validTiles.add(tile);
                    }
                }

                wave.get(idx).retainAll(validTiles);
            }
        }

        if (constraints.leftEdge != null) {
            for (int y = 0; y < size; y++) {
                int requiredTile = constraints.leftEdge[y];
                int idx = y * size + 0;

                Set<Integer> validTiles = new HashSet<>();
                for (int tile : allTiles) {
                    if (rules.isAllowed(requiredTile, tile, 1, 0)) {
                        validTiles.add(tile);
                    }
                }

                wave.get(idx).retainAll(validTiles);
            }
        }

        if (constraints.rightEdge != null) {
            for (int y = 0; y < size; y++) {
                int requiredTile = constraints.rightEdge[y];
                int idx = y * size + (size - 1);

                Set<Integer> validTiles = new HashSet<>();
                for (int tile : allTiles) {
                    if (rules.isAllowed(requiredTile, tile, -1, 0)) {
                        validTiles.add(tile);
                    }
                }

                wave.get(idx).retainAll(validTiles);
            }
        }
    }

    private int findLowestE(ArrayList<Set<Integer>> wave) {
        int min = Integer.MAX_VALUE;
        ArrayList<Integer> candidates = new ArrayList<>();

        for (int i = 0; i < wave.size(); i++) {
            int s = wave.get(i).size();
            if (s > 1 && s < min) {
                min = s;
                candidates.clear();
                candidates.add(i);
            } else if (s > 1 && s == min) {
                candidates.add(i);
            }
        }

        if (candidates.isEmpty()) return -1;

        return candidates.get(rand.nextInt(candidates.size()));
    }

    private int pickRandom(Set<Integer> options) {
        int n = rand.nextInt(options.size());
        int i = 0;
        for (int val : options) {
            if (i == n) return val;
            i++;
        }
        return options.iterator().next();
    }

    private void propagateAll(ArrayList<Set<Integer>> wave) {
        val(wave);
    }

    private void propagate(ArrayList<Set<Integer>> wave, int startIdx) {
        val(wave);
    }

    private void val(ArrayList<Set<Integer>> wave) {
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        boolean changed = true;
        int iterations = 0;
        int maxIterations = size * size * 10;

        while (changed && iterations < maxIterations) {
            changed = false;
            iterations++;

            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    int idx = y * size + x;
                    Set<Integer> possibilities = wave.get(idx);

                    if (possibilities.size() <= 1) continue;

                    for (int dir = 0; dir < 4; dir++) {
                        int nx = x + dx[dir];
                        int ny = y + dy[dir];
                        if (nx < 0 || ny < 0 || nx >= size || ny >= size) continue;
                        Set<Integer> neighborPoss = wave.get(ny * size + nx);

                        Set<Integer> allowed = new HashSet<>();
                        for (int t : possibilities) {
                            for (int n : neighborPoss) {
                                if (rules.isAllowed(t, n, dx[dir], dy[dir])) {
                                    allowed.add(t);
                                    break;
                                }
                            }
                        }

                        if (allowed.size() < possibilities.size() && !allowed.isEmpty()) {
                            possibilities.retainAll(allowed);
                            changed = true;
                        }
                    }
                }
            }
        }
    }
}