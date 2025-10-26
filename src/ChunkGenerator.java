import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChunkGenerator {

    public Chunk spawnChunk;
    public boolean firstChunkSpawned;

    Map<Integer, Map<Integer, Chunk>> generatedChunksMap;
    ArrayList<Chunk> allChunksList;

    public static int CHUNK_SIZE = 8;

    public ChunkGenerator() {
        generatedChunksMap = new HashMap<>();
        allChunksList = new ArrayList<>();
        firstChunkSpawned = false;


    }

    public Chunk grabChunk(int atX, int atY) {
        HashMap<Integer, Chunk> xChunks;
        if (generatedChunksMap.get(atY) == null) {
            generatedChunksMap.put(atY, xChunks = new HashMap<>());
        } else xChunks = (HashMap<Integer, Chunk>) generatedChunksMap.get(atY);

        Chunk foundChunk;
        if (xChunks.get(atX) == null) {
            xChunks.put(atX, foundChunk = new Chunk(atX, atY, this));
        } else foundChunk = xChunks.get(atX);

        if (!allChunksList.contains(foundChunk))
            allChunksList.add(foundChunk);

        if (!firstChunkSpawned) {
            firstChunkSpawned = true;
            spawnChunk = foundChunk;
            System.out.println("Spawn Chunk Set: " + spawnChunk);
        }

        return foundChunk;
    }

    public Chunk getChunkIfExists(int atX, int atY) {
        if (generatedChunksMap.containsKey(atY)) {
            Map<Integer, Chunk> xChunks = generatedChunksMap.get(atY);
            if (xChunks.containsKey(atX)) {
                return xChunks.get(atX);
            }
        }
        return null;
    }

    public boolean hasChunkBeenGeneratedAt(int atX, int atY) {
        return generatedChunksMap.containsKey(atY) && generatedChunksMap.get(atY).containsKey(atX);
    }

    public void listChunksThatExist() {
        if (allChunksList.isEmpty()) {
            System.out.println("No Chunks Currently Exist");
            return;
        }
        for (Chunk c : allChunksList) {
            System.out.print(c + ", ");
        }
        System.out.println();
    }
}