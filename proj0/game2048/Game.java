package game2048;

import java.awt.event.KeyEvent;

import static game2048.Side.*;

/** The input/output and GUI controller for play of a game of 2048.
 *  @author P. N. Hilfinger. */
public class Game {

    /** Controller for a game represented by MODEL, using SOURCE as the
     *  source of key inputs and random Tiles. */
    public Game(Model model, InputSource source) {
        _model = model;
        _source = source;
        _playing = true;
    }

    /** Return true iff we have not received a Quit command. */
    boolean playing() {
        return _playing;
    }

    /** Clear the board and play one game, until receiving a quit or
     *  new-game request.  Update the viewer with each added tile or
     *  change in the board from tilting. */
    void playGame() {
        _model.clear();
        _model.addTile(getValidNewTile());
        while (_playing) {
            if (!_model.gameOver()) {
                _model.addTile(getValidNewTile());
                _model.notifyObservers();
            }

            boolean moved;
            moved = false;
            while (!moved) {
                String cmnd = _source.getKey();
                switch (cmnd) {
                    case "Quit":
                        _playing = false;
                        return;
                    case "New Game":
                        return;
                    case KeyEvent.VK_UP + "": case KeyEvent.VK_DOWN + "": case KeyEvent.VK_LEFT + "": case KeyEvent.VK_RIGHT+ "":
                    case "\u2190": case "\u2191": case "\u2192": case "\u2193":
                        if (!_model.gameOver() && _model.tilt(keyToSide(cmnd))) {
                            _model.notifyObservers(cmnd);
                            moved = true;
                        }
                        break;
                    default:
                        break;
                }

            }
        }
    }

    /** Return the side indicated by KEY ("Up", "Down", "Left",
     *  or "Right"). */
    private Side keyToSide(String key) {
        return switch (key) {
            case KeyEvent.VK_UP + "", "\u2191" -> NORTH;
            case KeyEvent.VK_DOWN + "", "\u2193" -> SOUTH;
            case KeyEvent.VK_LEFT + "", "\u2190" -> WEST;
            case KeyEvent.VK_RIGHT+ "", "\u2192" -> EAST;
            default -> throw new IllegalArgumentException("unknown key designation");
        };
    }

    /** Return a valid tile, using our source's tile input until finding
     *  one that fits on the current board. Assumes there is at least one
     *  empty square on the board. */
    private Tile getValidNewTile() {
        while (true) {
            Tile tile = _source.getNewTile(_model.size());
            if (_model.tile(tile.col(), tile.row()) == null) {
                return tile;
            }
        }
    }

    /** The playing board. */
    private final Model _model;

    /** Input source from standard input. */
    private final InputSource _source;

    /** True while user is still willing to play. */
    private boolean _playing;

}
