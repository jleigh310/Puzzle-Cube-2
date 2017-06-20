package com.fivevsthree.puzzlecube;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import aurelienribon.tweenengine.Tween;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.fivevsthree.puzzlecube.Models.Layer;
import com.fivevsthree.puzzlecube.Models.Puzzle;
import com.fivevsthree.puzzlecube.Models.ScoreList;
import com.fivevsthree.puzzlecube.Screens.CreditsScreen;
import com.fivevsthree.puzzlecube.Screens.GameScreen;
import com.fivevsthree.puzzlecube.Screens.HelpScreen;
import com.fivevsthree.puzzlecube.Screens.MainMenu;
import com.fivevsthree.puzzlecube.Screens.OptionsScreen;
import com.fivevsthree.puzzlecube.Screens.ScoreScreen;
import com.fivevsthree.puzzlecube.Screens.SplashScreen;
import com.fivevsthree.puzzlecube.Tweens.ActorTween;
import com.fivevsthree.puzzlecube.Tweens.LayerTween;
import com.fivevsthree.puzzlecube.Tweens.PuzzleTween;
import com.fivevsthree.puzzlecube.Tweens.SpriteTween;
import com.fivevsthree.puzzlecube.Tweens.StageTween;
import com.fivevsthree.puzzlecube.Views.PuzzleRenderer;

public class PuzzleCube extends Game {

	private AssetManager assetManager;

	private SplashScreen splashScreen;
	private MainMenu mainMenu;
	private GameScreen gameScreen;
	private OptionsScreen optionsScreen;
	private CreditsScreen creditsScreen;
	private HelpScreen helpScreen;
	private ScoreScreen scoreScreen;

	private Puzzle puzzle;
	private PuzzleRenderer puzzleRenderer;

	private boolean gameSaved;

	private ScoreList bestScores;

	private static DateFormat dateFormat = DateFormat.getDateInstance();

	public static final String PREFERENCES_FILE = "puzzle-cube";
	public static final String AUTO_SAVE_FILE = "autosave";
	public static final String SCORES_FILE = "scores";

	public static final String PUZZLE_MUSIC_FILE = "data/music/puzzle.mp3";
	public static final String FORWARD_SOUND_FILE = "data/sounds/forward.mp3";
	public static final String INVALID_SOUND_FILE = "data/sounds/invalid.mp3";
	public static final String EXPLOSION_SOUND_FILE = "data/sounds/explosion.mp3";
	public static final String NEW_RECORD_FILE = "data/sounds/new-record.mp3";
	public static final String ALARM_FILE = "data/sounds/alarm.mp3";

	public static final float DEFAULT_SOUND_VOLUME = 0.5f;
	public static final float DEFAULT_MUSIC_VOLUME = 0.5f;
	public static final float DEFAULT_TOUCH_SENSITIVITY = 10f;

	public static final long PUZZLE_STARTING_SECONDS = -15;
	public static final int PUZZLE_SCRAMBLE_ROTATIONS = 20;

	public static final Color[] DEFAULT_COLOR = {
			// Bottom - Purple/Orange
			new Color(0.5f, 0.3f, 0.8f, 1f),

			// Top - Red
			new Color(1f, 0.1f, 0.1f, 1f),

			// Back - Yellow
			new Color(1f, 1f, 0.2f, 1f),

			// Front - White
			new Color(1f, 1f, 1f, 1f),

			// Left - Blue
			new Color(0f, 0.4f, 0.9f, 1f),

			// Right - Green
			new Color(0f, 0.8f, 0.2f, 1f) };

	/**
	 * Currently loaded assets
	 * 
	 * @return asset manager
	 */
	public AssetManager getAssets() {
		return assetManager;
	}

	/**
	 * Currently loaded puzzle
	 * 
	 * @return puzzle
	 */
	public Puzzle getPuzzle() {
		return puzzle;
	}

	public PuzzleRenderer getPuzzleRenderer() {
		return puzzleRenderer;
	}

	public ScoreList getBestScores() {
		return bestScores;
	}

	public static DateFormat getDateFormat() {
		return dateFormat;
	}

	public static void setDateFormat(DateFormat dateFormat) {
		PuzzleCube.dateFormat = dateFormat;
	}

	/**
	 * Create our screens after assets are loaded
	 */
	public void createScreens() {
		/*
		 * We do not want to instantiate the screens when they are first shown
		 * in case they are shown from a callback. If that happens, there is a
		 * chance of a crash occurring in the native code.
		 */
		if (puzzleRenderer == null) {
			puzzleRenderer = new PuzzleRenderer(this);
		}
		if (mainMenu == null) {
			mainMenu = new MainMenu(this);
		}
		if (optionsScreen == null) {
			optionsScreen = new OptionsScreen(this);
		}
		if (gameScreen == null) {
			gameScreen = new GameScreen(this);
		}
		if (creditsScreen == null) {
			creditsScreen = new CreditsScreen(this);
		}
		if (helpScreen == null) {
			helpScreen = new HelpScreen(this);
		}
		if (scoreScreen == null) {
			scoreScreen = new ScoreScreen(this);
		}
	}

	/**
	 * Display the main menu, disposing of the splash screen
	 */
	public void showMainMenu() {
		// Make the main menu the active screen
		if (mainMenu != null) {
			setScreen(mainMenu);
		}
	}

	public void showOptionsScreen() {
		// Make the options screen the active screen
		if (optionsScreen != null) {
			setScreen(optionsScreen);
		}
	}

	/**
	 * Display the game screen
	 */
	public void showGameScreen() {
		// Make the game screen the active screen
		if (gameScreen != null) {
			setScreen(gameScreen);
			gameSaved = false;
		}
	}

	public void showCreditsScreen() {
		if (creditsScreen != null) {
			setScreen(creditsScreen);
		}
	}

	public void showHelpScreen() {
		if (helpScreen != null) {
			setScreen(helpScreen);
		}
	}

	public void showScoreScreen() {
		if (scoreScreen != null) {
			setScreen(scoreScreen);
		}
	}

	/**
	 * Create a new game, disposing of the current if there is one
	 */
	public void newGame() {
		// Create a new puzzle
		if (puzzle != null) {
			puzzle.dispose();
		}

		puzzle = new Puzzle();
		puzzle.scramble(PUZZLE_SCRAMBLE_ROTATIONS);

		gameScreen.reset();
		showGameScreen();
	}

	/**
	 * Show the existing game screen
	 */
	public void resumeGame() {
		showGameScreen();
	}

	public void gameSolved() {
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		bestScores.addScore(puzzle.getSeconds(), puzzle.getMoves(),
				df.format(date), puzzle.getRotationAnimation());
	}

	/**
	 * Does a game currently exist?
	 * 
	 * @return is a game exists
	 */
	public boolean isGameLoaded() {
		return (puzzle != null);
	}

	public boolean isGameSolved() {
		return (puzzle != null && puzzle.isSolved());
	}

	/**
	 * Check if there are any saved games
	 * 
	 * @return true if saved games are found
	 */
	public boolean savedGamesExist() {
		if (Gdx.files.isLocalStorageAvailable()) {
			// Get root directory of game
			FileHandle dir = Gdx.files.local("/");

			// Make sure the handle is a directory
			if (dir.isDirectory()) {
				// Find any save files
				if (dir.list(".sav").length > 0) {
					// Found at least one
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Save a current game
	 * 
	 * @param fileName
	 *            name of the save file
	 * @param isAutoSave
	 *            is this an auto saved game
	 */
	public void saveGame(String fileName) {
		if (isGameLoaded() && Gdx.files.isLocalStorageAvailable()) {
			Json json = new Json(OutputType.minimal);

			// Save current game
			FileHandle file = Gdx.files.local(fileName);
			file.writeString(json.toJson(puzzle), false);

			// Remove auto save
			file = Gdx.files.local(AUTO_SAVE_FILE);
			if (file.exists()) {
				file.delete();
			}

			gameSaved = true;
		}
	}

	public void autoSave() {
		if (isGameLoaded() && Gdx.files.isLocalStorageAvailable()) {
			FileHandle file = Gdx.files.local(AUTO_SAVE_FILE);

			if (isGameSolved() || gameSaved) {
				// Remove auto save if game was solved
				if (file.exists()) {
					file.delete();
				}
			} else {
				// Save current game as auto save
				Json json = new Json(OutputType.minimal);
				file.writeString(json.toJson(puzzle), false);
			}
		}
	}

	public void saveScores() {
		if (Gdx.files.isLocalStorageAvailable() && bestScores != null) {
			Json json = new Json(OutputType.minimal);

			FileHandle file = Gdx.files.local(SCORES_FILE);
			file.writeString(json.toJson(bestScores), false);
	}

	public void autoLoad() {
		if (Gdx.files.isLocalStorageAvailable()) {
			Json json = new Json(OutputType.minimal);

			FileHandle loadFile = Gdx.files.local(AUTO_SAVE_FILE);
			if (loadFile.exists()) {
				puzzle = json.fromJson(Puzzle.class, loadFile);
			}
		}
	}

	/**
	 * Load an existing game
	 * 
	 * @param fileName
	 *            name of the save file
	 * @param isAutoSave
	 *            is this an auto saved game
	 */
	public void loadGame(String fileName) {
		if (Gdx.files.isLocalStorageAvailable()) {
			Json json = new Json(OutputType.minimal);

			FileHandle loadFile = Gdx.files.local(fileName);
			if (loadFile.exists()) {
				puzzle = json.fromJson(Puzzle.class, loadFile);

				gameScreen.reset();
				showGameScreen();
			}
		}
	}

	public void loadScores() {
		if (Gdx.files.isLocalStorageAvailable()) {
			Json json = new Json(OutputType.minimal);

			FileHandle file = Gdx.files.local(SCORES_FILE);
			if (file.exists()) {
				bestScores = json.fromJson(ScoreList.class, file);
			}
		}

		if (bestScores == null) {
			bestScores = new ScoreList();
		}
	}

	/**
	 * Called when app is started initially or after disposed
	 */
	@Override
	public void create() {
		// Start loading assets
		assetManager = new AssetManager();

		// Textures
		assetManager.load("data/textures/mask.png", Texture.class);
		assetManager.load("data/textures/solved.atlas", TextureAtlas.class);
		assetManager.load("data/textures/new-record.atlas", TextureAtlas.class);

		// Skins
		assetManager.load("data/skins/ui.json", Skin.class);

		// Music
		assetManager.load(PUZZLE_MUSIC_FILE, Music.class);

		// Sound Effects
		assetManager.load(INVALID_SOUND_FILE, Sound.class);
		assetManager.load(FORWARD_SOUND_FILE, Sound.class);
		assetManager.load(EXPLOSION_SOUND_FILE, Sound.class);
		assetManager.load(NEW_RECORD_FILE, Sound.class);
		assetManager.load(ALARM_FILE, Sound.class);

		assetManager.setErrorListener(new AssetErrorListener() {
			@SuppressWarnings("rawtypes")
			@Override
			public void error(AssetDescriptor asset, Throwable throwable) {
				if (asset.type.equals(Music.class)
						|| asset.type.equals(Sound.class)) {
					Gdx.app.log("Asset Manager", String.format(
							"Could not load asset '%s'", asset.fileName));
				} else {
					Gdx.app.exit();
				}
			}
		});

		// The splash screen is displayed while we wait for assets to load
		splashScreen = new SplashScreen(this);
		setScreen(splashScreen);

		// If there is an auto saved game then load it
		autoLoad();

		loadScores();

		Tween.setWaypointsLimit(10);
		Tween.setCombinedAttributesLimit(3);
		Tween.registerAccessor(Layer.class, new LayerTween());
		Tween.registerAccessor(Puzzle.class, new PuzzleTween());
		Tween.registerAccessor(Sprite.class, new SpriteTween());
		Tween.registerAccessor(Stage.class, new StageTween());
		Tween.registerAccessor(Actor.class, new ActorTween());
	}

	@Override
	public void render() {
		super.render();
	}

	/**
	 * Called on desktop app, not on Android
	 */
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	/**
	 * Called when app is disposed (back button) or suspended (incoming call,
	 * home button, etc)
	 */
	@Override
	public void pause() {
		super.pause();

		// If a puzzle is loaded then auto save it
		autoSave();
		saveScores();
	}

	/**
	 * Called when app is started after suspension. Cannot resume if back button
	 * is pressed
	 */
	@Override
	public void resume() {
		super.resume();

		// If the game screen is active, pause it and show the menu screen
		if (!getScreen().equals(splashScreen)) {
			showMainMenu();
		}
	}

	/**
	 * Called when back button is pressed
	 */
	@Override
	public void dispose() {
		super.dispose();

		if (splashScreen != null) {
			splashScreen.dispose();
			splashScreen = null;
		}
		if (mainMenu != null) {
			mainMenu.dispose();
			mainMenu = null;
		}
		if (gameScreen != null) {
			gameScreen.dispose();
			gameScreen = null;
		}
		if (optionsScreen != null) {
			optionsScreen.dispose();
			optionsScreen = null;
		}
		if (creditsScreen != null) {
			creditsScreen.dispose();
			creditsScreen = null;
		}
		if (helpScreen != null) {
			helpScreen.dispose();
			helpScreen = null;
		}
		if (scoreScreen != null) {
			scoreScreen.dispose();
			scoreScreen = null;
		}
		if (assetManager != null) {
			assetManager.dispose();
			assetManager = null;
		}
		if (puzzleRenderer != null) {
			puzzleRenderer.dispose();
			puzzleRenderer = null;
		}
		if (puzzle != null) {
			puzzle.dispose();
			puzzle = null;
		}
	}

}
