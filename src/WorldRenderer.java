import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class WorldRenderer {

    public static int worldRenderDistance = 16;
    public static int spriteSizeOffset = 32;

    private ChunkGenerator worldChunkGenerator;
    private int chunkThatPlayerIsCurrentlyInX;
    private int chunkThatPlayerIsCurrentlyInY;

    private final boolean renderChunkBorder = true;

    private long window;
    private int windowWidth;
    private int windowHeight;

    private double cameraXLocation, cameraYLocation;
    private double lastReleasedPositionX, lastReleasedPositionY;
    private double lastMouseX, lastMouseY;
    private boolean isDragging = false;

    private double zoomLevel = 1.0;
    private static final double ZOOM_FACTOR = 0.8;
    private static final double MIN_ZOOM = 0.1;
    private static final double MAX_ZOOM = 10.0;

    public WorldRenderer(ChunkGenerator cgn, long window, int width, int height) {
        worldChunkGenerator = cgn;
        chunkThatPlayerIsCurrentlyInX = 0;
        chunkThatPlayerIsCurrentlyInY = 0;

        this.window = window;
        this.windowWidth = width;
        this.windowHeight = height;

        cameraXLocation = 350;
        cameraYLocation = 350;
        lastReleasedPositionX = 350;
        lastReleasedPositionY = 350;

        setupInputCallbacks();
    }

    private void setupInputCallbacks() {
        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                if (action == GLFW_PRESS) {
                    isDragging = true;
                    double[] xpos = new double[1];
                    double[] ypos = new double[1];
                    glfwGetCursorPos(window, xpos, ypos);
                    lastMouseX = xpos[0];
                    lastMouseY = ypos[0];
                } else if (action == GLFW_RELEASE) {
                    isDragging = false;
                    lastReleasedPositionX = cameraXLocation;
                    lastReleasedPositionY = cameraYLocation;
                }
            }
        });

        glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
            if (isDragging) {
                double xDragDelta = xpos - lastMouseX;
                double yDragDelta = ypos - lastMouseY;
                cameraXLocation = lastReleasedPositionX + xDragDelta;
                cameraYLocation = lastReleasedPositionY + yDragDelta;
            }
        });

        glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
            double[] xpos = new double[1];
            double[] ypos = new double[1];
            glfwGetCursorPos(window, xpos, ypos);

            double worldXBeforeZoom = (xpos[0] - 256 - cameraXLocation) / zoomLevel;
            double worldYBeforeZoom = (ypos[0] - 256 - cameraYLocation) / zoomLevel;

            double oldZoom = zoomLevel;
            zoomLevel *= Math.pow(1.1, -yoffset * ZOOM_FACTOR);
            zoomLevel = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoomLevel));

            double worldXAfterZoom = (xpos[0] - 256 - cameraXLocation) / zoomLevel;
            double worldYAfterZoom = (ypos[0] - 256 - cameraYLocation) / zoomLevel;

            cameraXLocation += (worldXAfterZoom - worldXBeforeZoom) * zoomLevel;
            cameraYLocation += (worldYAfterZoom - worldYBeforeZoom) * zoomLevel;

            lastReleasedPositionX = cameraXLocation;
            lastReleasedPositionY = cameraYLocation;
        });
    }

    private void renderTile(int tileType, int tileX, int tileY, int tileSize) {
        int halfSize = tileSize / 2;
        int quarterSize = tileSize / 4;
        int threeQuarterSize = (3 * tileSize) / 4;

        switch (tileType) {
            case 0:
                return;

            case 1: // Full block
                glColor3f(0.0f, 1.0f, 0.0f);
                glBegin(GL_POLYGON);
                glVertex2i(tileX, tileY);
                glVertex2i(tileX + tileSize, tileY);
                glVertex2i(tileX + tileSize, tileY + tileSize);
                glVertex2i(tileX, tileY + tileSize);
                glEnd();
                break;

            case 2: // Slope facing north (bottom-left to top-right)
                glColor3f(0.0f, 0.8f, 0.8f);
                glBegin(GL_TRIANGLES);
                glVertex2i(tileX, tileY + tileSize);
                glVertex2i(tileX + tileSize, tileY + tileSize);
                glVertex2i(tileX + tileSize, tileY);
                glEnd();
                break;

            case 3: // Slope facing south (top-left to bottom-right)
                glColor3f(0.8f, 0.8f, 0.0f);
                glBegin(GL_TRIANGLES);
                glVertex2i(tileX, tileY);
                glVertex2i(tileX + tileSize, tileY + tileSize);
                glVertex2i(tileX, tileY + tileSize);
                glEnd();
                break;

            case 4: // Slope facing east (top-left to bottom-right)
                glColor3f(0.8f, 0.0f, 0.8f);
                glBegin(GL_TRIANGLES);
                glVertex2i(tileX, tileY);
                glVertex2i(tileX + tileSize, tileY);
                glVertex2i(tileX + tileSize, tileY + tileSize);
                glEnd();
                break;

            case 5: // Slope facing west (bottom-left to top-right)
                glColor3f(1.0f, 0.5f, 0.0f);
                glBegin(GL_TRIANGLES);
                glVertex2i(tileX, tileY);
                glVertex2i(tileX + tileSize, tileY);
                glVertex2i(tileX, tileY + tileSize);
                glEnd();
                break;

            case 6: // Half block (bottom half)
                glColor3f(0.5f, 0.5f, 1.0f);
                glBegin(GL_POLYGON);
                glVertex2i(tileX, tileY + halfSize);
                glVertex2i(tileX + tileSize, tileY + halfSize);
                glVertex2i(tileX + tileSize, tileY + tileSize);
                glVertex2i(tileX, tileY + tileSize);
                glEnd();
                break;

            case 7: // Half block (top half)
                glColor3f(1.0f, 0.5f, 0.5f);
                glBegin(GL_POLYGON);
                glVertex2i(tileX, tileY);
                glVertex2i(tileX + tileSize, tileY);
                glVertex2i(tileX + tileSize, tileY + halfSize);
                glVertex2i(tileX, tileY + halfSize);
                glEnd();
                break;

            case 8: // Half block (left half)
                glColor3f(0.6f, 0.3f, 0.8f);
                glBegin(GL_POLYGON);
                glVertex2i(tileX, tileY);
                glVertex2i(tileX + halfSize, tileY);
                glVertex2i(tileX + halfSize, tileY + tileSize);
                glVertex2i(tileX, tileY + tileSize);
                glEnd();
                break;

            case 9: // Half block (right half)
                glColor3f(0.8f, 0.3f, 0.6f);
                glBegin(GL_POLYGON);
                glVertex2i(tileX + halfSize, tileY);
                glVertex2i(tileX + tileSize, tileY);
                glVertex2i(tileX + tileSize, tileY + tileSize);
                glVertex2i(tileX + halfSize, tileY + tileSize);
                glEnd();
                break;


            case 10: // Convex corner (rounded bottom-right)
                glColor3f(0.1f, 0.6f, 0.9f);
                glBegin(GL_POLYGON);
                glVertex2i(tileX, tileY);
                glVertex2i(tileX + tileSize, tileY);
                glVertex2i(tileX + tileSize, tileY + tileSize);
                glVertex2i(tileX + halfSize, tileY + tileSize);
                glVertex2i(tileX, tileY + halfSize);
                glEnd();
                break;

            case 11: // Convex corner (rounded bottom-left)
                glColor3f(0.1f, 0.6f, 0.9f);
                glBegin(GL_POLYGON);
                glVertex2i(tileX, tileY);
                glVertex2i(tileX + tileSize, tileY);
                glVertex2i(tileX + tileSize, tileY + halfSize);
                glVertex2i(tileX + halfSize, tileY + tileSize);
                glVertex2i(tileX, tileY + tileSize);
                glEnd();
                break;

            case 12: // Convex corner (rounded top-right)
                glColor3f(0.1f, 0.6f, 0.9f);
                glBegin(GL_POLYGON);
                glVertex2i(tileX, tileY);
                glVertex2i(tileX + halfSize, tileY);
                glVertex2i(tileX + tileSize, tileY + halfSize);
                glVertex2i(tileX + tileSize, tileY + tileSize);
                glVertex2i(tileX, tileY + tileSize);
                glEnd();
                break;

            case 13: // Convex corner (rounded top-left)
                glColor3f(0.1f, 0.6f, 0.9f);
                glBegin(GL_POLYGON);
                glVertex2i(tileX, tileY + halfSize);
                glVertex2i(tileX + halfSize, tileY);
                glVertex2i(tileX + tileSize, tileY);
                glVertex2i(tileX + tileSize, tileY + tileSize);
                glVertex2i(tileX, tileY + tileSize);
                glEnd();
                break;

            case 14: // Corner triangle (bottom-left)
                glColor3f(1.0f, 0.0f, 0.0f);
                glBegin(GL_TRIANGLES);
                glVertex2i(tileX, tileY);                     // bottom-left corner
                glVertex2i(tileX + halfSize, tileY);          // bottom edge midpoint
                glVertex2i(tileX, tileY + halfSize);          // left edge midpoint
                glEnd();
                break;

            case 15: // Corner triangle (bottom-right)
                glColor3f(1.0f, 0.0f, 0.0f);
                glBegin(GL_TRIANGLES);
                glVertex2i(tileX + tileSize, tileY);          // bottom-right corner
                glVertex2i(tileX + tileSize, tileY + halfSize); // right edge midpoint
                glVertex2i(tileX + halfSize, tileY);          // bottom edge midpoint
                glEnd();
                break;

            case 16: // Corner triangle (top-right)
                glColor3f(1.0f, 0.0f, 0.0f);
                glBegin(GL_TRIANGLES);
                glVertex2i(tileX + tileSize, tileY + tileSize);   // top-right corner
                glVertex2i(tileX + halfSize, tileY + tileSize);   // top edge midpoint
                glVertex2i(tileX + tileSize, tileY + halfSize);   // right edge midpoint
                glEnd();
                break;

            case 17: // Corner triangle (top-left)
                glColor3f(1.0f, 0.0f, 0.0f);
                glBegin(GL_TRIANGLES);
                glVertex2i(tileX, tileY + tileSize);           // top-left corner
                glVertex2i(tileX + halfSize, tileY + tileSize); // top edge midpoint
                glVertex2i(tileX, tileY + halfSize);            // left edge midpoint
                glEnd();
                break;



            default:
                // Unknown tile type - render as red error block
                glColor3f(1.0f, 0.0f, 0.0f);
                glBegin(GL_POLYGON);
                glVertex2i(tileX, tileY);
                glVertex2i(tileX + tileSize, tileY);
                glVertex2i(tileX + tileSize, tileY + tileSize);
                glVertex2i(tileX, tileY + tileSize);
                glEnd();
                break;
        }
    }

    private void renderChunkOpenings(Chunk chunk, int offsetX, int offsetY) {
        int chunkPixelSize = (int) (spriteSizeOffset * ChunkGenerator.CHUNK_SIZE * zoomLevel);
        int halfChunkSize = chunkPixelSize / 2;
        int textOffset = (int) (15 * zoomLevel);

        glColor3f(1.0f, 0.0f, 0.0f);

        for (Chunk.Direction dir : chunk.getOpenings()) {
            int indicatorSize = (int) (20 * zoomLevel);
            int textX, textY;

            switch (dir) {
                case TOP:
                    textX = offsetX + halfChunkSize - (int) (10 * zoomLevel);
                    textY = offsetY - textOffset;
                    glBegin(GL_TRIANGLES);
                    glVertex2i(offsetX + halfChunkSize, offsetY);
                    glVertex2i(offsetX + halfChunkSize - indicatorSize, offsetY - indicatorSize);
                    glVertex2i(offsetX + halfChunkSize + indicatorSize, offsetY - indicatorSize);
                    glEnd();
                    break;

                case BOTTOM:
                    textX = offsetX + halfChunkSize - (int) (20 * zoomLevel);
                    textY = offsetY + chunkPixelSize + textOffset;
                    glBegin(GL_TRIANGLES);
                    glVertex2i(offsetX + halfChunkSize, offsetY + chunkPixelSize);
                    glVertex2i(offsetX + halfChunkSize - indicatorSize, offsetY + chunkPixelSize + indicatorSize);
                    glVertex2i(offsetX + halfChunkSize + indicatorSize, offsetY + chunkPixelSize + indicatorSize);
                    glEnd();
                    break;

                case LEFT:
                    textX = offsetX - (int) (40 * zoomLevel);
                    textY = offsetY + halfChunkSize;
                    glBegin(GL_TRIANGLES);
                    glVertex2i(offsetX, offsetY + halfChunkSize);
                    glVertex2i(offsetX - indicatorSize, offsetY + halfChunkSize - indicatorSize);
                    glVertex2i(offsetX - indicatorSize, offsetY + halfChunkSize + indicatorSize);
                    glEnd();
                    break;

                case RIGHT:
                    textX = offsetX + chunkPixelSize + (int) (5 * zoomLevel);
                    textY = offsetY + halfChunkSize;
                    glBegin(GL_TRIANGLES);
                    glVertex2i(offsetX + chunkPixelSize, offsetY + halfChunkSize);
                    glVertex2i(offsetX + chunkPixelSize + indicatorSize, offsetY + halfChunkSize - indicatorSize);
                    glVertex2i(offsetX + chunkPixelSize + indicatorSize, offsetY + halfChunkSize + indicatorSize);
                    glEnd();
                    break;
            }
        }
    }

    private void renderChunk(int posX, int posY) {
        int offsetX = (int) (256 + cameraXLocation + posX * spriteSizeOffset * ChunkGenerator.CHUNK_SIZE * zoomLevel);
        int offsetY = (int) (256 + cameraYLocation + posY * spriteSizeOffset * ChunkGenerator.CHUNK_SIZE * zoomLevel);

        Chunk chunk = worldChunkGenerator.grabChunk(posX, posY);
        int tileSize = (int) (spriteSizeOffset * zoomLevel);

        for (int y = 0; y < ChunkGenerator.CHUNK_SIZE; y++) {
            for (int x = 0; x < ChunkGenerator.CHUNK_SIZE; x++) {
                int tileX = offsetX + x * tileSize;
                int tileY = offsetY + y * tileSize;
                renderTile(chunk.dataLayer[y][x], tileX, tileY, tileSize);
            }
        }

        if (renderChunkBorder) {
            int chunkPixelSize = (int) (spriteSizeOffset * ChunkGenerator.CHUNK_SIZE * zoomLevel);
            glColor3f(1.0f, 0.0f, 0.0f);
            glBegin(GL_LINE_LOOP);
            glVertex2i(offsetX, offsetY);
            glVertex2i(offsetX + chunkPixelSize, offsetY);
            glVertex2i(offsetX + chunkPixelSize, offsetY + chunkPixelSize);
            glVertex2i(offsetX, offsetY + chunkPixelSize);
            glEnd();
        }

//        renderChunkOpenings(chunk, offsetX, offsetY);
    }

    private void renderAllChunksInRadiusOf(int posX, int posY, int chunkRadius) {
        for (int y = (posY - chunkRadius); y < posY + (chunkRadius + 1); y++) {
            for (int x = (posX - chunkRadius); x < posX + (chunkRadius + 1); x++) {
                renderChunk(x, y);
            }
        }
    }

    public void render() {
        glLoadIdentity();

        renderAllChunksInRadiusOf(chunkThatPlayerIsCurrentlyInX, chunkThatPlayerIsCurrentlyInY, worldRenderDistance);

        chunkThatPlayerIsCurrentlyInX = -(int) (cameraXLocation / (256 * zoomLevel)) + (cameraXLocation < 0 ? 1 : 0);
        chunkThatPlayerIsCurrentlyInY = -(int) (cameraYLocation / (256 * zoomLevel)) + (cameraYLocation < 0 ? 1 : 0);
    }
}