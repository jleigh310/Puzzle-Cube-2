package com.fivevsthree.puzzlecube.Screens;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;
import aurelienribon.tweenengine.TweenManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.fivevsthree.puzzlecube.PuzzleCube;
import com.fivevsthree.puzzlecube.Callbacks.PuzzleCallback;
import com.fivevsthree.puzzlecube.Callbacks.PuzzleControllerCallback;
import com.fivevsthree.puzzlecube.Controllers.PuzzleController;
import com.fivevsthree.puzzlecube.Controllers.ScreenController;
import com.fivevsthree.puzzlecube.Models.Puzzle;
import com.fivevsthree.puzzlecube.Models.Puzzle.SolvedAnimation;
import com.fivevsthree.puzzlecube.Tweens.ActorTween;
import com.fivevsthree.puzzlecube.Tweens.SpriteTween;

public class GameScreen implements Screen {

	private PuzzleCube game;

	private PuzzleController puzzleController;
	private GestureDetector gestureDetector;
	private ScreenController screenController;
	private InputMultiplexer inputMultiplexer;

	private Texture texture;
	private TextureAtlas solvedAtlas, newRecordAtlas;

	private Array<Sprite> solvedSprites;
	private Sprite newRecordSprite;

	private Music backgroundMusic;
	private Sound explosionSound, newRecordSound, alarmSound;

	private TweenManager tweenManager;

	private Stage stage;
	private Table table;
	private Image timeImage, movesImage, bestTimeImage, bestMovesImage;
	private Label timeLabel, movesLabel;
	private SpriteBatch batch;
	private Skin skin;

	private ImageButton lockButton, undoButton, pauseButton,
			verticalLockButton, horizontalLockButton, shuffleButton;

	private SolvedAnimation solvedAnimation;

	private Preferences preferences;

	public GameScreen(PuzzleCube game) {
		this.game = game;

		stage = new Stage(800, 480, true);
		batch = new SpriteBatch();

		table = new Table();

		// Get the texture to apply to the cubes
		texture = game.getAssets().get("data/textures/mask.png");
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		solvedAtlas = game.getAssets().get("data/textures/solved.atlas");
		solvedSprites = solvedAtlas.createSprites();

		float w = solvedSprites.first().getWidth();
		float h = solvedSprites.first().getHeight();
		float x = stage.getWidth() / 2 - (solvedSprites.size * w) / 2;
		float y = stage.getHeight() / 2 - h / 2;

		int i = 0;
		for (Sprite sprite : solvedSprites) {
			sprite.setColor(PuzzleCube.DEFAULT_COLOR[i]);
			sprite.setOrigin(w / 2, h / 2);
			sprite.setPosition(x + w * i, y);
			i++;
		}

		newRecordAtlas = game.getAssets().get("data/textures/new-record.atlas");
		newRecordSprite = newRecordAtlas.createSprite("new-record");

		skin = game.getAssets().get("data/skins/ui.json");

		createUI();

		if (game.getAssets().isLoaded(PuzzleCube.PUZZLE_MUSIC_FILE)) {
			backgroundMusic = game.getAssets()
					.get(PuzzleCube.PUZZLE_MUSIC_FILE);
			backgroundMusic.setLooping(true);
		}

		if (game.getAssets().isLoaded(PuzzleCube.EXPLOSION_SOUND_FILE)) {
			explosionSound = game.getAssets().get(
					PuzzleCube.EXPLOSION_SOUND_FILE);
		}

		if (game.getAssets().isLoaded(PuzzleCube.NEW_RECORD_FILE)) {
			newRecordSound = game.getAssets().get(PuzzleCube.NEW_RECORD_FILE);
		}

		if (game.getAssets().isLoaded(PuzzleCube.ALARM_FILE)) {
			alarmSound = game.getAssets().get(PuzzleCube.ALARM_FILE);
		}

		preferences = Gdx.app.getPreferences(PuzzleCube.PREFERENCES_FILE);

		tweenManager = new TweenManager();
	}

	private void createUI() {
		timeImage = new Image(skin, "time-image");
		timeImage.setScaling(Scaling.none);
		timeImage.setAlign(Align.left);

		movesImage = new Image(skin, "moves-image");
		movesImage.setScaling(Scaling.none);
		movesImage.setAlign(Align.right);

		bestTimeImage = new Image(skin, "star-image");
		bestTimeImage.setScaling(Scaling.none);
		bestTimeImage.setAlign(Align.left);

		bestMovesImage = new Image(skin, "star-image");
		bestMovesImage.setScaling(Scaling.none);
		bestMovesImage.setAlign(Align.right);

		timeLabel = new Label("", skin);
		timeLabel.setAlignment(Align.left);

		movesLabel = new Label("", skin);
		movesLabel.setAlignment(Align.right);

		newRecordSprite.setPosition(
				stage.getWidth() / 2 - newRecordSprite.getWidth() / 2,
				stage.getHeight() / 2 + newRecordSprite.getHeight() * 1.5f);

		newRecordSprite.setOrigin(newRecordSprite.getWidth() / 2,
				newRecordSprite.getHeight() / 2);

		lockButton = new ImageButton(skin, "lock");
		lockButton.setChecked(false);
		lockButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (lockButton.isChecked()) {
					verticalLockButton.setChecked(true);
					horizontalLockButton.setChecked(true);
				} else {
					verticalLockButton.setChecked(false);
					horizontalLockButton.setChecked(false);
				}
			}
		});

		verticalLockButton = new ImageButton(skin, "vertical-lock");
		verticalLockButton.setChecked(false);
		verticalLockButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (verticalLockButton.isChecked()) {
					puzzleController.pauseVerticalRotation();
				} else {
					puzzleController.resumeVerticalRotation();
				}

				lockButton.setChecked(verticalLockButton.isChecked()
						&& horizontalLockButton.isChecked());
			}
		});

		horizontalLockButton = new ImageButton(skin, "horizontal-lock");
		horizontalLockButton.setChecked(false);
		horizontalLockButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (horizontalLockButton.isChecked()) {
					puzzleController.pauseHorizontalRotation();
				} else {
					puzzleController.resumeHotizontalRotation();
				}

				lockButton.setChecked(verticalLockButton.isChecked()
						&& horizontalLockButton.isChecked());
			}
		});

		undoButton = new ImageButton(skin, "undo");
		undoButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				game.getPuzzle().undoRotation(0.4f);
			}
		});

		pauseButton = new ImageButton(skin, "pause");
		pauseButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				game.showMainMenu();
			}
		});

		shuffleButton = new ImageButton(skin, "shuffle");
		shuffleButton.setVisible(false);
		shuffleButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				shuffleButton.setVisible(true);
				game.getPuzzle().scramble(PuzzleCube.PUZZLE_SCRAMBLE_ROTATIONS);
				game.getPuzzle().setSeconds(PuzzleCube.PUZZLE_STARTING_SECONDS);
				timeLabel.setColor(Color.RED);
				timeLabel.setText(timeString(game.getPuzzle().getSeconds()));
				game.getPuzzle().resetTimer();
			}
		});
	}

	private String timeString(long seconds) {
		/*
		 * Do not use TimeUnit as some methods, such as toHours and toMinutes,
		 * are not available for devices running an API earlier than 9
		 */

		seconds = Math.abs(seconds);

		// Calculate hours
		long h = seconds / 3600;

		// Calculate minutes
		long m = (seconds / 60) - (h * 60);

		// Calculate seconds
		long s = seconds - (m * 60) - (h * 3600);

		return String.format("%d:%02d:%02d", h, m, s);
	}

	@SuppressWarnings("unchecked")
	private void gameSolved() {
		inputMultiplexer.removeProcessor(screenController);
		inputMultiplexer.removeProcessor(stage);
		puzzleController.pauseHitCheck();

		if (backgroundMusic != null) {
			backgroundMusic.stop();
		}

		float w = solvedSprites.first().getWidth();
		float h = solvedSprites.first().getHeight();
		float x = stage.getWidth() / 2 - (solvedSprites.size * w) / 2;
		float y = stage.getHeight();
		float target = y / 2 - h / 2;

		Timeline overall = Timeline.createSequence();

		Timeline timelineIn = Timeline.createParallel();

		float delay = 0;
		int i = 0;
		for (Sprite sprite : solvedSprites) {
			sprite.setPosition(x + w * i, y);

			timelineIn.push(Tween.to(sprite, SpriteTween.POSITION, 2f)
					.target(sprite.getX(), target).delay(delay)
					.ease(TweenEquations.easeOutBounce));

			timelineIn.push(Tween.to(sprite, SpriteTween.FADE, 1f).target(1f)
					.delay(delay));

			delay += 0.5f;
			i++;
		}

		int compareTime = (int) (game.getBestScores().getBestSeconds().seconds - game
				.getPuzzle().getSeconds());
		int compareMoves = (int) (game.getBestScores().getBestMoves().moves - game
				.getPuzzle().getMoves());

		final boolean newTimeRecord = (game.getBestScores().getBestSeconds().seconds == 0 || compareTime > 0);
		final boolean newMovesRecord = (game.getBestScores().getBestMoves().moves == 0 || compareMoves > 0);

		if (newTimeRecord || newMovesRecord) {
			timelineIn.push(Tween.to(newRecordSprite, SpriteTween.FADE, 0.5f)
					.target(1f).delay(4f).ease(TweenEquations.easeNone));

			timelineIn.push(Tween
					.from(newRecordSprite, SpriteTween.SCALE, 0.25f)
					.target(5f)
					.delay(4f)
					.ease(TweenEquations.easeNone)
					.setCallback(new TweenCallback() {
						@Override
						public void onEvent(int type, BaseTween<?> source) {
							switch (type) {
							case START:
								if (newRecordSound != null
										&& preferences
												.getBoolean("sound", true)) {
									newRecordSound.play(preferences.getFloat(
											"sound-volume",
											PuzzleCube.DEFAULT_SOUND_VOLUME));
								}
								break;

							case COMPLETE:
								if (newTimeRecord) {
									table.getCell(timeImage).setWidget(
											bestTimeImage);
									timeLabel.setVisible(true);
								}
								if (newMovesRecord) {
									table.getCell(movesImage).setWidget(
											bestMovesImage);
									movesLabel.setVisible(true);
								}
								break;
							}
						}
					})
					.setCallbackTriggers(
							TweenCallback.START | TweenCallback.COMPLETE));
		}

		timelineIn.setCallback(new TweenCallback() {
			@Override
			public void onEvent(int type, BaseTween<?> source) {
				inputMultiplexer.addProcessor(screenController);
			}
		}).setCallbackTriggers(TweenCallback.COMPLETE);

		Timeline timelineOut = Timeline.createParallel().delay(3f);

		for (Sprite sprite : solvedSprites) {
			timelineOut.push(Tween.to(sprite, SpriteTween.FADE, 1f).target(0f)
					.ease(TweenEquations.easeNone));
		}

		timelineOut.push(Tween.to(newRecordSprite, SpriteTween.FADE, 1f)
				.target(0f).ease(TweenEquations.easeNone));

		overall.push(timelineIn);

		if (solvedAnimation == Puzzle.SolvedAnimation.NoAnimation) {
			overall.push(timelineOut);
		}

		overall.start(tweenManager);

		if (solvedAnimation == Puzzle.SolvedAnimation.Explode) {
			if (explosionSound != null && preferences.getBoolean("sound", true)) {
				explosionSound.play(preferences.getFloat("sound-volume",
						PuzzleCube.DEFAULT_SOUND_VOLUME));
			}

			game.getPuzzle().explode();
		}
	}

	public void reset() {
		// Restart the music if a previous game was being played
		if (backgroundMusic != null) {
			backgroundMusic.stop();
		}

		verticalLockButton.setChecked(false);
		horizontalLockButton.setChecked(false);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		texture.bind();

		// Draw the puzzle
		game.getPuzzleRenderer().render(game.getPuzzle(), delta);

		// Update and draw the HUD
		stage.act(delta);
		stage.draw();

		tweenManager.update(delta);

		batch.setProjectionMatrix(stage.getCamera().combined);
		batch.begin();

		for (Sprite sprite : solvedSprites) {
			sprite.draw(batch);
		}

		newRecordSprite.draw(batch);

		batch.end();
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void show() {
		// We want to accept HUD and button input, then gesture input
		puzzleController = new PuzzleController(game.getPuzzle());
		puzzleController.setCallback(new PuzzleControllerCallback() {
			@Override
			public void layerRotation(Vector3 axis, float depth) {
				game.getPuzzle().rotateLayerAnimation(axis, depth, 0.4f, true);
			}
		});
		gestureDetector = new GestureDetector(20, 0.5f, 2, 0.25f,
				puzzleController);
		screenController = new ScreenController(game);
		inputMultiplexer = new InputMultiplexer(screenController, stage,
				gestureDetector);

		if (verticalLockButton.isChecked()) {
			puzzleController.pauseVerticalRotation();
		}
		if (horizontalLockButton.isChecked()) {
			puzzleController.pauseHorizontalRotation();
		}
		lockButton.setChecked(verticalLockButton.isChecked()
				&& horizontalLockButton.isChecked());

		game.getPuzzle().setCallback(new PuzzleCallback() {
			@Override
			public void rotationStarted() {
				puzzleController.pauseHitCheck();

				boolean allowUndo = !game.getPuzzle().isRotationsEmpty();
				skin.setEnabled(undoButton, allowUndo);
				undoButton.setTouchable((allowUndo) ? Touchable.enabled
						: Touchable.disabled);

				if (game.getPuzzle().getSeconds() < 0) {
					game.getPuzzle().setSeconds(0);
					game.getPuzzle().resetTimer();
					timeLabel.setColor(Color.WHITE);
					timeLabel
							.setText(timeString(game.getPuzzle().getSeconds()));
					shuffleButton.setVisible(false);
					tweenManager.killAll();
				}
			}

			@Override
			public void rotationComplete(boolean solved) {
				if (!solved) {
					puzzleController.resumeHitCheck();
				} else {
					game.getPuzzle().stopTimer();
					gameSolved();
					game.gameSolved();
				}
			}

			@Override
			public void timerChanged(long seconds) {
				timeLabel.setText(timeString(seconds));

				if (seconds < 0) {
					if (timeLabel.getColor().equals(Color.WHITE)) {
						timeLabel.setColor(Color.RED);
						shuffleButton.setVisible(true);
					}
					if (seconds == -5) {
						if (alarmSound != null
								&& preferences.getBoolean("sound", true)) {
							alarmSound.play(preferences.getFloat(
									"sound-volume",
									PuzzleCube.DEFAULT_SOUND_VOLUME));
						}
					}
				} else if (timeLabel.getColor().equals(Color.RED)) {
					timeLabel.setColor(Color.WHITE);
					shuffleButton.setVisible(false);
				}
			}

			@Override
			public void moveCounterChanged(long counter) {
				movesLabel.setText(String.valueOf(counter));
			}
		});

		boolean showTimer = preferences.getBoolean("timer", true);
		boolean showCounter = preferences.getBoolean("counter", true);

		if (game.getPuzzle().getSeconds() < 0) {
			timeLabel.setColor(Color.RED);
			shuffleButton.setColor(Color.WHITE);
			shuffleButton.setVisible(true);

			Timeline.createParallel()
					.push(Tween.to(shuffleButton, ActorTween.COLOR, 0.5f)
							.target(Color.RED.r, 0, 0).repeatYoyo(1, 0)
							.ease(TweenEquations.easeNone))
					.repeat(Tween.INFINITY, 0).start(tweenManager);
		} else {
			timeLabel.setColor(Color.WHITE);
			shuffleButton.setVisible(false);
		}

		timeLabel.setText(timeString(game.getPuzzle().getSeconds()));
		movesLabel.setText(String.valueOf(game.getPuzzle().getMoves()));

		timeImage.setVisible(showTimer);
		timeLabel.setVisible(showTimer);
		movesImage.setVisible(showCounter);
		movesLabel.setVisible(showCounter);

		boolean allowUndo = !game.getPuzzle().isRotationsEmpty();
		skin.setEnabled(undoButton, allowUndo);
		undoButton.setTouchable((allowUndo) ? Touchable.enabled
				: Touchable.disabled);

		float width = stage.getWidth();

		table.clear();
		table.setFillParent(true);
		table.setTransform(false);

		table.row().expandX();
		table.add(timeImage).left().width(width * 0.08f);
		table.add(timeLabel).left().width(width * 0.42f);
		table.add(movesLabel).right().width(width * 0.42f);
		table.add(movesImage).right().width(width * 0.08f);

		table.row().expand();
		table.add(shuffleButton).colspan(3).center().left();
		table.add(verticalLockButton).bottom().right();

		table.row().expandX();
		table.add(undoButton).bottom().left();
		table.add();
		table.add(horizontalLockButton).bottom().right();
		table.add(lockButton).bottom().right();

		stage.clear();
		stage.addActor(table);

		// Start accepting input
		Gdx.input.setInputProcessor(inputMultiplexer);
		Gdx.input.setCatchBackKey(true);
		Gdx.input.setCatchMenuKey(true);

		if (!game.getPuzzle().isSolved()) {
			game.getPuzzle().startTimer();

			for (Sprite sprite : solvedSprites) {
				sprite.setColor(sprite.getColor().mul(1f, 1f, 1f, 0f));
			}

			newRecordSprite.setColor(newRecordSprite.getColor().mul(1f, 1f, 1f,
					0f));
			newRecordSprite.setScale(1f);
		} else {
			puzzleController.pauseHitCheck();
		}

		solvedAnimation = SolvedAnimation.values()[preferences.getInteger(
				"solved", 0)];

		// Set background music volume to the setting from options screen
		if (backgroundMusic != null && preferences.getBoolean("music", true)) {
			backgroundMusic.setVolume(preferences.getFloat("music-volume",
					PuzzleCube.DEFAULT_MUSIC_VOLUME));
			backgroundMusic.play();
		}
	}

	@Override
	public void hide() {
		game.getPuzzle().stopTimer();
		tweenManager.killAll();

		// Pause the background music if it is playing
		if (backgroundMusic != null) {
			backgroundMusic.pause();
		}
	}

	@Override
	public void pause() {
		game.getPuzzle().stopTimer();
		tweenManager.killAll();

		// Pause the background music if it is playing
		if (backgroundMusic != null) {
			backgroundMusic.pause();
		}
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		if (stage != null) {
			stage.dispose();
			stage = null;
		}
		if (batch != null) {
			batch.dispose();
			batch = null;
		}
	}

}
