package game2048;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the non-empty constructor of Model.
 *
 * @author Zoe Plaxco
 */
public class TestArgsConstructor {

    /**
     * Note that this isn't a possible board state.
     */
    @Test
    public void testCompletelyEmpty() {
        int[][] rawVals = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };

        Model m = new Model(rawVals, 0, 0, false);
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                assertNull(m.tile(x, y));
            }
        }
    }

    /**
     * Tests a board that is completely full except for the top row.
     */
    @Test
    public void testEmptyTopRow() {
        int[][] rawVals = new int[][]{
                {0, 0, 0, 0},
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 4, 2, 4},
        };

        Model m = new Model(rawVals, 0, 0, false);

        for (int x = 0; x < 4; x++) {
            assertNull(m.tile(x, 3));
        }
        for (int x = 1; x < 4; x++) {
            for (int y = 0; y < 3; y++) {
                assertNotNull(m.tile(x, y));
            }
        }
    }

    /**
     * Tests a board that is completely full except for the bottom row.
     */
    @Test
    public void testEmptyBottomRow() {
        int[][] rawVals = new int[][]{
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 4, 2, 4},
                {0, 0, 0, 0},
        };

        Model m = new Model(rawVals, 0, 0, false);
        for (int x = 0; x < 4; x++) {
            assertNull(m.tile(x, 0));
        }
        for (int x = 0; x < 4; x++) {
            for (int y = 1; y < 4; y++) {
                assertNotNull(m.tile(x, y));
            }
        }
    }


    /**
     * Tests a board that is completely full except for the left column.
     */
    @Test
    public void testEmptyLeftCol() {
        int[][] rawVals = new int[][]{
                {0, 4, 2, 4},
                {0, 2, 4, 2},
                {0, 4, 2, 4},
                {0, 2, 4, 2},
        };

        Model m = new Model(rawVals, 0, 0, false);
        for (int y = 0; y < 4; y++) {
            assertNull(m.tile(0, y));
        }
        for (int x = 1; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                assertNotNull(m.tile(x, y));
            }
        }
    }

    /**
     * Tests a board that is completely full except for the right column.
     */
    @Test
    public void testEmptyRightCol() {
        int[][] rawVals = new int[][]{
                {2, 4, 2, 0},
                {4, 2, 4, 0},
                {2, 4, 2, 0},
                {4, 2, 4, 0},
        };

        Model m = new Model(rawVals, 0, 0, false);
        for (int i = 0; i < 4; i++) {
            assertNull(m.tile(3, i));
        }
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 4; y++) {
                assertNotNull(m.tile(x, y));
            }
        }
    }

    /**
     * Tests a completely full board except one piece.
     */
    @Test
    public void testAlmostFullBoard() {
        int[][] rawVals = new int[][]{
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 0, 2, 4},
                {4, 2, 4, 2},
        };

        Model m = new Model(rawVals, 0, 0, false);
        for (int x = 1; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                if (x == 1 && y == 1) {
                    assertNull(m.tile(x, y));
                } else {
                    assertNotNull(m.tile(x, y));
                }
            }
        }
    }

    /**
     * Tests a completely full board.
     * The game isn't over since you can merge, but the emptySpaceExists method
     * should only look for empty space (and not adjacent values).
     */
    @Test
    public void testFullBoard() {
        int[][] rawVals = new int[][]{
                {2, 2, 2, 2},
                {2, 2, 2, 2},
                {2, 2, 2, 2},
                {2, 2, 2, 2},
        };

        Model m = new Model(rawVals, 0, 0, false);
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                assertNotNull(m.tile(x, y));
            }
        }
    }

    /**
     * Tests a completely full board.
     */
    @Test
    public void testFullBoardNoMerge() {
        int[][] rawVals = new int[][]{
                {2, 4, 2, 4},
                {4, 2, 4, 2},
                {2, 4, 2, 4},
                {4, 2, 4, 2},
        };

        Model m = new Model(rawVals, 0, 0, false);
        for (int x = 1; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                assertNotNull(m.tile(x, y));
            }
        }
    }

    /**
     * Tests that score parameters are accounted for.
     */
    @Test
    public void testScores() {
        int[][] rawVals = new int[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };

        int score = 7;
        int maxScore = 2048;

        Model m = new Model(rawVals, score, maxScore, false);
        assertEquals("Model has score passed to constructor", score,
                m.score());
        assertEquals("Model has maxScore passed to constructor", maxScore,
                m.maxScore());
    }
}
