package com.fivevsthree.puzzlecube.Screens;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.fivevsthree.puzzlecube.PuzzleCube;
import com.fivevsthree.puzzlecube.Models.Puzzle;
import com.fivevsthree.puzzlecube.Tweens.PuzzleTween;
import com.fivevsthree.puzzlecube.Tweens.StageTween;

/**
 * Game screen displaying demo puzzle and menu buttons which also functions as a
 * pause screen
 * 
 * @author splude@fivevsthree.com
 * 
 */
public class MainMenu implements Screen {

	private PuzzleCube game;

	private Stage stage;
	private Skin skin;

	private TweenManager tweenManager;

	private Timeline fadeOut;

	private Sound forwardSound;

	private Puzzle puzzle;

	private TextButton resumeGameButton, newGameButton, loadGameButton,
			saveGameButton, optionsButton, scoresButton;

	private ImageButton creditsButton, helpButton;

	private ScrollPane scrollPane;

	private Dialog confirmationDialog;

	private Timer timer;

	private Preferences preferences;

	private enum ConfirmationResult {
		YES, NO
	};

	/**
	 * Create the game screen
	 * 
	 * @param game
	 *            instance of PuzzleCube game
	 */
	public MainMenu(PuzzleCube game) {
		this.game = game;

		// Create the demo puzzle
		puzzle = new Puzzle(true);
		puzzle.getCamera().moveCamera(3.5f, 0f, 0f);

		// Set up the stage
		stage = new Stage(800, 480, true);
		tweenManager = new TweenManager();

		timer = new Timer();
		timer.stop();

		// Get skin asset
		skin = game.getAssets().get("data/skins/ui.json");

		// Set texture filter for font otherwise it will look bad
		skin.getFont("default").getRegion().getTexture()
				.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		// Get sound assets
		if (game.getAssets().isLoaded(PuzzleCube.FORWARD_SOUND_FILE)) {
			forwardSound = game.getAssets().get(PuzzleCube.FORWARD_SOUND_FILE);
		}

		preferences = Gdx.app.getPreferences(PuzzleCube.PREFERENCES_FILE);

		createUI();

		Tween.to(puzzle, PuzzleTween.ROTATE, 5f).target(360, 360, 0)
				.repeat(Tween.INFINITY, 0f).ease(TweenEquations.easeNone)
				.setCallback(new TweenCallback() {
					@Override
					public void onEvent(int type, BaseTween<?> source) {
						puzzle.getCamera().rememberCurrentRotation();
					}
				}).setCallbackTriggers(TweenCallback.ANY).start(tweenManager);

		// Schedule a random layer rotation
		timer.scheduleTask(new Task() {
			@Override
			public void run() {
				// Randomly pick the X, Y, or Z axis
				int randomAxis = MathUtils.random(1, 3);

				// Randomly pick whether to rotate forward or backward
				int randomDirection = (MathUtils.random(1, 2) == 1) ? -1 : 1;

				// Randomly pick a depth along the axis
				int randomDepth = MathUtils.random(-1, 1);

				Vector3 axis = new Vector3((randomAxis == 1) ? randomDirection
						: 0f, (randomAxis == 2) ? randomDirection : 0f,
						(randomAxis == 3) ? randomDirection : 0f);

				// Rotate the layer
				puzzle.rotateLayerAnimation(axis, randomDepth, 1f, false);
			}
		}, 2f, 2f);
	}

	/**
	 * Create menu buttons and their click listeners
	 */
	private void createUI() {
		// Create our buttons
		resumeGameButton = new TextButton("Resume Game", skin);
		newGameButton = new TextButton("New Game", skin);
		loadGameButton = new TextButton("Load Game", skin);
		saveGameButton = new TextButton("Save Game", skin);
		optionsButton = new TextButton("Options", skin);
		scoresButton = new TextButton("Scores", skin);

		creditsButton = new ImageButton(skin, "credits");
		helpButton = new ImageButton(skin, "help");
		helpButton.setPosition(0, stage.getHeight() - helpButton.getHeight());

		// Resume game click listener
		resumeGameButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				game.resumeGame();
			}
		});

		// New game click listener
		newGameButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (game.isGameLoaded() && !game.isGameSolved()) {
					confirmationDialog.show(stage);

					puzzle.color.set(0.25f, 0.25f, 0.25f, 1f);
					for (Actor actor : stage.getActors()) {
						if (!actor.equals(confirmationDialog)) {
							actor.getColor().a = 0.25f;
						}
					}
				} else {
					newGame();
				}
			}
		});

		// Load game click listener
		loadGameButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				game.loadGame("game.sav");
			}
		});

		// Save game click listener
		saveGameButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				game.saveGame("game.sav");
			}
		});

		// Options click listener
		optionsButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				game.showOptionsScreen();
			}
		});

		scoresButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				game.showScoreScreen();
			}
		});

		creditsButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				game.showCreditsScreen();
			}
		});

		helpButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				game.showHelpScreen();
			}
		});

		// Confirmation dialog
		confirmationDialog = new Dialog("", skin) {
			@Override
			protected void result(Object object) {
				ConfirmationResult result = (ConfirmationResult) object;

				puzzle.color.set(1f, 1f, 1f, 1f);
				for (Actor actor : stage.getActors()) {
					if (!actor.equals(confirmationDialog)) {
						actor.getColor().a = 1f;
					}
				}

				switch (result) {
				case YES:
					newGame();
					break;

				default:
					break;
				}
			}
		};

		Table buttonTable = confirmationDialog.getButtonTable();

		confirmationDialog.getCell(buttonTable).expandX().fillX();
		buttonTable.defaults().expandX().fillX();

		confirmationDialog.text("Current game will be lost.\nAre you sure?");
		confirmationDialog.button("Yes", ConfirmationResult.YES);
		confirmationDialog.button("No", ConfirmationResult.NO);
		confirmationDialog.setMovable(false);
		confirmationDialog.setTransform(false);

		scrollPane = new ScrollPane(null);
		scrollPane.setFillParent(true);
		scrollPane.setTransform(false);

		// No fading
		Dialog.fadeDuration = 0f;
	}

	/**
	 * Fade out and start a new game
	 */
	private void newGame() {
		// We don't want any buttons pressed at this point
		Gdx.input.setInputProcessor(null);

		if (forwardSound != null && preferences.getBoolean("sound", true)) {
			forwardSound.play(preferences.getFloat("sound-volume",
					PuzzleCube.DEFAULT_SOUND_VOLUME));
		}

		TweenCallback outCallback = new TweenCallback() {
			@Override
			public void onEvent(int type, BaseTween<?> source) {
				// Fade our is done so start our new game
				game.newGame();
			}
		};

		// Start fading out
		fadeOut = Timeline
				.createParallel()
				.push(Tween.to(puzzle, PuzzleTween.FADE, 2).target(0f, 0f, 0f)
						.ease(TweenEquations.easeInOutQuad))
				.push(Tween.to(stage, StageTween.FADE, 2).target(0f)
						.ease(TweenEquations.easeInOutQuad))
				.setCallback(outCallback)
				.setCallbackTriggers(TweenCallback.COMPLETE)
				.start(tweenManager);
	}

	/**
	 * Revolve camera around puzzle, draw the demo puzzle, update tweens and
	 * stage actors
	 */
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Update our tweens if there are any
		tweenManager.update(delta);

		// Draw the puzzle
		game.getPuzzleRenderer().render(puzzle, delta);

		// Update actors on the stage
		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void show() {
		// Create our table of menu buttons
		Table table = new Table(skin);
		table.setTransform(false);
		table.right();

		float cellWidth = 350f;

		// Add our buttons to the table
		if (game.isGameLoaded() && !game.isGameSolved()) {
			table.row();
			table.add(resumeGameButton).width(cellWidth);
		}

		table.row();
		table.add(newGameButton).width(cellWidth);

		// if (game.savedGamesExist()) {
		// table.row();
		// table.add(loadGameButton).width(cellWidth);
		// }

		// if (game.isGameLoaded() && !game.isGameSolved()) {
		// table.row();
		// table.add(saveGameButton).width(cellWidth);
		// }

		table.row();
		table.add(optionsButton).width(cellWidth);

		if (game.getBestScores().getScores().length > 0) {
			table.row();
			table.add(scoresButton).width(cellWidth);
		}

		scrollPane.setWidget(table);
		scrollPane.pack();

		stage.clear();
		stage.addActor(scrollPane);
		// stage.addActor(creditsButton);

		// Process input from our stage
		Gdx.input.setInputProcessor(stage);
		Gdx.input.setCatchBackKey(false);
		Gdx.input.setCatchMenuKey(false);

		timer.start();
		tweenManager.resume();

		// Reset colors back to original
		stage.getRoot().setColor(1f, 1f, 1f, 1f);
		puzzle.color.set(1f, 1f, 1f, 1f);
		for (Actor actor : stage.getActors()) {
			if (!actor.equals(confirmationDialog)) {
				actor.getColor().a = 1f;
			}
		}
	}

	@Override
	public void hide() {
		// Stop random rotations and remove the scheduled task
		timer.stop();
		tweenManager.pause();
	}

	@Override
	public void pause() {
		// Stop random rotations
		timer.stop();
		tweenManager.pause();

		if (fadeOut != null) {
			fadeOut.kill();
		}
	}

	@Override
	public void resume() {
		// Resume random rotations
		timer.start();
		tweenManager.resume();

		// Reset colors back to original
		stage.getRoot().setColor(1f, 1f, 1f, 1f);
		puzzle.color.set(1f, 1f, 1f, 1f);

		// Start accepting input again if it was stopped
		Gdx.input.setInputProcessor(stage);
		Gdx.input.setCatchBackKey(false);
		Gdx.input.setCatchMenuKey(false);
	}

	@Override
	public void dispose() {
		if (stage != null) {
			stage.dispose();
			stage = null;
		}
		if (puzzle != null) {
			puzzle.dispose();
			puzzle = null;
		}
	}

}
